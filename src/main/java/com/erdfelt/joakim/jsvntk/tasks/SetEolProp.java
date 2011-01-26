package com.erdfelt.joakim.jsvntk.tasks;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import com.erdfelt.joakim.jsvntk.SVN;
import com.erdfelt.joakim.jsvntk.Task;
import com.erdfelt.joakim.jsvntk.annotations.TaskDefault;
import com.erdfelt.joakim.jsvntk.annotations.TaskId;
import com.erdfelt.joakim.jsvntk.annotations.TaskOption;
import com.erdfelt.joakim.jsvntk.util.PathUtil;

@TaskId(id = "set-eol-prop", description = "Set the svn:eol-style property on files that need it throughout the working copy.")
public class SetEolProp implements Task {
	private static final Logger log = Logger.getLogger(SetEolProp.class);
	private File dir;
	private String[] nativeMatches = {
			// C Development
			"*.c", "*.cpp", "*.h",
			// Java Development
			"*.java", "*.properties", 
			// Maven Files
			"*.pom", "*.md5", "*.sha1", 
			// Eclipse
			".project", ".classpath", "*.config", "*.prefs", "*.options",
			"*.target",
			// XML
			"*.xml", "*.xsd", "*.dtd", "*.svg", "*.xsl", "*.xslt",
			// SOA
			"*.wsdl", "*.episode", "*.ftl", 
			// Web
			"*.js", "*.jsp", "*.html", "*.htm", "*.css",
			// Text
			"*.csv", "*.txt", "README", "CHANGELOG", "LICENSE"
			};
	private String[] crlfMatches = { "*.bat", "*.cmd", "*.dsp", "*.dsw" };
	private String[] lfMatches = { "*.sh" };

	public SetEolProp() {
		this.dir = new File(SystemUtils.USER_DIR);
	}

	@Override
	public void exec() throws Throwable {
		SVN svn = new SVN();
		SVNWCClient wcClient = svn.getWCClient();
		recurseSourceTree(dir, wcClient);
	}

	private void recurseSourceTree(File dir, SVNWCClient wcClient) {
		for (File path : dir.listFiles()) {
			if (path.isDirectory()) {
				if (".svn".equals(path.getName())) {
					// Skip SVN dirs
					continue;
				}

				// recurse
				recurseSourceTree(path, wcClient);
			} else if (path.isFile()) {
				// Identify File Properties
				setProperty(path, wcClient);
			}
		}
	}

	@TaskOption(key = "dir", description = "Directory to set properties in")
	public void setDir(File dir) {
		this.dir = dir;
	}

	@TaskDefault(key = "dir")
	public File getDir() {
		return dir;
	}

	private void setProperty(File path, SVNWCClient wcClient) {
		if (matches(path.getName(), nativeMatches)) {
			setProperty(wcClient, path, "svn:eol-style", "native");
			return;
		}

		if (matches(path.getName(), crlfMatches)) {
			setProperty(wcClient, path, "svn:eol-style", "crlf");
			return;
		}

		if (matches(path.getName(), lfMatches)) {
			setProperty(wcClient, path, "svn:eol-style", "lf");
			return;
		}
	}

	private boolean matches(String name, String[] patterns) {
		for (String pattern : patterns) {
			if (FilenameUtils.wildcardMatch(name, pattern)) {
				return true;
			}
		}
		return false;
	}

	private void setProperty(SVNWCClient wcClient, File path, String propName,
			String value) {
		boolean skipChecks = false;
		try {
			log.info(String.format("Setting [%s] on %s", propName, PathUtil
					.relative(dir, path)));
			wcClient.doSetProperty(path, propName, SVNPropertyValue
					.create(value), skipChecks, SVNDepth.EMPTY, null, null);
		} catch (SVNException e) {
			log.warn("Unable to set the property [" + propName + "] on "
					+ path.getAbsolutePath(), e);
		}
	}
}
