/*
 * Created on Jun 6, 2003
 */
package edu.duke.cs.ambient.snarfer;

import java.util.Date;

import edu.duke.cs.snarfer.Package;

/**
 * This class serves as a container for information about package instalations.
 * It contains information about the package, the date it was installed, and the
 * location.
 * 
 * @see edu.duke.cs.snarfer.Package
 * @author Duke Curious Team 2003
 * @version 1.0
 */
public class PackageInstallRecord {
    private Package pkg;

    private String location;

    private Date installDate;

    /**
     * Creates a new record of install information.
     * 
     * @param pkg
     *            the information about the package.
     * @param loc
     *            the location of the instalation.
     * @param date
     *            the date of the instalation.
     */
    public PackageInstallRecord(Package pkg, String loc, Date date) {
        this.pkg = pkg;
        location = loc;
        installDate = date;
    }

    public Package getPackage() {
        return pkg;
    }

    /**
     * Returns the location of the instalation of the package for this record.
     * 
     * @return the location of the instalation of the package for this record.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the date of the instalation of this package.
     * 
     * @return the date of the instalation of this package.
     */
    public Date getInstallDate() {
        return installDate;
    }

    /**
     * Returns a human-readable description of this PackageInstallRecord.
     * 
     * @return a human-readable description of this PackageInstallRecord
     */
    public String toString() {
        return pkg.getName() + " (ver: " + pkg.getVersion() + ") on "
                + installDate + " in " + location;
    }
}
