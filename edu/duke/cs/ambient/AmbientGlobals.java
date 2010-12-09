/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
 *******************************************************************************/
package edu.duke.cs.ambient;

import org.eclipse.core.resources.IProjectDescription;

public interface AmbientGlobals {

    // CVS stuff
    public final static String PROJECT_EXT = IProjectDescription.DESCRIPTION_FILE_NAME;

    // the basic cvs root folder that is present in every cvs repository but
    // which we don't want the user to see
    public static final String CVSROOT_FOLDER = "CVSROOT";

    public static final String CVS_CONN_TYPE_PROPERTY = "connection";

    public static final String CVS_CONN_USER_PROPERTY = "user";

    public static final String CVS_CONN_HOST_PROPERTY = "host";

    public static final String CVS_CONN_ROOT_PROPERTY = "root";

    public static final String CVS_CONN_EXTSSH_TYPE = "extssh";

    // //////////////////////////////////
    // CHECKIN/CHECKOUT PREFERENCE CONSTANTS

    public static final String HOST_NAME = "edu.duke.cs.ambient.prefs.checkin.host";

    public static final String USER_NAME = "edu.duke.cs.ambient.prefs.checkin.user";

    public static final String CHECKIN_PREFS_SET = "edu.duke.cs.ambient.prefs.checkin.set";

    // ///////////////////////////////////
    // SUBMIT PREFERENCE CONSTANTS

    public static final String P_HOST = "edu.duke.cs.ambient.prefs.submit.host";

    public static final String P_PORT = "edu.duke.cs.ambient.prefs.submit.port";

}
