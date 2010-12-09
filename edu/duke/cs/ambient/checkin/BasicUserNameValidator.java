/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.checkin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This basic validator validates user names based on basic unix account name
 * rules:
 * <ul>
 * <li>Only alphanumeric characters and underscores are allowed</li>
 * <li>The name must begin with a letter</li>
 * </ul>
 * The validation is equivalent to matching the user name against a regular
 * expression of the form <code>[a-zA-Z]\w+</code>.
 * <p>
 * Additionally, the regular expression used can be defined by the client.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class BasicUserNameValidator implements IUserNameValidator {

    private Pattern pattern;

    /**
     * This is the default regular expression used for account validation (value
     * <code>[a-zA-Z]\w+</code>).
     */
    public static final String REGEX = "[a-zA-Z]\\w+";

    /**
     * Creates a basic user name validator with the default regular expression:
     * <code>[a-zA-Z]\w+</code>.
     */
    public BasicUserNameValidator() {
        this(REGEX);
    }

    /**
     * Creates a basic user name validator using the given regular expression.
     * 
     * @param regEx
     *            the regular expression to be used during validation
     */
    public BasicUserNameValidator(String regEx) {
        pattern = Pattern.compile(regEx);
    }

    /**
     * Returns <code>true</code> if userName is valid (i.e. matches the
     * regular expression used during the creation of this validator).
     * 
     * @param userName
     *            the user name to be tested
     * @return <code>true</code> if the user name is valid, <code>false</code>
     *         otherwise
     */
    public boolean isValidUserName(String userName) {
        Matcher m = pattern.matcher(userName);
        return m.matches();
    }

}
