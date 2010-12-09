package edu.duke.cs.ambient.snarfer.ui.views;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.snarfer.PackageInstallRecord;
import edu.duke.cs.ambient.snarfer.ui.wizards.InstallWizard;
import edu.duke.cs.snarfer.Package;

/**
 * This class manages the Package View.
 */
public class PackageViewPart extends ViewPart {
    /**
     * The identifier string for this view with the value
     * "edu.duke.cs.ambient.snarfer.PackageView"
     */
    public static final String ID = "edu.duke.cs.ambient.snarfer.PackageView";

    private Package myPkg;

    private ScrolledComposite myWindow = null;

    private Label myProjectName = null;

    private Button myInstallButton = null;

    private Label myVersionNumber = null;

    private Label myPublisherName = null;

    private Text myInfoText = null;

    private StyledText myDescriptionText = null;

    /**
     * Creates a new instance of this view.
     */
    public PackageViewPart() {
    }

    /**
     * Updates the display in this view with the information from the provided
     * info object. If info is <code>null</code> then nothing gets displayed.
     * 
     * @param info
     *            the source of the displayed information.
     */
    public void setPackageInfo(Package info) {
        if (info == null) {
            myWindow.setVisible(false);
            return;
        }
        myPkg = info;

        myProjectName.setText(myPkg.getName());
        myVersionNumber.setText(myPkg.getVersion());
        myPublisherName.setText(myPkg.getPublisher());
        PackageInstallRecord record = AmbientPlugin.getDefault()
                .getSnarferSettings().getLastInstall(myPkg.getName());
        if (record != null) {
            // TODO handle history reporting
            // myInfoText.setText("Version " + record.getPackage().getVersion()
            // + " of this project was installed "
            // + record.getInstallDate() + " at location '"
            // + record.getLocation() + "'.");
        } else if (myPkg != null) {
            // myInfoText.setText("This project has not been previously
            // installed.");
        } else {
            myInfoText.setVisible(false);
        }
        if (myPkg != null) {
            myDescriptionText.setText(myPkg.getDescription());
            myDescriptionText.layout(true);
            myDescriptionText.setVisible(true);
        } else {
            myDescriptionText.setVisible(false);
        }
        myWindow.setMinSize(myWindow.getContent().computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
        myWindow.setVisible(true);
        myWindow.layout(true);
        myPublisherName.pack();
        myProjectName.pack();
        myVersionNumber.pack();
        myDescriptionText.pack();
        myWindow.redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent2) {
        Color blue = new Color(this.getSite().getShell().getDisplay(), 0, 0,
                255);
        parent2.setLayout(new FillLayout());

        myWindow = new ScrolledComposite(parent2, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        myWindow.setAlwaysShowScrollBars(false);
        myWindow.setExpandHorizontal(true);
        myWindow.setExpandVertical(true);

        Composite parent = new Composite(myWindow, SWT.NONE);
        parent.setLayout(new GridLayout(3, false));

        myInstallButton = new Button(parent, SWT.NONE);
        myInstallButton.setText("Install Project ...");
        GridData installButtonGridData = new GridData(
                GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_END);
        installButtonGridData.verticalSpan = 3;
        myInstallButton.setLayoutData(installButtonGridData);
        myInstallButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                onInstall();
            }
        });

        Label projectLabel = new Label(parent, SWT.NONE);
        projectLabel.setText("Project:");
        projectLabel.setForeground(blue);

        myProjectName = new Label(parent, SWT.NONE);

        Label versionLabel = new Label(parent, SWT.NONE);
        versionLabel.setText("Version:");
        versionLabel.setForeground(blue);

        myVersionNumber = new Label(parent, SWT.NONE);
        myVersionNumber.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));

        Label publisherLabel = new Label(parent, SWT.NONE);
        publisherLabel.setForeground(blue);
        publisherLabel.setText("Publisher:");

        myPublisherName = new Label(parent, SWT.NONE);
        GridData publisherNameGridData = new GridData();
        myPublisherName.setLayoutData(publisherNameGridData);

        myInfoText = new Text(parent, SWT.MULTI | SWT.WRAP);
        GridData infoTextGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        infoTextGridData.horizontalSpan = 3;
        myInfoText.setLayoutData(infoTextGridData);
        myInfoText.setEditable(false);
        myInfoText.setBackground(myPublisherName.getBackground());

        Label descriptionLabel = new Label(parent, SWT.NONE);
        descriptionLabel.setForeground(blue);
        descriptionLabel.setText("Description:");

        myDescriptionText = new StyledText(parent, SWT.WRAP);
        GridData descTextGridData = new GridData(GridData.FILL_HORIZONTAL);
        descTextGridData.horizontalSpan = 3;
        myDescriptionText.setLayoutData(descTextGridData);
        myDescriptionText.setEditable(false);
        myDescriptionText.setBackground(myPublisherName.getBackground());

        setPackageInfo(null);

        myWindow.setContent(parent);
        myWindow.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * This implementation does nothing.
     */
    public void setFocus() {
        // do nothing
    }

    /**
     * Launches the InstallWizard. This function gets called when the Install
     * button is pressed.
     * 
     * @see InstallWizard
     */
    protected void onInstall() {
        Wizard installWiz = new InstallWizard(myPkg/*
                                                     * ,
                                                     * AmbientPlugin.getDefault()
                                                     * .getSnarferSettings().getLastInstallLocation()
                                                     */);

        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), installWiz);
        dialog.open();
    }
}
