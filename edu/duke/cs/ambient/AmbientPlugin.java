package edu.duke.cs.ambient;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;

import edu.duke.cs.ambient.snarfer.SnarferSettings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class AmbientPlugin extends AbstractUIPlugin {
    // The shared instance.
    private static AmbientPlugin pluginInstance;

    // Resource bundle.
    private ResourceBundle resourceBundle;

    // Settings/history used by the snarfer component
    private SnarferSettings mySnarferSettings;

    /**
     * The constructor.
     */
    public AmbientPlugin() {
        super();
        pluginInstance = this;
        try {
            resourceBundle = ResourceBundle
                    .getBundle("edu.duke.cs.ambient.AmbientPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /**
     * Returns the settings and history associated with the snarfer module.
     * 
     * @return the settings and history associated with the snarfer module.
     */
    public SnarferSettings getSnarferSettings() {
        return getDefault().mySnarferSettings;
    }

    /**
     * Returns an image descriptor for an image in path, which is relative to
     * the root directory for this plug-in.
     * 
     * @param path
     *            the relative path to the image.
     * @return an image descriptor.
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        try {
            URL installURL = getDefault().getBundle().getEntry("/");
            URL url = new URL(installURL, path);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        initSettings();
    }

    private void initSettings() {
        initSnarferSettings();
        initCheckinSettings();
    }

    private void initSnarferSettings() {
        mySnarferSettings = new SnarferSettings(getDefault().getStateLocation());
        mySnarferSettings.readSettings();
    }

    private void initCheckinSettings() {
        // TODO change how defaults are initialized, maybe a config file
        IPreferenceStore store = getPreferenceStore();
        if (!store.getBoolean(AmbientGlobals.CHECKIN_PREFS_SET)) {
            store.putValue(AmbientGlobals.CHECKIN_PREFS_SET, "true");
            store.putValue(AmbientGlobals.HOST_NAME, "godzilla.acpub.duke.edu");
            store.putValue(AmbientGlobals.USER_NAME, "");
        }
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     */
    public static AmbientPlugin getDefault() {
        return pluginInstance;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = AmbientPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    
    /**
     * Returns the current page chosen on the workbench
     */
    public static WorkbenchPage getWorkbenchPage() {
        return (WorkbenchPage) (PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage());
    }
}