/*
 * Created on Jun 4, 2003
 */
package edu.duke.cs.ambient.snarfer.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;

/**
 * @author Administrator
 */
public class OverwriteDialog extends Dialog {
    public static int YES = 0;

    public static int YESTOALL = 1;

    public static int NO = 2;

    public static int NOTOALL = 3;

    private String filename;

    private String title;

    private boolean showAllButtons;

    public OverwriteDialog(Shell shell, String title, String filename,
            boolean all) {
        super(shell);
        this.title = title;
        this.filename = filename;
        showAllButtons = all;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        this.createButton(parent, YES, "Yes", false);
        if (showAllButtons)
            this.createButton(parent, YESTOALL, "Yes to all", false);
        this.createButton(parent, NO, "No", true);
        if (showAllButtons)
            this.createButton(parent, NOTOALL, "No to all", false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(1, false));
        Label question = new Label(panel, SWT.LEFT);
        question.setText("Overwrite '" + filename + "'?");
        return panel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#open()
     */
    public int open() {
        return super.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        setReturnCode(buttonId);
        close();
    }
}
