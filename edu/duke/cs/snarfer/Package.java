package edu.duke.cs.snarfer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @version 2.0
 * @see PackageSite
 */
public class Package implements Comparable {

    // ////////////////////////////
    // STATE VARIABLES
    private String myDescription = "";

    private ArrayList myEntries;

    private String myName = "";

    private String myVersion = "";

    private String myPublisher = "";

    private String myCategory = "";

    private String myType = "";

    private URL myInfoURL;

    // //////////////////////////////
    // CONSTRUCTORS

    public Package(String name, String description, String publisher,
            String version, String category, String type, URL infoURL) {
        myEntries = new ArrayList();
        setDescription(description);
        setName(name);
        setVersion(version);
        setPublisher(publisher);
        setCategory(category);
        setType(type);
        setInfoURL(infoURL);
    }

    // ///////////////////////////////
    // CONSTRUCTION METHODS

    public static Package buildFrom(Element root) throws PackageException {
        if (!root.getNodeName().equals(SnarfConstants.PACKAGE_NODE))
            throw new PackageException("Expected '"
                    + SnarfConstants.PACKAGE_NODE
                    + "' as the root tag of a package definition.");
        String name = root.getAttribute(SnarfConstants.NAME_ATTRIB);
        String publisher = root.getAttribute(SnarfConstants.PUBLISHER_ATTRIB);
        String version = root.getAttribute(SnarfConstants.VERSION_ATTRIB);
        String category = root.getAttribute(SnarfConstants.CATEGORY_ATTRIB);
        String type = root.getAttribute(SnarfConstants.PROJECT_TYPE_NODE);
        URL info = null;
        try {
            info = new URL(root.getAttribute(SnarfConstants.INFO_URL_ATTRIB));
        } catch (MalformedURLException e) {
            info = null;
        }

        NodeList descs = root
                .getElementsByTagName(SnarfConstants.DESCRIPTION_NODE);
        String description = null;
        try {
            description = descs.item(0).getFirstChild().getNodeValue();
        } catch (Exception e) {
            description = "";
        }
        Package pkg = new Package(name, description, publisher, version,
                category, type, info);

        NodeList files = root.getElementsByTagName(SnarfConstants.ENTRY_NODE);
        for (int i = 0; i < files.getLength(); i++) {
            Element f = (Element) files.item(i);
            pkg.addEntry(PackageEntry.buildFrom(f));
        }
        return pkg;
    }

    // ////////////////////////////////
    // ACCESSORS

    /**
     * @return the number of entries in this package
     */
    public int getEntryCount() {
        return myEntries.size();
    }

    /**
     * Get the i'th PackageEntry.
     * 
     * @param i
     *            which entry to return
     * @return the i'th PackageEntry
     */
    public PackageEntry getEntry(int i) {
        if (0 <= i && i < myEntries.size()) {
            return (PackageEntry) myEntries.get(i);
        }
        return null;
    }

    /**
     * Returns all the entries in this package.
     * 
     * @return all the entries in this package
     */
    public Collection getEntries() {
        return myEntries;
    }

    public Element toDOMElement(Document doc) {
        Element root = doc.createElement(SnarfConstants.PACKAGE_NODE);
        root.setAttribute(SnarfConstants.NAME_ATTRIB, myName);
        root.setAttribute(SnarfConstants.VERSION_ATTRIB, myVersion);
        root.setAttribute(SnarfConstants.PUBLISHER_ATTRIB, myPublisher);
        root.setAttribute(SnarfConstants.PROJECT_TYPE_NODE, myType);
        root.setAttribute(SnarfConstants.CATEGORY_ATTRIB, myCategory);
        if (myInfoURL != null)
            root.setAttribute(SnarfConstants.INFO_URL_ATTRIB, myInfoURL
                    .toExternalForm());
        if (myDescription != null && myDescription.length() > 0) {
            Element desc = doc.createElement(SnarfConstants.DESCRIPTION_NODE);
            desc.appendChild(doc.createTextNode(myDescription));
            root.appendChild(desc);
        }

        Iterator entries = myEntries.iterator();
        while (entries.hasNext()) {
            PackageEntry entry = (PackageEntry) entries.next();
            Element file = entry.toDOMElement(doc);
            root.appendChild(file);
        }
        return root;
    }

    /**
     * Returns this package's description.
     * 
     * @return the description for this package.
     */
    public String getDescription() {
        return myDescription;
    }

    public String getCategory() {
        return myCategory;
    }

    public String getName() {
        return myName;
    }

    public String getPublisher() {
        return myPublisher;
    }

    public String getType() {
        return myType;
    }

