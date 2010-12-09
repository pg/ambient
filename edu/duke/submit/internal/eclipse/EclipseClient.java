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
 * Created on Jun 3, 2003
 */
package edu.duke.submit.internal.eclipse;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import edu.duke.cs.ambient.AmbientGlobals;
import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.submit.internal.client.Command;
import edu.duke.submit.internal.client.ServerResponse;
import edu.duke.submit.internal.client.SubmitConnection;
import edu.duke.submit.internal.client.SubmitConstants;

/**
 * @author jett
 */

public class EclipseClient {
    private boolean shallProceed = true;

    private Shell myShell;

    private SubmitConnection myConnection;

    private String myCourse;

    private String myProject;

    private ServerResponse myResponse;

    private ClientWindow myCurrentWindow;

    private Command myCommand;

    public EclipseClient(Shell shl, boolean isSubmitClient)
    // isSubmit client is true if you want to submit, false to get history
    {
        myShell = shl;
        myConnection = new SubmitConnection();
        String host = AmbientPlugin.getDefault().getPreferenceStore()
                .getString(AmbientGlobals.P_HOST);
        int port = AmbientPlugin.getDefault().getPreferenceStore().getInt(
                AmbientGlobals.P_PORT);
        try {
            // use default at the beginning
            if (host == "")
                host = SubmitConstants.REDIRECT_NAME;
            if (port == 0)
                port = SubmitConstants.REDIRECT_PORT;
            // System.out.println("host "+host+" port "+port);
            myConnection.setRedirectServer(host, port);
        } catch (IOException e) {
            shallProceed = false;
            MessageDialog
                    .openError(
                            myShell,
                            "ERROR",
                            "Cannot connect to server, make sure you have the corret values in the submit fields under Window->Preferences.");
        }
        if (shallProceed && isSubmitClient) {
            runSubmit();
        } else {
            // user only wants history
            myCurrentWindow = new HistoryMenuWindow(myShell, this);
            myCurrentWindow.open();
        }
    }

    public void runLogin() {
        ClientWindow login = new LoginWindow(myShell, this);
        login.open();
    }

    private void runSubmit() {
        myCurrentWindow = new SubmitWindow(myShell, this);
        myCurrentWindow.open();
    }

    private void runHistory() {
        myCurrentWindow = new HistoryWindow(myShell, this);
        myCurrentWindow.open();
    }

    private void processResponse() {
        if (!myResponse.isOK()) {
            MessageDialog.openError(myShell, "ERROR", makeMessage());
            myCurrentWindow.reEnable();
        } else if (myCommand.getCommand().equals("SUBMIT")) {
            myCurrentWindow.close();
            runHistory();
        }
    }

    private String makeMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append("Error Messages:\n\n");

        for (int i = 0; i < myResponse.getSize(); i++) {
            sb.append("Error message " + myResponse.getNumber(i) + ": "
                    + myResponse.getMessage(i) + '\n');
        }

        return sb.toString();
    }

    public ServerResponse getResponse() {
        return myResponse;
    }

    public void connect(String course) {
        int didConnect = -1;
        try {
            didConnect = myConnection.open(course);
            if (didConnect == SubmitConnection.CONNECTION_BROKEN) {
                MessageDialog
                        .openError(myShell, "ERROR",
                                "Internal error with course name! Please notify your professor");
            }
        } catch (Exception e) {
            System.out.println(didConnect);
            if (didConnect != SubmitConnection.SERVER_DOWN) {
                MessageDialog
                        .openError(myShell, "ERROR",
                                "Connection with submit server could not be established");
            }
        }
    }

    public void close() {
        myConnection.exit();
    }

    public void setCurrentProject(String course, String proj) {
        myCourse = course;
        myProject = proj;
    }

    public String getCurrentCourse() {
        return myCourse;
    }

    public String getCurrentProject() {
        return myProject;
    }

    public void handleCommand(Command cmd) {
        myCommand = cmd;
        myResponse = myConnection.runCommand(cmd);
        processResponse();
    }

    public Object[] getCourseList() {
        return myConnection.getCourseList();
    }
}
