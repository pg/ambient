package edu.duke.cs.ambient.checkin.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SSHTool {
    private JSch mySSH;

    private Session mySession;

    private Channel myChannel;

    private BufferedReader myBufferedReader;

    private OutputStream myWriter;

    public SSHTool() {
        mySSH = new JSch();
    }

    public void openSession(String user, String host) throws JSchException {
        mySession = mySSH.getSession(user, host, 22);
    }

    public BufferedReader connect(String password) throws JSchException,
            IOException {
        UserInfo ui = new User(password);
        mySession.setUserInfo(ui);

        mySession.connect();
        myChannel = mySession.openChannel("shell");
        myBufferedReader = new BufferedReader(new InputStreamReader(myChannel
                .getInputStream()));
        myWriter = myChannel.getOutputStream();
        myChannel.connect();

        setTerminal();
        return myBufferedReader;
    }

    public void sendCommand(String cmd) throws UnsupportedEncodingException,
            IOException, NullPointerException {
        cmd = cmd + "\n";
        myWriter.write(cmd.getBytes("US-ASCII"));
    }

    private void setTerminal() {
        String cmd = "setenv TERM vt100";
        try {
            sendCommand(cmd);
            sendCommand("echo the terminal is $TERM");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            myChannel.getOutputStream().close();
        } catch (Exception e) {
        }
        try {
            myChannel.disconnect();
            mySession.disconnect();
        } catch (Exception e) {
        }
        return;
    }

}
