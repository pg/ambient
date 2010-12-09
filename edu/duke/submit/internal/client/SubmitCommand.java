/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
 *******************************************************************************/
/*
 * Created on May 28, 2003
 */
package edu.duke.submit.internal.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * @author jett
 */
public class SubmitCommand extends Command {
    // Constructors
    public SubmitCommand(String course, String project, String root,
            String[] files) {
        super();
        setClass(course);
        setProject(project);
        setRoot(root);
        setFiles(files);
    }

    // Modfiers
    public void setClass(String newClass) {
        myClass = newClass;
    }

    public void setProject(String newProj) {
        myProj = newProj;
    }

    public void setRoot(String root) {
        myRoot = root;
        if (myRoot.endsWith(File.separator)) {
            myRoot = myRoot.substring(0, myRoot.length() - 1);
        }
    }

    public void setFiles(String[] newFiles) {
        myFiles = new String[newFiles.length];
        System.arraycopy(newFiles, 0, myFiles, 0, newFiles.length);
    }

    // Protected
    public String getCommand() {
        return SubmitConstants.SUBMIT;
    }

    // Private
    private String myClass;

    private String myProj;

    private String[] myFiles;

    private File myTempFile;

    private String myRoot;

    private void makeJar() {
        BufferedInputStream myInput;
        byte[] myBuffer = new byte[1024];
        int size;
        File tFile;
        try {
            myTempFile = File.createTempFile("submit", ".jar");
            JarOutputStream jarOutput = new JarOutputStream(
                    new FileOutputStream(myTempFile));
            for (int i = 0; i < myFiles.length; i++) {
                tFile = new File(myRoot, myFiles[i]);
                if (tFile.isDirectory()) {
                    JarEntry entry = new JarEntry(myFiles[i]
                            .endsWith(File.separator) ? myFiles[i] + "."
                            : myFiles[i] + File.separator + ".");
                    entry.setSize(0);
                    jarOutput.putNextEntry(entry);
                    jarOutput.closeEntry();
                } else {
                    JarEntry entry = new JarEntry(myFiles[i]);
                    try {
                        myInput = new BufferedInputStream(new FileInputStream(
                                tFile));
                    } catch (FileNotFoundException e) {
                        System.out
                                .println("You attempted to submit file \""
                                        + myRoot
                                        + File.separator
                                        + myFiles[i]
                                        + "\" which was not found on your file system.");
                        System.out
                                .println("Your submission will continue without that file");
                        continue;
                    }
                    entry.setSize(tFile.length());
                    jarOutput.putNextEntry(entry);
                    while ((size = myInput.read(myBuffer)) != -1)
                        jarOutput.write(myBuffer, 0, size);
                    myInput.close();
                    jarOutput.closeEntry();
                }
            }
            jarOutput.close();
        } catch (IOException e) {
            System.err.println("Error creating temp jar file: " + e);
        }
    }

    // RUN-----------------------------------
    protected ServerResponse run() {
        sendProgress("Validating files...");
        // write class
        // this is first communication since login, so check that
        // the connection is still up
        if (myConnection.isOpen() && myConnection.writeString(myClass)) {
            // myConnection.writeString(myClass);
            // write project
            myConnection.writeString(myProj);
            // setup for next few commands
            int x = 0;
            int size = 0;
            // send number of files
            ArrayList justFiles = new ArrayList();
            for (x = 0; x < myFiles.length; x++) {
                File curFile = new File(myRoot, myFiles[x]);
                if (curFile.isDirectory())
                    continue;
                justFiles.add(myFiles[x]);
                size += curFile.length();
            }
            myConnection.writeString(justFiles.size() + "");
            for (x = 0; x < justFiles.size(); x++)
                myConnection.writeString(justFiles.get(x).toString());
            // Write file size
            myConnection.writeString(String.valueOf(size));
            // get response, see if files sent were good
            getServerResponse();
            if (!myResponse.isOK()) {
                return myResponse;
            }
            // If here, all previous data was correct
            // create jar file and submit
            sendProgress("Compressing files...");
            makeJar();
            // Drop a file across the network
            sendProgress("Submitting files...");
            myConnection.writeFile(myTempFile);
            myTempFile.deleteOnExit();
            // Let me know that the file is completed
            getServerResponse();
            sendProgress("Files transferred");
            return myResponse;
        } else {
            myResponse = new ServerResponse(1);
            myResponse
                    .addResponse("409: Session timed out, please login again.");
            return myResponse;
        }
    }
}
