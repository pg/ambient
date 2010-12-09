/*
 * Created on Jun 28, 2005
 * 
 * This file contains code modified from the JavaProject class from an earlier
 * version of the edu.duke.snarfer plug-in.
 */
package edu.duke.cs.ambient.projects;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.duke.cs.snarfer.XMLUtils;

/**
 * This project handler is responsible for creating simple Java projects. It
 * creates a ".project" file and a ".classpath" file.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class JavaProjectHandler extends AbstractProjectHandler implements
        IProjectHandler {

    public final static String[] BUILD_COMMANDS = { "org.eclipse.jdt.core.javabuilder" };

    public final static String[] NATURES = { "org.eclipse.jdt.core.javanature" };

    public final static String CLASSPATH_FILE_NAME = ".classpath";

    protected void createClasspathFile(File projectDir, String[] Sources,
            String[] Libraries, String Output) {
        Document doc = XMLUtils.createDOM();
        Element root = doc.createElement("classpath");
        Element entry = null;

        if (Sources == null) {
            Sources = new String[1];
            Sources[0] = "";
        }

        if (Output == null) {
            Output = "";
        }

        for (int k = 0; k < Sources.length; k++) {
            entry = doc.createElement("classpathentry");
            entry.setAttribute("kind", "src");
            entry.setAttribute("path", Sources[k]);
            root.appendChild(entry);
        }

        entry = doc.createElement("classpathentry");
        entry.setAttribute("kind", "con");
        entry.setAttribute("path", "org.eclipse.jdt.launching.JRE_CONTAINER");
        root.appendChild(entry);

        entry = doc.createElement("classpathentry");
        entry.setAttribute("kind", "output");
        entry.setAttribute("path", Output);
        root.appendChild(entry);

        if (Libraries != null) {
            for (int k = 0; k < Sources.length; k++) {
                entry = doc.createElement("classpathentry");
                entry.setAttribute("kind", "lib");
                entry.setAttribute("path", Libraries[k]);
                root.appendChild(entry);
            }
        }

        doc.appendChild(root);

        writeDocToFile(projectDir, CLASSPATH_FILE_NAME, doc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.duke.cs.ambient.projects.IProjectHandler#createProject(java.io.File,
     *      java.lang.String)
     */
    public void createProject(File projectDir, String projectName) {
        createClasspathFile(projectDir, null, null, null);
        createProjectFile(projectDir, projectName, NATURES, BUILD_COMMANDS/* ,PERSPECTIVE */);
    }

}
