/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
 *******************************************************************************/
package edu.duke.submit.internal.client;

/*
 * Created on May 29, 2003
 */

/**
 * @author jett
 */
public class ServerResponse {
    public static String NO_PROJECTS = "NO_PROJECTS";

    public ServerResponse(int size) {
        mySize = size;
        myNumbers = new int[size];
        myMessages = new String[size];
    }

    public ServerResponse() {
        mySize = 0;
        myNumbers = new int[0];
        myMessages = new String[0];
    }

    private void addNumber(int num) {
        myNumbers[index] = num;
    }

    private void addMessage(String message) {
        myMessages[index] = message;
    }

    public int getNumber(int i) {
        return myNumbers[i];
    }

    public String getMessage(int i) {
        try {
            return myMessages[i];
        } catch (Exception e) {
            return NO_PROJECTS;
        }
    }

    public void addResponse(String newMessage) {
        if (index < mySize) {
            if ((newMessage.length() > SubmitConstants.SR_CODE_LENGTH)
                    && (newMessage.charAt(SubmitConstants.SR_CODE_LENGTH) == ':')) {
                addNumber(Integer.parseInt(newMessage.substring(0,
                        SubmitConstants.SR_CODE_LENGTH)));
                addMessage(newMessage
                        .substring(SubmitConstants.SR_CODE_LENGTH + 1));
            } else {
                addNumber(299);
                addMessage(newMessage);
            }
            index++;
        }
    }

    public int getSize() {
        return mySize;
    }

    public boolean isOK() {
        try {
            return (myNumbers[0] / 100 == SubmitConstants.SR_CODE_SERIES_GOOD);
        } catch (Exception e) {
        }

        return true;
    }

    public void print() {
        System.out.println("Server Response:");
        for (int i = 0; i < mySize; i++) {
            System.out.println("[" + i + "] = " + myNumbers[i] + ":"
                    + myMessages[i]);
        }
    }

    private String[] myMessages;

    private int[] myNumbers;

    private int index = 0;

    private int mySize;
}
