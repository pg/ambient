/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.checkin;

import edu.duke.cs.ambient.checkin.IUserNameValidator;

// TODO fill in what the precise rules are
/**
 * This IUserNameValidator implementation validates a user name based on the
 * rules for user names on Duke University's ACPUB system.
 * 
 * @since 2.0
 * @see IUserNameValidator
 * @author Marcin Dobosz
 */
public class DukeUserNameValidator extends BasicUserNameValidator {

    /*
     * (non-Javadoc)
     * 
     * @see edu.duke.cs.ambient.checkin.IUserNameValidator#isValid(java.lang.String)
     */
    public boolean isValidUserName(String userName) {
        return (userName.length() >= 2 && super.isValidUserName(userName));
    }

}
