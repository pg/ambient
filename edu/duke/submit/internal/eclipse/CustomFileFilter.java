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
 * Created on Jun 5, 2003
 * for Duke Eclipse project
 *
 */
package edu.duke.submit.internal.eclipse;

import java.io.File;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class CustomFileFilter extends ViewerFilter {
    private String myExtension;

    public CustomFileFilter(String extension) {
        myExtension = extension;
    }

    public boolean select(Viewer viewer, Object parent, Object element) {
        return ((File) element).getName().endsWith(myExtension);
    }

    public String toString() {
        return myExtension;
    }
}