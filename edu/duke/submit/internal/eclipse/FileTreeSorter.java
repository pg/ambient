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
 * Created on Jun 10, 2003
 * for Duke Eclipse project
 *
 */
package edu.duke.submit.internal.eclipse;

import java.io.File;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author jett
 */
public class FileTreeSorter extends ViewerSorter {
    public int compare(Viewer viewer, Object e1, Object e2) {
        String path1 = ((File) e1).getName();
        String path2 = ((File) e2).getName();
        try {
            if (((File) e1).isFile() && ((File) e2).isDirectory()) {
                return 1;
            } else if (((File) e1).isDirectory() && ((File) e2).isFile()) {
                return -1;
            }
            String ext1 = ((String) path1).substring(((String) path1)
                    .lastIndexOf('.'));
            String ext2 = ((String) path2).substring(((String) path2)
                    .lastIndexOf('.'));
            int compareValue = ext1.compareTo(ext2);
            if (compareValue == 0) {
                return path1.compareTo(path2);
            }
            return compareValue;
        } catch (IndexOutOfBoundsException e) {
            return path1.compareTo(path2);
        }
    }
}
