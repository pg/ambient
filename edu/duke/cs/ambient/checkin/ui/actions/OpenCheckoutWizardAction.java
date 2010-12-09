/*******************************************************************************
 * Copyright (c) 2004 Duke University
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 *  
 ******************************************************************************/
package edu.duke.cs.ambient.checkin.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import edu.duke.cs.ambient.checkin.ui.wizards.CheckoutWizard;
import edu.duke.cs.ambient.ui.wizards.AmbientWizardDialog;

/**
 * This action opens the
 * {@link edu.duke.cs.ambient.checkin.ui.wizards.CheckoutWizard}.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenCheckoutWizardAction implements IWorkbenchWindowActionDelegate {

    /**
     * Running this action will open the {@link CheckoutWizard}wizard dialog.
     * 
     * @param action
     *            not used.
     */
    public void run(IAction action) {
        // open project checkout wizard
        try {
            IWizard wizard = new CheckoutWizard();
            WizardDialog dialog = new AmbientWizardDialog(PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getShell(),
                    wizard);
            dialog.open();
        } catch (Exception e) {
            // nothing
        }
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

    /**
     * This implementation does nothing.
     */
    public void init(IWorkbenchWindow window) {
        // do nothing
    }
}
