/*
 * Created on Jul 14, 2005
 */
// This class is a modified version of a file that was in the previous release of Ambient.
package edu.duke.cs.ambient.checkin.ui.wizards;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import edu.duke.cs.ambient.AmbientGlobals;

/**
 * This ILabalProvider implementation is used in the
 * {@link edu.duke.cs.ambient.checkin.ui.wizards.CheckoutWizard checkout wizard}
 * to provide labels and images to elements of the remote project tree viewer.
 * 
 * @version 2.0
 * @author Marcin Dobosz
 */
public class CvsViewerLabelProvider implements ILabelProvider {

    /**
     * Provides images for objects that are an instance of
     * {@link ICVSRemoteResource}. If the name of element ends with
     * {@link AmbientGlobals#PROJECT_EXT}, then element is considered a project
     * and a project image is returned, otherwise a folder image is returned.
     * 
     * @param element
     *            must be an instance of {@link ICVSRemoteResource}. The
     *            element for which the imagae is to be provided.
     * @return returns a project or folder image.
     */
    public Image getImage(Object element) {
        String name = ((ICVSRemoteResource) element).getName();

        if (name.endsWith(AmbientGlobals.PROJECT_EXT)) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    IDE.SharedImages.IMG_OBJ_PROJECT);
        } else {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJ_FOLDER);
        }
    }

    /**
     * Returns the text label for element. Element must be an instance of
     * {@link ICVSRemoteResource}. If the name of element ends with
     * {@link AmbientGlobals#PROJECT_EXT}then only the beggining (without the
     * suffix) gets returned, otherwise the entire name of element is returned.
     * 
     * @param element
     *            an instance of {@link ICVSRemoteResource}.
     * @return the text label for element.
     */
    public String getText(Object element) {
        String name = ((ICVSRemoteResource) element).getName();
        try {
            if (name.endsWith(AmbientGlobals.PROJECT_EXT)) {
                return name.split(AmbientGlobals.PROJECT_EXT)[0];
            } else {
                return name;
            }
        } catch (Exception e) {
            return name;
        }
    }

    /**
     * This implementation does nothing.
     */
    public void addListener(ILabelProviderListener listener) {
        // do nothing
    }

    /**
     * This implementation does nothing.
     */
    public void dispose() {
        // do nothing, since we're using somebody else's images.
    }

    /**
     * This implementation does nothing and always returns <code>false</code>.
     */
    public boolean isLabelProperty(Object element, String property) {
        // do nothing
        return false;
    }

    /**
     * This implementation does nothing.
     */
    public void removeListener(ILabelProviderListener listener) {
        // do nothing
    }

}