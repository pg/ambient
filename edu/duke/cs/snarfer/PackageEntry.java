/*
 * Created on May 29, 2003
 */
package edu.duke.cs.snarfer;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class encapsulates a single file of any type (including jar archives)
 * that is part of a Package.
 * 
 * @author Marcin Dobosz
 * @version 2.0
 */
public class PackageEntry {

    // ////////////////////////////////
    // CONSTANTS

    /**
     * This constant (value -1) indicates an unknown entry size.
     */
    public static final int UNKNOWN_SIZE = -1;

    /**
     * This constant (value <code>false</code>) is the default value for the
     * optional attribute.
     */
    public static final boolean DEFAULT_OPTIONAL = false;

    /**
     * This constant (value <code>true</code>) is the default value for the
     * archive attribute.
     */
    public static final boolean DEFAULT_FILE = false;

    // ////////////////////////////////
    // STATE VARIABLES

    private URL myUrl;

    private boolean isFile = DEFAULT_FILE;

    private boolean isOptional = DEFAULT_OPTIONAL;

    private String myDescription = "";

    private String myPath = "";

    private int mySize = UNKNOWN_SIZE;

    // //////////////////////////////////
    // CONSTRUCTORS

    public PackageEntry(URL url, String path, boolean file, boolean optional,
            int size, String description) {
        setUrl(url);
        isFile = file;
        isOptional = optional;
        setSize(size);
        setDescription(description);
        setPath(path);
    }

    public PackageEntry(URL url, String path, boolean file, boolean required,
            int size) {
        this(url, path, file, required, size, "");
    }

    // ////////////////////////////////
    // CONSTRUCTION METHODS

    public static PackageEntry buildFrom(Element root) throws PackageException {
        if (!root.getNodeName().equals(SnarfConstants.ENTRY_NODE))
            throw new PackageException("Expected '" + SnarfConstants.ENTRY_NODE
                    + "' as the root tag of a package definition.");
        URL url = null;
        try {
            url = new URL(root.getAttribute(SnarfConstants.URL_ATTRIB));
        } catch (MalformedURLException e) {
        }
        boolean file, required;
        if (root.getAttribute(SnarfConstants.FILE_ATTRIB).equals(""))
            file = DEFAULT_FILE;
        else
            file = new Boolean(root.getAttribute(SnarfConstants.FILE_ATTRIB))
                    .booleanValue();
        if (root.getAttribute(SnarfConstants.OPTIONAL_ATTRIB).equals(""))
            required = DEFAULT_OPTIONAL;
        else
            required = new Boolean(root
                    .getAttribute(SnarfConstants.OPTIONAL_ATTRIB))
                    .booleanValue();
        int size = UNKNOWN_SIZE;
        try {
            size = Integer.parseInt(root
                    .getAttribute(SnarfConstants.SIZE_ATTRIB));
        } catch (NumberFormatException e) {
            size = UNKNOWN_SIZE;
        }

        String path = root.getAttribute(SnarfConstants.PATH_ATTRIB);

        NodeList descs = root
                .getElementsByTagName(SnarfConstants.DESCRIPTION_NODE);
        String description = null;
        try {
            description = descs.item(0).getFirstChild().getNodeValue();
        } catch (Exception e) {
            description = "";
        }

        PackageEntry entry = new PackageEntry(url, path, file, required, size,
                description);

        return entry;
    }

    // ////////////////////////////////
    // MODIFIERS

    /**
     * Sets the file parameter for this package entry.
     * 
     * @param b
     *            the new archive flag for this entry.
     */
    public boolean setFile(boolean b) {
        boolean old_val = isFile;
        isFile = b;
        return old_val;
    }

    public boolean setOptional(boolean b) {
        boolean old_val = isOptional;
        isOptional = b;
        return old_val;
    }

    public String setPath(String path) {
        String old = myPath;
        myPath = (path == null ? "" : path);
        return old;
    }

    /**
     * Sets the URL for this entry.
     * 
     * @param url
     *            the new URL for this entry.
     */
    public URL setUrl(URL url) {
        if (url == null)
            throw new IllegalArgumentException(
                    "A PackageEntry cannot have a null URL");
        URL old = myUrl;
        myUrl = url;
        return old;
    }

    public String setDescription(String description) {
        String old = myDescription;
        myDescription = (description == null ? "" : description);
        return old;
    }

    public int setSize(int size) {
        int old = mySize;
        mySize = (size < UNKNOWN_SIZE ? UNKNOWN_SIZE : size);
        return old;
    }

    // /////////////////////////////
    // ACCESSORS

    /**
     * Returns whether this package entry is an archive or a regular file.
     * 
     * @return <code>false</code> if this package entry is an archive (jar),
     *         <code>true</code> otherwise.
     */
    public boolean isFile() {
        return isFile;
    }

    public boolean isOptional() {
        return isOptional;
    }

    /**
     * Returns the URL of this PackageEntry.
     * 
     * @return the URL of this PackageEntry.
     */
    public URL getURL() {
        return myUrl;
    }

    public String getPath() {
        return myPath;
    }

    public String getDescription() {
        return myDescription;
    }

    public int getSize() {
        return mySize;
    }

    /**
     * Opens a connection to the URL of this package entry and returns an
     * InputStream to read from that connection.
     * 
     * @return an InputStream for the connection to this entry's URL.
     * @throws java.io.IOException
     *             if a problem occurs connecting to this entry's URL.
     */
    public InputStream openStream() throws java.io.IOException {
        return myUrl.openStream();
    }

    public Element toDOMElement(Document doc) {
        Element root = doc.createElement(SnarfConstants.ENTRY_NODE);
        root.setAttribute(SnarfConstants.URL_ATTRIB, myUrl.toExternalForm());
        if (isFile != DEFAULT_FILE)
            root.setAttribute(SnarfConstants.FILE_ATTRIB, Boolean
                    .toString(isFile));
        if (isOptional != DEFAULT_OPTIONAL)
            root.setAttribute(SnarfConstants.OPTIONAL_ATTRIB, Boolean
                    .toString(isOptional));
        if (mySize != UNKNOWN_SIZE)
            root.setAttribute(SnarfConstants.SIZE_ATTRIB, Integer
                    .toString(mySize));
        if (myPath != null && myPath.length() > 0) {
            root.setAttribute(SnarfConstants.PATH_ATTRIB, myPath);
        }
        if (myDescription != null && myDescription.length() > 0) {
            Element desc = doc.createElement(SnarfConstants.DESCRIPTION_NODE);
            desc.appendChild(doc.createTextNode(myDescription));
            root.appendChild(desc);
        }
        return root;
    }

    /**
     * Returns a display-friendly representation of this entry.
     * 
     * @return a displayable representation of this entry
     */
    public String toString() {
        return "PackageEntry [url=" + myUrl.toExternalForm() + ", path="
                + myPath + ", file=" + isFile + ", required=" + isOptional
                + ", size="
                + (mySize == UNKNOWN_SIZE ? "UNKNOWN" : "" + mySize)
                + ", desc=" + myDescription + "]";
    }
}
