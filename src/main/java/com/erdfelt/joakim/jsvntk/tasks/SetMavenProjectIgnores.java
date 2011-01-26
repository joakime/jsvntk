package com.erdfelt.joakim.jsvntk.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import com.erdfelt.joakim.jsvntk.SVN;
import com.erdfelt.joakim.jsvntk.Task;
import com.erdfelt.joakim.jsvntk.annotations.TaskDefault;
import com.erdfelt.joakim.jsvntk.annotations.TaskId;
import com.erdfelt.joakim.jsvntk.annotations.TaskOption;
import com.erdfelt.joakim.jsvntk.util.PathUtil;

@TaskId(id = "set-maven-project-ignores", description = "Set the svn:ignore values on directories that are also have a pom.xml file")
public class SetMavenProjectIgnores implements Task
{
    private static final Logger log = Logger.getLogger(SetMavenProjectIgnores.class);
    private boolean ignoreTemp = true;
    private String ignoreTempEntries[] = new String[] { "*~", "*.swp", "*.log", "*.patch", "*.diff" };
    private boolean ignoreEclipse = true;
    private String ignoreEclipseEntries[] = new String[] { ".project", ".classpath", ".settings", ".fbprefs", ".pmd" };
    private boolean ignoreNetbeans = true;
    private String ignoreNetbeansEntries[] = new String[] { "nbproject", "build.xml" };
    private boolean ignoreIntellij = true;
    private String ignoreIntellijEntries[] = new String[] { "*.ipr", "*.iml", "*.iws" };
    private File dir;

    public SetMavenProjectIgnores()
    {
        dir = new File(SystemUtils.USER_DIR);
    }

    @Override
    public void exec() throws Throwable
    {
        SVN svn = new SVN();
        SVNWCClient wcClient = svn.getWCClient();
        recurseSourceTree(dir, wcClient);
    }

    @TaskDefault(key = "dir")
    public File getDir()
    {
        return dir;
    }

    @TaskDefault(key = "ignore-eclipse")
    public boolean isIgnoreEclipse()
    {
        return ignoreEclipse;
    }

    @TaskDefault(key = "ignore-intellij")
    public boolean isIgnoreIntellij()
    {
        return ignoreIntellij;
    }

    @TaskDefault(key = "ignore-netbeans")
    public boolean isIgnoreNetbeans()
    {
        return ignoreNetbeans;
    }

    @TaskDefault(key = "ignore-temp")
    public boolean isIgnoreTemp()
    {
        return ignoreTemp;
    }

    private void recurseSourceTree(File dir, SVNWCClient wcClient)
    {
        for (File path : dir.listFiles())
        {
            if (path.isDirectory())
            {
                if (".svn".equals(path.getName()))
                {
                    // Skip SVN dirs
                    continue;
                }

                // recurse
                recurseSourceTree(path, wcClient);
            } else if (path.isFile() && path.getName().equals("pom.xml"))
            {
                // Identify File Properties
                setIgnores(path, wcClient);
            }
        }
    }

    @TaskOption(key = "dir", description = "Directory to set properties in")
    public void setDir(File dir)
    {
        this.dir = dir;
    }

    @TaskOption(key = "ignore-eclipse", description = "Apply ignore for Eclipse Files (.project, .classpath, etc...)")
    public void setIgnoreEclipse(boolean ignoreEclipse)
    {
        this.ignoreEclipse = ignoreEclipse;
    }

    @TaskOption(key = "ignore-intellij", description = "Ignore Intellij IDEA Files (*.ipr, *.iml, *.iws)")
    public void setIgnoreIntellij(boolean ignoreIntellij)
    {
        this.ignoreIntellij = ignoreIntellij;
    }

    @TaskOption(key = "ignore-netbeans", description = "Ignore Netbeans Files (nbproject, build, dist, build.xml, etc...)")
    public void setIgnoreNetbeans(boolean ignoreNetbeans)
    {
        this.ignoreNetbeans = ignoreNetbeans;
    }

    private void setIgnores(File path, SVNWCClient wcClient)
    {
        File projectDir = path.getParentFile();

        try
        {
            SVNPropertyData svnignores = wcClient.doGetProperty(projectDir, "svn:ignore", SVNRevision.WORKING,
                    SVNRevision.WORKING);

            Set<String> ignoredValues = getIgnoredValues(svnignores);

            ignoredValues.add("target");
            ignoredValues.add("build");
            ignoredValues.add("bin");

            addIfEnabled(ignoredValues, ignoreTemp, ignoreTempEntries);
            
            if(ignoreEclipse) {
            	// Special case - don't add eclipse project ignores on PDE Plugin Projects
            	if(isPdePluginProject(projectDir)) {
            		log.warn("PDE Project: (not ignoring eclipse files): " + PathUtil.relative(this.dir, projectDir));
            	} else {
            		addIfEnabled(ignoredValues, ignoreEclipse, ignoreEclipseEntries);
            	}
            }
            
            addIfEnabled(ignoredValues, ignoreNetbeans, ignoreNetbeansEntries);
            addIfEnabled(ignoredValues, ignoreIntellij, ignoreIntellijEntries);

            SVNPropertyValue value = toPropertyValue(ignoredValues);

            log.info(String.format("Setting svn:ignore on %s", PathUtil.relative(this.dir, projectDir)));
            wcClient.doSetProperty(projectDir, "svn:ignore", value, false, SVNDepth.EMPTY, null, null);
        } catch (SVNException e)
        {
            log.warn("Unable to get svn:ignores for dir: " + projectDir, e);
        }
    }

	private boolean isPdePluginProject(File path) {
		File eclipseProjFile = new File(path, ".project");
		if(eclipseProjFile.exists() == false) {
			return false;
		}
		try {
			String eclipseProj = FileUtils.readFileToString(eclipseProjFile);
			return ( eclipseProj.contains("org.eclipse.pde.PluginNature") 
				||	 eclipseProj.contains("org.eclipse.pde.FeatureNature")
				||   eclipseProj.contains("org.eclipse.ode.UpdateSiteNature"));
		} catch (IOException e) {
			log.warn("Unable to read eclipse .project file: " + eclipseProjFile, e);
		}
		return false;
	}

	private void addIfEnabled(Set<String> values, boolean enabled, String[] entries)
    {
        if (enabled)
        {
            for (String entry : entries)
            {
                values.add(entry);
            }
        }
    }

    private SVNPropertyValue toPropertyValue(Set<String> entries)
    {
        StringBuffer value = new StringBuffer();
        for (String entry : entries)
        {
            value.append(entry).append(SystemUtils.LINE_SEPARATOR);
        }
        return SVNPropertyValue.create(value.toString());
    }

    private Set<String> getIgnoredValues(SVNPropertyData svnignores)
    {
        Set<String> values = new TreeSet<String>(new SvnIgnoresComparator());

        if (svnignores == null)
        {
            return values;
        }

        String[] entries = StringUtils.split(svnignores.getValue().getString(), SystemUtils.LINE_SEPARATOR);

        for (String entry : entries)
        {
            values.add(entry);
        }

        return values;
    }

    @TaskOption(key = "ignore-temp", description = "Apply ignore for Temporary Files (*.swp, *.diff, etc...)")
    public void setIgnoreTemp(boolean ignoreTemp)
    {
        this.ignoreTemp = ignoreTemp;
    }

    class SvnIgnoresComparator implements Comparator<String>
    {
        @Override
        public int compare(String o1, String o2)
        {
            return o1.compareTo(o2);
        }
    }
}
