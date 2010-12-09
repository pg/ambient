/*******************************************************************************
 * Copyright (c) 2004 Duke University
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 *  
 ******************************************************************************/
package edu.duke.cs.ambient.ui.preferences;

import org.eclipse.ui.IWorkbench;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The main preference page of Ambient.
 */
public class MainPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * This implementation does nothing.
     */
    public void init(IWorkbench workbench) {
        // do nothing
    }

    /**
     * Creates the information labels to be displayed in this preference page.
     */
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();

        // The main composite
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        // TODO change these labels later
        new Label(composite, SWT.NONE)
                .setText("Provided by Duke University Computer Science Department");
        new Label(composite, SWT.NONE).setText("http://www.cs.duke.edu");
        new Label(composite, SWT.NONE)
                .setText("Questions? Go to our website at http://www.cs.duke.edu/csed/ambient");
        return composite;
    }
}
