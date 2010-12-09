package edu.duke.cs.ambient.checkin.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jcraft.jsch.JSchException;

import edu.duke.cs.ambient.checkin.remote.SSHTool;

public class SetupCVSAction {

     private static final String COMMAND =
     "/afs/acpub/project/cps/bin/ambient/setup_cvs.sh";

    private SSHTool mySSHTool = null;

    private String myHost;

    private String myUser;

    private Shell myShell;

    private class PasswordDialog extends Dialog {

        /**
         * The title of the dialog.
         */
        private String title;

        /**
         * The message to display, or <code>null</code> if none.
         */
        private String message;

        /**
         * The input value; the empty string by default.
         */
        private String value = "";//$NON-NLS-1$

        /**
         * Input text widget.
         */
        private Text text;

        /**
         * Error message label widget.
         */

        /**
         * Creates an input dialog with OK and Cancel buttons. Note that the
         * dialog will have no visual representation (no widgets) until it is
         * told to open.
         * <p>
         * Note that the <code>open</code> method blocks for input dialogs.
         * </p>
         * 
         * @param parentShell
         *            the parent shell, or <code>null</code> to create a
         *            top-level shell
         * @param dialogTitle
         *            the dialog title, or <code>null</code> if none
         * @param dialogMessage
         *            the dialog message, or <code>null</code> if none
         */
        public PasswordDialog(Shell parentShell, String dialogTitle,
                String dialogMessage) {
            super(parentShell);
            this.title = dialogTitle;
            message = dialogMessage;
        }

        /*
         * (non-Javadoc) Method declared on Dialog.
         */
        protected void buttonPressed(int buttonId) {
            if (buttonId == IDialogConstants.OK_ID) {
                value = text.getText();
            } else {
                value = null;
            }
            super.buttonPressed(buttonId);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
         */
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            if (title != null)
                shell.setText(title);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        protected void createButtonsForButtonBar(Composite parent) {
            // create OK and Cancel buttons by default
            createButton(parent, IDialogConstants.OK_ID,
                    IDialogConstants.OK_LABEL, true);
            createButton(parent, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
            // do this here because setting the text will set enablement on the
            // ok button
            text.setFocus();
        }

        /*
         * (non-Javadoc) Method declared on Dialog.
         */
        protected Control createDialogArea(Composite parent) {
            // create composite
            Composite composite = (Composite) super.createDialogArea(parent);
            // create message
            if (message != null) {
                Label label = new Label(composite, SWT.WRAP);
                label.setText(message);
                GridData data = new GridData(GridData.GRAB_HORIZONTAL
                        | GridData.GRAB_VERTICAL
                        | GridData.HORIZONTAL_ALIGN_FILL
                        | GridData.VERTICAL_ALIGN_CENTER);
                data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
                label.setLayoutData(data);
                label.setFont(parent.getFont());
            }
            text = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
            text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.HORIZONTAL_ALIGN_FILL));

            applyDialogFont(composite);
            return composite;
        }

        /**
         * Returns the string typed into this input dialog.
         * 
         * @return the input string
         */
        public String getValue() {
            return value;
        }
    }

    public SetupCVSAction(Shell shell, String host, String user) {
        myHost = host;
        myUser = user;
        myShell = shell;
    }

    public void execute() {
        if (startConnection()) {
            try {
                mySSHTool.sendCommand(COMMAND);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MessageDialog.openInformation(myShell, "CVS Set-up Complete",
                    "The CVS repository has been successfully initialized for the user '"
                            + myUser + "'.");
        }
    }

    private boolean startConnection() {
        if (mySSHTool != null) {
            return true;
        }

        boolean badFlag = false;
        mySSHTool = new SSHTool();
        try {
            mySSHTool.openSession(myUser, myHost);
        } catch (JSchException e) {
            badFlag = true;
        }

        BufferedReader myStream = null;
        try {
            PasswordDialog passwordDialog = new PasswordDialog(myShell,
                    "Enter password", "Please enter the password for " + myUser
                            + " at " + myHost
                            + ". The password you enter will not be stored.");
            passwordDialog.setBlockOnOpen(true);
            switch (passwordDialog.open()) {
            case InputDialog.CANCEL:
                return false;
            case InputDialog.OK:
                myStream = mySSHTool.connect(passwordDialog.getValue());
            }

        } catch (JSchException e1) {
            String reason = null;
            if (e1.getLocalizedMessage().contains("UnknownHostException")) {
                reason = "Reason: unknown host " + myHost;
            } else if (e1.getLocalizedMessage().equalsIgnoreCase("auth fail")) {
                reason = "Reason: invalid username or password";
            }
            String errorMsg = "Cannot connect to user "
                    + myUser
                    + " at "
                    + myHost
                    + ".\n"
                    + (reason == null ? "Reason: " + e1.getLocalizedMessage()
                            : reason) + "\nPlease try again.";
            MessageDialog.openError(myShell, "Connection error", errorMsg);
            badFlag = true;
        } catch (IOException e1) {
            e1.printStackTrace();
            badFlag = true;
        }

        if (!badFlag) {
            StreamReader myReader = new StreamReader(myStream);
//            new Thread(myReader).start();
        } else {
            mySSHTool.disconnect();
            mySSHTool = null;
            return false;
        }

        return true;
    }

    class StreamReader implements Runnable {
        private boolean myHaltFlag = false;

        BufferedReader myBufferedReader;

        public StreamReader(BufferedReader reader) {
            myBufferedReader = reader;
        }

        private void halt() {
            myHaltFlag = false;
        }

        public synchronized void run() {
            try {
                while (true) {
                    final String out = myBufferedReader.readLine();
                    if (out == null)
                        break;

                    // wait(100);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }
}
