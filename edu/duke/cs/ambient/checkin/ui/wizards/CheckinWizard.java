/*
 * Created on Jul 18, 2005
 */
package edu.duke.cs.ambient.checkin.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSCommunicationException;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.operations.ShareProjectOperation;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceCommitOperation;
import org.eclipse.ui.PlatformUI;

import edu.duke.cs.ambient.AmbientGlobals;
import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.checkin.DukeUserNameValidator;
import edu.duke.cs.ambient.checkin.IPathMaker;
import edu.duke.cs.ambient.checkin.IUserNameValidator;
import edu.duke.cs.ambient.checkin.jobs.AmbientSyncAction;
import edu.duke.cs.ambient.checkin.jobs.AmbientCommitOperation;
import edu.duke.cs.ambient.checkin.jobs.SynchronizationChecker;
import edu.duke.cs.ambient.checkin.ui.dialogs.OverwriteSynchronizeDialog;
import edu.duke.cs.ambient.ui.UI;
import edu.duke.cs.ambient.ui.wizards.AmbientWizardPage;

/**
 * @since 2.0
 * @author Marcin Dobosz
 */
public class CheckinWizard extends Wizard {
    

    private boolean DEBUG = false;

    private FirstPage page1;

    private SecondPage page2;

    private IPreferenceStore myStore;

    private IPathMaker myPathMaker;

    private class FirstPage extends AmbientWizardPage {

        // //////////////////////////////////
        // WIDGETS

        private Label statusLabel;

        private TreeViewer myTreeViewer;

        // //////////////////////////////////
        // CONSTRUCTORS

        public FirstPage() {
            super("First Page");

            setPageComplete(false);
        }

