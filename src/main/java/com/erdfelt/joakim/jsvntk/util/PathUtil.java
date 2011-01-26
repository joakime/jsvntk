package com.erdfelt.joakim.jsvntk.util;

import java.io.File;

public class PathUtil
{
    public static String relative(File base, File fullPath)
    {
        if (fullPath.equals(base))
        {
            return File.separator;
        }

        if (fullPath.getAbsolutePath().startsWith(base.getAbsolutePath()))
        {
            return fullPath.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);
        }
        return fullPath.getAbsolutePath();
    }
}
