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
public class LoginCommand extends Command {

    public LoginCommand() {
    }

    public LoginCommand(String name, String PW) {
        super();
        setName(name);
        setPW(PW);
    }

    // Modfiers
    public void setName(String newName) {
        myName = newName;
    }

    public void setPW(String newPW) {
        myPW = newPW;
    };

    // Protected
    public String getCommand() {
        return SubmitConstants.LOGIN;
    }

    private String myName;

    private String myPW;

    // RUN--------------------------------------
    protected ServerResponse run() {
        sendProgress("Authenticating...");

        // System.out.println("socket " + myConnection.mySocket.isClosed());
        // write name
        myConnection.writeString(myName);
        // write pw
        myConnection.writeString(myPW);
        // get authentication results

        getServerResponse();

        return myResponse;
    }

}