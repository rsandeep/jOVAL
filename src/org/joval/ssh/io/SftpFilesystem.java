// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt

package org.joval.ssh.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.NoSuchElementException;

import org.vngx.jsch.Session;
import org.vngx.jsch.ChannelType;
import org.vngx.jsch.ChannelSftp;
import org.vngx.jsch.exception.JSchException;

import org.joval.intf.io.IFile;
import org.joval.intf.io.IFilesystem;
import org.joval.intf.io.IRandomAccess;
import org.joval.intf.system.IBaseSession;
import org.joval.intf.system.IEnvironment;
import org.joval.intf.system.IProcess;
import org.joval.intf.util.IPathRedirector;
import org.joval.intf.util.tree.INode;
import org.joval.intf.util.tree.ITreeBuilder;
import org.joval.intf.system.IEnvironment;
import org.joval.util.tree.CachingTree;
import org.joval.util.tree.Tree;
import org.joval.util.JOVALMsg;
import org.joval.util.JOVALSystem;

/**
 * An implementation of the IFilesystem interface for SSH-connected sessions.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SftpFilesystem extends CachingTree implements IFilesystem {
    final static char DELIM_CH = '/';
    final static String DELIM_STR = "/";

    private Session jschSession;
    private IBaseSession session;
    private IEnvironment env;
    private boolean autoExpand = true;
    private boolean preloaded = false;

    ChannelSftp cs;

    public SftpFilesystem(Session jschSession, IBaseSession session, IEnvironment env) {
	super();
	this.jschSession = jschSession;
	this.session = session;
	this.env = env;
    }

    public void setAutoExpand(boolean autoExpand) {
	this.autoExpand = autoExpand;
    }

    // Implement IPathRedirector

    public String getRedirect(String path) {
	return path;
    }

    // Implement methods left abstract in CachingTree

    public boolean preload() {
	if (preloaded) {
	    return true;
	}

	ITreeBuilder tree = cache.getTreeBuilder("");
	if (tree == null) {
	    tree = new Tree("", DELIM_STR);
	    cache.addTree(tree);
	}
	try {
	    IProcess p = session.createProcess("find / *");
	    p.start();
	    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    ErrorReader er = new ErrorReader(p.getErrorStream());
	    er.start();
	    String line = null;
	    while((line = br.readLine()) != null) {
		String path = line;
		if (!path.equals(getDelimiter())) { // skip the root node
		    INode node = tree.getRoot();
		    try {
			while ((path = trimToken(path, getDelimiter())) != null) {
			    node = node.getChild(getToken(path, getDelimiter()));
			}
		    } catch (UnsupportedOperationException e) {
			do {
			    node = tree.makeNode(node, getToken(path, getDelimiter()));
			} while ((path = trimToken(path, getDelimiter())) != null);
		    } catch (NoSuchElementException e) {
			do {
			    node = tree.makeNode(node, getToken(path, getDelimiter()));
			} while ((path = trimToken(path, getDelimiter())) != null);
		    }
		}
	    }
	    br.close();
	    er.join();
	    preloaded = true;
	    return true;
	} catch (Exception e) {
	    JOVALSystem.getLogger().warn(JOVALMsg.ERROR_PRECACHE);
	    JOVALSystem.getLogger().warn(JOVALSystem.getMessage(JOVALMsg.ERROR_EXCEPTION), e);
	    return false;
	}
    }

    public String getDelimiter() {
	return DELIM_STR;
    }

    public INode lookup(String path) throws NoSuchElementException {
	try {
	    IFile f = getFile(path);
	    if (f.exists()) {
		return f;
	    } else {
		throw new NoSuchElementException(path);
	    }
	} catch (IOException e) {
	    JOVALSystem.getLogger().warn(JOVALMsg.ERROR_IO, path);
	    JOVALSystem.getLogger().warn(JOVALSystem.getMessage(JOVALMsg.ERROR_EXCEPTION), e);
	    return null;
	}
    }

    // Implement IFilesystem

    public boolean connect() {
	try {
	    cs = jschSession.openChannel(ChannelType.SFTP);
	    cs.connect();
	    return true;
	} catch (JSchException e) {
	    JOVALSystem.getLogger().error(JOVALSystem.getMessage(JOVALMsg.ERROR_EXCEPTION), e);
	    return false;
	}
    }

    public void disconnect() {
	try {
	    if (cs != null && cs.isConnected()) {
		cs.disconnect();
	    }
	} catch (Throwable e) {
	    JOVALSystem.getLogger().warn(JOVALSystem.getMessage(JOVALMsg.ERROR_EXCEPTION), e);
	}
    }

    public IFile getFile(String path) throws IllegalArgumentException, IOException {
	if (env != null && autoExpand) {
	    path = env.expand(path);
	}
	path = getRedirect(path);
	if (path.length() > 0 && path.charAt(0) == DELIM_CH) {
	    try {
	        return new SftpFile(this, path);
	    } catch (JSchException e) {
		throw new IOException(e);
	    }
	} else {
	    throw new IllegalArgumentException(JOVALSystem.getMessage(JOVALMsg.ERROR_FS_LOCALPATH, path));
	}
    }

    public IFile getFile(String path, boolean vol) throws IllegalArgumentException, IOException {
	return getFile(path);
    }

    public IRandomAccess getRandomAccess(IFile file, String mode) throws IllegalArgumentException, IOException {
	return file.getRandomAccess(mode);
    }

    public IRandomAccess getRandomAccess(String path, String mode) throws IllegalArgumentException, IOException {
	return getFile(path).getRandomAccess(mode);
    }

    public InputStream getInputStream(String path) throws IllegalArgumentException, IOException {
	return getFile(path).getInputStream();
    }

    public OutputStream getOutputStream(String path) throws IllegalArgumentException, IOException {
	return getFile(path).getOutputStream(false);
    }

    public OutputStream getOutputStream(String path, boolean append) throws IllegalArgumentException, IOException {
	return getFile(path).getOutputStream(append);
    }

    // Internal

    ChannelSftp getCS() {
	return cs;
    }

    // Private

    class ErrorReader implements Runnable {
	InputStream err;
	Thread t;

	ErrorReader(InputStream err) {
	    this.err = err;
	}

	void start() {
	    t = new Thread(this);
	    t.start();
	}

	void join() throws InterruptedException {
	    t.join();
	}

	public void run() {
	    try {
		BufferedReader br = new BufferedReader(new InputStreamReader(err));
		String line = null;
		while((line = br.readLine()) != null) {
		    JOVALSystem.getLogger().warn(JOVALMsg.ERROR_PRECACHE_LINE, line);
		}
	    } catch (IOException e) {
		JOVALSystem.getLogger().warn(JOVALSystem.getMessage(JOVALMsg.ERROR_EXCEPTION), e);
	    } finally {
		try {
		    err.close();
		} catch (IOException e) {
		}
	    }
	}
    }
}
