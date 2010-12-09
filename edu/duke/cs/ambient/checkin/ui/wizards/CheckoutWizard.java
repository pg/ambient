/*
 * Created on Jul 8, 2005
 */
package edu.duke.cs.ambient.checkin.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
import org.eclipse.team.internal.ccvs.ui.model.AllRootsElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSModelElement;
import org.eclipse.team.internal.ccvs.ui.model.CVSTagElement;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryRoot;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

import edu.duke.cs.ambient.AmbientGlobals;
import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.checkin.DukePathMaker;
import edu.duke.cs.ambient.checkin.DukeUserNameValidator;
import edu.duke.cs.ambient.checkin.IPathMaker;
import edu.duke.cs.ambient.checkin.IUserNameValidator;
import edu.duke.cs.ambient.checkin.jobs.AmbientCheckoutOperation;
import edu.duke.cs.ambient.checkin.jobs.AmbientSyncAction;
import edu.duke.cs.ambient.checkin.jobs.SynchronizationChecker;
import edu.duke.cs.ambient.checkin.ui.dialogs.OverwriteSynchronizeDialog;
import edu.duke.cs.ambient.ui.UI;
import edu.duke.cs.ambient.ui.wizards.AmbientWizardPage;

/**
 * @since 2.0
 * @author Marcin Dobosz
 */
public class CheckoutWizard extends Wizard {

    // //////////////////////////
    // WIZARD DATA

    private FirstPage page1;

    private SecondPage page2;

    private IPreferenceStore myStore;

    // /////////////////////////////
    // WIZARD PAGES

    private class FirstPage extends AmbientWizardPage {

        // ////////////////////////////////
        // USER INTERFACE WIDGETS

        private Text userNameField = null;

        private Button altOwnerCheckBox = null;

        private Text ownerNameField = null;

        private Button altPathCheckBox = null;

        private Text pathField = null;

        // /////////////////////////////////
        // CVS CONNECTION INFORMATION

        // provides access to all know repositories
        private AllRootsElement root;

        private IUserNameValidator myValidator;

        private IPathMaker myPathMaker;

        // //////////////////////////////////
        // CONSTRUCTOR

        protected FirstPage(IUserNameValidator validator, IPathMaker pathMaker) {
            super("First Page");
            setPageComplete(false);

            myValidator = validator;
            myPathMaker = pathMaker;
            root = new AllRootsElement();
        }

        // /////////////////////////////
        // CONSTRUCTION METHODS

        /**
         * Creates the user section of the page in the given composite.
         * 
         * @param composite
         *            a composite
         */
        protected void createUserSection(Composite composite) {
            UI.createLabel(composite, SWT.NONE, "User name:", 2);

            userNameField = new Text(composite, SWT.BORDER);
            userNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            userNameField.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    String user = userNameField.getText();
                    if (!altOwnerCheckBox.getSelection()) {
                        ownerNameField.setText(user);
                    }
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
        }

