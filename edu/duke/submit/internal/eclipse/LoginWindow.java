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
 *
 */
package edu.duke.submit.internal.eclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.duke.submit.internal.client.LoginCommand;

/**
 * @author jett
 */
public class LoginWindow extends ClientWindow {
    public LoginWindow(Shell shl, EclipseClient ec) {
        super(shl, ec);
    }

    protected void configureShell(Shell shl) {
        super.configureShell(shl);
        shl.setText("Login");

    }

    private void createLoginGroup(Composite parent) {
        try {
            myLoginGroup = new Group(parent, 0);
            myLoginGroup.setText("Login");
            myLoginGroup.setLayout(new GridLayout(2, false));

            Label UserLabel = new Label(myLoginGroup, SWT.LEFT);
            UserLabel.setText("Username:");

            int style = SWT.SINGLE | SWT.LEFT | SWT.BORDER;
            myUser = new Text(myLoginGroup, style);
            myUser.setToolTipText("Username");
            myUser.addModifyListener(new Validator());

            Label PassLabel = new Label(myLoginGroup, SWT.LEFT);
            PassLabel.setText("Password:");

            style = SWT.SINGLE | SWT.LEFT | SWT.BORDER;
            myPass = new Text(myLoginGroup, style);
            myPass.setEchoChar('*');
            myPass.setToolTipText("Password");
            myPass.addModifyListener(new Validator());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Control createDialogArea(Composite parent) {
        createLoginGroup(parent);
        return myLoginGroup;
    }

    protected Control createButtonBar(Composite parent) {
        Composite bar = new Composite(parent, SWT.NONE);
        bar.setLayout(new GridLayout(2, false));

        myProgressLabel = new Label(bar, SWT.LEFT | SWT.HORIZONTAL);
        myProgressLabel.setText("");

        mySubmit = new Button(bar, SWT.CENTER);
        mySubmit.setText("Login");
        mySubmit.addListener(SWT.Selection, this);
        getShell().setDefaultButton(mySubmit);

        mySubmit.setEnabled(false);

        return bar;
    }

    protected Control createContents(Composite parent) {
        super.createContents(parent);

        Rectangle rec;
        getShell().setSize(166, 145);
        getShell().setLocation((1024 - 166) / 2, (768 - 145) / 2);
        rec = buttonBar.getBounds();
        rec.width = dialogArea.getBounds().width;
        buttonBar.setBounds(rec);
        int myWidth = buttonBar.getBounds().width;
        rec = dialogArea.getBounds();
        rec.x += 5;
        dialogArea.setBounds(rec);
        rec = myProgressLabel.getBounds();
        // rec.width = mySubmit.getBounds().y - 8;
        rec.width = myWidth - mySubmit.getBounds().width - 13;
        myProgressLabel.setBounds(rec);
        rec = mySubmit.getBounds();
        rec.x = myWidth - mySubmit.getBounds().width - 5;
        mySubmit.setBounds(rec);
        return getShell();
    }

    public void handleEvent(Event e) {
        LoginCommand lc = new LoginCommand(myUser.getText(), myPass.getText());
        mySubmit.setEnabled(false);
        lc.addObserver(this);
        myClient.handleCommand(lc);
        this.close();
    }

    protected void setProgressText(String s) {
        myProgressLabel.setText(s);
    }

    public void reEnable() {
        mySubmit.setEnabled(true);
    }

    private Group myLoginGroup;

    private Text myUser;

    private Text myPass;

    private Button mySubmit;

    private Label myProgressLabel;

    class Validator implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            mySubmit.setEnabled(!(myUser.getText().equals("") || myPass
                    .getText().equals("")));
        }
    }
}
