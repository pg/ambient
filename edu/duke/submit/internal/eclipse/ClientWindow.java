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

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @author jett
 */
public abstract class ClientWindow extends Dialog implements Listener, Observer {
    protected EclipseClient myClient;

    public ClientWindow(Shell sh, EclipseClient ec) {
        super(sh);
        myClient = ec;
    }

    public void update(Observable o, Object arg) {
        if (arg.getClass().isInstance(new String())) {
            setProgressText((String) arg);
        }
    }

    protected void setProgressText(String prog) {
    }

    public abstract void reEnable();

    public void handleEvent(Event e) {
    }

}
