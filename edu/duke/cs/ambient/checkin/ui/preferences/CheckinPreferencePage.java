/*
 * Created on Jul 8, 2005
 */
package edu.duke.cs.ambient.checkin.ui.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.duke.cs.ambient.AmbientGlobals;
import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.checkin.jobs.SetupCVSAction;
import edu.duke.cs.ambient.ui.UI;

/**
 * This preference page handles preferences specific to the checkin
 * 
 * @version 2.0
 * @author Marcin Dobosz
 */
public class CheckinPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public CheckinPreferencePage() {
        super(GRID);
        setPreferenceStore(AmbientPlugin.getDefault().getPreferenceStore());
        setDescription("Checkin/Checkout Settings");
        noDefaultAndApplyButton();
    }

    /**
     * Returns a preference store associated with this preference page. This
     * call is equivalent to
     * <code>AmbientPlugin.getDefault().getPreferenceStore()</code>.
     */
    public IPreferenceStore doGetPreferenceStore() {
        return AmbientPlugin.getDefault().getPreferenceStore();
    }

    /**
     * This implementation does nothing.
     */
    public void init(IWorkbench workbench) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        final StringFieldEditor hostField = new StringFieldEditor(
                AmbientGlobals.HOST_NAME, "CVS Host", getFieldEditorParent());
        addField(hostField);
        final StringFieldEditor userField = new StringFieldEditor(
                AmbientGlobals.USER_NAME, "User Name", getFieldEditorParent());
        addField(userField);

        Composite comp = getFieldEditorParent();

        Composite buttonComp = new Composite(comp, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        GridData data = UI.createData(GridData.FILL_HORIZONTAL,
                ((GridLayout) comp.getLayout()).numColumns);
        data.horizontalIndent = 0;
        data.verticalIndent = 15;
        buttonComp.setLayoutData(data);
        Button b = new Button(buttonComp, SWT.NONE);
        b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER
                | GridData.GRAB_HORIZONTAL));
        b.setText("Set up CVS Repository");
        b.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                String user = userField.getTextControl(getFieldEditorParent())
                        .getText();
                String host = hostField.getTextControl(getFieldEditorParent())
                        .getText();
                if (user == null || user.length() == 0)
                    MessageDialog
                            .openError(getShell(), "No user name defined",
                                    "Please enter a proper user name into the User Name field first.");
                else
                    new SetupCVSAction(getShell(), host, user).execute();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

}