        // //////////////////////////////////
        // CONSTRUCTION METHODS

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));

            statusLabel = UI
                    .createSimpleLabel(composite, "Available projects:");
            statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            myTreeViewer = new TreeViewer(composite, SWT.BORDER);
            myTreeViewer.getTree().setLayoutData(
                    new GridData(GridData.FILL_BOTH));
            myTreeViewer.setLabelProvider(new ProjectViewerLabelProvider());
            myTreeViewer.setContentProvider(new ProjectViewerContentProvider());
            myTreeViewer
                    .addSelectionChangedListener(new ISelectionChangedListener() {
                        public void selectionChanged(SelectionChangedEvent event) {
                            if (isCurrentShared()) {
                                setPageComplete(true);
                                page2.setPageComplete(true);
                            } else {
                                setPageComplete(false);
                                page2.setPageComplete(false);
                            }
                            getContainer().updateButtons();
                        }
                    });
            myTreeViewer.setInput(getProjects());

            setControl(composite);
        }

        // ///////////////////////////////////////
        // CONTROL METHODS

        public boolean canFlipToNextPage() {
            return getSelection() != null && !isCurrentShared();
        }

        public void performNext() {
            setPageComplete(true);
            page2.setPageComplete(page2.isIDValid(page2.getUsername()));
        }

        // //////////////////////////////////////
        // ACCESSORS

        /**
         * Returns the project selected by the user in this wizard page, or
         * <code>null</code> if a project has not yet been selected (this will
         * happen if the viewer has not yet been created).
         * 
         * @return the selected project, or <code>null</code>
         */
        public IProject getSelection() {
            if (myTreeViewer == null)
                return null;
            return (IProject) ((IStructuredSelection) myTreeViewer
                    .getSelection()).getFirstElement();
        }

        /**
         * Returns <code>true</code> if the project returned by
         * {@link CheckinWizard.FirstPage#getSelection()} is shared with a team
         * repository, and <code>false</code> otherwise. If the value returned
         * by {@link CheckinWizard.FirstPage#getSelection()} is
         * <code>null</code> the <code>false</code> is returned.
         * 
         * @return <code>true</code> if the currently selected project is
         *         shared with a repository, and <code>false</code> otherwise
         */
        public boolean isCurrentShared() {
            IProject current = getSelection();
            if (current == null)
                return false;
            return RepositoryProvider.isShared(current);
        }

        /**
         * Returns an ArrayList of all open projects available in the workspace.
         * 
         * @return an ArrayList of all open projects in the workspace
         */
        private ArrayList getProjects() {
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                    .getProjects();
            ArrayList input = new ArrayList();
            for (int i = 0; i < projects.length; i++) {
                if (projects[i].isOpen())
                    input.add(projects[i]);
            }
            return input;
        }
    }

    private class SecondPage extends AmbientWizardPage {

        // /////////////////////////////////////
        // WIDGETS

        private Text userNameField;

        private Text modulePathField;

        // /////////////////////////////////////
        // OTHER STATE VARIABLES

        private IUserNameValidator myValidator;

        // /////////////////////////////////////
        // CONSTRUCTOR

        public SecondPage(IUserNameValidator validator) {
            super("Second Page");
            myValidator = validator;
            // setPageComplete(false);
        }

        // /////////////////////////////////////
        // CONSTRUCTION METHODS

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));

            setControl(composite);

            UI.createLabel(composite, SWT.NONE, "User name:", 1);

            userNameField = new Text(composite, SWT.BORDER);
            userNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            userNameField.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    String user = getUsername();
                    if (!isIDValid(user)) {
                        setMessage(
                                "Please enter a valid identifier in the user name field",
                                ERROR);
                        setPageComplete(false);
                    } else {
                        setMessage(null);
                        setPageComplete(true);
                    }
                }
            });
            userNameField.setText(myStore.getString(AmbientGlobals.USER_NAME));
            if (userNameField.getText().length() == 0) {
                UI
                        .createLabel(
                                composite,
                                SWT.NONE,
                                "Hint: You can permanently set your user name in Window->Preferences->Ambient.",
                                2);
            }

            UI.createSeparator(composite);

            UI.createLabel(composite, SWT.NONE, "Project path:", 1);

            modulePathField = new Text(composite, SWT.BORDER);
            modulePathField
                    .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            ((GridData) UI
                    .createLabel(
                            composite,
                            SWT.WRAP,
                            "Enter the path of the directory in which you want this project checked in, e.g. 'cps100/'",
                            2).getLayoutData()).widthHint = 400;
        }

        // /////////////////////////////////////
        // CONTROL METHODS

        protected boolean isIDValid(String user) {
            return myValidator.isValidUserName(user);
        }

        public void performBack() {
            ((WizardPage) getPreviousPage()).setPageComplete(false);
        }

        public String getUsername() {
            return userNameField.getText();
        }

        public String getModulePath() {
            String path = modulePathField.getText();
            if (path.startsWith("/"))
                path = path.substring(1);
            if (!path.endsWith("/"))
                path += "/";
            return path;
        }
    }

    // //////////////////////////////////////
    // CONSTRUCTORS

    public CheckinWizard(IPathMaker pathMaker) {
        myStore = AmbientPlugin.getDefault().getPreferenceStore();
        myPathMaker = pathMaker;
    }

    /**
     * Asks the user to save all unsaved resources and performs that save.
     * 
     * @return <code>true</code> if operation was successful,
     *         <code>false</code> if the user has cancelled
     */
    public static boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {
        saveDirtyEditors();
        IProject project = page1.getSelection();
        if (page1.isCurrentShared()) {
            if (DEBUG) {
                System.out.println("Project " + project.getName()
                        + " is shared");
            }

            // the project being checked out already exists
            Subscriber sub = CVSProviderPlugin.getPlugin()
                    .getCVSWorkspaceSubscriber();
            int syncState = 0;

            SynchronizationChecker checker = new SynchronizationChecker(sub,
                    project);
            if (DEBUG) {
                System.out.println("Getting sync state");
            }
            try {
                new ProgressMonitorDialog(getShell()).run(true, true, checker);
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
                return true;
            } catch (InterruptedException e2) {
                // user cancelled performing synchronization check, just
                // exit
                return true;
            }
            try {
                syncState = checker.getState();
            } catch (TeamException e1) {
                // an error during synchronization occured, display a
                // message and return;
                MessageDialog.openConfirm(getShell(), "Synchronization Error",
                        "An error occured during the synchronization process."
                                + " The checkin wizard will exit."
                                + " Please try again.");
                return true;
            }
            if ((syncState & SyncInfo.INCOMING) != 0) {
                if (DEBUG) {
                    System.out.println("There are incoming changes");
                }
                // there are incoming changes while the user is trying
                // to upload files. Must ask user for what is to be done.
                return doConflictingCommit(project, checker);
            } else if (syncState == 0) {
                if (DEBUG) {
                    System.out.println("There is nothing to sync");
                }
                // project is already up to date, no sync necessary
                MessageDialog
                        .openInformation(
                                getShell(),
                                "Project is already synchronized",
                                "The project '"
                                        + project.getName()
                                        + "' has not changes since the last time it was checked in. No further synchronization is necessary.");
                return true;
            } else {
                if (DEBUG) {
                    System.out.println("There are no incoming changes");
                }
                // there are no incoming changes, normal checkin can
                // proceed.
                return doNormalCommit(project);
            }
        } else {
            return doNewCommit(project);
        }
    }

    /**
     * Returns a path matching the given user id, according to this page's
     * IPathMaker object.
     * 
     * @see IPathMaker
     * @param user
     *            the name of the user used to generate a path
     * @return the path generated for user
     */
    protected String makePath(String user) {
        return myPathMaker.makePath(user);
    }

    private boolean doNewCommit(IProject project) {
        String host = myStore.getString(AmbientGlobals.HOST_NAME);
        String user = page2.getUsername();
        String path = makePath(user);

        Properties properties = new Properties();
        properties.put(AmbientGlobals.CVS_CONN_TYPE_PROPERTY,
                AmbientGlobals.CVS_CONN_EXTSSH_TYPE);
        properties.put(AmbientGlobals.CVS_CONN_USER_PROPERTY, user);
        properties.put(AmbientGlobals.CVS_CONN_HOST_PROPERTY, host);
        properties.put(AmbientGlobals.CVS_CONN_ROOT_PROPERTY, path);

        ICVSRepositoryLocation location = null;
        try {
            location = CVSRepositoryLocation.fromProperties(properties);
        } catch (CVSException e1) {
            e1.printStackTrace();
        }

        boolean isNewLocation = !KnownRepositories.getInstance()
                .isKnownRepository(location.getLocation(false));
        if (isNewLocation) {
            location = KnownRepositories.getInstance().addRepository(location,
                    false);
        }

        try {
            location.validateConnection(new NullProgressMonitor());
        } catch (CVSCommunicationException e) {
            // Couldn't connect to the given host (internet down/wrong host)
            MessageDialog
                    .openError(
                            getShell(),
                            "Connection unsuccessful",
                            "There has been a problem connecting to the CVS host "
                                    + location.getHost()
                                    + ". Reason: "
                                    + e.getLocalizedMessage()
                                    + ". Please check you internet connection and verify that the host is correct.");
            KnownRepositories.getInstance().disposeRepository(location);
            return true;
        } catch (CVSServerException e) {
            // The cvs server returned an error (desired directory not there)
            MessageDialog
                    .openError(
                            getShell(),
                            "Connection unsuccessful",
                            "There has been a problem connecting to the CVS repository "
                                    + location.getLocation(true)
                                    + ". Reason: "
                                    + e.getLocalizedMessage()
                                    + ". Your CVS repository might not be set up correctly.");
            KnownRepositories.getInstance().disposeRepository(location);
            return true;
        } catch (CVSException e1) {
            // Some other cvs problem
            MessageDialog.openError(getShell(), "Connection unsuccessful",
                    "There has been a problem connecting to the CVS host "
                            + location.getHost() + ". Reason: "
                            + e1.getLocalizedMessage()
                            + ". Please verify your connection details.");
            KnownRepositories.getInstance().disposeRepository(location);
            e1.printStackTrace();
            return true;
        } catch (OperationCanceledException e) {
            // the user pressed cancel in the password dialog, do nothing else
            KnownRepositories.getInstance().disposeRepository(location);
            return false;
        }

        location = KnownRepositories.getInstance()
                .addRepository(location, true);

        String modulName = page2.getModulePath() + project.getName()
                + AmbientGlobals.PROJECT_EXT;

        final ICVSRemoteFolder remoteFolder = location.getRemoteFolder(
                modulName, CVSTag.DEFAULT);
        final boolean remoteExists[] = { false };

        try {
            new ProgressMonitorDialog(getShell()).run(true, false,
                    new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            monitor.beginTask("Checking server...",
                                    IProgressMonitor.UNKNOWN);
                            try {
                                remoteExists[0] = remoteFolder
                                        .exists(new NullProgressMonitor());
                                monitor.worked(10);
                            } catch (TeamException e) {
                                throw new InvocationTargetException(e);
                            } finally {
                                monitor.done();
                            }
                        }
                    });
        } catch (InvocationTargetException e1) {
        } catch (InterruptedException e1) {
            return false;
        }

        if (remoteExists[0]) {
            MessageDialog
                    .openError(
                            getShell(),
                            "Project already exists",
                            "The project '"
                                    + project.getName()
                                    + "' you are trying to check in already exists on the server. You must perform a checkout on that project first.");
            return true;
        }

        final ShareProjectOperation op = new ShareProjectOperation(null,
                location, project, modulName);
        try {
            new ProgressMonitorDialog(getShell()).run(true, true,
                    new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            op.run(monitor);

                        }
                    });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return doNormalCommit(project);
    }

    private boolean doConflictingCommit(final IProject targetProject,
            SynchronizationChecker checker) {
        if (DEBUG) {
            System.out.println("Doing conflicting commit");
        }
        String message = "The remote files in the repository project '"
                + targetProject.getName()
                + "' contain changes that are"
                + " inconsistent with the files you are trying to upload to the server."
                + " Use one of the following options:";

        OverwriteSynchronizeDialog dialog = new OverwriteSynchronizeDialog(
                getShell(),
                "Conflicting incoming changes found",
                message,
                "- Overwrite any and all remote changes",
                "- Synchronize manually using the Synchronization perspective (for advanced users)");
        int userChoice = dialog.open();
        switch (userChoice) {
        case OverwriteSynchronizeDialog.CANCEL:
            if (DEBUG) {
                System.out.println("Cancel was chosen");
            }
            // cancel was pressed, so just do nothing and
            // exit the wizard
            return false;
        case OverwriteSynchronizeDialog.OVERWRITE:
            if (DEBUG) {
                System.out.println("Overwrite was chosen");
            }
            // overwrite was pressed, so checkin new versions
            return doOverwriteCommit(checker);
        case OverwriteSynchronizeDialog.SYNCHRONIZE:
            if (DEBUG) {
                System.out.println("Synchronize chosen");
            }
            IResource[] selectedResources = { targetProject };
            try {
                AmbientSyncAction action = new AmbientSyncAction(
                        selectedResources);
                action.execute();
            } catch (InvocationTargetException e) {
            }
        }
        return true;
    }

    private boolean doOverwriteCommit(final SynchronizationChecker checker) {
        if (DEBUG) {
            System.out.println("Doing overwrite commit");
        }
        try {
            new ProgressMonitorDialog(getShell()).run(true, true,
                    new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            new WorkspaceCommitOperation(null,
                                    new IDiffElement[0], true) {
                                public SyncInfoSet getSyncInfoSet() {
                                    SyncInfoSet result = null;
                                    try {
                                        result = new SyncInfoSet(
                                                (SyncInfo[]) checker
                                                        .getConflictingSyncInfos()
                                                        .toArray(
                                                                new SyncInfo[0]));
                                    } catch (TeamException e) {
                                        e.printStackTrace();
                                    }
                                    return result;
                                }
                            }.run(monitor);
                        }
                    });
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return true;
    }

    private boolean doNormalCommit(IProject project) {
        if (DEBUG) {
            System.out.println("Doing normal commit");
        }

        AmbientCommitOperation commit = new AmbientCommitOperation(project);
        try {
            new ProgressMonitorDialog(getShell()).run(true, true, commit);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void addPages() {
        page1 = new FirstPage();
        page1.setTitle("Check in a project");
        page1.setDescription("Please select the project you want to check in.");
        addPage(page1);

        page2 = new SecondPage(new DukeUserNameValidator());
        page2.setDescription("You are checking in a new project."
                + " Please enter your login information.");
        page2.setTitle("Check in a new project");
        addPage(page2);
    }
}
