package edu.duke.cs.snarfer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This object represents a package site that can contain many different
 * packages.
 * 
 * @version 2.0
 * @author Marcin Dobosz
 */
public class PackageSite {

    // ///////////////////////////////
    // STATE VARIABLES
    private URL myURL;

    private String myName;

    /**
     * Stores the packages available at this site
     */
    private ArrayList myPackages; // type Package

    // ///////////////////////////////
    // CONSTRUCTORS

    public PackageSite(String name) {
        myName = name;
        myURL = null;
        myPackages = new ArrayList();
    }

    // ////////////////////////////////
    // CONSTRUCTION METHODS

    /**
     * Parses the provided root of an XML document containing the package site
     * manifest and initializes state information. The root element must be the
     * root of a properly formatted package site manifest document.
     * 
     * @param root
     *            the root element of the package site manifest document
     * @see PackageSite#update()
     * @throws PackageException
     *             if the provided manifest document is formatted incorrectly or
     *             some other problem occurs.
     */
    public static PackageSite buildFrom(Element root) throws PackageException {
        if (!root.getNodeName().equals(SnarfConstants.SITE_NODE))
            throw new PackageException(
                    "Root of project site xml must be named '"
                            + SnarfConstants.SITE_NODE + "'.");

        String name = root.getAttribute(SnarfConstants.NAME_ATTRIB);
        PackageSite site = new PackageSite(name);
        NodeList packages = root
                .getElementsByTagName(SnarfConstants.PACKAGE_NODE);
        for (int i = 0; i < packages.getLength(); i++) {
            Element pkgElement = (Element) packages.item(i);
            site.addPackage(Package.buildFrom(pkgElement));
        }
        return site;
    }

    public static PackageSite buildFrom(URL url) throws PackageException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            Element root = doc.getDocumentElement();
            PackageSite site = buildFrom(root);
            site.setURL(url);
            return site;
        } catch (ParserConfigurationException e) {
            throw new PackageException("Could not create XML parser", e);
        } catch (FactoryConfigurationError e) {
            throw new PackageException("Could not create XML parser", e);
        } catch (SAXException e) {
            throw new PackageException("Could not parse XML", e);
        } catch (IOException e) {
            throw new PackageException("Error fetching descriptor", e);
        }
    }

    // ////////////////////////////////
    // ACCESSORS

    /**
     * Returns the URL defined for this package site.
     * 
     * @return the URL defined for this package site
     * @throws IllegalStateException
     *             if the URL has not been set for this package site
     */
    public URL getURL() {
        if (myURL == null)
            throw new IllegalStateException(
                    "A URL has not been set for the PackageSite");

        return myURL;
    }

    /**
     * Returns the name of this package site.
     * 
     * @return the name of this package site.
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns the number of packages at this package site.
     * 
     * @return the number of packages at this package stie.
     */
    public int getPackageCount() {
        return myPackages.size();
    }

    /**
     * Returns the PackageInfo object for the chosen package.
     * 
     * @param i
     *            the number of the package to be accessed.
     * @return the PackageInfo object for the chosen package.
     */
    public Package getPackage(int i) {
        if (i >= myPackages.size())
            return null;
        return (Package) myPackages.get(i);
    }

    /**
     * Returns a DOM representation of this package site. This can be used to
     * write the package site manifest in XML format.
     * 
     * @return a DOM representation of this package site's manifest document.
     */
    public Element toDOMElement(Document doc) {
        Element root = doc.createElement(SnarfConstants.SITE_NODE);
        root.setAttribute(SnarfConstants.NAME_ATTRIB, myName);
        for (int k = 0; k < myPackages.size(); k++) {
            Package pkg = getPackage(k);
            Element el = pkg.toDOMElement(doc);
            root.appendChild(el);
        }
        return root;
    }

    /**
     * Returns a display-friendly representation of this package site.
     * 
     * @return a displayable representation of this site
     */
    public String toString() {
        return "PackageSite [name=" + myName + ", url="
                + (myURL == null ? "not set" : myURL.toExternalForm()) + "]";
    }

    // /////////////////////////////////
    // MODIFIERS

    /**
     * Sets the URL for this package site.
     * 
     * @param url
     *            the new URL for this package site.
     * @return the old URL
     */
    public URL setURL(URL url) {
        if (url == null)
            throw new IllegalArgumentException(
                    "A null url is not an acceptable value for a PackageSite");
        URL oldURL = url;
        myURL = url;
        return oldURL;
    }

    /**
     * Sets the name for this package site.
     * 
     * @param name
     *            the new name
     * @return the old name
     */
    public String setName(String name) {
        String oldName = myName;
        myName = (name == null ? "" : name);
        return oldName;
    }

    /**
     * Adds a package to this package site's list of packages.
     * 
     * @param pkg
     *            the new package information object.
     */
    public void addPackage(Package pkg) {
        myPackages.add(pkg);
    }

    public void addPackages(Collection packages) {
        myPackages.addAll(packages);
    }

    /**
     * Removes the PackageInfo object at index from this package site.
     * 
     * @param index
     *            the index of the PackageInfo object to be removed
     * @return the PackageInfo being removed
     */
    public Package removePackage(int index) {
        return (Package) myPackages.remove(index);
    }

    /**
     * Removes all the Package objects from this package site.
     */
    public void removeAllPackages() {
        myPackages.clear();
    }
}
