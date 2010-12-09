/*******************************************************************************
 * Copyright (c) 2004 Duke University
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.cs.duke.edu/csed/ambient/copyright.html
 * 
 *******************************************************************************/
/*
 * Created on Jun 2, 2003
 **/

package edu.duke.submit.internal.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * The purpose of this class is to parse arguments given at the command line (or
 * a manually generated arguments passed by a wrapper). The model for this
 * parser uses the standard conventions. Flags can take the form "-f" or
 * "--fullword" when identifying the proper flag. This class will take an array
 * of Strings and will extract the appropriate information for each flag (as
 * long as the proper information is provided).
 * 
 * @author sbh4@duke.edu
 */

public class ArgumentParser {
    HashMap myExpectedMap;

    HashMap myMap;

    ArrayList myInvalidTags;

    ArrayList myExtras;

    /**
     * @param expected
     *            is an array of expected Arguments that this parser will look
     *            for when parsing.
     * @param given
     *            is an array of command line arguments that have been given.
     */
    public ArgumentParser(Argument[] expected, String[] given) {
        myExtras = new ArrayList(); // Place to store extra stuff.
        myInvalidTags = new ArrayList();// Place to store garbarge arguments if
        // there are any
        myMap = new HashMap(); // Maps flags to subsequent arguments
        myExpectedMap = new HashMap(); // Maps flags to number of expected
        // arguments

        for (int k = 0; k < expected.length; k++) // generates the expectedMap
        // & sets myMap to null
        {
            Argument a = expected[k];
            myExpectedMap.put(a.getFlag(), new Integer(a.getNumArgs()));
            myMap.put(a.getFlag(), null);
        }

        for (int k = 0; k < given.length; k++) // loop through the given
        // arguments
        {
            String arg = given[k];
            if (arg.indexOf("--") == 0) // if the flag begins with "--"
            {
                arg = arg.substring(2); // chop it off
                Set s = myMap.keySet(); // get my keys
                if (s.contains(arg)) // if I have a key for this flag
                {
                    ArrayList args = new ArrayList();
                    int r;
                    for (r = 1; r < ((Integer) myExpectedMap.get(arg))
                            .intValue() + 1; r++) // get all the needed
                    // subsequent arguments for
                    // this flag
                    {
                        if (given[k + r].charAt(0) == '-') // if we run into
                        // another flag,
                        // then we don't
                        // have enough args
                        // for this flag.
                        {
                            System.err
                                    .println("Insufficient Arguments for flag: "
                                            + arg);
                            r--;
                            break; // print error and quit.
                        }
                        args.add(given[k + r]); // add argument for this flag
                    }
                    myMap.put(arg, args); // put all the arguments to this
                    // flag
                    k += r - 1;
                } else // if this flag doesn't have a key, then it's invalid.
                {
                    Invalid(arg);
                }

            } else if (arg.indexOf("-") == 0) // if this is an abbreviated
            // flag
            {
                arg = arg.substring(1); // chop off the dash
                Set s = myMap.keySet(); // get my keys
                Object[] keys = s.toArray();

                int j;
                for (j = 0; j < keys.length; j++) // find my matching flag
                {
                    if (((String) keys[j]).charAt(0) == arg.charAt(0)) // if
                    // the
                    // first
                    // letter
                    // matches,
                    // then
                    // the
                    // flag
                    // matches.
                    {
                        ArrayList args = new ArrayList();
                        int r;
                        for (r = 1; r < ((Integer) myExpectedMap.get(keys[j]))
                                .intValue() + 1; r++) {
                            if (given[k + r].charAt(0) == '-') // if we run
                            // into another
                            // flag, then we
                            // don't have
                            // enough args
                            // for this
                            // flag.
                            {
                                System.out
                                        .println("Insufficient Arguments for flag: "
                                                + keys[j]);
                                r--;
                                break;
                            }
                            args.add(given[k + r]);

                        }
                        myMap.put(keys[j], args);
                        k += r - 1;
                        break;
                    }
                }
                if (j == keys.length) { // if we couldn't find a matching flag,
                    // then this is invalid.
                    Invalid(arg);
                }
            } else {
                myExtras.add(arg);
            }
        }

    }

    /**
     * Prints out an error message that an invalid flag was given.
     * 
     * @param flag
     *            holds the value of the invalid flag
     */
    private void Invalid(String flag) {
        System.out.println("\"" + flag + "\" is an invalid tag.");
        myInvalidTags.add(flag);
    }

    /**
     * @param key
     *            is the value of the key that you wish to know if it was
     *            specified on the command line.
     * @return Returns true if that key was specified. A key is specified if it
     *         was typed on the command line and sufficient subsequent arguments
     *         were given.
     */

    public boolean isSpecified(String key) {
        if (myMap.get(key) != null) {
            ArrayList args = ((ArrayList) myMap.get(key));
            Integer i = ((Integer) myExpectedMap.get(key));
            if (args.size() == i.intValue())
                return true;
        }
        return false;
    }

    /**
     * @return Returns any arguments that were not matched with a flag. They are
     *         returned in the same order in which they were received.
     */
    public String[] getExtras() {
        String[] retArgs = new String[myExtras.size()]; // convert to an array
        for (int k = 0; k < retArgs.length; k++) {
            retArgs[k] = (String) myExtras.get(k);
        }
        return retArgs;
    }

    /**
     * @return Returns all of the arguments that were specified with this flag.
     *         The flag must be in its full word form.
     */
    public String[] getInfo(String key) {
        ArrayList info = (ArrayList) myMap.get(key);

        String[] retArgs = new String[info.size()]; // convert to an array
        for (int k = 0; k < retArgs.length; k++) {
            retArgs[k] = (String) info.get(k);
        }
        return retArgs;
    }

    /**
     * @return Returns all of the invalid flags that were specified.
     */
    public String[] getInvalids() {
        String[] retArgs = new String[myInvalidTags.size()]; // convert to an
        // array
        for (int k = 0; k < retArgs.length; k++) {
            retArgs[k] = (String) myInvalidTags.get(k);
        }
        return retArgs;
    }

}
