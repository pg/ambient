/*
 * Created on Jun 3, 2003
 * 
 * This class has been heavily modified from the initial release of the snarfer
 * plug-in.
 */
package edu.duke.cs.ambient.snarfer.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.projects.ProjectHandlerFactory;
import edu.duke.cs.ambient.projects.ProjectLoader;
import edu.duke.cs.ambient.projects.ProjectHandlerFactory.InvalidProjectTypeException;
import edu.duke.cs.ambient.snarfer.PackageInstallRecord;
import edu.duke.cs.ambient.ui.UI;
import edu.duke.cs.ambient.snarfer.ui.dialogs.OverwriteDialog;
import edu.duke.cs.snarfer.SnarferEngine;
import edu.duke.cs.snarfer.FetchListener;
import edu.duke.cs.snarfer.Package;
import edu.duke.cs.snarfer.PackageEntry;
import edu.duke.cs.snarfer.PackageException;

/**
 * This class defines and manages the package install wizard.
 * 
 * @version 2.0
 * @author Marcin Dobosz
 * @author Ethan
 */
public class InstallWizard extends Wizard {

    private Display myDisplay;

    private Package pkg;

    private File destination;

    private Page1 page1;

    private Page2 page2;

    private ProjectLoader myLoader;

    private File projectFile;

    /**
     * This is the first page of the Snarfer import project wizard. Upon
     * completion of this page, the user will have already downloaded all the
     * project file into a directory on the system.
     * 
     * @version 2.0
     * @author Marcin Dobosz
     */
    private class Page1 extends WizardPage {
        /*
         * This class has been heavily modified from the initial version of
         * Ambient. Changes include cleaning up control layout code, moving
         * FetchListener and Runnable implementations into their own seperate
         * internal classes, redisigning control flow logic to prevent
         * unpredictable mistakes.
         */

        private class SnarferListener implements FetchListener {

            private int currArchivedFileBytes, currEntryBytes;

            private int overwrite = OverwriteDialog.NO;

            private OverwriteDialog overwriteDlg;

            private long currEntrySize;

            private File currArchivedFile;

            public void onFileFinished(File f) {
                if (f.getName().equals(
                        IProjectDescription.DESCRIPTION_FILE_NAME)
                        && destination.getPath().equals(f.getParent())) {
                    // once the root ".project" file is found, set projectFile
                    // to something non-null.
                    projectFile = f;
                }
            }

            public void onBeginArchivedFile(PackageEntry archive, File f,
                    long size) {
                currArchivedFile = f;
                currArchivedFileBytes = 0;
            }

            public void onEntryFinished(PackageEntry entry) {
            }

            public boolean onFileExists(File f) {
                if (overwrite == OverwriteDialog.YESTOALL)
                    return true;
                else if (overwrite == OverwriteDialog.NOTOALL)
                    return false;
                overwriteDlg = new OverwriteDialog(getShell(),
                        "Overwrite Confirm", f.getAbsolutePath(), true);
                getShell().getDisplay().syncExec(new Runnable() {
                    public void run() {
                        overwrite = overwriteDlg.open();
                    }
                });
                return (overwrite == OverwriteDialog.YES || overwrite == OverwriteDialog.YESTOALL);
            }

            public void onArchivedFileProgress(int bytes) {
                // System.out.println("Got next "+bytes+" bytes of archive");
                currArchivedFileBytes += bytes;
                UI.asyncSetText(myDisplay, myFileLabel, "Getting "
                        + currArchivedFile.getName() + ": "
                        + (currArchivedFileBytes / 1024) + "K");
            }

            public void onBeginEntry(PackageEntry entry, long size) {
                // currEntry = entry;
                currEntrySize = size;
                currEntryBytes = 0;
                if (!entry.isFile()) {
                    UI.asyncSetText(myDisplay, myStatusLabel,
                            "Beginning archive " + entry.getURL());
                } else {
                    UI.asyncSetText(myDisplay, myStatusLabel, "Fetching file "
                            + entry.getURL());
                }
                UI.asyncSetProgress(myDisplay, myFileProgress, 0);
            }

            public void onEntryProgress(int bytes) {
                currEntryBytes += bytes;
                UI.asyncSetProgress(myDisplay, myFileProgress,
                        (int) (100 * currEntryBytes / (double) currEntrySize));
            }
        }

