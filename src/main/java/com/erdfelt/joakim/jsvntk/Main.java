package com.erdfelt.joakim.jsvntk;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.erdfelt.joakim.jsvntk.annotations.TaskId;
import com.erdfelt.joakim.jsvntk.annotations.TaskOption;
import com.erdfelt.joakim.jsvntk.tasks.CopyPropertiesTask;
import com.erdfelt.joakim.jsvntk.tasks.RemoveEmptyDirs;
import com.erdfelt.joakim.jsvntk.tasks.SetEolProp;
import com.erdfelt.joakim.jsvntk.tasks.SetKeywordsProp;
import com.erdfelt.joakim.jsvntk.tasks.SetMavenProjectIgnores;

public class Main
{
    private static Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args)
    {
        (new Main()).exec(args);
    }

    private List<Task> availableTasks = new ArrayList<Task>();

    public Main()
    {
        availableTasks.add(new CopyPropertiesTask());
        availableTasks.add(new SetEolProp());
        availableTasks.add(new SetKeywordsProp());
        availableTasks.add(new SetMavenProjectIgnores());
        availableTasks.add(new RemoveEmptyDirs());
    }

    private Map<String, Method> collectOptionSetterMethods(Task task)
    {
        Map<String, Method> optionmethods = new HashMap<String, Method>();

        // Collect option methods.
        for (Method method : task.getClass().getMethods())
        {
            if (!method.getName().startsWith("set"))
            {
                // Not a setter method.  skip
                continue;
            }

            TaskOption taskopt = method.getAnnotation(TaskOption.class);
            if (taskopt == null)
            {
                // Not found, skip
                continue;
            }

            // Check for 1 single parameter.
            Class<?> params[] = method.getParameterTypes();

            if (params == null)
            {
                // No params, skip
                continue;
            }

            if (params.length != 1)
            {
                // Not 1 param, skip
                continue;
            }

            optionmethods.put(taskopt.key(), method);
        }

        return optionmethods;
    }

    private void configureTask(Task task, Map<String, String> options) throws InvalidCommandLineException
    {
        Map<String, Method> optionmethods = collectOptionSetterMethods(task);

        for (Map.Entry<String, String> option : options.entrySet())
        {
            Method method = optionmethods.get(option.getKey());
            if (method == null)
            {
                throw new InvalidCommandLineException("Option [" + option.getKey() + "] is not available for task "
                        + getTaskId(task));
            }

            Class<?> paramTypes[] = method.getParameterTypes();
            assert (paramTypes != null);
            assert (paramTypes.length == 1);

            Object paramArgs[] = new Object[1];
            Class<?> paramType = paramTypes[0];

            if (paramType.isAssignableFrom(File.class))
            {
                log.info("File value: " + option.getValue());
                paramArgs[0] = new File(option.getValue());
            } else if (paramType.isAssignableFrom(Boolean.class) || (paramType == boolean.class))
            {
                log.info("Boolean value: " + option.getValue());
                paramArgs[0] = Boolean.parseBoolean(option.getValue());
            } else if (paramType.isAssignableFrom(String.class))
            {
                log.info("String value: " + option.getValue());
                paramArgs[0] = option.getValue();
            } else
            {
                log.warn("Unable to detect paramType: " + paramType.getName());
            }

            try
            {
                method.invoke(task, paramArgs);
            } catch (IllegalArgumentException e)
            {
                throw new InvalidCommandLineException("Invalid option value type " + option.getKey() + " -> \""
                        + option.getValue() + "\"", e);
            } catch (IllegalAccessException e)
            {
                throw new InvalidCommandLineException("Invalid option value type " + option.getKey() + " -> \""
                        + option.getValue() + "\"", e);
            } catch (InvocationTargetException e)
            {
                throw new InvalidCommandLineException("Invalid option value type " + option.getKey() + " -> \""
                        + option.getValue() + "\"", e);
            }
        }
    }

    private void exec(String[] args)
    {
        setupLogging();
        String command = null;
        Map<String, String> options = new HashMap<String, String>();

        try
        {
            for (int i = 0; i < args.length; i++)
            {
                // First arg is always the command / task id.
                if (i == 0)
                {
                    command = args[i];
                    continue;
                }

                if (args[i].startsWith("--"))
                {
                    String option[] = args[i].substring(2).split("=");
                    if (option == null)
                    {
                        throw new InvalidCommandLineException("Invalid Option Line: " + args[i]);
                    }
                    if (option.length != 2)
                    {
                        throw new InvalidCommandLineException("Invalid Option Line: " + args[i]);
                    }
                    options.put(option[0], option[1]);
                } else
                {
                    throw new InvalidCommandLineException("Expected option starting with \"--\", but got \"" + args[i]
                            + "\".");
                }
            }

            if (command == null)
            {
                throw new InvalidCommandLineException("No task specified.");
            }

            Task task = findTaskForCommand(command);
            configureTask(task, options);
            try
            {
                task.exec();
                log.info("Task complete.");
            } catch (Throwable e)
            {
                log.fatal("Failed to run task " + getTaskId(task), e);
            }
        } catch (InvalidCommandLineException e)
        {
            log.error("Invalid Command Line: " + e.getMessage(), e);
            printUsage();
            System.exit(-1);
        }
    }

    private Task findTaskForCommand(String command) throws InvalidCommandLineException
    {
        for (Task task : availableTasks)
        {
            if (getTaskId(task).equals(command))
            {
                return task;
            }
        }
        throw new InvalidCommandLineException("No such task: " + command);
    }

    private String getTaskId(Task task)
    {
        TaskId id = task.getClass().getAnnotation(TaskId.class);
        if (id == null)
        {
            return "<undefined>";
        }
        return id.id();
    }

    private void printUsage()
    {
        log.info("$java -jar jsvntk.jar [task] [options]");
        for (Task task : availableTasks)
        {
            new TaskUsage(task).usage();
        }
    }

    private void setupLogging()
    {
        Logger rootLogger = Logger.getRootLogger();
        File loggingDir = new File(SystemUtils.USER_DIR, "logs");
        if (!loggingDir.exists())
        {
            loggingDir.mkdirs();
        }

        String appenderName = "jsvntk-internal-logger";

        // Remove any appender that might be there already.
        Appender appender = rootLogger.getAppender("jsvntk-internal-logger");
        if (appender != null)
        {
            if (appender instanceof FileAppender)
            {
                FileAppender fileAppender = (FileAppender) appender;
                if (fileAppender.getFile().startsWith(loggingDir.getAbsolutePath()))
                {
                    /*
                     * Logger already exists, and with expected directory. nothing to change here. consider the logging
                     * properly setup.
                     */
                    return;
                }
            }

            // We have a logger that we need to remove (and rebuild)
            rootLogger.removeAppender(appender);
        }

        // Create new appender
        try
        {
            PatternLayout layout = new PatternLayout("[%-5p %c{1}] - %m%n");
            String filename = (new SimpleDateFormat("'jsvntk'-yyyy-MM-dd_HH.mm.'log'").format(new Date()));
            File logFile = new File(loggingDir, filename);
            appender = new FileAppender(layout, logFile.getAbsolutePath(), true);
            appender.setName(appenderName);
            rootLogger.addAppender(appender);
        } catch (IOException e)
        {
            log.error("Unable to create custom FileAppender", e);
        }
    }
}
