/*
 * Created on Jun 28, 2005
 * 
 * This file contains code modified from the ProjectMaker class from an earlier
 * version of the edu.duke.snarfer plug-in.
 */
package edu.duke.cs.ambient.projects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.resources.IProjectDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.duke.cs.snarfer.XMLUtils;

/**
 * This is an abstract base class for project handlers. It provides
 * functionality for creating a ".project" file, which is required for every
 * Eclipse project.
 * <p>
 * Implementing classes can use this functionality in addition to creating other
 * required project type-specific files (like ".classpath" or ".cdtproject").
 * {@link edu.duke.cs.ambient.projects.JavaProjectHandler}provides an example
 * of a basic implementation.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public abstract class AbstractProjectHandler implements IProjectHandler {

    /**
     * Writes the provided XML document to the specified file in the given
     * directory. If the provided directory is not a directory, the function
     * does nothing.
     * 
     * @param directory
     *            the directory in which the file is to be written.
     * @param fileName
     *            the name of the file.
     * @param doc
     *            and XML Document that is to be written.
     */
    protected void writeDocToFile(File directory, String fileName, Document doc) {
        if (!directory.isDirectory())
            return;

        File f = new File(directory, fileName);
        try {
            f.createNewFile();
        } catch (IOException e) {
        }

        OutputStream siteOut = null;
        try {
            siteOut = XMLUtils.openOutput(f);
        } catch (FileNotFoundException e) {
            // should not happen as the file was just recently created.
        }
        XMLUtils.writeXML(siteOut, doc);
    }

    /**
     * Creates a ".project" file. The ".project" file contains the basic
     * information necessary to maintain and build an Eclipse project. Please
     * refer to documentation for the exact format.
     * <p>
     * Please note that this function will only create a simple build commands
     * structure and does not allow to specify build command arguments.
     * 
     * @param projectDir
     *            the directory in which the project is to be installed.
     * @param projectName
     *            the name of the project.
     * @param projectNatures
     *            natures required by this project.
     * @param buildCommands
     *            simple build commands for this project.
     */
    protected void createProjectFile(File projectDir, String projectName,
            String[] projectNatures, String[] buildCommands/*
                                                             * , String
                                                             * perspective
                                                             */) {
        Document doc = createProjectDocument(projectName, projectNatures,
                buildCommands);
        writeDocToFile(projectDir, IProjectDescription.DESCRIPTION_FILE_NAME,
                doc);
    }

    private Document createProjectDocument(String projectName,
            String[] projectNatures, String[] buildCommands) {
        Document doc = XMLUtils.createDOM();

        Element root = doc.createElement("projectDescription");
        root.appendChild(createName(doc, projectName));
        root.appendChild(createComment(doc));
        root.appendChild(createProjects(doc));
        root.appendChild(createBuildSpec(doc, buildCommands));
        root.appendChild(createNatures(doc, projectNatures));

        doc.appendChild(root);
        return doc;
    }

    private Element createName(Document doc, String projectName) {
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(projectName));
        return name;
    }

    private Element createComment(Document doc) {
        Element commment = doc.createElement("comment");
        commment.appendChild(doc.createTextNode("Auto-Generated Project File"));
        return commment;
    }

    private Element createProjects(Document doc) {
        return doc.createElement("projects");
    }

    private Element createBuildSpec(Document doc, String[] buildCommands) {
        Element spec = doc.createElement("buildSpec");
        for (int i = 0; i < buildCommands.length; i++) {
            Element command = doc.createElement("buildCommand");
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(buildCommands[i]));
            Element arguments = doc.createElement("arguments");
            command.appendChild(name);
            command.appendChild(arguments);
            spec.appendChild(command);
        }
        return spec;
    }

    private Element createNatures(Document doc, String[] projectNatures) {
        Element natures = doc.createElement("natures");
        for (int k = 0; k < projectNatures.length; k++) {
            Element nature = doc.createElement("nature");
            nature.appendChild(doc.createTextNode(projectNatures[k]));
            natures.appendChild(nature);
        }
        return natures;
    }
}
