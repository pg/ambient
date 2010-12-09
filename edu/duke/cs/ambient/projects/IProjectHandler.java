/*
 * Created on Jun 28, 2005
 */
package edu.duke.cs.ambient.projects;

import java.io.File;

/**
 * Classes implementing this interface are responsible for knowing how to handle
 * different types of Eclipse projects. This can include Java projects, CDT
 * projects, etc.
 * <p>
 * An implementing class must be able to create appropriate project
 * configuration files (for example ".project" files) specific to a particular
 * project type. It is not necessarily responsible for creating/modifying source
 * files or other resources.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public interface IProjectHandler {

    /**
     * Creates files appropriate to this project type in the specified project
     * directory.
     * 
     * @param projectDir
     *            the root directory of the project.
     * @param projectName
     *            the name of the project being created.
     */
    public void createProject(File projectDir, String projectName);
}
