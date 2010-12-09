package edu.duke.cs.ambient.checkin.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @author Marcin Dobosz
 * @since 2.0
 */
public class ProjectViewerLabelProvider implements ILabelProvider {
	private static Image normal = PlatformUI.getWorkbench().getSharedImages()
			.getImage(IDE.SharedImages.IMG_OBJ_PROJECT);

	private static Image checkedin = new Desc().createImage();

	private static class Desc extends CompositeImageDescriptor {

		protected void drawCompositeImage(int width, int height) {
			ImageDescriptor im = TeamImages
					.getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR);
			drawImage(normal.getImageData(), 0, 0);
			drawImage(im.getImageData(), 10, 8);
		}

		protected Point getSize() {
			return new Point(16, 16);
		}

	}

	public Image getImage(Object element) {
		if (isShared((IProject) element))
			return checkedin;
		else
			return normal;
	}

	private boolean isShared(IProject project) {
		return RepositoryProvider.isShared(project);
	}

	public String getText(Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getName();
		}
		return "";
	}

	/**
	 * This implementation does nothing.
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * This implementation does nothing.
	 */
	public void dispose() {
	}

	/**
	 * This implementation does nothing and always returns <code>false</code>.
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * This implementation does nothing.
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}
