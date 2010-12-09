/*******************************************************************************
 * Copyright (c) 2004 Duke University
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 *  
 ******************************************************************************/
package edu.duke.cs.ambient.submit.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.duke.cs.ambient.AmbientGlobals;
import edu.duke.cs.ambient.AmbientPlugin;

public class SubmitPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public SubmitPreferencePage() {
        super(GRID);
        setPreferenceStore(AmbientPlugin.getDefault().getPreferenceStore());
        setDescription("Submit server settings");
        initializeDefaults();
    }

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults() {
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(AmbientGlobals.P_PORT, 31415);
        store.setDefault(AmbientGlobals.P_HOST, "submit.cs.duke.edu");
    }

    public void createFieldEditors() {
        addField(new StringFieldEditor(AmbientGlobals.P_HOST, "Server address",
                getFieldEditorParent()));
        addField(new IntegerFieldEditor(AmbientGlobals.P_PORT, "Port number",
                getFieldEditorParent()));
    }

    /**
     * This implementation does nothing.
     */
    public void init(IWorkbench workbench) {
        // do nothing
    }
}