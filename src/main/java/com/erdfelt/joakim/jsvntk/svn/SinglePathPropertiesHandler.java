package com.erdfelt.joakim.jsvntk.svn;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNPropertyData;

public class SinglePathPropertiesHandler implements ISVNPropertyHandler, Iterable<SVNPropertyData>
{
    private static final Logger log = Logger.getLogger(SinglePathPropertiesHandler.class);
    private File expectedPath;
    private Map<String, SVNPropertyData> props = new HashMap<String, SVNPropertyData>();

    public SinglePathPropertiesHandler(File expectedPath)
    {
        this.expectedPath = expectedPath;
    }

    public File getExpectedPath()
    {
        return expectedPath;
    }

    public Map<String, SVNPropertyData> getProps()
    {
        return props;
    }

    @Override
    public void handleProperty(File path, SVNPropertyData property) throws SVNException
    {
        if (!path.equals(this.expectedPath))
        {
            log.warn("Got properties for unexpected path <" + path + "> expected <" + expectedPath + ">");
            return;
        }

        props.put(property.getName(), property);
    }

    @Override
    public void handleProperty(long revision, SVNPropertyData property) throws SVNException
    {
        /* ignore */
    }

    @Override
    public void handleProperty(SVNURL url, SVNPropertyData property) throws SVNException
    {
        /* ignore */
    }

    public boolean isEmpty()
    {
        return props.isEmpty();
    }

    @Override
    public Iterator<SVNPropertyData> iterator()
    {
        return props.values().iterator();
    }
}
