/*
 * Created on Jun 28, 2005
 * 
 * This file contains code modified from the CppProject class from an earlier
 * version of the edu.duke.snarfer plug-in.
 */
package edu.duke.cs.ambient.projects;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.duke.cs.snarfer.XMLUtils;

// TODO this needs some heavy looking into about what exact files are necessary
// to create a C++ project skeleton.
/**
 * @author Marcin Dobosz
 */
public class CppProjectHandler extends AbstractProjectHandler implements
        IProjectHandler {

    public final static String BUILD_COMMANDS[] = { "org.eclipse.cdt.core.cbuilder" };

    public final static String PERSPECTIVE = "org.eclipse.cdt.ui.CPerspective";

    public final static String[] NATURES = { "org.eclipse.cdt.core.cnature",
            "org.eclipse.cdt.core.ccnature" };

    public final static String CDTPROJECT_FILE_NAME = ".cdtproject";

    /*
     * (non-Javadoc)
     * 
     * @see edu.duke.cs.ambient.projects.IProjectHandler#createProject(java.io.File,
     *      java.lang.String)
     */
    public void createProject(File projectDir, String name) {
        createCdtProject(projectDir);
        createProjectFile(projectDir, name, NATURES, BUILD_COMMANDS/*
                                                                     * ,
                                                                     * PERSPECTIVE
                                                                     */);
    }

    protected void createCdtProject(File projectDir) {
        Document doc = XMLUtils.createDOM();
        Element root = doc.createElement("cdtproject");
        root.setAttribute("id", "org.eclipse.cdt.core.make");

        Element ext = doc.createElement("extension");
        ext.setAttribute("id", "org.eclipse.cdt.core.makeBuilder");
        ext.setAttribute("point", "org.eclipse.cdt.core.CBuildModel");

        Element attrib = doc.createElement("attribute");
        attrib.setAttribute("key", "command");
        attrib.setAttribute("value", "make");

        ext.appendChild(attrib);
        root.appendChild(ext);
        doc.appendChild(root);

        writeDocToFile(projectDir, CDTPROJECT_FILE_NAME, doc);
    }

}
