/*******************************************************************************
 * Copyright (c) 2004 Duke University All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Common
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 ******************************************************************************/

package edu.duke.cs.ambient.submit.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.duke.submit.internal.eclipse.EclipseClient;

public class SubmitAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;

    public void run(IAction action) {
        // TODO break this up into two seperate actions
        new EclipseClient(window.getShell(), true);
    }

    /**
     * This implementation does nothing.
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

    /**
     * This implementation does nothing.
     */
    public void dispose() {
        // do nothing
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}