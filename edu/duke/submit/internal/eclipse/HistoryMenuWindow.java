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
 * Created on Jun 4, 2003
 *
 */

package edu.duke.submit.internal.eclipse;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import edu.duke.ambient.internal.MessageResources;
import edu.duke.submit.internal.client.HistoryCommand;
import edu.duke.submit.internal.client.ListCommand;
import edu.duke.submit.internal.client.ServerResponse;

/**
 * @author jett
 */

public class HistoryMenuWindow extends ClientWindow {
    private ServerResponse myResponse;

    private List myList;

    private ScrolledComposite myScroller;

    private boolean isLoggedin = false;

    private Tree myTree;

    private String selectedCourse = "", selectedProject = "";

    private Button myGetHistoryButton;

    private Set myProjects, myDownServers;

    private boolean myServerDownDialogFlag = false;

    public HistoryMenuWindow(Shell shl, EclipseClient ec) {
        super(shl, ec);
        myDownServers = new HashSet();
    }

    protected void configureShell(Shell shl) {
        super.configureShell(shl);
        shl.setText("Submit History");
    }

    protected void handleShellCloseEvent() {
        myClient.close();
        close();
    }

    public void reEnable() {
    }

    protected Control createContents(Composite parent) {
        super.createContents(parent);
        createButtonBar(parent);
        // Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        getShell().setBounds((900 - 250) / 2, 200, 350, 360);
        dialogArea.setSize(350, 300);
        // Rectangle rec = myTextArea.getBounds();
        // rec.width += 230;
        // myTextArea.setBounds(rec);
        myTree.setBounds(5, 30, 250, 90);
        myScroller.setBounds(5, 130, 335, 150);
        myScroller.setSize(335, 150);
        return getShell();
    }

    protected Control createDialogArea(Composite parent) {
        Composite Form = new Composite(parent, 0);
        Form.setLayout(new GridLayout(1, false));
        createHeader(Form);
        createTextArea(Form);
        return Form;
    }

    private void createHeader(Composite parent) {
        myProjects = new HashSet();
        // try
        // {
        Label l = new Label(parent, SWT.LEFT);
        l.setText("Select course and project:");
        myTree = new Tree(parent, SWT.BORDER | SWT.SINGLE);

        Object[] courseList = myClient.getCourseList();
        for (int i = 0; i < courseList.length; i++) {
            TreeItem item = new TreeItem(myTree, SWT.LEFT);
            String currentCourse = (String) courseList[i];
            item.setText(currentCourse);
            item.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJ_FOLDER));

            myClient.connect(currentCourse);
            myClient.handleCommand(new ListCommand(currentCourse));
            ServerResponse res = myClient.getResponse();

            int projNum = res.getSize();
            if (!res.getMessage(0).equalsIgnoreCase(ListCommand.SERVER_DOWN)
                    && !res.getMessage(0).equalsIgnoreCase(
                            ServerResponse.NO_PROJECTS)) {
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
            } else if (res.getMessage(0).equalsIgnoreCase(
                    ListCommand.SERVER_DOWN)) {
                myDownServers.add(courseList[i]);
                for (int j = 1; j < projNum; j++) {
                    TreeItem subitem = new TreeItem(item, SWT.LEFT);
                    subitem.setText(res.getMessage(j));
                    subitem.setImage(PlatformUI.getWorkbench()
                            .getSharedImages().getImage(
                                    ISharedImages.IMG_OBJS_WARN_TSK));
                }

            } else if (res.getMessage(0).equalsIgnoreCase(
                    ServerResponse.NO_PROJECTS)) {
                TreeItem subitem = new TreeItem(item, SWT.LEFT);
                subitem.setText("no projects are collected");
                subitem.setImage(PlatformUI.getWorkbench().getSharedImages()
                        .getImage(ISharedImages.IMG_OBJS_WARN_TSK));
            }

        }

        myTree.addListener(SWT.Expand, new Listener() {
            public void handleEvent(final Event event) {
                TreeItem selectedItem = (TreeItem) event.item;
                String course = selectedItem.getText();
                if (myDownServers.contains(course) && !myServerDownDialogFlag) {
                    myServerDownDialogFlag = true;
                    // myBeeper.beep();
                    MessageDialog.openError(getShell(), "SERVER DOWN",
                            MessageResources.SERVER_DOWN_MESSAGE);
                }

            }
        });
        myTree.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    myGetHistoryButton.setEnabled(false);
                    TreeItem[] selected = myTree.getSelection();
                    if (myProjects.contains(selected[0].getText())) {
                        selectedCourse = selected[0].getParentItem().getText();
                        selectedProject = selected[0].getText();
                        myGetHistoryButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

    }

    private void createTextArea(Composite parent) {
        // myTextArea = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.LEFT);
        // myTextArea.append("\n\n\n");
        // myTextArea.setEditable(false);
        myScroller = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        myScroller.setLayout(new GridLayout(1, false));
        myScroller.setExpandHorizontal(true);
        myScroller.setExpandVertical(true);
        myList = new List(myScroller, SWT.NONE);
        myList.setSize(myList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        myScroller.setContent(myList);
        myScroller.setMinSize(myList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    protected Control createButtonBar(Composite parent) {
        Composite bar = new Composite(parent, SWT.NONE);
        bar.setLayout(new GridLayout(3, false));
        Label empty = new Label(bar, SWT.NONE);
        empty.setText("\t\t\t\t");
        myGetHistoryButton = new Button(bar, SWT.CENTER);
        myGetHistoryButton.setText(" Get History ");
        myGetHistoryButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                doHistory();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                doHistory();
            }
        });
        Button closeButton = new Button(bar, SWT.CENTER);
        closeButton.setText("    Close    ");
        getShell().setDefaultButton(myGetHistoryButton);
        closeButton.addListener(SWT.Selection, this);
        return bar;
    }

    public void handleEvent(Event e) {
        this.close();
    }

    public void doHistory() {
        if (selectedCourse == "" || selectedProject == "") {
            // myBeeper.beep();
            MessageDialog
                    .openError(getShell(), "ERROR",
                            "You must select a course and project for history information.");
            return;
        }

        String course = selectedCourse;
        myClient.connect(course);

        if (!isLoggedin) {
            myClient.runLogin();
            isLoggedin = true;
        }

        myClient.handleCommand(new HistoryCommand(course, selectedProject));

        myResponse = myClient.getResponse();
        // myTextArea.setText("");
        myList.removeAll();
        int numMessages = myResponse.getSize();
        if (numMessages == 1) {
            myList.add(myResponse.getMessage(0) + "\n");
            return;
        }
        int i;
        for (i = 0; i < 3; i++) {
            myList.add(myResponse.getMessage(i) + "\n");
        }
        while (i < numMessages) {
            myList.add(myResponse.getMessage(i));
            myScroller.setMinSize(myList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            i++;
        }

    }
}
