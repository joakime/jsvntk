package com.erdfelt.joakim.jsvntk.tasks;

import java.io.File;

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
import com.erdfelt.joakim.jsvntk.svn.SinglePathPropertiesHandler;

@TaskId(id = "copy-props", description = "Copy properties from one subversion working copy to another subversion working copy.")
public class CopyPropertiesTask implements Task
{
    private static final Logger log = Logger.getLogger(CopyPropertiesTask.class);
    private File source;
    private File dest;
    private boolean merge = false;

    public CopyPropertiesTask()
    {
        this.source = new File(SystemUtils.USER_DIR);
        this.dest = null;
        this.merge = false;
    }

    @Override
    public void exec() throws Throwable
    {
        log.info("Source: " + source);
        log.info("Dest: " + dest);
        log.info("Merge: " + merge);

        SVN svn = new SVN();
        SVNWCClient wcClient = svn.getWCClient();
        recursiveWalkSourceTree(source, wcClient);
    }

    private void recursiveWalkSourceTree(File dir, SVNWCClient wcClient)
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

                // Identify Directory Properties
                copyProperties(path, wcClient);

                // recurse
                recursiveWalkSourceTree(path, wcClient);
            } else if (path.isFile())
            {
                // Identify File Properties
                copyProperties(path, wcClient);
            }
        }
    }

    private void copyProperties(File path, SVNWCClient wcClient)
    {
        try
        {
            SinglePathPropertiesHandler props = new SinglePathPropertiesHandler(path);
            wcClient.doGetProperty(path, null, SVNRevision.WORKING, SVNRevision.WORKING, SVNDepth.EMPTY, props, null);

            if (props.isEmpty())
            {
                return;
            }

            String relativeSourcePath = path.getAbsolutePath().substring(source.getAbsolutePath().length() + 1);
            File destPath = new File(dest, relativeSourcePath);

            for (SVNPropertyData prop : props)
            {
                if (merge)
                {
                    SVNPropertyData destProp = wcClient.doGetProperty(destPath, prop.getName(), SVNRevision.WORKING,
                            SVNRevision.WORKING);
                    if (destProp == null)
                    {
                        destProp = new SVNPropertyData(prop.getName(), SVNPropertyValue.create(""), null);
                    }
                    log.error("Merge not supported (yet)");
                } else
                {
                    wcClient
                            .doSetProperty(destPath, prop.getName(), prop.getValue(), false, SVNDepth.EMPTY, null, null);
                    log.info(String.format("   prop[%s%s] // %s=%s", relativeSourcePath,
                            (path.isDirectory() ? "/" : ""), prop.getName(), prop.getValue()));
                }
            }
        } catch (SVNException e)
        {
            log.error("Unable to get property information for dir: " + path, e);
        }
    }

    @TaskDefault(key = "dest")
    public File getDest()
    {
        return dest;
    }

    @TaskDefault(key = "source")
    public File getSource()
    {
        return source;
    }

    @TaskDefault(key = "merge-props")
    public boolean isMerge()
    {
        return merge;
    }

    @TaskOption(key = "dest", description = "Destination path (directory)")
    public void setDest(File path)
    {
        this.dest = path;
    }

    @TaskOption(key = "merge-props", description = "Merge Properties (true merges, false overwrites)")
    public void setMerge(boolean merge)
    {
        this.merge = merge;
    }

    @TaskOption(key = "source", description = "Source path (directory)")
    public void setSource(File path)
    {
        this.source = path;
    }
}
