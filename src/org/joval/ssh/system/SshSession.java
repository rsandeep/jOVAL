// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.ssh.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.vngx.jsch.JSch;
import org.vngx.jsch.ChannelExec;
import org.vngx.jsch.ChannelType;
import org.vngx.jsch.Session;
import org.vngx.jsch.UserInfo;
import org.vngx.jsch.exception.JSchException;

import org.joval.identity.Credential;
import org.joval.ssh.identity.SshCredential;
import org.joval.intf.identity.ICredential;
import org.joval.intf.identity.ILocked;
import org.joval.intf.io.IFilesystem;
import org.joval.intf.system.IBaseSession;
import org.joval.intf.system.IEnvironment;
import org.joval.intf.system.IProcess;
import org.joval.util.JOVALMsg;
import org.joval.util.JOVALSystem;

/**
 * A representation of an SSH session, which simply uses JSch to implement an IBaseSession.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SshSession implements IBaseSession, ILocked, UserInfo {
    protected String hostname;
    protected ICredential cred;
    protected Session session;

    public SshSession(String hostname) {
	this.hostname = hostname;
    }

    public Session getJschSession() {
	return session;
    }

    // Implement ILocked

    public boolean unlock(ICredential cred) {
	this.cred = cred;
	return true;
    }

    // Implement IBaseSession

    public boolean connect() {
	if (cred == null) {
	    return false;
	} else if (session == null) {
	    try {
		JSch jsch = JSch.getInstance();
		session = jsch.createSession(cred.getUsername(), hostname);
		session.setUserInfo(this);
	    } catch (JSchException e) {
		e.printStackTrace();
		return false;
	    }
	}
	if (session.isConnected()) {
	    return true; // already connected
	} else {
	    try {
		session.connect(3000);
		return true;
	    } catch (JSchException e) {
		e.printStackTrace();
		return false;
	    }
	}
    }

    public void disconnect() {
	if (session != null && session.isConnected()) {
	    session.disconnect();
	}
    }

    public IProcess createProcess(String command, long millis, boolean debug) throws Exception {
	ChannelExec ce = session.openChannel(ChannelType.EXEC);
	return new SshProcess(ce, command, millis, debug);
    }

    public IProcess createProcess(String command) throws Exception {
	ChannelExec ce = session.openChannel(ChannelType.EXEC);
	return new SshProcess(ce, command);
    }

    public Type getType() {
	if (connect()) {
	    BufferedReader reader = null;
	    try {
		IProcess p = createProcess("pwd");
		p.start();
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
		    if (line.startsWith("flash")) {
	 		return Type.CISCO_IOS;
		    } else if (line.startsWith("/")) {
			return Type.UNIX;
		    } else if (line.equals("")) {
			// ignore;
		    } else {
			JOVALSystem.getLogger().warn(JOVALMsg.ERROR_SSH_UNEXPECTED_RESPONSE, line);
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (reader != null) {
		    try {
			reader.close();
		    } catch (IOException e) {
		    }
		}
	    }
	}
	return Type.SSH;
    }

    // Implement UserInfo

    public String getPassphrase() {
	if (cred instanceof SshCredential) {
	    return ((SshCredential)cred).getPassphrase();
	} else {
	    return "";
	}
    }

    public String getPassword() {
	return cred.getPassword();
    }

    public boolean promptPassphrase(String message) {
	return true;
    }

    public boolean promptPassword(String message) {
	return true;
    }

    public boolean promptYesNo(String message) {
	return true;
    }

    public void showMessage(String message) {
	JOVALSystem.getLogger().info(JOVALMsg.STATUS_AUTHMESSAGE, message);
    }
}
