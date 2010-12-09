/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;

/**
 * This extension of the standard WizardPage has the ability to react to
 * pressing the next and back button.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public abstract class AmbientWizardPage extends WizardPage implements
        IAmbientWizardPage {

    /**
     * Creates a new WizardPage with the given name.
     * 
     * @param name
     *            the name of the page
     */
    public AmbientWizardPage(String name) {
        super(name);
    }

    /**
     * This default implementation does nothing.
     */
    public void performNext() {
        // do nothing
    }

    /**
     * This default implementation does nothing.
     */
    public void performBack() {
        // do nothing
    }
}
