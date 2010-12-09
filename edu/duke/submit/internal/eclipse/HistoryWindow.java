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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.duke.submit.internal.client.HistoryCommand;
import edu.duke.submit.internal.client.ServerResponse;

/**
 * @author jett
 * 
 */
public class HistoryWindow extends ClientWindow {

    public HistoryWindow(Shell shl, EclipseClient ec) {
        super(shl, ec);
        myClient.handleCommand(new HistoryCommand(myClient.getCurrentCourse(),
                myClient.getCurrentProject()));
        myResponse = myClient.getResponse();
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
        getShell().setBounds((900 - 250) / 2, 200, 350, 340);
        dialogArea.setSize(350, 300);

        Rectangle rec = myTextArea.getBounds();
        rec.width += 90;
        myTextArea.setBounds(rec);
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
        Composite Header = new Composite(parent, 0);
        Header.setLayout(new GridLayout(2, false));

        Label classLbl = new Label(Header, SWT.LEFT);
        classLbl.setText("Course:");

        int style = SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY;
        Label classTxt = new Label(Header, style);
        classTxt.setText(myClient.getCurrentCourse());

        Label projLbl = new Label(Header, SWT.LEFT);
        projLbl.setText("Project:");

        style = SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY;
        Label projTxt = new Label(Header, style);
        projTxt.setText(myClient.getCurrentProject());
    }

    private void createTextArea(Composite parent) {
        myTextArea = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.LEFT);
        int numMessages = myResponse.getSize();
        int i;
        for (i = 0; i < 3; i++) {
            myTextArea.append(myResponse.getMessage(i));
            myTextArea.append("\n");

        }
        myScroller = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        myScroller.setLayout(new GridLayout(1, false));
        myScroller.setExpandHorizontal(true);
        myScroller.setExpandVertical(true);
        myList = new List(myScroller, SWT.NONE);
        myList.setSize(myList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        myScroller.setContent(myList);
        myScroller.setMinSize(myList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        while (i < numMessages) {
            myList.add(myResponse.getMessage(i));
            myScroller.setMinSize(myList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            i++;
        }
    }

    protected Control createButtonBar(Composite parent) {
        Composite bar = new Composite(parent, SWT.NONE);
        bar.setLayout(new GridLayout(2, false));
        Label empty = new Label(bar, SWT.NONE);
        empty.setText("\t\t\t\t\t    ");
        Button okButton = new Button(bar, SWT.CENTER);
        okButton.setText("       OK       ");
        getShell().setDefaultButton(okButton);
        okButton.addListener(SWT.Selection, this);
        return bar;
    }

    public void handleEvent(Event e) {
        this.close();
    }

    private ServerResponse myResponse;

    private List myList;

    private Text myTextArea;

    private ScrolledComposite myScroller;
}
