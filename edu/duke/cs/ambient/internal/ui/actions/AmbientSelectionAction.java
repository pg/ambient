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
 * Created on Nov 19, 2003
 *
 */
package edu.duke.cs.ambient.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author Duke Curious 2004
 */
public abstract class AmbientSelectionAction
	extends Action
	implements IWorkbenchWindowActionDelegate {

	protected ISelection fSelection;

	/**
	 * default constructor
	 */
	public AmbientSelectionAction() {
		fSelection = StructuredSelection.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		//remember the element the user selected last
		fSelection = selection;
	}

	/**
	 * 
	 * @return the last selected element
	 */
	public ISelection getSelection() {
		return fSelection;
	}
}
