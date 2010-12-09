/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * WizardPages implementing this interface can react to the next or back button
 * being pressed. Clients need to implement the performNext() and performBack()
 * methods, in which they can perform the work that is to be done after the next
 * or back button gets pressed but before the appropriate page gets displayed.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public interface IAmbientWizardPage extends IWizardPage {

    /**
     * Called right after the next button gets pressed in a WizardDialgo window,
     * but before the next page gets displayed.
     */
    public void performNext();

    /**
     * Called right after the back button gets pressed in a WizardDialog window,
     * but before the previous page gets displayed.
     */
    public void performBack();

}
