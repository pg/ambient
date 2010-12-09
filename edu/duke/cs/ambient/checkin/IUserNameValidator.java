/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.checkin;

/**
 * Classes implementing this interface are capable of validating if a user name
 * is a valid one based on some criteria.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public interface IUserNameValidator {

    /**
     * Returns <code>true</code> if userName is a valid user name and
     * <code>false</code> otherwise.
     * 
     * @param userName
     *            the user name to be validated
     * @return <code>true</code> if userName is a valid user name and
     *         <code>false</code> otherwise
     */
    public boolean isValidUserName(String userName);

}
