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

import java.io.*;
import org.eclipse.jface.viewers.*;

public class DirectoryFilter extends ViewerFilter {
    public boolean select(Viewer viewer, Object parent, Object element) {
        File file = (File) element;
        String name = file.getName();
        if (name.startsWith(".")) {
            return false;
        } else if (file.isDirectory()) {
            if (name.equals("CVS") || name.equals("bin")) {
                return false;
            }
            return true;
        } else if (name.endsWith("README") || name.endsWith("Makefile")) {// README
            // and
            // Makefile
            // should
            // always
            // show,
            // since
            // there's
            // always
            // a
            // directory
            // filter,
            // i'm just sticking this code here
            return true;
        } else
            return false;
    }
}