        private class SnarferRunner implements Runnable {
            public void run() {
                UI.asyncSetText(myDisplay, myStatusLabel, "Fetching files...");
                SnarferEngine engine = new SnarferEngine();
                Iterator it = pkg.getEntries().iterator();
                int i = 0;
                while (it.hasNext()) {
                    PackageEntry entry = (PackageEntry) it.next();
                    UI.asyncSetText(myDisplay, myFileLabel, entry.getURL()
                            .toString());

                    SnarferListener listener = new SnarferListener();
                    try {
                        engine.fetchEntry(entry, destination, listener);
                        i++;
                        UI.asyncSetProgress(myDisplay, myTotalProgress, i);
                    } catch (PackageException e) {
                        UI.asyncSetText(myDisplay, myStatusLabel, e
                                .getMessage());
                        return;
                    } catch (IOException e) {
                        UI.asyncSetText(myDisplay, myStatusLabel, e
                                .getMessage());
                        return;
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                getShell().getDisplay().syncExec(new Runnable() {
                    public void run() {
                        myFileLabel.setVisible(false);
                        myStatusLabel.setText("Finished fetching files.");
                        myFileProgress.setVisible(false);
                        myTotalProgress.setSelection(myTotalProgress
                                .getMaximum());
                        page2.refresh();
                        getWizard().getContainer().showPage(page2);
                    }
                });
            }
        }

        public Page1() {
            super("Install Project");
        }

        private Thread worker;

        private boolean myFetchInProgress = false;

        private Button myDefaultCheckbox = null;

        private Text myDestinationPath = null;

        private Button myBrowseButton = null;

        private Label myFileLabel = null;

        private ProgressBar myFileProgress = null;

        private ProgressBar myTotalProgress = null;

        private Label myStatusLabel = null;

        public void createControl(Composite parent) {
            setTitle("Select destination");
            setDescription("Select the destination folder for the project that is being installed.");

            // Create top level page composite that will contain all controls
            Composite page = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(4, false);
            page.setLayout(gridLayout);

            // Create the "Use Default" Label
            UI.createSimpleLabel(page, "Use Default");

            // Create the "default" checkbox and add modification logic
            myDefaultCheckbox = new Button(page, SWT.CHECK);
            myDefaultCheckbox
                    .setToolTipText("Select this to install the project into your workspace directory");
            myDefaultCheckbox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    // depending on selection status, the destination panel will
                    // be on or off.
                    if (myDefaultCheckbox.getSelection()) {
                        myDestinationPath.setText(ResourcesPlugin
                                .getWorkspace().getRoot().getLocation()
                                .toFile().getAbsolutePath());
                        toggleDestinationPanel(false);
                        validate();
                    } else {
                        toggleDestinationPanel(true);
                    }
                }
            });

            // create "(your current workspace)" label
            UI
                    .createLabel(page, SWT.NONE,
                            "(your current workspace folder)", 2);

            // create "Destination" label
            UI.createSimpleLabel(page, "Destination:");

