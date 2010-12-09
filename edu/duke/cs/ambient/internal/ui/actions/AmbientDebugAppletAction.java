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
import org.eclipse.jdt.internal.debug.ui.launcher.JavaAppletLaunchShortcut;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

import edu.duke.cs.ambient.AmbientPlugin;


/**
 * @author Duke Curious 2004
 */
public class AmbientDebugAppletAction extends AmbientRunAction {

	public AmbientDebugAppletAction() {
		super();
	}

	public void run(IAction action) {
		JavaAppletLaunchShortcut shortcut = new JavaAppletLaunchShortcut();
		if (getSelection() instanceof IStructuredSelection) {
			shortcut.launch(getSelection(), ILaunchManager.DEBUG_MODE);
		} else
			shortcut.launch(
				AmbientPlugin.getWorkbenchPage().getActiveEditor(),
				ILaunchManager.DEBUG_MODE);
	}
}
