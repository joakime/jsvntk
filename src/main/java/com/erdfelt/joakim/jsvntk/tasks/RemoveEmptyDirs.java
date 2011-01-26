package com.erdfelt.joakim.jsvntk.tasks;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import com.erdfelt.joakim.jsvntk.SVN;
import com.erdfelt.joakim.jsvntk.Task;
import com.erdfelt.joakim.jsvntk.annotations.TaskDefault;
import com.erdfelt.joakim.jsvntk.annotations.TaskId;
import com.erdfelt.joakim.jsvntk.annotations.TaskOption;
import com.erdfelt.joakim.jsvntk.util.PathUtil;

@TaskId(id = "rm-empty-dirs", description = "Remove empty directories throughout the working copy.")
public class RemoveEmptyDirs implements Task {
	private static final Logger log = Logger.getLogger(RemoveEmptyDirs.class);
	private File dir;
	private NotSvnFilenameFilter filter = new NotSvnFilenameFilter();

	class NotSvnFilenameFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory() && (".svn".equals(pathname.getName()))) {
				return false;
			}
			return true;
		}
	}

	public RemoveEmptyDirs() {
		this.dir = new File(SystemUtils.USER_DIR);
	}

	@Override
	public void exec() throws Throwable {
		SVN svn = new SVN();
		SVNWCClient wcClient = svn.getWCClient();
		recurseSourceTree(dir, wcClient);
	}

	private void recurseSourceTree(File dir, SVNWCClient wcClient) {
		File paths[] = dir.listFiles(filter);
		if(paths.length == 0) {
			// issue svn rm
			issueSvnRm(wcClient, dir);
		} else {
			for (File path : dir.listFiles(filter)) {
				if (path.isDirectory()) {
					// recurse
					recurseSourceTree(path, wcClient);
				}
			}
		}
	}

	@TaskOption(key = "dir", description = "Directory to remove empty dirs from")
	public void setDir(File dir) {
		this.dir = dir;
	}

	@TaskDefault(key = "dir")
	public File getDir() {
		return dir;
	}
	
	private void issueSvnRm(SVNWCClient wcClient, File path) {
		boolean force = true;
		boolean dryRun = false;
		try {
			log.info(String.format("SVN Remove on %s",
					PathUtil.relative(dir, path)));
			wcClient.doDelete(path, force, dryRun);
		} catch (SVNException e) {
			log.warn("Unable to remove the dir: " + path, e);
		}
	}
}
