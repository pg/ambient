/*
 * Created on Aug 15, 2003
 */
package edu.duke.cs.ambient.projects;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.IEditorInput;

/**
 * @author jett
 */
public class ProjectLoader {
    private Shell myShell;

    public ProjectLoader(Shell shell) {
        myShell = shell;
    }

    public void loadProject(IProjectDescription desc, IProject project,
            IFile fileToOpen) {
        final IFile toOpen = fileToOpen;

        WorkspaceModifyOperation op = new InstallProjectOperation(project, desc);
        try {
            (new ProgressMonitorDialog(myShell)).run(true, true, op);
        } catch (InvocationTargetException e) {
            MessageDialog.openError(myShell, "InstallWizard.load.error",
                    "Error loading the project '" + desc.getName()
                            + "' into the workspace:\n"
                            + e.getCause().getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (toOpen != null) {
            myShell.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage page = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getActivePage();
                    try {
                        page.openEditor((IEditorInput) toOpen, "");
                    } catch (PartInitException e) {
                    }
                }
            });
        }
    }

    private class InstallProjectOperation extends WorkspaceModifyOperation {
        private IProject project;

        private IProjectDescription desc;

        public InstallProjectOperation(IProject project,
                IProjectDescription desc) {
            this.project = project;
            this.desc = desc;
        }

        protected void execute(IProgressMonitor monitor) throws CoreException {
            monitor.beginTask("Loading and opening project...", 2000);

            String rootpath = ResourcesPlugin.getWorkspace().getRoot()
                    .getLocation().toFile().getAbsolutePath();
            String path = desc.getLocation().toFile().getAbsolutePath();

            if (path.indexOf(rootpath) == 0) // if the default
            {
                project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                        desc.getLocation().toFile().getName());
                project.create(new SubProgressMonitor(monitor, 1000));

            } else {
                project.create(desc, new SubProgressMonitor(monitor, 1000));
            }
            if (monitor.isCanceled())
                throw new OperationCanceledException();
            project.open(new SubProgressMonitor(monitor, 1000));
        }
    }
}