    public String getVersion() {
        return myVersion;
    }

    public URL getInfoURL() {
        return myInfoURL;
    }

    public String toString() {
        return "Package [name=" + myName + ", cat=" + myCategory + ", ver="
                + myVersion + ", publisher=" + myPublisher + ", type=" + myType
                + " info="
                + (myInfoURL == null ? "" : myInfoURL.toExternalForm())
                + ", desc=" + myDescription + ", entries=" + myEntries.size()
                + "]";
    }

    // //////////////////////////////////////
    // MODIFIERS

    public void addEntries(Collection entries) {
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            PackageEntry element = (PackageEntry) it.next();
            addEntry(element);
        }
    }

    public void addEntry(PackageEntry entry) {
        if (entry == null)
            throw new IllegalArgumentException(
                    "A null PackageEntry cannot be added to a Package");
        myEntries.add(entry);
    }

    public String setName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException(
                    "A package must have a non-empty name");
        }
        String old = myName;
        myName = name;
        return old;
    }

    public String setCategory(String category) {
        String old = myCategory;
        myCategory = (category == null ? "" : category);
        return old;
    }

    private String stripWhitespace(String s) {
        return s.trim();
    }

    public String setDescription(String description) {
        String old = myDescription;
        myDescription = (description == null ? ""
                : stripWhitespace(description));
        return old;
    }

    public String setPublisher(String publisher) {
        String old = myPublisher;
        myPublisher = (publisher == null ? "" : publisher);
        return old;
    }

    public String setType(String type) {
        String old = myType;
        myType = (type == null ? "" : type);
        return old;
    }

    // TODO make sure error checking is done correctly
    public String setVersion(String version) {
        String old = myVersion;
        myVersion = (version == null ? "" : version);
        return old;
    }

    public URL setInfoURL(URL info) {
        URL old = myInfoURL;
        myInfoURL = info;
        return old;
    }

    // TODO make sure Comparison works the way it should

    /**
     * This method evaluates this package information set against another
     * object. This method returns <code>true</code> iff
     * <code>compareTo(o) == 0</code> and the arrays of categories of both
     * objects are exactly identical.
     * 
     * @see Package#compareTo(Object)
     * @return <code>true</code> only if all the data fields of the compared
     *         objects are exactly identical, <code>false</code> otherwise.
     */
    public boolean equals(Object o) {
        if (compareTo(o) == 0)
            return myCategory.equals(((Package) o).myCategory);
        return false;
    }

    /**
     * This method compares two Strings representing numeric version numbers.
     * The version numbers must be of the format "n[.n]+" where n is any number.
     * The values returned by this method corespond to the standard compareTo
     * method of the Comparable interface.
     * 
     * @param a
     *            one version description.
     * @param b
     *            other version description.
     * @return negative value if a is older than b, positive value if a is newer
     *         than be, and 0 if they are equal.
     */
    public static int versionCompare(String a, String b) {
        StringTokenizer ta = new StringTokenizer(a, ".");
        StringTokenizer tb = new StringTokenizer(b, ".");
        while (ta.hasMoreTokens() && tb.hasMoreTokens()) {
            int na = Integer.parseInt(ta.nextToken());
            int nb = Integer.parseInt(tb.nextToken());
            if (na < nb)
                return -1;
            if (nb < na)
                return 1;
        }
        if (ta.hasMoreTokens()) {
            while (ta.hasMoreTokens()) {
                int na = Integer.parseInt(ta.nextToken());
                if (na != 0)
                    return 1;
            }
            return 0;
        }
        if (tb.hasMoreTokens()) {
            while (tb.hasMoreTokens()) {
                int nb = Integer.parseInt(tb.nextToken());
                if (nb != 0)
                    return -1;
            }
            return 0;
        }
        return 0;
    }

    /**
     * This method compares this Package to another object. The comparison is
     * first done in the following order (with the first criterion being the
     * most important): name, version, publisher name, and url. The type and
     * category fields are ignored.
     * 
     * @see Package#versionCompare(String, String)
     * @return a negative value if this object is lesser than o, a positive if
     *         it is greater, and 0 if they are equal.
     */
    public int compareTo(Object o) {
        if (!(o instanceof Package))
            return -1;

        Package other = (Package) o;
        int byName = myName.compareTo(other.getName());
        int byVersion = versionCompare(myVersion, other.getVersion());
        int byPublisher = myPublisher.compareTo(other.getPublisher());
        return byName != 0 ? byName : (byVersion != 0 ? byVersion
                : (byPublisher != 0 ? byPublisher : 0));
    }
}
