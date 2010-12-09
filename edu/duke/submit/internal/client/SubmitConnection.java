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
 * Created on May 28, 2003
 */
package edu.duke.submit.internal.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

/**
 * @author jett
 */
public class SubmitConnection {
    public final static int SERVER_OK = 0;

    public final static int SERVER_DOWN = 1;

    public final static int CONNECTION_BROKEN = 2;

    private final boolean DEBUG = false;

    private void setUpRedirect() throws IOException {
        // TODO : fix the socket time-out issue;
        myHostMap = new HashMap();
        Socket sock = new Socket(RedirectServerName, RedirectServerPort);
        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream();
        byte[] getCommand = new byte[] { 'G', 'E', 'T', '\n' };
        out.write(getCommand);
        BufferedReader buf = new BufferedReader(new InputStreamReader(in));
        int total = Integer.parseInt(buf.readLine());
        while (total > 0) {
            String course = buf.readLine();
            String hostName = buf.readLine();
            String hostPort = buf.readLine();
            HostPair host = new HostPair(hostName, Integer.parseInt(hostPort));
            if (DEBUG) {
                System.out.println("Host " + hostName + " Port " + hostPort
                        + " course " + course);
            }
            myHostMap.put(course, host);
            total--;
        }
    }

    class HostPair {
        public String myName;

        public int myPort;

        public HostPair(String name, int port) {
            myName = name;
            myPort = port;
        }
    }

    public Object[] getCourseList() // Object[] is actually String[], i'm just
    // not wasting the time to cast all the Objects and put them in a new array
    {
        Set courseSet = myHostMap.keySet();
        Object[] courses = courseSet.toArray();
        return courses;
    }

    public int open(String course) {
        exit();
        HostPair host = (HostPair) myHostMap.get(course);

        if (host.myName.equalsIgnoreCase("DOWN")) {
            try {
                if (!mySocket.isClosed()) {
                    mySocket.close();
                }
            } catch (Exception e) {
            }
            mySocket = null;
            return SubmitConnection.SERVER_DOWN;
        }

        if (host == null)
            return SubmitConnection.CONNECTION_BROKEN;
        myServer = host.myName;
        myPort = host.myPort;

        try {
            mySocket = SSLTrustingSocketFactory.getDefault().createSocket();
            mySocket.setSoTimeout(60000);
            mySocket.connect(new InetSocketAddress(myServer, myPort));
            myInput = new DataInputStream(mySocket.getInputStream());
            myReader = new BufferedReader(new InputStreamReader(myInput));
            myOutput = new DataOutputStream(mySocket.getOutputStream());
            return SubmitConnection.SERVER_OK;
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
        return SubmitConnection.CONNECTION_BROKEN;
    }

    public boolean isOpen() {
        if (mySocket == null) {
            return false;
        }
        return mySocket.isConnected();
    }

    public void close() {
        writeString(SubmitConstants.QUIT);
    }

    public boolean writeString(String s) // returns whether writing worked or
    // not, indicating if connection is still alive
    {
        s = s + LINE_TERMINATOR;
        try {
            if (DEBUG) {
                System.out.println("writing to server " + s);
            }
            byte b[] = s.getBytes("US-ASCII");
            myOutput.write(b, 0, b.length);
            return true;
        } catch (IOException e) {
            if (DEBUG) {
                System.err.println("error writing " + s + " " + e.getMessage());
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void writeFile(File f) {
        long mySize = f.length();
        writeString(String.valueOf(mySize));
        try {
            FileInputStream myJarInput = new FileInputStream(f);
            byte[] myBuffer = new byte[1024];
            int size;
            while ((size = myJarInput.read(myBuffer)) != -1) {
                myOutput.write(myBuffer, 0, size);
            }
            myOutput.flush();
            myJarInput.close();
        } catch (FileNotFoundException e) {
            System.err.println("file not found");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    public String readServerLine() {
        String s = null;
        try {
            s = myReader.readLine();
            if (DEBUG) {
                System.out.println("server line " + s);
            }
        } catch (IOException e) {
            System.err.println("error reading from server");
            return "IOException";
        }
        // catch (NullPointerException npe)
        // {
        // System.err.println("error reading from server");
        // return "Null exception";
        // System.out.println("Reopening pipe");
        // reOpen();
        // }
        return s.trim();
    }

    public ServerResponse runCommand(Command cmd) {
        if (cmd.getCommand().equalsIgnoreCase("LIST")) {
            cmd.addConnection(this);
            return cmd.issue();
        }

        if (isOpen()) {
            cmd.addConnection(this);
            return cmd.issue();
        }
        if (DEBUG) {
            System.out.println("Command " + cmd.getCommand());
        }
        ServerResponse sr = new ServerResponse(1);
        sr.addResponse("999:Unable to connect to server, server may be down");
        return sr;
    }

    public void exit() {
        if (isOpen()) {
            try {
                // System.out.println("closing sockets");
                mySocket.close();
                // mySocket = null;
            } catch (IOException e) {
                System.err.println("error closing socket");
            }
        }
    }

    public void setRedirectServer(String name, int port) throws IOException {
        RedirectServerName = name;
        RedirectServerPort = port;
        setUpRedirect();
    }

    protected Socket mySocket;

    protected DataInputStream myInput;

    protected BufferedReader myReader;

    protected DataOutputStream myOutput;

    private String myServer;

    private int myPort;

    private HashMap myHostMap;

    private static String RedirectServerName = SubmitConstants.REDIRECT_NAME;

    private static int RedirectServerPort = SubmitConstants.REDIRECT_PORT;

    private static final String LINE_TERMINATOR = "\n";
}
