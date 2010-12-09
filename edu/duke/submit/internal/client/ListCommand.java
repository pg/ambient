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
 * Created on Jun 4, 2003
 *
 */
package edu.duke.submit.internal.client;

import java.util.ArrayList;

/**
 * @author jett
 */
public class ListCommand extends Command {

    private String myCourse = "x";

    public final static String SERVER_DOWN = "server down bla bla";

    public ListCommand(String course) {
        myCourse = course;
    }

    public String getCommand() {
        return SubmitConstants.LIST;
    }

    protected ServerResponse run() {
        if (!myConnection.isOpen()) {
            myResponse = new ServerResponse(2);
            myResponse.addResponse(SERVER_DOWN);
            myResponse.addResponse("server is down");
            return myResponse;
        }

        sendProgress("Receiving courses...");
        ArrayList temp = new ArrayList();
        myConnection.writeString(myCourse);
        int projectNum = 0;
        try {
            projectNum = Integer.parseInt(myConnection.readServerLine());
        } catch (Exception e) {

        }
        for (int i = 0; i < projectNum; i++) {
            temp.add(myConnection.readServerLine());
        }
        myResponse = new ServerResponse(temp.size());
        for (int x = 0; x < temp.size(); x++) {
            myResponse.addResponse((String) temp.get(x));
        }
        sendProgress("Courses and projects received...");
        return myResponse;
    }
}
