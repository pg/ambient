/*
 * Created on May 28, 2003
 */
package edu.duke.cs.snarfer;

/**
 * This exception signifies that an error occured while building a package.
 * 
 * @author jett
 */
public class PackageException extends Exception {
    private static final long serialVersionUID = -2021760065428978984L;

    private String myMessage;

    private Throwable myCause;

    /**
     * Creates a new PackageException with the specified message.
     * 
     * @param message
     *            the message for this exception.
     */
    public PackageException(String message) {
        this(message, null);
    }

    /**
     * Creates a new PackageException with the specified message and the
     * underlying cause.
     * 
     * @param message
     *            the message for this exception.
     * @param cause
     *            the cause that caused this exception being thrown.
     */
    public PackageException(String message, Throwable cause) {
        myMessage = message;
        myCause = cause;
    }

    /**
     * Returns the message String for this PackageException.
     * 
     * @return the message String for this PackageException.
     */
    public String getMessage() {
        return myMessage
                + (getCause() == null ? "." : ": " + getCause().getMessage());
    }

    /**
     * Returns the throwable that caused this exception to be be thrown.
     * 
     * @return the throwable that caused this exception to be be thrown.
     */
    public Throwable getCause() {
        return myCause;
    }
}
