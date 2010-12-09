/*
 * Created on Jul 5, 2005
 * 
 * This file is a modified version of the NewSiteDialog/EditSiteDialgo classes
 * from the initial release of Snarfer.
 */
package edu.duke.cs.ambient.snarfer.ui.dialogs;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This is a dialog for entering/modifying a URL. It has a customizable message
 * and the URL is validated for correctness before the user can proceed.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class ChangeSiteDialog extends Dialog {

    protected Text urlField;

    protected Label statusLabel;

    protected URL myURL;

    private String myMessage;

    /**
     * Creates a new ChangeSiteDialog with the specified message and URL. If url
     * is <code>null</code> then the editable text field will not be
     * initialized with that URL.
     * 
     * @param parent
     *            the parent window shell for this dialog
     * @param url
     *            the URL with which to initialize the editable text field, or
     *            <code>null</code>
     * @param message
     *            a customizable message for the dialog
     */
    public ChangeSiteDialog(Shell parent, URL url, String message) {
        super(parent);
        myURL = url;
        myMessage = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);
        parent.setLayout(new GridLayout(2, false));

        Label messageLabel = new Label(parent, SWT.NONE);
        GridData labelData = new GridData();
        labelData.horizontalSpan = 2;
        messageLabel.setLayoutData(labelData);
        messageLabel.setText(myMessage);

        Label urlLabel = new Label(parent, SWT.NONE);
        urlLabel.setText("URL:");
        urlField = new Text(parent, SWT.BORDER);
        urlField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (myURL != null) {
            urlField.setText(myURL.toString());
        }
        urlField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                verifyURL();
            }
        });

        statusLabel = new Label(parent, SWT.NONE);
        GridData statusData = new GridData(GridData.FILL_HORIZONTAL);
        statusData.horizontalSpan = 2;
        statusLabel.setLayoutData(statusData);

        return parent;
    }

    /**
     * Returns the URL enetered into this dialog.
     * 
     * @return the URL enetered into this dialog
     */
    public URL getURL() {
        return myURL;
    }

    /**
     * Returns the String representation of the URL typed into this dialog
     * (after the URL has been verified and possibly modified by the
     * {@link ChangeSiteDialog#verifyURL()} method.
     * 
     * @return the verified and possibly modified representation of the URL
     *         typed into this dialog
     */
    public String getValue() {
        return myURL.toString();
    }

    protected void cancelPressed() {
        super.cancelPressed();
    }

    protected void okPressed() {
        super.okPressed();
    }

    /**
     * Verifies the URL typed into this dialog and modifies it if needed. The
     * appropriate dialog buttons are enabled/disabled based on whether the URL
     * is valid.
     */
    protected void verifyURL() {
        try {
            String u = urlField.getText();
            if (u.endsWith(".xml") == false) {
                if (u.endsWith("/") == false) {
                    u += "/";
                }
                u += "snarf.xml";
            }
            if (u.indexOf("//") == -1) {
                u = "http://" + u;
            }
            myURL = new URL(u);
            statusLabel.setText("Press OK to accept this snarf URL.");
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        } catch (MalformedURLException e) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            statusLabel.setText("Enter a valid snarf site URL.");
        }
    }

    protected Control createButtonBar(Composite parent) {
        Control ret = super.createButtonBar(parent);
        verifyURL();
        return ret;
    }
}
