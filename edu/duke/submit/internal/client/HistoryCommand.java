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
 *
 */
package edu.duke.submit.internal.client;

/**
 * @author jett
 */

public class HistoryCommand extends Command {

    public HistoryCommand(String course, String project) {
        myClass = course;
        myProject = project;
    }

    public String getCommand() {
        return SubmitConstants.HISTORY;
    }

    protected ServerResponse run() {
        sendProgress("Getting history...");
        myConnection.writeString(myClass);
        myConnection.writeString(myProject);

        getServerResponse();

        sendProgress("History received");
        return myResponse;
    }

    private String myClass;

    private String myProject;
}