/*
 * Created on Jun 27, 2005
 * 
 * This file has been created from the main plug-in class of the previous
 * version of the snarfer plug-in with the purpose of seperating the
 * settings-keeping functionality from the plug-in code.
 */
package edu.duke.cs.ambient.snarfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.duke.cs.snarfer.PackageException;
import edu.duke.cs.snarfer.Package;
import edu.duke.cs.snarfer.PackageSite;
import edu.duke.cs.snarfer.XMLUtils;

/**
 * This class represents and handles settings relevant to the snarfer part of
 * the plug-in, including history, sites, etc.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class SnarferSettings {

    private IPath myStateLocation;

    private Set mySiteURLs;

    private Map mySitesByURL;

    private ArrayList myHistory;

    private DateFormat myDateFormat;

    private DocumentBuilder documentBuilder;

    /**
     * Creates an empty array of settings. StateLocation is the root path to
     * where the settings will be read from and stored to.
     * 
     * @param stateLocation
     *            the root location of the settings files.
     */
    public SnarferSettings(IPath stateLocation) {
        myStateLocation = stateLocation;
        mySitesByURL = new HashMap();
        myDateFormat = new java.text.SimpleDateFormat("MM.dd.yyyy.hh.mm.ss");
        mySiteURLs = new HashSet();
        myHistory = new ArrayList();
    }

    /**
     * Returns an InputStream for the file specified by filePath. FilePath must
     * be relative to the state location with which these SnarferSettings were
     * created.
     * 
     * @param filePath
     *            the relative path to the file
     * @return an InputStream pointing to the file at filePath
     * @throws IOException
     *             when an input problem occurs
     */
    private InputStream openInput(String filePath) throws IOException {
        File inFile = myStateLocation.append(filePath).toFile();
        return new FileInputStream(inFile);
    }

    /**
     * Returns an OutputStream to the file specified by filePath. See
     * {@link SnarferSettings#openInput(String)}for restrictions on filePath.
     * 
     * @param filePath
     *            the relative path to the file
     * @return an OutputStream pointing to the file at filePath
     * @throws IOException
     *             when an input problem occurs
     */
    private OutputStream openOutput(String filePath) throws IOException {
        return XMLUtils.openOutput(myStateLocation.append(filePath).toFile());
    }

    /**
     * Reads settings located in the file "settings.xml" at the state location
     * used in creating this instance of SnarferSettings. If the settings are
     * not available, an empty set is created.
     */
    public void readSettings() {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            InputStream in = openInput("snarf_settings.xml");
            parseSettings(XMLUtils.parseXML(in));
            in.close();
        } catch (ParserConfigurationException x) {
            parseSettings(null);
        } catch (SAXException x) {
            parseSettings(null);
        } catch (FileNotFoundException e) {
            parseSettings(null);
        } catch (IOException e) {
            parseSettings(null);
            e.printStackTrace();
        }
    }

    /**
     * Parses the given XML document and reads settings it contains.
     * 
     * @param doc
     *            the XML document with the settings
     */
    private void parseSettings(Document doc) {
        if (doc == null) {
            // no previous settings available, reset storage
            return;
        }

        Element root = doc.getDocumentElement();

        // get all of user's old sites and store them in mySiteURLs as a set of
        // URL objects
        NodeList sites = root.getElementsByTagName("site");
        for (int i = 0; i < sites.getLength(); i++) {
            Element site = (Element) sites.item(i);
            URL siteURL;
            try {
                siteURL = new URL(site.getAttribute("siteurl"));
                mySiteURLs.add(siteURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        // TODO change how history works

        // get history of user's old instalations and store them in myHistory as
        // a list of PackageInstallRecord objects.
        NodeList history = root.getElementsByTagName("install");
        for (int i = 0; i < history.getLength(); i++) {
            Element record = (Element) history.item(i);
            String name = record.getAttribute("name");
            String version = record.getAttribute("version");
            String publisher = record.getAttribute("publisher");
            String loc = record.getAttribute("location");
            Date date = null;
            try {
                date = myDateFormat.parse(record.getAttribute("date"));
            } catch (ParseException e) {
                date = null;
            }

            // TODO see if any of the other things are not needed.
            Package pkg = new Package(name, "", publisher, version, "", "",
                    null);
            myHistory.add(new PackageInstallRecord(pkg, loc, date));
        }
    }

    /**
     * Saves the user's settings.
     * 
     * @throws IOException
     */
    public void saveSettings() throws IOException {
        Document doc = documentBuilder.newDocument();
        Element root = doc.createElement("settings");
        Element sites = doc.createElement("sites");
        Element history = doc.createElement("history");

        for (Iterator it = mySiteURLs.iterator(); it.hasNext();) {
            Element site = doc.createElement("site");
            site.setAttribute("siteurl", it.next().toString());
            sites.appendChild(site);
        }

        for (int i = 0; i < myHistory.size(); i++) {
            Element install = doc.createElement("install");
            PackageInstallRecord record = (PackageInstallRecord) myHistory
                    .get(i);
            install.setAttribute("name", record.getPackage().getName());
            install.setAttribute("version", record.getPackage().getVersion());
            install.setAttribute("publisher", record.getPackage()
                    .getPublisher());
            install.setAttribute("location", record.getLocation());
            install.setAttribute("date", myDateFormat.format(record
                    .getInstallDate()));
            history.appendChild(install);
        }

        root.setAttribute("settings_version", "1.0");

        root.appendChild(sites);
        root.appendChild(history);
        doc.appendChild(root);

        // create XML form of new settings
        OutputStream xmlOut = openOutput("snarf_settings.xml.new");
        XMLUtils.writeXML(xmlOut, doc);
        xmlOut.close();

        // Remove old settings, make current settings be old settings and save
        // new settings as current settings.
        // Old settings might be useful if there is need to revert to them.
        // TODO this seems to complicated and unnecessary
        File oldSettings = myStateLocation.append("snarf_settings.xml")
                .toFile();
        File oldOldSettings = myStateLocation.append("snarf_settings.xml.old")
                .toFile();
        File newSettings = myStateLocation.append("snarf_settings.xml.new")
                .toFile();
        if (oldOldSettings.exists()) {
            oldOldSettings.delete();
        }
        oldSettings.renameTo(myStateLocation.append("snarf_settings.xml.old")
                .toFile());
        newSettings.renameTo(myStateLocation.append("snarf_settings.xml")
                .toFile());
    }

    /**
     * Returns a collection of site URLs that the user has stored.
     * 
     * @return a collection of site URLs that the user has stored
     */
    public Collection getSiteURLs() {
        return mySiteURLs;
    }

    /**
     * Returns a collection of {@link PackageInstallRecord}containing the
     * user's history of installations.
     * 
     * @return the user's history of installed packages
     */
    public Collection getHistory() {
        return myHistory;
    }

    /**
     * Returns the most recent {@link PackageInstallRecord}for the given
     * packageName.
     * 
     * @param packageName
     *            the name of the package whose install record is to be
     *            retrieved
     * @return the most recent {@link PackageInstallRecord}packageName
     */
    public PackageInstallRecord getLastInstall(String packageName) {
        Iterator it = myHistory.iterator();
        PackageInstallRecord record = null;
        while (it.hasNext()) {
            PackageInstallRecord r = (PackageInstallRecord) it.next();
            if (r.getPackage().getName().toLowerCase().equals(
                    packageName.toLowerCase())
                    && (record == null || r.getInstallDate().compareTo(
                            record.getInstallDate()) > 0))
                record = r;
        }
        return record;
    }

    /**
     * @param url
     * @return
     * @throws PackageException
     */
    public PackageSite getSite(URL url) throws PackageException {
        if (mySitesByURL.containsKey(url))
            return (PackageSite) mySitesByURL.get(url);
        else
            return refreshSite(url);
    }

    public PackageSite refreshSite(URL url) throws PackageException {
        PackageSite site = null;
        try {
            site = PackageSite.buildFrom(url);
        } catch (PackageException e) {
            mySitesByURL.remove(url);
            throw e;
        }
        mySitesByURL.put(url, site);
        return site;
    }

    public void uncacheSite(URL url) {
        mySitesByURL.remove(url);
    }
}
