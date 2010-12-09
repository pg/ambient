package edu.duke.cs.snarfer;

/**
 * This interface contains the xml node names and other constants used by the
 * snarfer module.
 * 
 * @author Marcin Dobosz
 * @since 2.0
 */
public interface SnarfConstants {
    // /////////////////////////////
    // NODES

    public static final String SITE_NODE = "snarf_site";

    public static final String PACKAGE_NODE = "package";

    public static final String DESCRIPTION_NODE = "description";

    public static final String ENTRY_NODE = "entry";

    // /////////////////////////////
    // NODE ATTRIBUTES

    public static final String NAME_ATTRIB = "name";

    public static final String CATEGORY_ATTRIB = "category";

    public static final String PUBLISHER_ATTRIB = "publisher";

    public static final String VERSION_ATTRIB = "version";

    public static final String PROJECT_TYPE_NODE = "project_type";

    public static final String INFO_URL_ATTRIB = "info_url";

    public static final String URL_ATTRIB = "url";

    public static final String FILE_ATTRIB = "file";

    public static final String OPTIONAL_ATTRIB = "optional";

    public static final String SIZE_ATTRIB = "size";

    public static final String PATH_ATTRIB = "path";
}
