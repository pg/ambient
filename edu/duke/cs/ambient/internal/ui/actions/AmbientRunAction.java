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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine;
import org.eclipse.jdt.internal.debug.ui.launcher.MainTypeSelectionDialog;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

/**
 * @author Duke Curious 2004
 */
public abstract class AmbientRunAction extends AmbientSelectionAction {

    protected String arguments;

    public AmbientRunAction() {
        super();
    }

    public void init(IWorkbenchWindow window) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // no need to dispose anything
    }

    public void run() {
        IWorkbenchWindow wb = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (wb != null) {
            IWorkbenchPage page = wb.getActivePage();
            if (page != null) {
                ISelection selection = page.getSelection();
                // update the selection.
                selectionChanged(null, selection);
            }
        }

        saveDirtyEditors();
        // run the action as null; there is no other action to run besides this
        // one.
        run(null);
    }

    public static boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(true);
    }

    public void run(IAction action) {
        // do nothing
    }

    /**
     * @param search
     *            the java elements to search for a main type
     * @param mode
     *            the mode to launch in
     * @param whether
     *            activated on an editor (or from a selection in a myViewer)
     */
    public void searchAndLaunch(Object[] search, String mode, boolean editor) {
        IType[] types = null;
        if (search != null) {
            try {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                        getShell());
                MainMethodSearchEngine engine = new MainMethodSearchEngine();

                IJavaSearchScope scope = SearchEngine
                        .createJavaSearchScope((IJavaElement[]) search);
                types = engine.searchMainMethods(dialog, scope, false);
            } catch (InterruptedException e) {
                return;
            } catch (java.lang.reflect.InvocationTargetException e) {
                return;
            }
            IType type = null;
            if (types.length == 0) {
                String message = null;
                if (editor) {
                    message = "The active editor does not contain a main type";// LauncherMessages.getString("JavaApplicationLaunchShortcut.The_active_editor_does_not_contain_a_main_type._1");
                       // TODO find out what all the old messages really were                                                         // //$NON-NLS-1$
                } else {
                    message = "The selection does not contain a main type";// LauncherMessages.getString("JavaApplicationLaunchShortcut.The_selection_does_not_contain_a_main_type._2");
                                                                            // //$NON-NLS-1$
                }
                MessageDialog
                        .openError(
                                getShell(),
                                "Launch failed"/* LauncherMessages.getString("JavaApplicationAction.Launch_failed_7") */, message); //$NON-NLS-1$
            } else if (types.length > 1) {
                type = chooseType(types, mode);
            } else {
                type = types[0];
            }
            if (type != null) {
                launch(type, mode);
            }
        }

    }

    /**
     * Prompts the user to select a type
     * 
     * @return the selected type or <code>null</code> if none.
     */
    protected IType chooseType(IType[] types, String mode) {
        MainTypeSelectionDialog dialog = new MainTypeSelectionDialog(
                getShell(), types);
        if (mode.equals(ILaunchManager.DEBUG_MODE)) {
            dialog
                    .setTitle("Type Selection Debug"/* LauncherMessages.getString("JavaApplicationAction.Type_Selection_Debug") */); //$NON-NLS-1$
        } else {
            dialog
                    .setTitle("Type Selection Run"/* LauncherMessages.getString("JavaApplicationAction.Type_Selection_Run") */); //$NON-NLS-1$
        }
        dialog.setMultipleSelection(false);
        if (dialog.open() == MainTypeSelectionDialog.OK) {
            return (IType) dialog.getFirstResult();
        }
        return null;
    }

    /**
     * Launches a configuration for the given type
     */
    protected void launch(IType type, String mode) {
        ILaunchConfiguration config = findLaunchConfiguration(type, mode);
        ILaunchConfigurationWorkingCopy wc = null;
        try {
            wc = config.getWorkingCopy();
            if (wc == null)
                return;
            wc.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                    arguments);
            config = wc.doSave();
        } catch (CoreException e) {
        }
        if (config != null) {
            DebugUITools.launch(config, mode);
        }
    }

    /**
     * @return
     */
    protected String getArguments() {
        AmbientArgumentsDialog dialog = new AmbientArgumentsDialog(getShell());
        if (dialog.open() == AmbientArgumentsDialog.OK)
            return dialog.getArguments();
        return null;
    }

    /**
     * Locate a configuration to relaunch for the given type. If one cannot be
     * found, create one.
     * 
     * @return a re-useable config or <code>null</code> if none
     */
    protected ILaunchConfiguration findLaunchConfiguration(IType type,
            String mode) {
        ILaunchConfigurationType configType = getJavaLaunchConfigType();
        List candidateConfigs = Collections.EMPTY_LIST;
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault()
                    .getLaunchManager().getLaunchConfigurations(configType);
            candidateConfigs = new ArrayList(configs.length);
            for (int i = 0; i < configs.length; i++) {
                ILaunchConfiguration config = configs[i];
                if (config.getAttribute(
                        IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                        "").equals(type.getFullyQualifiedName())) { //$NON-NLS-1$
                    if (config
                            .getAttribute(
                                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                                    "").equals(type.getJavaProject().getElementName())) { //$NON-NLS-1$
                        candidateConfigs.add(config);
                    }
                }
            }
        } catch (CoreException e) {
            JDIDebugUIPlugin.log(e);
        }

        // If there are no existing configs associated with the IType, create
        // one.
        // If there is exactly one config associated with the IType, return it.
        // Otherwise, if there is more than one config associated with the
        // IType, prompt the
        // user to choose one.
        int candidateCount = candidateConfigs.size();
        if (candidateCount < 1) {
            return createConfiguration(type);
        } else if (candidateCount == 1) {
            return (ILaunchConfiguration) candidateConfigs.get(0);
        } else {
            // Prompt the user to choose a config. A null result means the user
            // cancelled the dialog, in which case this method returns null,
            // since cancelling the dialog should also cancel launching
            // anything.
            ILaunchConfiguration config = chooseConfiguration(candidateConfigs,
                    mode);
            if (config != null) {
                return config;
            }
        }

        return null;
    }

    /**
     * Show a selection dialog that allows the user to choose one of the
     * specified launch configurations. Return the chosen config, or
     * <code>null</code> if the user cancelled the dialog.
     */
    protected ILaunchConfiguration chooseConfiguration(List configList,
            String mode) {
        IDebugModelPresentation labelProvider = DebugUITools
                .newDebugModelPresentation();
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                getShell(), labelProvider);
        dialog.setElements(configList.toArray());
        dialog
                .setTitle("Launch Configuration Selection"/* LauncherMessages.getString("JavaApplicationAction.Launch_Configuration_Selection_1") */); //$NON-NLS-1$
        if (mode.equals(ILaunchManager.DEBUG_MODE)) {
            dialog
                    .setMessage("Choose a launch configuration to debug"/* LauncherMessages.getString("JavaApplicationAction.Choose_a_launch_configuration_to_debug_2") */); //$NON-NLS-1$
        } else {
            dialog
                    .setMessage("Choose a launch configuration to run"/* LauncherMessages.getString("JavaApplicationAction.Choose_a_launch_configuration_to_run_3") */); //$NON-NLS-1$
        }
        dialog.setMultipleSelection(false);
        int result = dialog.open();
        labelProvider.dispose();
        if (result == MainTypeSelectionDialog.OK) {
            return (ILaunchConfiguration) dialog.getFirstResult();
        }
        return null;
    }

    /**
     * Create & return a new configuration based on the specified
     * <code>IType</code>.
     */
    protected ILaunchConfiguration createConfiguration(IType type) {
        ILaunchConfiguration config = null;
        ILaunchConfigurationWorkingCopy wc = null;
        try {
            ILaunchConfigurationType configType = getJavaLaunchConfigType();
            wc = configType.newInstance(null, getLaunchManager()
                    .generateUniqueLaunchConfigurationNameFrom(
                            type.getElementName()));
        } catch (CoreException exception) {
            reportCreatingConfiguration(exception);
            return null;
        }
        wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                type.getFullyQualifiedName());
        wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                type.getJavaProject().getElementName());
        wc.setAttribute(IDebugUIConstants.PLUGIN_ID
                + ".target_debug_perspective",
                IDebugUIConstants.PERSPECTIVE_DEFAULT);
        wc.setAttribute(
                IDebugUIConstants.PLUGIN_ID + ".target_run_perspective",
                IDebugUIConstants.PERSPECTIVE_DEFAULT);
        wc
                .setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
                        "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");

        try {
            config = wc.doSave();
        } catch (CoreException exception) {
            reportCreatingConfiguration(exception);
        }
        return config;
    }

    protected void reportCreatingConfiguration(final CoreException exception) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                ErrorDialog
                        .openError(
                                getShell(),
                                "Error Launching"/* LauncherMessages.getString("JavaApplicationLaunchShortcut.Error_Launching_1") */, "Exception" /* LauncherMessages.getString("JavaApplicationLaunchShortcut.Exception") */, exception.getStatus()); // new
                                                                                                                                                                                                                                                    // Status(IStatus.ERROR,
                                                                                                                                                                                                                                                    // JDIDebugUIPlugin.getUniqueIdentifier(),
                                                                                                                                                                                                                                                    // IStatus.ERROR,
                                                                                                                                                                                                                                                    // exception.getMessage(),
                                                                                                                                                                                                                                                    // exception));
                                                                                                                                                                                                                                                    // //$NON-NLS-1$
                                                                                                                                                                                                                                                    // //$NON-NLS-2$
            }
        });
    }

    /**
     * Returns the local java launch config type
     */
    protected ILaunchConfigurationType getJavaLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(
                IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
    }

    protected ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

    /**
     * Convenience method to get the window that owns this action's Shell.
     */
    protected Shell getShell() {
        return JDIDebugUIPlugin.getActiveWorkbenchShell();
    }

    /**
     * @see ILaunchShortcut#launch(IEditorPart, String)
     */
    public void launch(IEditorPart editor, String mode) {
        IEditorInput input = editor.getEditorInput();
        IJavaElement je = (IJavaElement) input.getAdapter(IJavaElement.class);
        if (je != null) {
            searchAndLaunch(new IJavaElement[] { je }, mode, true);
        } else {
            MessageDialog
                    .openError(
                            getShell(),
                            "Launch Failed" /* LauncherMessages.getString("JavaApplicationAction.Launch_failed_7") */, "The active editor does not contain a main type"/* LauncherMessages.getString("JavaApplicationLaunchShortcut.The_active_editor_does_not_contain_a_main_type._1") */); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    /**
     * @see ILaunchShortcut#launch(ISelection, String)
     */
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            searchAndLaunch(((IStructuredSelection) selection).toArray(), mode,
                    false);
        } else {
            MessageDialog
                    .openError(
                            getShell(),
                            "Launch Failed" /* LauncherMessages.getString("JavaApplicationAction.Launch_failed_7") */, "The selection does not contain a main type" /* LauncherMessages.getString("JavaApplicationLaunchShortcut.The_selection_does_not_contain_a_main_type._2") */); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
