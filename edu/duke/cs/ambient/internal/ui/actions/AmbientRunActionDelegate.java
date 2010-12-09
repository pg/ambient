/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
*******************************************************************************/
package edu.duke.cs.ambient.internal.ui.actions;
/**
 * @author Duke Curious 2004
 */
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;

import edu.duke.cs.ambient.AmbientPlugin;

public class AmbientRunActionDelegate extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {
	
	private AmbientRunAction applicationAction = null;
	private AmbientRunAction applicationCLAAction = null;
	private AmbientRunAction appletAction = null;
//	private WeeklyRunApplicationAction aptAction = null;
	private AmbientRunAction applicationDebugAction = null;
	private AmbientRunAction applicationDebugCLAAction = null;
	private AmbientRunAction appletDebugAction = null;
		
	/**
	 * The constructor.
	 */
	public AmbientRunActionDelegate() {
		super();
		
		//set up the action
		applicationAction = new AmbientRunApplicationAction();
		applicationAction.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		applicationAction.setText("Run Application");
		applicationAction.setActionDefinitionId("edu.duke.ambient.ui.AmbientRunApplicationCommand");
		//set up the icon
		applicationAction.setImageDescriptor(AmbientPlugin.getImageDescriptor("icons/ambientRunApplication.gif"));
        
		applicationCLAAction = new AmbientRunApplicationCLAAction();
		applicationCLAAction.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		applicationCLAAction.setText("Run Application w/ Command Line Arguments");
		applicationCLAAction.setActionDefinitionId("edu.duke.ambient.ui.AmbientRunApplicationCLACommand");
		//set up the icon
		applicationCLAAction.setImageDescriptor(AmbientPlugin.getImageDescriptor("icons/ambientRunApplication.gif"));
				
		appletAction= new AmbientRunAppletAction();
		appletAction.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		appletAction.setText("Run Applet");
		appletAction.setActionDefinitionId("edu.duke.ambient.ui.AmbientRunAppletCommand");
		//set up the icon
		appletAction.setImageDescriptor(AmbientPlugin.getImageDescriptor("icons/ambientRunApplet.gif"));

//		aptAction = new WeeklyRunApplicationAction();
//		aptAction.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
//		aptAction.setText("Run APT");
//		aptAction.setActionDefinitionId("edu.duke.eclipse.weekly.ui.AmbientRunApplicationCommand");
//		//set up the icon
//		iPath = new Path("icons/weekly.gif");
//		url = WorkbenchPlugin.getDefault().find(iPath);
//		aptAction.setImageDescriptor(ImageDescriptor.createFromURL(url));		

		//set up the action
		applicationDebugAction = new AmbientDebugApplicationAction();
		applicationDebugAction.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		applicationDebugAction.setText("Debug Application");
		applicationDebugAction.setActionDefinitionId("edu.duke.ambient.ui.AmbientDebugApplicationCommand");
		//set up the icon
		applicationDebugAction.setImageDescriptor(AmbientPlugin.getImageDescriptor("icons/ambientDebugApplication.gif"));
		
		//set up the action
		applicationDebugCLAAction = new AmbientDebugApplicationCLAAction();
		applicationDebugCLAAction.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		applicationDebugCLAAction.setText("Debug Application w/ Command Line Arguments");
		applicationDebugCLAAction.setActionDefinitionId("edu.duke.ambient.ui.AmbientDebugApplicationCLACommand");
		//set up the icon
		applicationDebugCLAAction.setImageDescriptor(AmbientPlugin.getImageDescriptor("icons/ambientDebugApplication.gif"));
		
		appletDebugAction= new AmbientDebugAppletAction();
		appletDebugAction.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		appletDebugAction.setText("Debug Applet");
		appletDebugAction.setActionDefinitionId("edu.duke.ambient.ui.AmbientDebugAppletCommand");
		//set up the icon
		appletDebugAction.setImageDescriptor(AmbientPlugin.getImageDescriptor("icons/ambientDebugApplet.gif"));

	}

	public void run(IAction action) {
		applicationAction.run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		Menu AmbientRunMenu = new Menu(parent);
		fillMenu(AmbientRunMenu);
		return AmbientRunMenu;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		Menu AmbientRunMenu = new Menu(parent);
		fillMenu(AmbientRunMenu);
		return AmbientRunMenu;
	}
	
	/*
	 * Adds the two actions to a new menu. 
	 * @param menu
	 */
	private void fillMenu(Menu menu) {
		ActionContributionItem applicationItem = 
			new ActionContributionItem(applicationAction);
		ActionContributionItem applicationCLAItem = 
					new ActionContributionItem(applicationCLAAction);
		ActionContributionItem appletItem =
			new ActionContributionItem(appletAction);
//		ActionContributionItem aptItem =
//			new ActionContributionItem(aptAction);
		ActionContributionItem applicationDebugItem = 
					new ActionContributionItem(applicationDebugAction);
		ActionContributionItem applicationDebugCLAItem = 
					new ActionContributionItem(applicationDebugCLAAction);
		ActionContributionItem appletDebugItem =
					new ActionContributionItem(appletDebugAction);
							
		applicationItem.fill(menu, -1);
		applicationCLAItem.fill(menu, -1);
		appletItem.fill(menu, -1);
//		aptItem.fill(menu, -1);
		applicationDebugItem.fill(menu, -1);
		applicationDebugCLAItem.fill(menu, -1);
		appletDebugItem.fill(menu, -1);
		
	}
}
