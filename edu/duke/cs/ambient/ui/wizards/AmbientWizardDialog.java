/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.ui.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import edu.duke.cs.ambient.ui.wizards.IAmbientWizardPage;

/**
 * This extension to the WizardDialog class contains the following extra
 * features:
 * <ul>
 * <li>The pages contained in the Wizard for this WizardDialog that implement
 * the {@link edu.duke.cs.ambient.ui.wizards.IAmbientWizardPage}interface will
 * be notified when the next or back button get pressed via calls to the
 * {@link IAmbientWizardPage#performNext()}and
 * {@link IAmbientWizardPage#performBack()}methods.
 * </ul>
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class AmbientWizardDialog extends WizardDialog {

    /**
     * Creates a new wizard dialog for the given wizard.
     * 
     * @param parentShell
     *            the parent shell
     * @param newWizard
     *            the wizard this dialog is working on
     */
    public AmbientWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }

    /**
     * Upon pressing of the next button, the current page will be notified of
     * this event via a call to {@link IAmbientWizardPage#performNext()}, given
     * that the current page is an instance of {@link IAmbientWizardPage}. The
     * page will then be switched to the next page.
     */
    protected void nextPressed() {
        if (getCurrentPage() instanceof IAmbientWizardPage) {
            ((IAmbientWizardPage) getCurrentPage()).performNext();
        }
        super.nextPressed();
    }

    /**
     * Upon pressing of the back button, the current page will be notified of
     * this event via a call to {@link IAmbientWizardPage#performBack()}, given
     * that the current page is an instance of {@link IAmbientWizardPage}. The
     * page will then be switched to the previous page.
     */
    protected void backPressed() {
        if (getCurrentPage() instanceof IAmbientWizardPage) {
            ((IAmbientWizardPage) getCurrentPage()).performBack();
        }
        super.backPressed();
    }
}
