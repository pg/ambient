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

import java.util.Observable;

/**
 * @author jett
 */
public abstract class Command extends Observable {

    protected SubmitConnection myConnection;

    protected ServerResponse myResponse;

    public Command() {
        myResponse = new ServerResponse();
    }

    public void addConnection(SubmitConnection sc) {
        myConnection = sc;
    }

    public abstract String getCommand();

    protected ServerResponse issue() {
        if (myConnection.isOpen()) {
            myConnection.writeString(getCommand());
        }
        return run();
    }

    protected void sendProgress(String s) {
        setChanged();
        notifyObservers(s);
    }

    protected void getServerResponse() {
        int numMessages = 0;
        try {
            String temp = myConnection.readServerLine();
            if (temp.equals(SubmitConstants.SERVER_TIMEOUT)) {
                System.err.println("timeout");
                myResponse = new ServerResponse(1);
                myResponse
                        .addResponse("499:Timed out while waiting for server response.");
            }
            numMessages = Integer.parseInt(temp);
        } catch (NumberFormatException e) {
            System.err.println("NFE: " + e.getMessage());
        }
        myResponse = new ServerResponse(numMessages);
        int x = 0;
        String myMessage;
        while (x < numMessages) {
            myMessage = myConnection.readServerLine();
            myResponse.addResponse(myMessage);
            x++;
        }
    }

    protected abstract ServerResponse run();

}
