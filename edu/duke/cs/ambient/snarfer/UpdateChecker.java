/*
 * Created on Jun 6, 2003
 */
package edu.duke.cs.ambient.snarfer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import edu.duke.cs.ambient.AmbientPlugin;
import edu.duke.cs.ambient.snarfer.PackageInstallRecord;
import edu.duke.cs.snarfer.PackageException;
import edu.duke.cs.snarfer.Package;
import edu.duke.cs.snarfer.PackageSite;

/**
 * UpdateChecker checks for available updates to installed projects. This
 * runnable should first be run
 */
public class UpdateChecker implements IRunnableWithProgress {
    private ArrayList mySiteURLs;

    private ArrayList myHistory;

    private Map updates = null, siteMap = null;

    /**
     * Creates a new UpdateChecker, which will use the provided URLs and history
     * as a reference point.
     * 
     * @param urls
     *            the URLs of sites the user is subscribed to
     * @param history
     *            a collection of {@link PackageInstallRecord}objects
     *            containing information about previous installations.
     */
    public UpdateChecker(Collection urls, Collection history) {
        mySiteURLs = new ArrayList(urls);
        myHistory = new ArrayList(history);
    }

    private static boolean less(Package a, Package b) {
        return Package.versionCompare(a.getVersion(), b.getVersion()) < 0;
    }

    /**
     * Running this runnbale will populate the maps returned by
     * {@link UpdateChecker#getSiteMap()}and {@link UpdateChecker#getUpdates()}
     * with possible update information.
     */
    public void run(IProgressMonitor monitor) {
        int i, j;
        monitor.beginTask("Contacting project sites...", mySiteURLs.size());
        Map pkgMap = new HashMap();
        siteMap = new HashMap();
        for (i = 0; i < mySiteURLs.size(); i++) {
            try {
                monitor.subTask("Contacting " + mySiteURLs.get(i));
                PackageSite site = AmbientPlugin.getDefault()
                        .getSnarferSettings().getSite((URL) mySiteURLs.get(i));
                for (j = 0; j < site.getPackageCount(); j++) {
                    Package pkg = site.getPackage(j);
                    siteMap.put(pkg, site);
                    Package saved = (Package) pkgMap.get(pkg.getName());
                    if (saved == null || less(saved, pkg))
                        pkgMap.put(pkg.getName(), pkg);
                }
            } catch (PackageException e) {
            }
            monitor.worked(1);
        }
        updates = new HashMap();
        for (i = 0; i < myHistory.size(); i++) {
            PackageInstallRecord record = (PackageInstallRecord) myHistory
                    .get(i);
            Package pkg = (Package) pkgMap.get(record.getPackage().getName());
            if (pkg == null)
                continue;
            if (less(record.getPackage(), pkg)) {
                Package[] infos = (Package[]) updates.get(pkg.getName());
                if (infos == null)
                    updates.put(pkg.getName(), new Package[] {
                            record.getPackage(), pkg });
                else if (less(infos[0], record.getPackage()))
                    infos[0] = record.getPackage();
            } else
                updates.remove(pkg.getName());
        }
        monitor.done();
    }

    /**
     * Returns a mapping of package names to arrays of 2 {@link Package}
     * objects. The first of these objects is the information about the existing
     * installation, the other is information about the most recent new version
     * of the package.
     * 
     * @return a mapping of package names to available update data, or
     *         <code>null</code> if
     *         {@link UpdateChecker#run(IProgressMonitor)} has not been run yet
     */
    public Map getUpdates() {
        return updates;
    }

    /**
     * Returns a mapping of {@link Package} objects to {@link PackageSite}
     * objects.
     * 
     * @return a mapping of {@link Package} objects to {@link PackageSite}
     *         objects, , or <code>null</code> if
     *         {@link UpdateChecker#run(IProgressMonitor)} has not been run yet
     */
    public Map getSiteMap() {
        return siteMap;
    }
}
