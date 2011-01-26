package com.erdfelt.joakim.jsvntk;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SVN
{
    private static final Logger log = Logger.getLogger(SVN.class);
    private SVNClientManager    ourClientManager;

    public SVN() {
        // Initialize repository handling for http:// and https://
        DAVRepositoryFactory.setup();

        // Initialize repository handling for file://
        FSRepositoryFactory.setup();

        // Initialize the SVN Client Manager
        ISVNOptions svnoptions = SVNWCUtil.createDefaultOptions(false);
        ourClientManager = SVNClientManager.newInstance(svnoptions);
    }

    public String getWorkingCopyURL(File wcPath) throws SVNException {
        SVNInfo info = ourClientManager.getWCClient().doInfo(wcPath, SVNRevision.HEAD);
        return info.getURL().toString();
    }
    
    public SVNWCClient getWCClient() {
        return ourClientManager.getWCClient();
    }

    public void addFile(File path) throws SVNException {
        if (isSubversionManaged(path)) {
            log.info("Not adding already added path: " + path);
            return;
        }

        SVNWCClient wc = ourClientManager.getWCClient();
        boolean force = false;
        boolean mkdir = false;
        boolean climbUnversionedParents = true;
        SVNDepth depth = SVNDepth.IMMEDIATES;
        boolean includeIgnored = false;
        boolean makeParents = true;

        log.info("Performing svn add: " + path);
        wc.doAdd(path, force, mkdir, climbUnversionedParents, depth, includeIgnored, makeParents);
    }

    public void ensureDirAdded(File dir) throws IOException, SVNException {
        String parts[] = StringUtils.split(dir.getAbsolutePath(), SystemUtils.FILE_SEPARATOR);
        log.info("Ensure dir added " + dir.getAbsolutePath());
        for (int level = 1; level < parts.length; level++) {
            String path = File.separatorChar + StringUtils.join(parts, File.separatorChar, 0, level);
            File xdir = new File(path);
            if (!xdir.exists()) {
                log.info("Creating path: " + path);
                if (!xdir.mkdir()) {
                    throw new IOException("Can't create directory: " + xdir);
                }
                addDir(xdir);
            }
        }
    }

    public void addDir(File path) throws SVNException {
        if (isSubversionManaged(path)) {
            log.info("Not adding already added path: " + path);
            return;
        }

        SVNWCClient wc = ourClientManager.getWCClient();
        boolean force = false;
        boolean mkdir = true;
        boolean climbUnversionedParents = true;
        SVNDepth depth = SVNDepth.INFINITY;
        boolean includeIgnored = false;
        boolean makeParents = true;

        log.info("Performing svn add: " + path);
        wc.doAdd(path, force, mkdir, climbUnversionedParents, depth, includeIgnored, makeParents);
    }

    private boolean isSubversionManaged(File path) {
        SVNWCClient wc = ourClientManager.getWCClient();
        try {
            SVNInfo info = wc.doInfo(path, SVNRevision.UNDEFINED);
            return info.getRevision() != null;
        } catch (SVNException e) {
            return false;
        }
    }
}
