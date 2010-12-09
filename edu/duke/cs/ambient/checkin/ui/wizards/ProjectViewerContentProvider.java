package edu.duke.cs.ambient.checkin.ui.wizards;

import java.util.ArrayList;

import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * @author Marcin Dobosz
 * @since 2.0
 */
public class ProjectViewerContentProvider extends WorkbenchContentProvider {
    public Object[] getChildren(Object o) {
        try {
            return ((ArrayList) o).toArray();
        } catch (Exception e) {
            return new Object[0];
        }
    }
}
