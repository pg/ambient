/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
 *******************************************************************************/
/*
 * Created on Jun 3, 2003
 * for Duke Eclipse project
 */
package edu.duke.submit.internal.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import edu.duke.ambient.internal.MessageResources;
import edu.duke.submit.internal.client.ListCommand;
import edu.duke.submit.internal.client.ServerResponse;
import edu.duke.submit.internal.client.SubmitCommand;

/**
 * @author jett
 */
public class SubmitWindow extends ClientWindow implements Listener,
        ICheckStateListener {

    private Group Form, myFilterGroup;

    private Composite myRootCom;

    private Text myRootText;

    private Button myRootButton, myUpButton, mySubmitButton;

    private Label myProgressLabel;

    private Button[] myFilterButtons;

    private ViewerFilter[] myFileFilters;

    private ViewerFilter myDirectoryFilter, myAcceptAllFilter, myFinalFilter;

    private CheckboxTreeViewer myTreeViewer;

    private List mySelectedList;

    private ScrolledComposite myScroller;

    private FileTreeContentProvider myProvider;

    private EclipseClient myClient;

    private File myRoot;

    private SashForm mySashForm;

    private Tree t;

    private Set myProjects, myDownServers;

    private boolean myServerDownDialogFlag = false;

    public SubmitWindow(Shell sh, EclipseClient client) {
        super(sh, client);
        myClient = client;
        this.setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
        myDownServers = new HashSet();
    }

    protected Control createContents(Composite parent) {
        int sashWidth = 490;
        int sashHeight = 405;
        super.createContents(parent);
        getShell().setBounds((900 - sashWidth) / 2, 120, sashWidth + 88,
                sashHeight + 300);
        dialogArea.setSize(sashWidth + 80, sashHeight + 235);
        Rectangle rec = dialogArea.getBounds();
        t.setBounds(10, 35, sashWidth / 2, 140);
        rec.x += 5;
        rec = buttonBar.getBounds();
        rec.width = dialogArea.getBounds().width;
        buttonBar.setBounds(rec);
        mySashForm.setSize(sashWidth, sashHeight);
        rec = mySashForm.getBounds();
        rec.y += 30;
        rec.height -= 50;
        mySashForm.setBounds(rec);
        rec = myRootCom.getBounds();
        rec.width = sashWidth + 64;
        rec.height -= 20;
        rec.y += 40;
        myRootCom.setBounds(rec);
        rec = myRootText.getBounds();
        rec.x = 0;
        rec.width = (int) (sashWidth * 0.8);
        rec.height += 2;
        myRootText.setBounds(rec);
        rec = myUpButton.getBounds();
        rec.y = myRootText.getBounds().y;
        rec.x = myRootText.getBounds().x + myRootText.getBounds().width + 5;
        rec.height = myRootButton.getBounds().height;
        myUpButton.setBounds(rec);
        rec = myRootButton.getBounds();
        rec.y = myRootText.getBounds().y;
        rec.x = myUpButton.getBounds().x + myUpButton.getBounds().width + 5;
        myRootButton.setBounds(rec);
        rec = myFilterGroup.getBounds();
        rec.y = mySashForm.getBounds().y + 300;
        myFilterGroup.setBounds(rec);
        rec = buttonBar.getBounds();
        rec.y = dialogArea.getBounds().y + dialogArea.getBounds().height;
        buttonBar.setBounds(rec);
        rec = mySubmitButton.getBounds();
        rec.width = (int) (rec.width * 2);
        rec.x = buttonBar.getBounds().width - rec.width - 10;
        mySubmitButton.setBounds(rec);
        rec = myProgressLabel.getBounds();
        rec.width = (int) (rec.width * 3);
        myProgressLabel.setBounds(rec);
        mySashForm.setSize(490, 250);
        return getShell();
    }

    private void createSubmitGroup(final Composite parent) {
        myProjects = new HashSet();
        try {
            Label l = new Label(parent, SWT.LEFT);
            l.setText("Submit to:");
            t = new Tree(parent, SWT.BORDER | SWT.SINGLE);
            Object[] courseList = myClient.getCourseList();
            for (int i = 0; i < courseList.length; i++) {
                TreeItem item = new TreeItem(t, SWT.LEFT);

                String currentCourse = (String) courseList[i];
                item.setText(currentCourse);
                item.setImage(PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_OBJ_FOLDER));

                myClient.connect(currentCourse);
                myClient.handleCommand(new ListCommand(currentCourse));
                ServerResponse res = myClient.getResponse();

                int projNum = res.getSize();
                if (!res.getMessage(0)
                        .equalsIgnoreCase(ListCommand.SERVER_DOWN)) {
                    for (int j = 0; j < projNum; j++) {
                        TreeItem subitem = new TreeItem(item, SWT.LEFT);
                        subitem.setText(res.getMessage(j));
                        subitem
                                .setImage(PlatformUI
                                        .getWorkbench()
                                        .getSharedImages()
                                        .getImage(
                                                org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));
                        myProjects.add(res.getMessage(j));
                    }
                } else {
                    myDownServers.add(courseList[i]);
                    for (int j = 1; j < projNum; j++) {
                        TreeItem subitem = new TreeItem(item, SWT.LEFT);
                        subitem.setText(res.getMessage(j));
                        subitem.setImage(PlatformUI.getWorkbench()
                                .getSharedImages().getImage(
                                        ISharedImages.IMG_OBJS_WARN_TSK));
                    }

                }
                myClient.close();
            }
            t.addListener(SWT.Expand, new Listener() {
                public void handleEvent(final Event event) {
                    TreeItem selectedItem = (TreeItem) event.item;
                    String course = selectedItem.getText();
                    if (myDownServers.contains(course)
                            && !myServerDownDialogFlag) {
                        myServerDownDialogFlag = true;
                        MessageDialog.openError(getShell(), "SERVER DOWN",
                                MessageResources.SERVER_DOWN_MESSAGE);
                    }

                }
            });

            t.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                    try {
                        mySubmitButton.setEnabled(false);
                        /*
                         * if (selected.length > 0 && treeCheckedFlag == 1 &&
                         * myProjects.contains(selected[0].getText()))
                         * mySubmitButton.setEnabled(true);
                         */
                        updateList();
                    } catch (Exception ex) {

                    }
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                    t.deselectAll();
                }
            });
            t.deselectAll();
            t.notifyListeners(SWT.Selection, new Event());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createForm(Composite parent) {
        Form = new Group(parent, 0);
        Form.setLayout(new GridLayout(1, false));
        createSubmitGroup(Form);
        createRootGroup();
        createFileTreeGroup();
        createFilterGroup();
        myRootText.setText(myRoot.toString());
    }

    private void setRootText(String s) {
        myRootText.setText(s);
        myRoot = new File(s);
    }

    private void createRootGroup() {
        myRootCom = new Composite(Form, SWT.NONE);
        myRootCom.setLayout(new GridLayout(2, false));
        myRootText = new Text(myRootCom, SWT.BORDER | SWT.SINGLE);
        myRootText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                File f = new File(myRootText.getText());
                if (f.isDirectory()) {
                    myTreeViewer.setInput(f);
                    myProvider.inputChanged(myTreeViewer, myRoot, f);
                    String newRoot = myRootText.getText();
                    myRoot = new File(newRoot);
                    mySelectedList.removeAll();
                    if (mySubmitButton != null) {
                        mySubmitButton.setEnabled(false);
                    }

                }
            }
        });
        myUpButton = new Button(myRootCom, SWT.NONE);
        myUpButton.setImage(PlatformUI.getWorkbench().getSharedImages()
                .getImage(ISharedImages.IMG_TOOL_UP));
        myUpButton.addMouseTrackListener(new MouseTrackAdapter() {
            public void mouseEnter(MouseEvent e) {
                myUpButton.setImage(PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_TOOL_UP));
            }

            public void mouseExit(MouseEvent e) {
                myUpButton.setImage(PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_TOOL_UP));
            }
        });
        myUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String currentRoot = myRootText.getText();
                int pos = currentRoot.lastIndexOf(File.separator);
                if (pos != -1) {
                    myRootText.setText(currentRoot.substring(0, pos));
                    if (myRootText.getText().lastIndexOf(File.separator) == -1) {
                        myRootText.setText(myRootText.getText()
                                + File.separator);
                    }
                }
            }
        });
        myRootButton = new Button(myRootCom, SWT.NONE);
        myRootButton.setText("Browse...");
        myRootButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog d = new DirectoryDialog(getShell());
                d.setMessage("Choose a new root directory.");
                String s = d.open();
                if (s != null) {
                    setRootText(s);
                }
            }
        });
    }

    private void createFileTreeGroup() {
        mySashForm = new SashForm(Form, SWT.HORIZONTAL | SWT.NULL);
        myTreeViewer = new CheckboxTreeViewer(mySashForm);
        myProvider = new FileTreeContentProvider();
        myTreeViewer.setContentProvider(myProvider);
        IWorkspaceRoot workRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = workRoot.getProjects();
        myRoot = null;
        if (projects != null && projects.length != 0) {
            try {
                for (int i = 0; i < projects.length; i++) {
                    IPath path = projects[0].getDescription().getLocation();
                    if (path != null) {
                        path = path.makeAbsolute();
                        myRoot = path.toFile();
                        break;
                    }
                }

            } catch (CoreException e) {
                System.out.println("invalid project path");
            }
        }
        if (myRoot == null) {
            myRoot = new File(System.getProperty("user.dir"));
        }
        myTreeViewer.setInput(myRoot);
        myTreeViewer.setLabelProvider(new FileTreeLabelProvider());
        myTreeViewer.setSorter(new FileTreeSorter());
        myTreeViewer.addCheckStateListener(this);
        myTreeViewer.expandToLevel(myRoot, 1);
        myTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                StructuredSelection i = (StructuredSelection) event
                        .getSelection();
                File root = (File) i.getFirstElement();
                if (root.isDirectory()) {
                    mySelectedList.removeAll();
                    Object[] children = myProvider.getChildren(root);
                    ArrayList checked = new ArrayList();
                    addCheckedChildren(children, checked);
                    myRootText.setText(((File) i.getFirstElement()).toString());
                    int size = checked.size();
                    int pos = myRoot.toString().length() + 1;
                    for (int j = 0; j < size; j++) {
                        myTreeViewer.setExpandedState(((File) checked.get(j)),
                                true);
                        myTreeViewer.setChecked(((File) checked.get(j)), true);
                        mySelectedList.add(((File) checked.get(j)).toString()
                                .substring(pos));
                    }
                    if (size > 0) {
                        mySubmitButton.setEnabled(true);
                    }
                }
            }
        });
        myScroller = new ScrolledComposite(mySashForm, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        myScroller.setLayout(new GridLayout(1, false));
        myScroller.setExpandHorizontal(true);
        myScroller.setExpandVertical(true);
        mySelectedList = new List(myScroller, SWT.NONE);
        mySelectedList.setSize(mySelectedList.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
        myScroller.setContent(mySelectedList);
        myScroller.setMinSize(mySelectedList.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));

    }

    private void addCheckedChildren(Object[] children, ArrayList checked) {
        for (int i = 0; i < children.length; i++) {
            if (myTreeViewer.getChecked(children[i])) {
                checked.add(children[i]);
                if (((File) children[i]).isDirectory()) {
                    addCheckedChildren(myProvider.getChildren(children[i]),
                            checked);
                }
            }

        }
    }

    private void createFilterGroup() {
        myFilterGroup = new Group(Form, 0);
        myFilterGroup.setLayout(new GridLayout(8, false));
        myFilterGroup.setText("Only show file types:");
        myFilterButtons = new Button[4];
        myFileFilters = new ViewerFilter[4];
        Label javaFilterLabel = new Label(myFilterGroup, 0);
        javaFilterLabel.setText(".java");
        myFileFilters[0] = new CustomFileFilter(".java");
        myFilterButtons[0] = new Button(myFilterGroup, SWT.CHECK);
        Label cppFilterLabel = new Label(myFilterGroup, 0);
        cppFilterLabel.setText(".cpp");
        myFileFilters[1] = new CustomFileFilter(".cpp");
        myFilterButtons[1] = new Button(myFilterGroup, SWT.CHECK);
        Label hFilterLabel = new Label(myFilterGroup, 0);
        hFilterLabel.setText(".h");
        myFileFilters[2] = new CustomFileFilter(".h");
        myFilterButtons[2] = new Button(myFilterGroup, SWT.CHECK);
        Label txtLabel = new Label(myFilterGroup, 0);
        txtLabel.setText(".txt");
        myFileFilters[3] = new CustomFileFilter(".txt");
        myFilterButtons[3] = new Button(myFilterGroup, SWT.CHECK);
        FilterListener fl = new FilterListener();
        for (int i = 0; i < myFilterButtons.length; i++) {
            myFilterButtons[i].addSelectionListener(fl);
        }
        // default show all folders and files
        myDirectoryFilter = new DirectoryFilter();
        myAcceptAllFilter = new CustomOrFilter(myDirectoryFilter,
                new CustomAcceptAllFilter());
        myFinalFilter = myAcceptAllFilter;
        myTreeViewer.addFilter(myFinalFilter);
    }

    class FilterListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            ArrayList selectedFilters = new ArrayList();
            mySelectedList.removeAll();
            mySubmitButton.setEnabled(false);
            Object[] expandedList = myTreeViewer.getExpandedElements();
            for (int i = 0; i < myFilterButtons.length; i++) {
                if (myFilterButtons[i].getSelection()) {
                    selectedFilters.add(new Integer(i));
                }
            }
            if (selectedFilters.size() == 0) {
                myTreeViewer.removeFilter(myFinalFilter);
                myFinalFilter = myAcceptAllFilter;
                myTreeViewer.addFilter(myFinalFilter);
            } else {
                myTreeViewer.removeFilter(myFinalFilter);
                myFinalFilter = new CustomOrFilter(myDirectoryFilter,
                        myFileFilters[((Integer) selectedFilters.get(0))
                                .intValue()]);
                int i = 1;
                while (i < selectedFilters.size()) {
                    myFinalFilter = new CustomOrFilter(myFinalFilter,
                            myFileFilters[((Integer) selectedFilters.get(i))
                                    .intValue()]);
                    i++;
                }
                myTreeViewer.addFilter(myFinalFilter);
            }
            myTreeViewer.setExpandedElements(expandedList);
            updateList();
        }
    }

    private void updateRecursiveHelper(Item[] roots, int rootIndex) {
        for (int i = 0; i < roots.length; i++) {
            if (((TreeItem) roots[i]).getChecked()) {
                mySelectedList.add(((File) ((TreeItem) roots[i]).getData())
                        .getPath().substring(rootIndex));
                updateRecursiveHelper(((TreeItem) roots[i]).getItems(),
                        rootIndex);
                // check if a project is selected
                if (t.getSelection() != null && t.getSelection().length > 0
                        && myProjects.contains(t.getSelection()[0].getText()))
                    mySubmitButton.setEnabled(true);

            }
        }
    }

    private void updateList() {
        mySelectedList.removeAll();
        mySubmitButton.setEnabled(false);
        Tree tree = myTreeViewer.getTree();
        Item[] roots = tree.getItems();
        String fullRoot = myRoot.toString();
        int rootIndex = myRoot.toString().length() + 1;
        if (fullRoot.charAt(fullRoot.length() - 1) == File.separatorChar) {
            rootIndex -= 1;
        }
        updateRecursiveHelper(roots, rootIndex);
        myScroller.setMinSize(mySelectedList.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));

    }

    private int getCheckCount(Object[] children) {
        int checkCount = 0;
        for (int i = 0; i < children.length; i++) {
            if (myTreeViewer.getChecked((File) children[i])) {
                checkCount++;
            }
        }
        return checkCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
     */
    public void checkStateChanged(CheckStateChangedEvent event) {
        File parent = new File((String) myProvider.getParent((File) event
                .getElement()));
        Object[] children = myProvider.getChildren(parent);
        int checkCount = getCheckCount(children);
        if (event.getChecked()) {
            myTreeViewer.setGrayed((File) event.getElement(), false);
            myTreeViewer.expandToLevel((File) event.getElement(), 1);
            myTreeViewer.setSubtreeChecked(event.getElement(), true);
            if (checkCount == children.length) {
                myTreeViewer.setGrayed(parent, false);
                myTreeViewer.setChecked(parent, true);
            }
        } else {
            myTreeViewer.collapseToLevel(event.getElement(), 1);
            myTreeViewer.setSubtreeChecked(event.getElement(), false);
            if (checkCount == 0) {
                myTreeViewer.setGrayChecked(parent, false);
            }
        }

        try {
            while (!parent.toString().equals(myRoot.toString())) {
                Object[] currentChildren = myProvider.getChildren(parent);
                int currentCheckCount = getCheckCount(currentChildren);
                if (currentCheckCount == 0) {
                    myTreeViewer.setGrayChecked(parent, false);
                } else if (currentCheckCount == currentChildren.length) {
                    myTreeViewer.setChecked(parent, true);
                    myTreeViewer.setGrayed(parent, false);
                } else {
                    myTreeViewer.setGrayChecked(parent, true);
                }
                parent = new File((String) myProvider.getParent(parent));
            }
            updateList();
        } catch (NullPointerException e) {
            return;
        }
    }

    public void handleEvent(Event e) {
        mySubmitButton.setEnabled(false);
        try {
            String chosenCourse = t.getSelection()[0].getParentItem().getText();
            myClient.connect(chosenCourse);
            myClient.runLogin();
            String[] files = mySelectedList.getItems();
            String chosenProject = t.getSelection()[0].getText();
            myClient.setCurrentProject(chosenCourse, chosenProject);
            SubmitCommand sc = new SubmitCommand(chosenCourse, chosenProject,
                    myRoot.toString(), files);
            sc.addObserver(this);
            myClient.handleCommand(sc);
        } catch (NullPointerException e1) {

        }
    }

    public void reEnable() {
        mySubmitButton.setEnabled(true);
        myProgressLabel.setText("                     ");
    }

    protected void setProgressText(String prog) {
        myProgressLabel.setText(prog);
        myProgressLabel.redraw();
    }

    protected Control createDialogArea(Composite parent) {
        createForm(parent);
        return Form;
    }

    protected Control createButtonBar(Composite parent) {
        Composite bar = new Composite(parent, SWT.NONE);
        bar.setLayout(new GridLayout(2, false));
        myProgressLabel = new Label(bar, SWT.BOLD);
        myProgressLabel.setText("                     ");
        mySubmitButton = new Button(bar, SWT.CENTER);
        mySubmitButton.setText("Submit");
        mySubmitButton.setEnabled(false);
        mySubmitButton.addListener(SWT.Selection, this);
        getShell().setDefaultButton(mySubmitButton);
        return bar;
    }

    protected void configureShell(Shell shl) {
        super.configureShell(shl);
        shl.setLocation(500, 100);
        shl.setText("Submit");
    }

}