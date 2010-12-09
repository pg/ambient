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
 * Created on Jun 7, 2003
 * for Duke Eclipse project
 *
 */
package edu.duke.submit.internal.eclipse;

import java.io.File;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author jett
 */
public class CustomAcceptAllFilter extends ViewerFilter {
    public boolean select(Viewer viewer, Object parent, Object element) {
        if (((File) element).isFile()) {
            if (((File) element).getName().startsWith(".")) {
                return false;
            }
            return true;
        }
        return false;
    }
}
