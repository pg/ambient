/*
 * Created on Jul 14, 2005
 */
// This file is modified from an earlier version that existed in the previous release of Ambient
package edu.duke.cs.ambient.checkin.ui.wizards;

import java.util.TreeMap;

import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

import edu.duke.cs.ambient.AmbientGlobals;

/**
 * This tree content provider to be used with the viewer in the Checkout Wizard.
 * 
 * @version 2.0
 * @author Marcin Dobosz
 */
class CvsViewerContentProvider extends BaseWorkbenchContentProvider {
    // a filter array for filtering out unwanted children of parent elements
    private final String[] myFilterReq = { AmbientGlobals.CVSROOT_FOLDER };

    /**
     * Returns <code>true</code> if element should be displayed as if it had
     * children, or <code>false</code> otherwise. <code>true</code> is
     * returned only if the name of element does not end with
     * {@link AmbientGlobals#PROJECT_EXT}, meaning that it is a folder and not
     * a project.
     * <p>
     * Note that element must be an instance of {@link ICVSRemoteResource}.
     * 
     * @param element
     *            the remote resource to be evaluated
     * @return <code>true</code> if element should have children (meaning that
     *         it is a folder), and <code>false</code> otherwise
     */
    public boolean hasChildren(Object element) {
        if (element == null) {
            return false;
        }
        String name = ((ICVSRemoteResource) element).getName();
        // only projects' names end with AmbientGlobals.PROJECT_EXT
        return !name.endsWith(AmbientGlobals.PROJECT_EXT);
    }

    /**
     * Returns an array of Objects representing the children of the given
     * parentElement. An empty array is returned if parentElement is
     * <code>null</code> or if it has no children.
     * 
     * @param parentElement
     *            the element for which children are to be returned
     * @return an array of Objects representing parentElement's children
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement == null)
            return new Object[0];
        Object[] children = super.getChildren(parentElement);
        // a map is fine because directory structures don't allow same name
        // directories
        TreeMap map = new TreeMap();
        String name = null;
        for (int k = 0; k < children.length; k++) {
            name = ((ICVSRemoteResource) children[k]).getName();
            // don't put in 'hidden' folders
            if (!name.startsWith("."))
                map.put(name, children[k]);
        }
        // filter out unwanted objects
        return filter(map, myFilterReq);
    }

    /**
     * Returns an array of Objects that consists of the value elements of the
     * given children map such that those value elements do not have associated
     * key elements which would be contained in the omit array.
     * 
     * @param children
     *            a mapping of Strings to Objects used to build the result array
     * @param omit
     *            array of keys to be omited
     * @return an array of values from children that do not have keys the same
     *         as any elements of omit
     */
    private Object[] filter(TreeMap children, String[] omit) {
        for (int k = 0; k < omit.length; k++) {
            if (children.containsKey(omit[k]))
                children.remove(omit[k]);
        }
        return children.values().toArray();
    }
}