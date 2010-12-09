/*
 * Created on Jul 10, 2005
 */
package edu.duke.cs.ambient.checkin;

/**
 * Classes implementing this interface can make (or map) a path to some resource
 * given the user name of the user to whom that resource belongs.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public interface IPathMaker {
    /**
     * Maps userName to the absolute path of a resource belonging to the user.
     * 
     * @param userName
     *            the name to be mapped
     * @return the absolute path of a resource for userName
     */
    public String makePath(String userName);
}
