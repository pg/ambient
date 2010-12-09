/*******************************************************************************
 * Copyright (c) 2004 Duke University All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Common
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 ******************************************************************************/
package edu.duke.cs.ambient.checkin.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.operations.CheckoutSingleProjectOperation;

public class AmbientCheckoutOperation extends CheckoutSingleProjectOperation {
    public AmbientCheckoutOperation(IProject proj, ICVSRemoteFolder target) {
        super(null, target, proj, null, false);
    }

    public boolean needsPromptForOverwrite(IProject project) {
        return false;
    }
}
