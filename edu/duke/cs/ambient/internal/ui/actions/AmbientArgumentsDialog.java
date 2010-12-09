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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.duke.cs.ambient.AmbientPlugin;

/**
 * @version 2.0
 */
public class AmbientArgumentsDialog extends TitleAreaDialog {

    public static final String DSECTIONNAME = "AmbientArgumentsDialog";

    public static final String ARGUMENTS = "arguments";

    private String arguments = null;

    // widgets
    private Text argsText;

    public AmbientArgumentsDialog(Shell parentShell) {
        super(parentShell);
    }

    /*
     * (non-Javadoc) Method declared in Window.
     */
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle("Command line arguments"); //$NON-NLS-1$
        setMessage("Please enter command line arguments you would like to use."); //$NON-NLS-1$

        return contents;
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        IDialogSettings dset = AmbientPlugin.getDefault().getDialogSettings();
        IDialogSettings dSection = dset.getSection(DSECTIONNAME);

        if (dSection == null) {
            dSection = dset.addNewSection(DSECTIONNAME);
            dSection.put(ARGUMENTS, "");
        }

        // top level composite
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        // create a composite with standard margins and spacing
        Composite composite = new Composite(parentComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parentComposite.getFont());

        argsText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        argsText.setFocus();
        argsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        argsText.setText(dSection.get(ARGUMENTS));
        argsText.selectAll();

        return parentComposite;
    }

    protected void okPressed() {
        IDialogSettings dset = AmbientPlugin.getDefault().getDialogSettings();
        IDialogSettings dSection = dset.getSection(DSECTIONNAME);
        arguments = argsText.getText();
        dSection.put(ARGUMENTS, arguments);
        super.close();
    }

    public String getArguments() {
        return arguments;
    }
}
