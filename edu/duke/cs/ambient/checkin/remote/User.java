package edu.duke.cs.ambient.checkin.remote;


import com.jcraft.jsch.UserInfo;


public class User implements UserInfo {
	private String myPassword;
	
	public User(String password){
		myPassword = password;
	}
	
	
	public String getPassword() {
		return myPassword;
	}

	public boolean promptYesNo(String str) {
		return true;
	}


	public String getPassphrase() {
		return null;
	}

	public boolean promptPassphrase(String message) {
		return true;
	}

	public boolean promptPassword(String message) {
		return true;
	}

	public void showMessage(String message) {
	}
}


