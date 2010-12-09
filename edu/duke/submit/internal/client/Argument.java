/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
 *******************************************************************************/
package edu.duke.submit.internal.client;

/**
 * The Argument class is for use primarily with/by the ArgumentParser class. It
 * serves has a holder for two basic pieces of information about a command line
 * argument: the flag that might be found and the number of subsequent arguments
 * that this flag requires. Therefore, the constructor takes both of these
 * pieces of information, and the only two methods are the get functions for
 * this data.
 * 
 * @author sbh4@duke.edu
 */

public class Argument {
    private int myNeededArgs;

    private String myFullFlag;

    /**
     * @param flag
     *            is the anticipated flag for this argument.
     * @param num
     *            is the number of subsequent arguments needed for this flag.
     */
    public Argument(String flag, int num) {
        myNeededArgs = num;
        myFullFlag = flag;
    }

    /**
     * @return the anticipated flag of this argument.
     */
    public String getFlag() {
        return myFullFlag;
    }

    /**
     * @return the number of subsequent arguments that this flag takes.
     */
    public int getNumArgs() {
        return myNeededArgs;
    }

}
