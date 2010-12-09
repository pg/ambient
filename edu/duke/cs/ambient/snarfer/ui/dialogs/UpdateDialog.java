/*
 * Created on Jun 9, 2003
 */
package edu.duke.cs.ambient.snarfer.ui.dialogs;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.snarfer.PackageInstallRecord;
import edu.duke.cs.ambient.snarfer.ui.wizards.InstallWizard;
import edu.duke.cs.snarfer.Package;
import edu.duke.cs.snarfer.PackageSite;

/**
 * @author Administrator
 */
public class UpdateDialog extends Dialog {
    private Map updates, siteMap;

    private Set installSet;

    private Table table;

    public static int INSTALL = Dialog.OK + 1;

    public UpdateDialog(Shell parent, Map updates, Map siteMap) {
        super(parent);
        this.updates = updates;
        this.siteMap = siteMap;
        installSet = new HashSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == INSTALL) {
            Iterator it = installSet.iterator();
            while (it.hasNext()) {
                Package pkg = (Package) it.next();
                PackageInstallRecord record = AmbientPlugin.getDefault()
                        .getSnarferSettings().getLastInstall(pkg.getName());
                Wizard installWiz = new InstallWizard(pkg/*
                                                             * , new File(record
                                                             * .getLocation())
                                                             */);
                WizardDialog dlg = new WizardDialog(getShell(), installWiz);
                dlg.open();
            }
        }
        super.buttonPressed(buttonId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Project Updates Available");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, INSTALL, "Install updates...", false).setEnabled(
                false);
        createButton(parent, Dialog.OK, "OK", true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(1, false));

        Label infoLabel = new Label(panel, SWT.WRAP);
        infoLabel
                .setText("The following projects have newer versions available on sites in your snarf site list.");
        infoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        table = new Table(panel, SWT.BORDER | SWT.CHECK);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumn nameCol = new TableColumn(table, SWT.LEFT);
        nameCol.setText("Project Name");
        nameCol.setWidth(200);
        nameCol.setResizable(true);

        TableColumn oldCol = new TableColumn(table, SWT.LEFT);
        oldCol.setText("Installed Version");
        oldCol.setWidth(100);
        oldCol.setResizable(true);

        TableColumn newCol = new TableColumn(table, SWT.LEFT);
        newCol.setText("New Version");
        newCol.setWidth(100);
        newCol.setResizable(true);

        TableColumn siteCol = new TableColumn(table, SWT.LEFT);
        siteCol.setText("Site");
        siteCol.setWidth(200);
        siteCol.setResizable(true);

        fillTable(table);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                onChecked(((TableItem) e.item).getChecked(),
                        (Package) ((TableItem) e.item).getData());
            }
        });
        return parent;
    }

    protected void fillTable(Table table) {
        Iterator it = updates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Package[] pkgs = (Package[]) entry.getValue();
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, entry.getKey().toString());
            item.setText(1, pkgs[0].getVersion());
            item.setText(2, pkgs[1].getVersion());
            item.setText(3, ((PackageSite) siteMap.get(pkgs[1])).getURL()
                    .toString());
            item.setData(pkgs[1]);
        }
    }

    private void onChecked(boolean checked, Package pkg) {
        if (checked)
            installSet.add(pkg);
        else
            installSet.remove(pkg);
        getButton(INSTALL).setEnabled(!installSet.isEmpty());
    }
}
