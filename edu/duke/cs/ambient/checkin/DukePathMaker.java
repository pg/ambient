/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.checkin;

// TODO make sure path in description is valid.

/**
 * This implementation of IPathMaker creates paths relevent to the context of
 * Computer Science courses at Duke University. It maps a user name USER to a
 * path <code>/afs/acpub/users/U/S/USER/cvs</code>.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class DukePathMaker implements IPathMaker {

    // TODO change how this get initialized/customized
    private static final String ERROR = "indeterminate path";

    private static final String ROOT = "/afs/acpub/users/";

    private static final String SEPARATOR = "/";

    private static final String CVS_FOLDER = "myCVS";

    /**
     * Matches the path that should be used in
     */
    public String makePath(String userName) {
        if (userName == null || userName.length() < 2)
            return ERROR;
        return ROOT + userName.charAt(0) + SEPARATOR + userName.charAt(1)
                + SEPARATOR + userName + SEPARATOR + CVS_FOLDER;
    }

}
