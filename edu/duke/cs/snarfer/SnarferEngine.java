/*
 * Created on May 29, 2003
 */
package edu.duke.cs.snarfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author jett
 */
public class SnarferEngine {
    private ArrayList sites;

    private Set packages;

    private Map siteMap;

    private Map infoMap;

    /**
     * Creates a new ClientEngine object.
     */
    public SnarferEngine() {
        sites = new ArrayList();
        packages = new TreeSet();
        siteMap = new HashMap();
        infoMap = new HashMap();
    }

    // TODO: fix so that people don't have to call refreshIndex: add a boolean
    // flag determining if engine had been modified or not and check for it
    // before accessor operations.
    /**
     * Adds a new package site from the provided URL. The method refreshIndex()
     * should be called after this operation and before other operations.
     * 
     * @param siteURL
     *            the URL of the new package site.
     * @see PackageSite
     * @see SnarferEngine#refreshIndex()
     * @throws PackageException
     *             when there are problems adding/reading the package site.
     */
    public void addSite(URL siteURL) throws PackageException {
        sites.add(PackageSite.buildFrom(siteURL));
    }

    /**
     * Adds a new package site.
     * 
     * @param site
     *            the new package site.
     */
    public void addSite(PackageSite site) {
        sites.add(site);
    }

    /**
     * Rebuilds internal references based on the current list of PackageSites
     * added to this ClientEngine.
     */
    public void refreshIndex() {
        packages.clear();
        siteMap.clear();
        infoMap.clear();

        for (int i = 0; i < sites.size(); i++) {
            PackageSite site = (PackageSite) sites.get(i);
            int count = site.getPackageCount();
            for (int j = 0; j < count; j++) {
                Package pkg = site.getPackage(j);
                packages.add(pkg);
                if (j == 0) {
                    siteMap.put(site, new HashSet());
                }
                ((Collection) siteMap.get(site)).add(pkg);
                infoMap.put(pkg, site);
            }
        }
    }

    /**
     * Returns all the sites.
     * 
     * @return a Collection of all the sites
     */
    public Collection getSites() {
        return sites;
    }

    /**
     * Returns the package site that contains the package specified by the given
     * PackageInfo object.
     * 
     * @param pkg
     *            the object specifying a package.
     * @see edu.duke.cs.snarfer.PackageInfo
     * @see edu.duke.cs.snarfer.PackageSite
     * @return the package site associated with the package specified by info.
     */
    public PackageSite getSiteFor(Package pkg) {
        return (PackageSite) infoMap.get(pkg);
    }

    /**
     * Returns a Collection of PackageInfo objects associated with the given
     * site.
     * 
     * @param site
     *            the querried PackageSite.
     * @return a Collection of PackageInfo objects associated with site.
     */
    public Collection getPackagesFor(PackageSite site) {
        return (Collection) siteMap.get(site);
    }

    /**
     * Returns a Set of all the PackageInfo objects added to this ClientEngine.
     * 
     * @return a Set of all PackageInfo objects in this ClientEngine.
     */
    public Set getPackages() {
        return packages;
    }

    public void fetchPackage(Package pkg, File prefix, FetchListener listener)
            throws PackageException, IOException, InterruptedException {
        if (!prefix.exists() && prefix.isDirectory()) {
            throw new FileNotFoundException("'" + prefix
                    + "' is not a directory");
        }

        for (int i = 0; i < pkg.getEntryCount(); i++) {
            checkInterrupted();
            PackageEntry entry = pkg.getEntry(i);
            fetchEntry(entry, prefix, listener);
            listener.onEntryFinished(entry);
        }
    }

    private/* boolean */void fetchArchive(PackageEntry entry, File prefix,
            FetchListener listener) throws PackageException, IOException,
            InterruptedException {
        byte[] buffer = new byte[4096];
        int bytesRead;

        if (!prefix.exists()) { // create required location
            prefix.mkdirs();
        }

        // open connection to archive
        URLConnection connection = entry.getURL().openConnection();
        checkInterrupted();
        listener.onBeginEntry(entry, connection.getContentLength());
        ZipInputStream zipInput = new ZipInputStream(connection
                .getInputStream());

        // unzip all the files in the zip input into the right directory
        ZipEntry zEntry = zipInput.getNextEntry();
        while (zEntry != null) {
            checkInterrupted();
            // prepare new file
            String fname = XMLUtils.stripRoot(zEntry.getName());
            File outFile = new File(prefix, fname);
            if (zEntry.isDirectory()) {
                // create right directories
                outFile.mkdirs();
                outFile.mkdir();
            } else if (!outFile.exists() || listener.onFileExists(outFile)) {
                // process file: read from zip and write to out
                listener.onBeginArchivedFile(entry, outFile, zEntry.getSize());
                OutputStream out = XMLUtils.openOutput(outFile);
                while ((bytesRead = zipInput.read(buffer)) != -1) {
                    checkInterrupted();
                    out.write(buffer, 0, bytesRead);
                    listener.onArchivedFileProgress(bytesRead);
                }
                out.close();
            }
            // move to next file
            listener.onFileFinished(outFile);
            listener.onEntryProgress((int) zEntry.getCompressedSize());
            zEntry = zipInput.getNextEntry();
        }
        zipInput.close();
        listener.onEntryFinished(entry);
        // return true;
    }

    private/* boolean */void fetchFile(PackageEntry entry, File prefix,
            FetchListener listener) throws PackageException, IOException,
            InterruptedException {
        int bytesRead;
        byte[] buffer = new byte[4096];

        try {
            // open connection to file, setup output
            URLConnection connection = entry.getURL().openConnection();
            checkInterrupted();
            String filename = XMLUtils.getFileName(entry.getURL());
            InputStream in = connection.getInputStream();
            File outFile = new File(prefix, filename);

            // if file exists and not override, exit
            if (outFile.exists() && !listener.onFileExists(outFile)) {
                in.close();
                return /* false */;
            }

            // create directories
            outFile.mkdirs();
            outFile.mkdir();

            listener.onBeginEntry(entry, connection.getContentLength());
            OutputStream out = XMLUtils.openOutput(outFile);

            // copy file
            while ((bytesRead = in.read(buffer)) != -1) {
                checkInterrupted();
                listener.onEntryProgress(bytesRead);
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();
            listener.onFileFinished(outFile);
            listener.onEntryFinished(entry);
            // return true;
        } catch (FileNotFoundException e) {
            throw new PackageException("Bad output file", e);
        } catch (IOException e) {
            throw new PackageException("IO error", e);
        }

    }

    // TODO check if fetch methods need to return a boolean
    public/* boolean */void fetchEntry(PackageEntry entry, File prefix,
            FetchListener listener) throws PackageException, IOException,
            InterruptedException {
        if (!entry.isFile()) {
            // handle archives
            fetchArchive(entry, prefix, listener);
        } else {
            // handle files
            /* return */fetchFile(entry, prefix, listener);
        }
    }

    /**
     * Convenience method for checking the status of the current Thread and
     * whether it's been interrupted. If it has, this method will throw a
     * InterruptedException.
     * 
     * @throws InterruptedException
     *             thrown if the current thread has been interupted.
     */
    protected void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
