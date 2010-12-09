package edu.duke.cs.ambient.checkin.jobs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.core.client.Commit;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.operations.AddOperation;
import org.eclipse.team.internal.ccvs.ui.operations.CommitOperation;

public class AmbientCommitOperation implements IRunnableWithProgress {
    private IProject project;

    private ArrayList resourcesToCommit;

    private final static String COMMIT_STR = "Committing...";

    public AmbientCommitOperation(IProject project) {
        this.project = project;
        resourcesToCommit = new ArrayList();
    }

    public void run(IProgressMonitor monitor) {
        try {
            monitor.beginTask(COMMIT_STR, 100);
            try {
                monitor.subTask("Adding files...");
                new AddOperation(null, AddOperation
                        .asResourceMappers(getResources()))
                        .run(new SubProgressMonitor(monitor, 50,
                                SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
                monitor.subTask("Uploading files...");
                new CommitOperation(null, CommitOperation
                        .asResourceMappers(new IResource[] { project }),
                        new LocalOption[] { Commit.FORCE }, "")
                        .run(new SubProgressMonitor(monitor, 50,
                                SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
            } catch (InvocationTargetException e) {
            } catch (InterruptedException e) {
            }
        } finally {
            monitor.done();
        }
    }

    private IResource[] getResources() {
        resourcesToCommit.clear();
        IResource[] children = {};
        try {
            try {
                children = project.members();
            } catch (CoreException e) {
            }
            for (int k = 0; k < children.length; k++)
                getAllFiles(children[k]);
            return (IResource[]) resourcesToCommit.toArray(new IResource[0]);
        } catch (NullPointerException e) {
        }
        return children;
    }

    private void getAllFiles(IResource root) {
        try {
            int type = root.getType();
            if (type == IResource.FILE
                    && !root.getFileExtension().equalsIgnoreCase("class")) {
                resourcesToCommit.add(root);
                return;
            }
            if (type == IResource.FOLDER) {
                IResource[] children = ((IFolder) root).members();
                for (int k = 0; k < children.length; k++) {
                    getAllFiles(children[k]);
                }
            }
        } catch (CoreException e) {
        }
    }

}
