/*
 * Created on Jul 14, 2005
 */
package edu.duke.cs.ambient.checkin.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.duke.cs.ambient.ui.UI;

/**
 * This warning dialog presents the user with an overwrite/synchronize/cancel
 * choice in the event that a checkin/checkout operation cannot be performed
 * automatically. The particular parts of the displayed message can be defined
 * by the user:
 * <ul>
 * <li>the general warning message providing details of the problem
 * <li>the description of the overwrite choice
 * <li>the description of the synchronize choice
 * <li>the title of the displayed window
 * </ul>
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class OverwriteSynchronizeDialog extends Dialog {

    // ///////////////////////////
    // RETURN CODES

    /**
     * Return code constant (value 2) indicating that the user selected the
     * overwrite option.
     */
    public static final int OVERWRITE = 2;

    /**
     * Return code constant (value 3) indicating that the user selected the
     * synchronize option.
     */
    public static final int SYNCHRONIZE = 3;

    // ///////////////////////////
    // BUTTON & MESSAGE LABELS

    private static final String OVERWRITE_LABEL = "Overwrite";

    private static final String SYNC_LABEL = "Synchronize";

    private static final String CANCEL_MESSAGE = "- Cancel and do nothing";

    // //////////////////////////
    // CUSTOMIZED DISPLAY MESSAGES

    private String myTitle;

    private String myWarningMessage;

    private String myOverwriteMessage;

    private String mySyncMessage;

    // ///////////////////////////
    // CONSTRUCTOR

    /**
     * Creates a new OverwriteSynchronizeDialog with the give detailed messages.
     * 
     * @param shell
     *            the parent Shell of this dialog
     * @param title
     *            the title of the dialog's window
     * @param warningMessage
     *            the warning message about the problem
     * @param overwriteMessage
     *            the description of the overwrite option
     * @param syncMessage
     *            the description of the sync option
     */
    public OverwriteSynchronizeDialog(Shell shell, String title,
            String warningMessage, String overwriteMessage, String syncMessage) {
        super(shell);
        setBlockOnOpen(true);
        this.myTitle = title;
        myWarningMessage = warningMessage;

        myOverwriteMessage = overwriteMessage;
        mySyncMessage = syncMessage;
    }

    // ///////////////////////////
    // CONSTRUCTION METHODS

    /**
     * Sets the title of the dialog to be that provided during construction as
     * well as calls the default implementation of this method.
     * 
     * @param newShell
     *            the shell to be configured
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(myTitle);
    }

    /**
     * Populates the button bar with the overwrite, synchronize, and cancel
     * buttons.
     * 
     * @param parent
     *            the Composite containing the button area.
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, OVERWRITE, OVERWRITE_LABEL, false);
        createButton(parent, SYNCHRONIZE, SYNC_LABEL, false);
        createButton(parent, CANCEL, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Creates the dialog area for this dialog, which consists of a warning
     * image, the general problem message, and a description of the three
     * possible choices: overwrite, synchronize, cancel.
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialog = (Composite) super.createDialogArea(parent);
        dialog.setLayout(new GridLayout(2, false));

        // Create the warning icon image
        Label warningImage = new Label(dialog, SWT.NONE);
        GridData imageData = new GridData();
        imageData.horizontalIndent = 6;
        warningImage.setLayoutData(imageData);
        warningImage.setImage(getShell().getDisplay().getSystemImage(
                SWT.ICON_WARNING));

        // Create the warning message area
        Label warningLabel = new Label(dialog, SWT.WRAP);
        GridData labelData = new GridData();
        labelData.widthHint = 500;
        warningLabel.setLayoutData(labelData);
        warningLabel.setText(myWarningMessage);

        // Create the option description labels
        UI.createLabel(dialog, SWT.NONE, "", 2);
        Label msg1 = UI.createLabel(dialog, SWT.WRAP, myOverwriteMessage, 2);
        Label msg2 = UI.createLabel(dialog, SWT.WRAP, mySyncMessage, 2);
        Label msg3 = UI.createLabel(dialog, SWT.WRAP, CANCEL_MESSAGE, 2);
        labelData.widthHint = Math.max(msg1.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).x, Math.max(msg2.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).x, msg3.computeSize(SWT.DEFAULT, SWT.DEFAULT).x))
                - warningImage.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

        return dialog;
    }

    // /////////////////////////////////
    // UI HANDLING METHODS

    /**
     * Sets the return code for this dialog to be buttonId and closes the dialog
     * window.
     * 
     * @param buttonId
     *            the code of the button that was pressed by the user
     */
    protected void buttonPressed(int buttonId) {
        setReturnCode(buttonId);
        close();
    }
}