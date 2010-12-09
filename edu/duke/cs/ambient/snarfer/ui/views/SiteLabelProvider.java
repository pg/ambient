/*
 * Created on May 29, 2003
 */
package edu.duke.cs.ambient.snarfer.ui.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import edu.duke.cs.ambient.snarfer.ui.views.SiteContentProvider;
import edu.duke.cs.snarfer.Package;

/**
 * This class provides the labels and images for the tree display of available
 * sites and packages to be used in the Snarf Browser view.
 * 
 * @author jett
 */
public class SiteLabelProvider extends LabelProvider {
    /**
     * Creates an new SiteLabelProvider.
     */
    public SiteLabelProvider() {
        super();
    }

    /**
     * This implementation of the getImage(Object) method returns a "folder"
     * image for category objects, an "element" image for {@link Package}
     * objects, and null otherwise.
     */
    public Image getImage(Object element) {
        if (element instanceof SiteContentProvider.Category) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJ_FOLDER);
        } else if (element instanceof Package) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJ_ELEMENT);
        } else
            return null;
    }

    /**
     * This implementation returns the appropriate text for the elements in the
     * snarf browser view.
     */
    public String getText(Object element) {
        if (element instanceof SiteContentProvider.Category) {
            return ((SiteContentProvider.Category) element).getName();
        } else if (element instanceof Package) {
            Package info = (Package) element;
            return info.getName() + " (" + info.getVersion() + ")";
        } else
            return null;
    }
}