        /**
         * Creates the owner section of the page in the given composite.
         * 
         * @param composite
         *            a composite
         */
        protected void createOwnerSection(Composite composite) {
            altOwnerCheckBox = new Button(composite, SWT.CHECK);
            altOwnerCheckBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (altOwnerCheckBox.getSelection()) {
                        ownerNameField.setEditable(true);
                        altPathCheckBox.setEnabled(true);
                    } else {
                        ownerNameField.setEditable(false);
                        altPathCheckBox.setEnabled(false);
                        ownerNameField.setText(userNameField.getText());
                        pathField.setEditable(false);
                        altPathCheckBox.setSelection(false);
                        pathField.setText(makePath(ownerNameField.getText()));
                    }
                }
            });

            UI.createLabel(composite, SWT.NONE, "Use different owner name", 2);

            UI.createLabel(composite, SWT.NONE, "CVS Owner name:", 2);

            ownerNameField = new Text(composite, SWT.BORDER);
            ownerNameField.setEditable(false);
            ownerNameField.setLayoutData(new GridData(
                    GridData.HORIZONTAL_ALIGN_FILL));
            ownerNameField.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    String user = ownerNameField.getText();
                    if (!altPathCheckBox.getSelection()) {
                        pathField.setText(makePath(user));
                    }
                    if (!isIDValid(user)) {
                        setMessage(
                                "Please enter a valid identifier in the CVS owner name field",
                                ERROR);
                        setPageComplete(false);
                    } else {
                        setMessage(null);
                        setPageComplete(true);
                    }
                }
            });
        }

        /**
         * Creates the path section of the page in the given composite.
         * 
         * @param composite
         *            a composite
         */
        protected void createPathSection(Composite composite) {
            altPathCheckBox = new Button(composite, SWT.CHECK);
            altPathCheckBox.setEnabled(false);
            altPathCheckBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (altPathCheckBox.getSelection()) {
                        pathField.setEditable(true);
                    } else {
                        pathField.setEditable(false);
                        pathField.setText(makePath(ownerNameField.getText()));
                    }
                }
            });

            UI.createLabel(composite, SWT.NONE,
                    "Use alternate repository path", 2);

            UI.createLabel(composite, SWT.NONE, "Repository path:", 2);

            pathField = new Text(composite, SWT.BORDER);
            pathField.setEditable(false);
            pathField
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        }

        /**
         * This implementation creates a control consisting of the user, owner,
         * and path selection sections in the given parent.
         * 
         * @param parent
         *            a composite
         */
        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(3, false));

            createUserSection(composite);

            UI.createSeparator(composite);

            createOwnerSection(composite);

            UI.createSeparator(composite);

            createPathSection(composite);

            if (myStore.contains(AmbientGlobals.USER_NAME)) {
                userNameField.setText(myStore
                        .getString(AmbientGlobals.USER_NAME));
            }

            if (userNameField.getText().length() == 0) {
                UI
                        .createLabel(
                                composite,
                                SWT.NONE,
                                "Hint: You can permanently set your user name in Window->Preferences->Ambient.",
                                3);
            }

            setControl(composite);
        }

        // ///////////////////////////////
        // PROCESSING METHODS

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

        protected boolean isIDValid(String user) {
            return myValidator.isValidUserName(user);
        }

        protected IWorkbenchAdapter getAdapter(Object o) {
            if (!(o instanceof IAdaptable)) {
                return null;
            }
            return (IWorkbenchAdapter) ((IAdaptable) o)
                    .getAdapter(IWorkbenchAdapter.class);
        }

        protected Object getTreeInput() {
            IWorkbenchAdapter adapter = getAdapter(root);

            if (adapter instanceof CVSModelElement) {
                Object[] children = ((CVSModelElement) adapter)
                        .getChildren(root);
                for (int i = 0; i < children.length; i++) {
                    if (children[i] instanceof RepositoryRoot) {
                        // we need to find the repository that stores the
                        // projects for this class
                        if ((((RepositoryRoot) children[i]).getRoot().getHost()
                                .equalsIgnoreCase(myStore
                                        .getString(AmbientGlobals.HOST_NAME)))
                                && (((RepositoryRoot) children[i]).getRoot()
                                        .getRootDirectory().equals(pathField
                                        .getText()))) {
                            // we need to find the head of the repository

                            IWorkbenchAdapter adapterRep = getAdapter(children[i]);
                            Object[] grandChildren = ((CVSModelElement) adapterRep)
                                    .getChildren(children[i]);
                            for (int j = 0; j < grandChildren.length; j++) {
                                if (grandChildren[j] instanceof CVSTagElement)
                                    return grandChildren[j];
                            }
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Attempts to connect to the CVS server specified by the user's input
         * and information from Ambient preferences. The returned value
         * determines whether the connection was successful.
         * 
         * @return <code>true</code> if the connection was successful,
         *         <code>false</code> otherwise
         */
        private boolean tryConnection(String userName, String host, String path) {
            // check if cvs is already set up with the right user/host/path:
            // search through all the known repositories until we get a match
            ICVSRepositoryLocation[] knownReps = KnownRepositories
                    .getInstance().getRepositories();
            for (int i = 0; i < knownReps.length; i++) {
                ICVSRepositoryLocation location = knownReps[i];
                if (location.getHost().equalsIgnoreCase(host)
                        && (new Path(location.getRootDirectory()))
                                .equals(new Path(path))) {
                    if (location.getUsername().equals(userName)) {
                        // a matching repository has been found,
                        return true;
                    } else {
                        // flush the password of other users who were connected
                        // to the same repository location.
                        location.flushUserInfo();
                        // TODO should disposing really be done?
                        // KnownRepositories.getInstance().disposeRepository(
                        // location);
                    }
                }
            }

            // a matching repository had not been found, so create a new cvs
            // connection with following properties
            Properties properties = new Properties();
            properties.put(AmbientGlobals.CVS_CONN_TYPE_PROPERTY,
                    AmbientGlobals.CVS_CONN_EXTSSH_TYPE);
            properties.put(AmbientGlobals.CVS_CONN_USER_PROPERTY, userName);
            properties.put(AmbientGlobals.CVS_CONN_HOST_PROPERTY, host);
            properties.put(AmbientGlobals.CVS_CONN_ROOT_PROPERTY, path);

            // trying to create a new CVS location right here
            final ICVSRepositoryLocation[] roots = new ICVSRepositoryLocation[1];
            boolean everythingOK = true;
            try {
                roots[0] = CVSRepositoryLocation.fromProperties(properties);
                roots[0] = KnownRepositories.getInstance().addRepository(
                        roots[0], false);
                try {
                    new ProgressMonitorDialog(getShell()).run(true, true,
                            new IRunnableWithProgress() {
                                public void run(IProgressMonitor monitor)
                                        throws InvocationTargetException,
                                        InterruptedException {
                                    try {
                                        // validate connection, ask for password
                                        roots[0].validateConnection(monitor);
                                        // at this point the connection is
                                        // validated
                                    } catch (TeamException e) {
                                        // a problem occured, connection could
                                        // not be validated
                                        throw new InvocationTargetException(e);
                                    }
                                }
                            });
                } catch (InvocationTargetException e) {
                    // error occured while connecting, connection not validated
                    everythingOK = false;
                } catch (InterruptedException e) {
                    // user cancelled operation, connection not validated
                    everythingOK = false;
                }
            } catch (CVSException e) {
                // some error occurred creating cvs location, not valid
                everythingOK = false;
            }

            if (everythingOK) {
                // fully add new repository
                KnownRepositories.getInstance().addRepository(roots[0], true);
            } else {
                // something didn't work out, dispose of the new repository and
                // return false.
                KnownRepositories.getInstance().disposeRepository(roots[0]);
                return false;
            }
            return true;
        }

        /**
         * This implementation tries to validate the connection defined by the
         * user. If the connection is not validated, the next page in the wizard
         * is prepped to display an error dialog by calling
         * {@link SecondPage#setIncompletable(String)}.
         */
        public void performNext() {
            String host = myStore.getString(AmbientGlobals.HOST_NAME);
            String userName = userNameField.getText();
            String path = pathField.getText();
            if (tryConnection(userName, host, path)) {
                // valid connection, proceed normally
                page2.setInput(getTreeInput());
            } else {
                // connection can't be validated, display error message
                page2.setIncompletable("An error occured while connecting to "
                        + userName + "@" + host + path);
            }
        }
    }

    private class SecondPage extends AmbientWizardPage {

        // Display widgets
        private TreeViewer myTreeViewer;

        private Label statusLabel;

        // State
        private boolean isCompletable;

        public SecondPage() {
            super("Second Page");

            setPageComplete(false);
            isCompletable = true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
         */
        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));

            statusLabel = new Label(composite, SWT.NONE);
            statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            statusLabel.setText("Available projects:");

            myTreeViewer = new TreeViewer(composite, SWT.BORDER);
            myTreeViewer.getTree().setLayoutData(
                    new GridData(GridData.FILL_BOTH));
            myTreeViewer.setLabelProvider(new CvsViewerLabelProvider());
            myTreeViewer.setContentProvider(new CvsViewerContentProvider());
            myTreeViewer
                    .addSelectionChangedListener(new ISelectionChangedListener() {

                        public void selectionChanged(SelectionChangedEvent event) {
                            String name;
                            try {
                                name = getSelection().getName();
                            } catch (Exception e) {
                                // do nothing
                                return;
                            }

                            if (name.endsWith(AmbientGlobals.PROJECT_EXT)) {
                                setPageComplete(true);
                                setMessage(null);
                            } else {
                                setPageComplete(false);
                                setMessage("Please select a valid project",
                                        ERROR);
                            }
                        }
                    });

            setControl(composite);
        }

        public ICVSRemoteResource getSelection() {
            return (ICVSRemoteResource) ((IStructuredSelection) myTreeViewer
                    .getSelection()).getFirstElement();
        }

        /**
         * Sets the status of this page to incomplete when the user goes back to
         * the previous page.
         */
        public void performBack() {
            // User pressed back so and will attempt to reconnect to another
            // cvs, so any error messages set previously by setIncompletable are
            // NO LONGER VALID.
            // Reset relevant values to defaults
            setPageComplete(false);
            setMessage(null);
            isCompletable = true;
            myTreeViewer.getTree().setVisible(true);
            statusLabel.setText("Available projects:");
        }

        /**
         * Sets the input object to be used by the tree display on this page.
         * 
         * @param input
         */
        public void setInput(Object input) {
            myTreeViewer.setInput(input);
        }

        public void setIncompletable(String errorMessage) {
            // set pageComplete so that user can finish wizard
            setPageComplete(true);
            // set isCompletable to indicate abnormal situation and prevent any
            // checking out
            isCompletable = false;
            // set an error message
            setMessage(errorMessage, ERROR);
            setInput(null);
            // make tree invisible as it cannot have a proper input
            myTreeViewer.getTree().setVisible(false);
            // leave instructions for the user
            statusLabel
                    .setText("You can go back and retry to connect with the same or new settings");
        }

        /**
         * Returns whether this page should be treated as normally completable.
         * More specifically, this method returns <code>true</code> when the
         * wizard can finish and successfully import a project into the
         * workbench and no abnormal conditions have occured
         * 
         * @return
         */
        public boolean isCompletable() {
            return isCompletable;
        }

    }

    // /////////////////////////////
    // CONSTRUCTOR

    public CheckoutWizard() {
        setWindowTitle("Project Checkout");
        myStore = AmbientPlugin.getDefault().getPreferenceStore();
    }

    // /////////////////////////////////
    // CONSTRUCTION METHODS

    /**
     * Adds the appropriate pages to this wizard.
     */
    public void addPages() {
        page1 = new FirstPage(new DukeUserNameValidator(), new DukePathMaker());
        page1.setTitle("Check out a project");
        page1
                .setDescription("Please enter information about your checkout connection.");
        addPage(page1);
        page2 = new SecondPage();
        page2.setTitle("Check out a project");
        page2
                .setDescription("Please select the project you would like to work on.");
        addPage(page2);
    }

    // ////////////////////////////
    // METHODS

    /**
     * Returns the IProject handle for a workspace project associated with
     * remoteFolder. The project is identified by the name of the remoteFolder,
     * unless the name ends with {@link AmbientGlobals#PROJECT_EXT}, in which
     * case that extensions gets truncated. The returned object does not signify
     * that the workspace project actually exists.
     * 
     * @param remoteFolder
     *            a remote folder for which a project is to be found
     * @return the workspace project associated with remoteFolder
     */
    private IProject getTargetProject(ICVSRemoteFolder remoteFolder) {
        String name = remoteFolder.getName();
        if (name.endsWith(AmbientGlobals.PROJECT_EXT))
            name = name.split(AmbientGlobals.PROJECT_EXT)[0];
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);

    }

    /**
     * Schedules a {@link CheckoutProjectJob}to check out files from the
     * remoteFolder into the given targetProject.
     * 
     * @param remoteFolder
     *            the cvs location of files to be checked out
     * @param targetProject
     *            handle to the target project into which checkout is to occur
     */
    private boolean doNormalCheckout(final ICVSRemoteFolder remoteFolder,
            final IProject targetProject) {
        try {
            new ProgressMonitorDialog(getShell()).run(true, true,
                    new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            monitor.beginTask("Fetching project ...",
                                    IProgressMonitor.UNKNOWN);
                            try {
                                new AmbientCheckoutOperation(targetProject,
                                        remoteFolder).execute(monitor);
                            } catch (CVSException e) {
                                throw new InvocationTargetException(e);
                            } finally {
                                monitor.done();
                            }
                        }
                    });
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
        return true;
    }

    private boolean doUnsharedCheckout(final ICVSRemoteFolder remoteFolder,
            final IProject targetProject) {
        if (!MessageDialog
                .openConfirm(
                        getShell(),
                        "",
                        "A project named '"
                                + targetProject.getName()
                                + "' already exists in your workspace. Checking it out will completely remove the local project. Do you want to proceed and lose all local changes?")) {
            return false;
        }

        try {
            new ProgressMonitorDialog(getShell()).run(true, true,
                    new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException,
                                InterruptedException {
                            monitor.beginTask("Deleting project '"
                                    + targetProject.getName() + "'",
                                    IProgressMonitor.UNKNOWN);
                            try {
                                targetProject.delete(true, true, monitor);
                            } catch (CoreException e) {
                                e.printStackTrace();
                                throw new InvocationTargetException(e);
                            } finally {
                                monitor.done();
                            }
                        }
                    });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return doNormalCheckout(remoteFolder, targetProject);
    }

    private boolean doSharedCheckout(final ICVSRemoteFolder remoteFolder,
            final IProject targetProject) {
        // the project being checked out already exists and is shared
        // with a repository
        Subscriber sub = CVSProviderPlugin.getPlugin()
                .getCVSWorkspaceSubscriber();
        int syncState = 0;

        SynchronizationChecker checker = new SynchronizationChecker(sub,
                targetProject);
        try {
            new ProgressMonitorDialog(getShell()).run(true, true, checker);
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
            return true;
        } catch (InterruptedException e2) {
            e2.printStackTrace();
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
                            + " The checkout wizard will exit."
                            + " Please try again.");
            return true;
        }
        if ((syncState & SyncInfo.OUTGOING) != 0) {
            // there are outgoing changes while the user is trying
            // to download files. Must ask user for what is to be done.
            return doConflictingCheckout(remoteFolder, targetProject);
        } else if (syncState == 0) {
            // project is already up to date, no sync necessary
            return true;
        } else {
            // there are no outgoing changes, normal checkout can
            // proceed.
            return doNormalCheckout(remoteFolder, targetProject);
        }
    }

    /**
     * Displays a dialog asking the user whether they want to overwrite any
     * local changes, synchronize manually using the synchronization
     * perspective, or simply cancel. Performs the appropriate task and after
     * the user has chosen.
     * 
     * @param remoteFolder
     *            the cvs location of files to be checked out
     * @param targetProject
     *            handle to the target project into which checkout is to occur
     */
    private boolean doConflictingCheckout(final ICVSRemoteFolder remoteFolder,
            final IProject targetProject) {

        String message = "The local files in project '"
                + targetProject.getName()
                + "' contain changes that are"
                + " inconsistent with the files you are trying to download from the server.  Use one of the following options:";

        OverwriteSynchronizeDialog dialog = new OverwriteSynchronizeDialog(
                getShell(),
                "Conflicting outgoing changes found",
                message,
                "- Overwrite any and all local changes",
                "- Synchronize manually using the Synchronization perspective (for advanced users)");
        int userChoice = dialog.open();
        switch (userChoice) {
        case OverwriteSynchronizeDialog.CANCEL:
            // cancel was pressed, so just do nothing and
            // exit the wizard
            break;
        case OverwriteSynchronizeDialog.OVERWRITE:
            // overwrite was pressed, so checkout project
            // from scratch
            return doNormalCheckout(remoteFolder, targetProject);
        case OverwriteSynchronizeDialog.SYNCHRONIZE:
            final IResource[] selectedResources = { targetProject };
            try {
                AmbientSyncAction action = new AmbientSyncAction(
                        selectedResources);
                action.execute();
            } catch (InvocationTargetException e) {
            }
        }
        return true;
    }

    /**
     * Asks the user to save all unsaved resources and performs that save.
     * 
     * @return <code>true</code> if operation was successful,
     *         <code>false</code> if the user has cancelled
     */
    private boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(true);
    }

    public boolean performFinish() {
        if (page2.isCompletable()) {
            saveDirtyEditors();

            // wizard can be completed (unless user does not have rights for
            // selected project).
            final ICVSRemoteFolder remoteFolder = (ICVSRemoteFolder) page2
                    .getSelection();
            final IProject targetProject = getTargetProject(remoteFolder);

            if (targetProject.exists()) {
                // the project being checked out already exists

                if (!targetProject.isOpen()) {
                    // open a closed project to make sure project can be
                    // analyzed properly
                    try {
                        new ProgressMonitorDialog(getShell()).run(true, true,
                                new IRunnableWithProgress() {
                                    public void run(IProgressMonitor monitor)
                                            throws InvocationTargetException,
                                            InterruptedException {
                                        monitor.beginTask(
                                                "Opening project '"
                                                        + targetProject
                                                                .getName()
                                                        + "'",
                                                IProgressMonitor.UNKNOWN);
                                        try {
                                            targetProject.open(monitor);
                                        } catch (CoreException e) {
                                            e.printStackTrace();
                                        } finally {
                                            monitor.done();
                                        }
                                    }
                                });
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        return true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return true;
                    }
                }

                if (RepositoryProvider.isShared(targetProject)) {
                    // Project is shared with a repository, a shared checkout is
                    // required.
                    return doSharedCheckout(remoteFolder, targetProject);
                } else {
                    // Project is not shared with a repository, an unshared
                    // checkout is needed.
                    return doUnsharedCheckout(remoteFolder, targetProject);
                }
            } else {
                // this is a fresh checkout and the target project doesn't exits
                // a clean checkout can be done

                return doNormalCheckout(remoteFolder, targetProject);
            }
        } else {
            // nothing can be done to finish, just return
            return true;
        }
    }
}