            // create "Desination path" text field
            myDestinationPath = new Text(page, SWT.BORDER);
            myDestinationPath
                    .setToolTipText("Enter the path into which the project files will be copied");
            myDestinationPath.setLayoutData(UI.createData(
                    GridData.FILL_HORIZONTAL, 2));
            myDestinationPath.setText(ResourcesPlugin.getWorkspace().getRoot()
                    .getLocation().toFile().getAbsolutePath());
            myDestinationPath.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onModify();
                }
            });

            // create the "Browse" button
            myBrowseButton = new Button(page, SWT.NONE);
            myBrowseButton.setToolTipText("Browse directories");
            myBrowseButton.setText("Browse...");
            myBrowseButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    onBrowse();
                }
            });

            // create the (initially empty) file transfer status label, which
            // will display information about which file is currently being
            // installed.
            myFileLabel = new Label(page, SWT.NONE);
            myFileLabel.setText("");
            myFileLabel.setLayoutData(UI.createData(
                    GridData.HORIZONTAL_ALIGN_FILL, 4));
            myFileLabel.setAlignment(SWT.CENTER);

            // create the package entry progress bar that will fill up as more
            // of the entry gets installed.
            myFileProgress = new ProgressBar(page, SWT.NONE);
            GridData fileProgressGridData = new GridData(
                    GridData.HORIZONTAL_ALIGN_FILL);
            fileProgressGridData.horizontalSpan = 4;
            myFileProgress.setLayoutData(fileProgressGridData);
            myFileProgress.setMinimum(0);
            myFileProgress.setMaximum(100);
            myFileProgress.setSelection(0);

            // create total package progress bar that will fill up as
            // consecutive package entries are installed.
            myTotalProgress = new ProgressBar(page, SWT.NONE);
            GridData totalProgressGridData = new GridData(
                    GridData.HORIZONTAL_ALIGN_FILL);
            totalProgressGridData.horizontalSpan = 4;
            myTotalProgress.setLayoutData(totalProgressGridData);
            try {
                myTotalProgress.setMaximum(pkg.getEntryCount());
            } catch (Exception e) {
                myTotalProgress.setMaximum(0);
            }
            myTotalProgress.setMinimum(0);
            myTotalProgress.setSelection(0);

            // create status label that will display information on the status
            // of the fetching job
            myStatusLabel = new Label(page, SWT.NONE);
            myStatusLabel.setText("");
            myStatusLabel.setLayoutData(UI.createData(
                    GridData.HORIZONTAL_ALIGN_FILL, 4));
            myStatusLabel.setAlignment(SWT.CENTER);

            // initialize state
            myDefaultCheckbox.setSelection(true);
            toggleDestinationPanel(false);

            setControl(page);
        }

        private void toggleDestinationPanel(boolean enable) {
            // enable/disable destination portion of the user interface.
            myDestinationPath.setEditable(enable);
            myDestinationPath.setEnabled(enable);
            myBrowseButton.setEnabled(enable);
        }

        /**
         * Launches a directory dialog and updates the destination path text
         * field with the user's choice.
         */
        private void onBrowse() {
            DirectoryDialog dlg = new DirectoryDialog(this.getShell());
            dlg.setFilterPath(myDestinationPath.getText());
            dlg.setMessage("Choose a destination directory");
            dlg.setText("Destination directory");
            String choice = dlg.open();
            if (choice != null) {
                myDestinationPath.setText(choice);
                onModify();
            }
        }

        private void onModify() {
            // force update of next/back/finish buttons
            getWizard().getContainer().updateButtons();
        }

        /**
         * This implemenation starts a new thread that executes the fetching of
         * a file and returns itself. The next page is loaded by the thread upon
         * its completion. User interaction buttons become disabled.
         */
        public IWizardPage getNextPage() {
            try {
                AmbientPlugin.getDefault().getSnarferSettings().saveSettings();
            } catch (IOException e) {
                System.err.println("Error saving plugin settings: " + e);
            }
            worker = new Thread(new SnarferRunner());
            toggleDestinationPanel(false);
            myDefaultCheckbox.setEnabled(false);
            myFetchInProgress = true;
            onModify();
            worker.start();
            return this;
        }

        /**
         * Validates that the provided directory is legitimate. If not, an error
         * message is presented.
         * 
         * @return
         */
        private boolean validate() {
            File rootDirectory = new File(myDestinationPath.getText());

            destination = new File(rootDirectory.getAbsolutePath(), pkg
                    .getName());

            if (rootDirectory.isDirectory()) {
                setMessage(null);
                return true;
            } else {
                setMessage("Please select a valid destination folder.", ERROR);
                return false;
            }
        }

        /**
         * This implemenation enable the Next button only if the user has
         * selected/typed in a valid directory and a fetch operation is not in
         * progress.
         */
        public boolean canFlipToNextPage() {
            return !myFetchInProgress && validate();
        }
    }

    /**
     * This is the second page of the Snarfer import project wizard.
     * 
     * @version 2.0
     * @author Marcin Dobosz
     */
    private class Page2 extends WizardPage {
        /*
         * This class has been heavily modified from the initial version of
         * Ambient. Changes include cleaning up control layout code, simplifying
         * instalation process and options, redisigning control flow logic to
         * prevent unpredictable mistakes.
         */

        private Button myImportCheckBox = null;

        private Composite myOptionsPanel = null;

        private Text myProjetNameText = null;

        private Text myProjectRootCombo = null;

        private Tree myTypeTree = null;

        private String myType = null;

        private Label myProjectTypeLabel;

        public Page2() {
            super("Install project");
            setPageComplete(false);
        }

        public void refresh() {
            myProjectRootCombo.setText(destination.getAbsolutePath());
            // Changed the combo selection to choose what should be the
            // topmost folder in the project folder structure.
            // importCombo.select(directories.size() > 0 ? 1 : 0);
            if (projectFile == null) {

                ((Composite) getControl()).layout(true);
                ((Composite) getControl()).redraw();

                if (myTypeTree == null)
                    setPageComplete(true);
            } else {
                myTypeTree.setVisible(false);
                myProjectTypeLabel.setVisible(false);
                setPageComplete(true);
                setMessage(null);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
         */
        public void createControl(Composite parent) {
            setTitle("Import project into workspace");
            setDescription("Project files have been successfully copied. Choose your import option.");

            Composite page = new Composite(parent, SWT.NONE);
            GridLayout pageLayout = new GridLayout(2, false);
            page.setLayout(pageLayout);

            myImportCheckBox = new Button(page, SWT.CHECK);
            myImportCheckBox.setSelection(true);
            myImportCheckBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    myOptionsPanel.setVisible(myImportCheckBox.getSelection());
                    if (myImportCheckBox.getSelection()) {
                        if (myTypeTree != null && projectFile == null) {
                            myTypeTree.deselectAll();
                            setUnrecognizedTypeError();
                            setPageComplete(false);
                        }
                    } else {
                        setMessage(null);
                        setPageComplete(true);
                    }

                }
            });

            Label myImportProjectLabel = new Label(page, SWT.NONE);
            myImportProjectLabel.setText("Import project into workspace");

            createOptionsPanel(page);

            setControl(page);
        }

        private void createOptionsPanel(Composite page) {
            myOptionsPanel = new Composite(page, SWT.NONE);
            GridLayout optionsLayout = new GridLayout(2, false);
            myOptionsPanel.setLayout(optionsLayout);
            GridData optionsPanelGridData = new GridData(
                    GridData.FILL_HORIZONTAL);
            optionsPanelGridData.horizontalSpan = 2;
            myOptionsPanel.setLayoutData(optionsPanelGridData);

            UI.createSimpleLabel(myOptionsPanel, "Project name");

            myProjetNameText = new Text(myOptionsPanel, SWT.BORDER
                    | SWT.READ_ONLY);
            myProjetNameText.setLayoutData(new GridData(
                    GridData.FILL_HORIZONTAL));
            myProjetNameText.setText(pkg.getName());

            UI.createSimpleLabel(myOptionsPanel, "Project root directory");

            myProjectRootCombo = new Text(myOptionsPanel, SWT.BORDER
                    | SWT.READ_ONLY);
            myProjectRootCombo.setLayoutData(new GridData(
                    GridData.FILL_HORIZONTAL));

            createTypeSelection();
        }

        private void createTypeSelection() {
            if (!isValidType() && projectFile == null) {
                myProjectTypeLabel = UI.createSimpleLabel(myOptionsPanel,
                        "Project type");
                myProjectTypeLabel.setLayoutData(new GridData(
                        GridData.HORIZONTAL_ALIGN_BEGINNING
                                | GridData.VERTICAL_ALIGN_BEGINNING));
                createTree();

                setUnrecognizedTypeError();
                setPageComplete(false);
            }
        }

        private boolean isValidType() {
            return ProjectHandlerFactory.getInstance().isValidType(
                    pkg.getType());
        }

        private void createTree() {
            myTypeTree = new Tree(myOptionsPanel, SWT.BORDER | SWT.SINGLE);

            GridData naturesLayout = new GridData(GridData.FILL_HORIZONTAL);
            myTypeTree.setLayoutData(naturesLayout);

            String[] types2 = ProjectHandlerFactory.getInstance()
                    .getValidTypes();
            for (int k = 0; k < types2.length; k++) {
                TreeItem item = new TreeItem(myTypeTree, SWT.LEFT);
                item.setText(ProjectHandlerFactory.getInstance()
                        .getValidNames()[k]);
                item.setData(types2[k]);
            }

            myTypeTree.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    TreeItem[] selected = myTypeTree.getSelection();
                    if (selected != null) {
                        setType((String) selected[0].getData());
                        setPageComplete(true);
                        setMessage(null);
                    }
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                    myTypeTree.deselectAll();
                    setPageComplete(false);
                    setUnrecognizedTypeError();
                }
            });

        }

        private void setType(String type) {
            myType = type;
        }

        public String getType() {
            String type = "";
            if (pkg.getType() != null) {
                type = pkg.getType();
                if (ProjectHandlerFactory.getInstance().isValidType(type))
                    return type;
            }
            return myType;
        }

        private void setUnrecognizedTypeError() {
            setMessage(
                    "This project is not of a recognized type. Please select one of the available types below.",
                    ERROR);
        }

        public boolean isImportChecked() {
            return myImportCheckBox.getSelection();
        }

        public String getImportDirectory() {
            return myProjectRootCombo.getText();
        }

        public String getImportName() {
            return myProjetNameText.getText();
        }

        public IWizardPage getPreviousPage() {
            return null;
        }

        public boolean canFlipToNextPage() {
            return false;
        }

    }

    public InstallWizard(Package pkg) throws NullPointerException {
        this.destination = ResourcesPlugin.getWorkspace().getRoot()
                .getLocation().toFile();
        this.pkg = pkg;

        myDisplay = Display.getCurrent();

        myLoader = new ProjectLoader(getShell());
        projectFile = null; // null signifies that no ".project" file was found

        setWindowTitle("Install Package " + pkg.getName() + " version "
                + pkg.getVersion());
    }

    public boolean performFinish() {
        // save ambient settigns
        try {
            PackageInstallRecord record = new PackageInstallRecord(pkg,
                    destination.getAbsolutePath(), new Date());
            AmbientPlugin.getDefault().getSnarferSettings().getHistory().add(
                    record);
            AmbientPlugin.getDefault().getSnarferSettings().saveSettings();
        } catch (IOException e) {
            System.err.println("Error saving fetcher settings: " + e);
        }

        // import project into workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (page2.isImportChecked()) {
            if (projectFile != null) {
                // When a ".project" file exists, it must be modified to fit
                // into the instalation directory and package name.
                // Then the project gets loaded
                IProjectDescription desc = null;
                try {
                    desc = workspace.loadProjectDescription(new Path(
                            projectFile.getAbsolutePath()));
                } catch (CoreException ce) {

                }
                desc.setName(desc.getLocation().toFile().getName());
                desc.setLocation(new Path(projectFile.getParent()));

                IProject project = workspace.getRoot().getProject(
                        desc.getName());
                myLoader.loadProject(desc, project, null);
            } else {
                // no ".project" file found so need to get ProjectHandler and
                // create it.
                File projDir = new File(page2.getImportDirectory());
                File projFile = new File(projDir,
                        IProjectDescription.DESCRIPTION_FILE_NAME);

                try {
                    ProjectHandlerFactory.getInstance().getHandler(
                            page2.getType()).createProject(projDir,
                            page2.getImportName());
                } catch (InvalidProjectTypeException e2) {
                    // should not happen as type is already validated.
                }

                if (projFile.exists()) {
                    IProjectDescription desc = null;
                    try {
                        desc = workspace.loadProjectDescription(new Path(
                                projFile.getAbsolutePath()));
                    } catch (CoreException e1) {
                        e1.printStackTrace();
                    }
                    desc.setLocation(new Path(projFile.getParent()));
                    IProject project = workspace.getRoot().getProject(
                            desc.getName());
                    myLoader.loadProject(desc, project, null);
                }
            }
        }
        return true;
    }

    public void addPages() {
        super.addPages();
        page1 = new Page1();
        page2 = new Page2();
        addPage(page1);
        addPage(page2);
    }

    public boolean performCancel() {
        if (page1 != null && page1.worker != null && page1.worker.isAlive()) {
            page1.worker.interrupt();
            try {
                page1.worker.join();
            } catch (InterruptedException e) {
            }
        }
        return super.performCancel();
    }

    public boolean canFinish() {
        return page2.isPageComplete();
    }
}
