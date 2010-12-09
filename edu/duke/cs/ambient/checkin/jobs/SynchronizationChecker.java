/*
 * Created on Jul 14, 2005
 */
package edu.duke.cs.ambient.checkin.jobs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;

/**
 * This class calculates the bitwise sum of the synchronization state of all
 * elements within a given resource. It is meant to be used on projects to
 * determine the overall state of synchronization of a project and not just
 * individual elements.
 * 
 * @since 2.0
 * @author Marcin Dobosz
 */
public class SynchronizationChecker implements IRunnableWithProgress {

    private Subscriber mySubscriber;

    private IResource myResource;

    private int myResult;

    private ArrayList conflictingSyncInfos;

    // This flag gets raised if some exception occurs during the process of
    // calculating the synchronization state. This will make getStatae throw a
    // TeamException
    private boolean errorFlag;

    // ////////////////////////////
    // CONSTRUCTOR

    /**
     * Creates a new SynchronizationChecker that will use the given subscriber
     * to calculate the state of the given resource.
     * 
     * @param subsriber
     *            a Subscriber that will be used in calculating the
     *            synchronization state of resource
     * @param resource
     *            the resource whose synchronization state needs to be
     *            calculated
     */
    public SynchronizationChecker(Subscriber subsriber, IResource resource) {
        mySubscriber = subsriber;
        myResource = resource;
        conflictingSyncInfos = new ArrayList();
    }

    // ////////////////////////////
    // RUN FUNCTIONALITY

    /**
     * Runs by recursively calculating the synchronization state of the resource
     * used during the construction of this object and all of its children.
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        myResult = 0;
        errorFlag = false;
        try {
            monitor.beginTask("Calculating synchronization state ...", 1000);
            // refresh the sync state as deep as will go
            mySubscriber.refresh(new IResource[] { myResource },
                    IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor,
                            800));

            // recurse sync state
            myResult = recurseSynchrozizationState(mySubscriber, myResource,
                    new SubProgressMonitor(monitor, 200));
        } catch (TeamException e) {
            // catch a synchronization problem and raise the error flag
            errorFlag = true;
        } finally {
            monitor.done();
        }
    }

    /**
     * This methods recursively calculates the synchronization state of the
     * given resource and all of its descendents.
     * 
     * @param subscriber
     *            the Subscriber object that provides synchronization
     *            information
     * @param resource
     *            the resource to be evaluated
     * @param monitor
     *            a progress monitor
     * @return the bitwise sum of the synchronization states of resource and all
     *         of its descendents
     * @throws TeamException
     *             if a problem occurs during synchronization
     */
    private int recurseSynchrozizationState(Subscriber subscriber,
            IResource resource, IProgressMonitor monitor) throws TeamException {
        int kind = 0;
        IResource[] children = subscriber.members(resource);
        monitor.beginTask("Evaluating " + resource.getName(),
                children.length + 1);
        // get the sync info for resource
        SyncInfo info = subscriber.getSyncInfo(resource);
        // might be null if subscriber doesn't care about it (for example,
        // binaries)
        if (info != null) {
            kind = info.getKind();
            if ((kind & SyncInfo.CONFLICTING) != 0) {
                conflictingSyncInfos.add(info);
            }
        }
        monitor.worked(1);

        // ... and all of its children
        for (int i = 0; i < children.length; i++) {
            monitor.subTask("Evaluating " + children[i].getName());
            kind |= recurseSynchrozizationState(subscriber, children[i],
                    new SubProgressMonitor(monitor, 1));
        }
        monitor.done();
        return kind;
    }

    // ///////////////////////////////////
    // RESULT ACCESS METHODS

    /**
     * Returns the bitwise sum of the synchronization states of all elements of
     * the synchronized resource. The returned int has the same bits as the
     * constants in SyncInfo.
     * 
     * @return the bitwise sum of all the synchronization states of all elements
     *         of the synchronized resource
     * @throws TeamException
     *             if a problem occurs while calculating the synchronization
     *             state
     */
    public int getState() throws TeamException {
        if (errorFlag) {
            throw new TeamException(
                    "An error occured while calculating the synchronization state.");
        }
        return myResult;
    }

    public ArrayList getConflictingSyncInfos() throws TeamException {
        if (errorFlag) {
            throw new TeamException(
                    "An error occured while calculating the synchronization state.");
        }
        return conflictingSyncInfos;
    }

}
