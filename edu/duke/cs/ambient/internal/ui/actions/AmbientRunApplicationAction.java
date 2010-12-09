/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
*******************************************************************************/
package edu.duke.cs.ambient.internal.ui.actions;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

import edu.duke.cs.ambient.AmbientPlugin;


/**
 * @author Duke Curious 2004
 */
public class AmbientRunApplicationAction extends AmbientRunAction {

	public AmbientRunApplicationAction() {
		super();
	}

	public void run(IAction action) {

		//get command line arguments from the user
		arguments = null;

		if (getSelection() instanceof IStructuredSelection) {
			launch(getSelection(), ILaunchManager.RUN_MODE);
		} else
			launch(
				AmbientPlugin.getWorkbenchPage().getActiveEditor(),
				ILaunchManager.RUN_MODE);
			}
}
