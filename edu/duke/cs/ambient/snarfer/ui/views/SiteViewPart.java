package edu.duke.cs.ambient.snarfer.ui.views;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.snarfer.UpdateChecker;
import edu.duke.cs.ambient.snarfer.ui.dialogs.ChangeSiteDialog;
import edu.duke.cs.ambient.snarfer.ui.dialogs.UpdateDialog;
import edu.duke.cs.ambient.snarfer.ui.wizards.InstallWizard;
import edu.duke.cs.snarfer.Package;

/**
 * This class defines the Snarf Browser view.
 */
public class SiteViewPart extends ViewPart implements
        ISelectionChangedListener, SelectionListener {
    /**
     * This is the identifier string for this view with the value
     * "edu.duke.cs.ambient.snarfer.SiteView".
     */
    public final static String ID = "edu.duke.cs.ambient.snarfer.SiteView";

    private SiteContentProvider contentProvider;

    private SiteLabelProvider labelProvider;

    private Composite panel;

    private TreeViewer viewer;

    private Label statusLabel;

    private NewSiteAction newSiteAction;

    private RemoveSiteAction removeSiteAction;

    private OpenAction openAction;

    private InstallAction installAction;

    private RefreshAction refreshAction;

    private EditSiteAction editSiteAction;

    private UpdateSearchAction updateSearchAction;

    /**
     * Creates a new instance of this view class.
     */
    public SiteViewPart() {
        super();

        contentProvider = new SiteContentProvider();
        labelProvider = new SiteLabelProvider();
        newSiteAction = new NewSiteAction("New site...");
        newSiteAction.setToolTipText("Add a new project site");
        newSiteAction.setImageDescriptor(AmbientPlugin
                .getImageDescriptor("icons/install.gif"));
        editSiteAction = new EditSiteAction();
        editSiteAction.setImageDescriptor(PlatformUI.getWorkbench()
                .getSharedImages().getImageDescriptor(
                        ISharedImages.IMG_OBJS_INFO_TSK));
        editSiteAction.setText("Edit site...");
        editSiteAction.setToolTipText("Edit the project site");
        removeSiteAction = new RemoveSiteAction();
        removeSiteAction.setImageDescriptor(PlatformUI.getWorkbench()
                .getSharedImages().getImageDescriptor(
                        ISharedImages.IMG_TOOL_DELETE));
        openAction = new OpenAction();
        openAction.setImageDescriptor(PlatformUI.getWorkbench()
                .getSharedImages().getImageDescriptor(
                        ISharedImages.IMG_OBJ_FOLDER));
        installAction = new InstallAction();
        installAction.setImageDescriptor(PlatformUI.getWorkbench()
                .getSharedImages().getImageDescriptor(
                        IDE.SharedImages.IMG_OBJ_PROJECT));
        refreshAction = new RefreshAction();
        refreshAction.setText("Refresh");
        refreshAction.setToolTipText("Refresh project tree");
        refreshAction.setImageDescriptor(AmbientPlugin
                .getImageDescriptor("icons/refresh.gif"));
        // TODO the update function has been disabled temporarily, change how it
        // works later on
        // updateSearchAction = new UpdateSearchAction();
        // updateSearchAction
        // .setToolTipText("Search for updates to installed projects");
        // updateSearchAction.setImageDescriptor(AmbientPlugin
        // .getImageDescriptor("icons/search.gif"));
        removeSiteAction.setEnabled(false);
        openAction.setEnabled(false);
        editSiteAction.setEnabled(false);
        installAction.setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(1, false));
        statusLabel = new Label(panel, SWT.HORIZONTAL);
        statusLabel.setText("Project Sites");
        viewer = new TreeViewer(panel);
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(labelProvider);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.addSelectionChangedListener(this);
        viewer.getTree().addSelectionListener(this);
        createContextMenu();
        createToolbar();
        onRefresh();
    }

    public void dispose() {
        super.dispose();
        statusLabel.dispose();
        panel.dispose();
    }

    /**
     * @see ViewPart#setFocus
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private void onRefresh() {
        Iterator it = AmbientPlugin.getDefault().getSnarferSettings()
                .getSiteURLs().iterator();
        while (it.hasNext()) {
            AmbientPlugin.getDefault().getSnarferSettings().uncacheSite(
                    (URL) it.next());
        }
        viewer.setInput(AmbientPlugin.getDefault().getSnarferSettings()
                .getSiteURLs());
        viewer.expandAll();
    }

    /**
     * This action launches the ChangeSiteDialog without specifying a site to be
     * edited. The newly entered site is added to the view and the plug-in's
     * permanent store.
     * 
     * @see ChangeSiteDialog
     */
    private class NewSiteAction extends Action {
        public NewSiteAction(String name) {
            super(name);
        }

        public void run() {
            ChangeSiteDialog dialog = new ChangeSiteDialog(
                    getSite().getShell(), null, "Enter new Snarf site ...");
            if (dialog.open() == Dialog.CANCEL) {
                // attempt to open the dialog window and return if action got
                // cancelled by user
                return;
            }
            AmbientPlugin.getDefault().getSnarferSettings().getSiteURLs().add(
                    dialog.getURL());
            try {
                AmbientPlugin.getDefault().getSnarferSettings().saveSettings();
            } catch (IOException e) {
                System.err.println("Error saving plugin settings.");
            }
            AmbientPlugin.getDefault().getSnarferSettings().uncacheSite(
                    dialog.getURL());
            viewer.setInput(AmbientPlugin.getDefault().getSnarferSettings()
                    .getSiteURLs());
            viewer.refresh();
        }
    }

    /**
     * This action launches the ChangeSiteDialog on the selected site. The URL
     * for the selected site is modified to be the value entered into the
     * dialog.
     * 
     * @see ChangeSiteDialog
     */
    private class EditSiteAction extends Action implements IInputValidator {
        private URL url;

        public EditSiteAction() {
            super("Edit site");
        }

        public void setURL(URL url) {
            this.url = url;
            setText("Edit site '" + url + "'");
            setToolTipText(getText());
        }

        public void run() {
            ChangeSiteDialog dlg = new ChangeSiteDialog(getSite().getShell(),
                    url, "Edit site ...");
            if (dlg.open() == Dialog.CANCEL)
                return;
            try {
                AmbientPlugin.getDefault().getSnarferSettings().getSiteURLs()
                        .remove(url);
                url = new URL(dlg.getValue());
                AmbientPlugin.getDefault().getSnarferSettings().getSiteURLs()
                        .add(url);
                AmbientPlugin.getDefault().getSnarferSettings().saveSettings();
            } catch (MalformedURLException e) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            viewer.setInput(AmbientPlugin.getDefault().getSnarferSettings()
                    .getSiteURLs());
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
         */
        public String isValid(String newText) {
            try {
                new URL(newText);
                return null;
            } catch (java.net.MalformedURLException e) {
                return "Not a valid URL";
            }
        }

    }

    /**
     * This action removes a package site from the display list as well as the
     * permanent storage.
     */
    private class RemoveSiteAction extends Action {
        private URL url;

        public RemoveSiteAction() {
            super("Remove site");
        }

        public void setURL(URL url) {
            this.url = url;
            setText("Remove site '" + url + "'");
            setToolTipText(getText());
        }

        public void run() {
            if (!MessageDialog.openQuestion(getSite().getShell(),
                    "Site removal confirmation", "Really remove site '" + url
                            + "'?"))
                return;
            AmbientPlugin.getDefault().getSnarferSettings().getSiteURLs()
                    .remove(url);
            try {
                AmbientPlugin.getDefault().getSnarferSettings().saveSettings();
            } catch (IOException e) {
                e.printStackTrace();
            }
            viewer.setInput(AmbientPlugin.getDefault().getSnarferSettings()
                    .getSiteURLs());
        }
    }

    /**
     * This action opens the InstallWizard on the selected package.
     * 
     * @see InstallWizard
     */
    private class InstallAction extends Action {
        Package pkg;

        public InstallAction() {
            super("Open");
        }

        public void setPackageInfo(Package pkg) {
            setText("Install " + pkg.getName() + " " + pkg.getVersion());
            setToolTipText(getText());
            this.pkg = pkg;
        }

        public void run() {
            InstallWizard installWiz = new InstallWizard(pkg/*
                                                             * , AmbientPlugin
                                                             * .getDefault().getSnarferSettings().getLastInstallLocation()
                                                             */);
            WizardDialog dlg = new WizardDialog(getSite().getShell(),
                    installWiz);
            dlg.open();
        }
    }

    /**
     * This action opens the package view on the selected package.
     * 
     * @see PackageViewPart
     */
    private class OpenAction extends Action {
        Package pkg;

        /**
         * Creates a new OpenAction.
         */
        public OpenAction() {
            super("Open");
        }

        /**
         * Sets the PackageInfo object for this action. The package view will
         * use this object to display information.
         * 
         * @param pkg
         *            the PackageInfo object for this action
         */
        public void setPackageInfo(Package pkg) {
            setText("Open " + pkg.getName() + " " + pkg.getVersion());
            setToolTipText(getText());
            this.pkg = pkg;
        }

        /**
         * Opens the package view {@link PackageViewPart}and sets it's display
         * information to be this actions package info.
         * 
         * @see SiteViewPart.OpenAction#setPackageInfo(PackageInfo)
         */
        public void run() {
            try {
                PackageViewPart pv = (PackageViewPart) getSite().getPage()
                        .showView(PackageViewPart.ID);
                pv.setPackageInfo(pkg);
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This action refreshes the site tree. The tree is refreshed by running the
     * {@link SiteViewPart#onRefresh()}method.
     */
    private class RefreshAction extends Action {
        /**
         * Runs {@link SiteViewPart#onRefresh()}.
         */
        public void run() {
            onRefresh();
        }
    }

    /**
     * This action performs an update search to see if there are newer versions
     * of installed packages available for download. If there are, an
     * UpdateDialog is opened.
     * 
     * @see UpdateChecker
     * @see UpdateDialog
     */
    private class UpdateSearchAction extends Action {
        public UpdateSearchAction() {
            super("Search for Updates");
        }

        public void run() {
            UpdateChecker checker = new UpdateChecker(AmbientPlugin
                    .getDefault().getSnarferSettings().getSiteURLs(),
                    AmbientPlugin.getDefault().getSnarferSettings()
                            .getHistory());
            try {
                new ProgressMonitorDialog(getSite().getShell()).run(true, true,
                        checker);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (checker.getUpdates() != null && checker.getUpdates().size() > 0) {
                UpdateDialog dlg = new UpdateDialog(getSite().getShell(),
                        checker.getUpdates(), checker.getSiteMap());
                dlg.open();
            } else {
                MessageDialog.openInformation(getSite().getShell(),
                        "No Updates Found",
                        "No newer versions of installed projects were found.");
            }
        }
    }

    private void createToolbar() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.add(newSiteAction);
        mgr.add(editSiteAction);
        mgr.add(removeSiteAction);
        mgr.add(openAction);
        mgr.add(installAction);
        mgr.add(refreshAction);
        // mgr.add(updateSearchAction);
    }

    private void createContextMenu() {
        // Create menu manager.
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillContextMenu(mgr);
            }
        });

        // Create menu.
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);

        // Register menu for extension.
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void fillContextMenu(IMenuManager mgr) {
        mgr.add(newSiteAction);
        mgr.add(refreshAction);
        if (editSiteAction.isEnabled())
            mgr.add(editSiteAction);
        if (removeSiteAction.isEnabled())
            mgr.add(removeSiteAction);
        if (openAction.isEnabled())
            mgr.add(openAction);
        if (installAction.isEnabled())
            mgr.add(installAction);
        mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        Object sel = ((IStructuredSelection) event.getSelection())
                .getFirstElement();
        if (contentProvider.getParent(sel) == null) {
            URL url = contentProvider.getSiteURL(sel);
            if (url != null) {
                removeSiteAction.setURL(url);
                editSiteAction.setURL(url);
                editSiteAction.setEnabled(true);
                removeSiteAction.setEnabled(true);
            } else {
                editSiteAction.setEnabled(false);
                removeSiteAction.setEnabled(false);
            }
        } else {
            removeSiteAction.setEnabled(false);
            editSiteAction.setEnabled(false);
        }
        if (sel instanceof Package) {
            openAction.setPackageInfo((Package) sel);
            openAction.setEnabled(true);
            installAction.setPackageInfo((Package) sel);
            installAction.setEnabled(true);
        } else {
            openAction.setEnabled(false);
            installAction.setEnabled(false);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e) {
        if (openAction.isEnabled())
            openAction.run();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e) {
        // do nothing
    }
}
