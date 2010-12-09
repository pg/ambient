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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class CustomOrFilter extends ViewerFilter {
    private ViewerFilter myFirstFilter;

    private ViewerFilter mySecondFilter;

    public CustomOrFilter(ViewerFilter filterOne, ViewerFilter filterTwo) {
        myFirstFilter = filterOne;
        mySecondFilter = filterTwo;
    }

    public boolean select(Viewer viewer, Object parent, Object element) {
        return myFirstFilter.select(viewer, parent, element)
                || mySecondFilter.select(viewer, parent, element);
    }
}