/*
 * Created on Jul 18, 2005
 */
package edu.duke.cs.ambient.checkin.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import edu.duke.cs.ambient.checkin.DukePathMaker;
import edu.duke.cs.ambient.checkin.ui.wizards.CheckinWizard;
import edu.duke.cs.ambient.ui.wizards.AmbientWizardDialog;

/**
 * @since 2.0
 * @author Marcin Dobosz
 */
public class OpenCheckinWizardAction implements IWorkbenchWindowActionDelegate {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IWizard wizard = new CheckinWizard(new DukePathMaker());
        AmbientWizardDialog dialog = new AmbientWizardDialog(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        dialog.open();
    }

    /**
     * This implementation does nothing.
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

    /**
     * This implementation does nothing.
     */
    public void dispose() {
        // do nothing
    }

    /**
     * This implementation does nothing.
     */
    public void init(IWorkbenchWindow window) {
        // do nothing
    }

}
