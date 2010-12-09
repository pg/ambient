package edu.duke.cs.ambient.checkin.jobs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.ui.actions.SyncAction;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

public class AmbientSyncAction extends SyncAction {
    private IResource[] selectedResources;

    public AmbientSyncAction(IResource[] resources) {
        selectedResources = resources;
    }

    public void execute() throws InvocationTargetException {
        // AMBIENT COMMENTS: This method was
        // copied and edited from the SyncAction
        // class. It works, however it is
        // unclear if it will continue to work
        // in the future.

        // First check if there is an existing
        // matching participant
        WorkspaceSynchronizeParticipant participant = (WorkspaceSynchronizeParticipant) SubscriberParticipant
                .getMatchingParticipant(WorkspaceSynchronizeParticipant.ID,
                        selectedResources);
        // If there isn't, create one and add to
        // the manager
        if (participant == null) {
            participant = new WorkspaceSynchronizeParticipant(
                    new ResourceScope(selectedResources));
            TeamUI.getSynchronizeManager().addSynchronizeParticipants(
                    new ISynchronizeParticipant[] { participant });
        }
        // AMBIENT COMMENT: here the last
        // argument was changed to null, since
        // there is no view part associated with
        // this action. Strings were changed to
        // empty because they don't really seem
        // to be needed. If problems do occur,
        // look into SyncAction#execute().
        participant.refresh(selectedResources, "", "", null);
    }
}