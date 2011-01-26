package com.erdfelt.joakim.jsvntk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.erdfelt.joakim.jsvntk.annotations.TaskDefault;
import com.erdfelt.joakim.jsvntk.annotations.TaskId;
import com.erdfelt.joakim.jsvntk.annotations.TaskOption;

public class TaskUsage
{
    private static final Logger log = Logger.getLogger(TaskUsage.class);
    private Task task;

    public TaskUsage(Task task)
    {
        this.task = task;
    }

    class OptionPair
    {
        TaskOption option;
        Method optionMethod;
        TaskDefault defaultOption;
        Method defaultMethod;
    }

    public void usage()
    {
        TaskId taskid = task.getClass().getAnnotation(TaskId.class);
        if (taskid == null)
        {
            log.warn("@TaskId not defined for class " + task.getClass().getName());
            return;
        }

        log.info("");
        log.info(String.format("%s (task)", taskid.id()));
        log.info(String.format("  %s", taskid.description()));

        Map<String, OptionPair> options = collectOptions();

        for (String key : sortedKeys(options.keySet()))
        {
            OptionPair pair = options.get(key);
            if (pair.option == null)
            {
                log.warn(String.format("     --%s (no defined option!?)", key));
                continue;
            }

            if (pair.optionMethod == null)
            {
                log.warn(String.format("     --%s (no defined option method!?)", key));
                continue;
            }

            // Get the option value type
            Class<?> params[] = pair.optionMethod.getParameterTypes();

            if (params == null)
            {
                // No params, skip
                log.warn(String.format("     --%s (no params!?)", key));
                continue;
            }

            if (params.length != 1)
            {
                // Not 1 param, skip
                log.warn(String.format("     --%s (more than 1 params? (found %d params))", key, params.length));
                continue;
            }

            log.info(String.format("     --%s=[%s]", key, params[0].getSimpleName()));
            log.info(String.format("         %s", pair.option.description()));

            if (pair.defaultOption == null)
            {
                // No default. skip
                log.info("         NO DEFAULT");
                continue;
            }

            Object ret = getDefaultValue(pair.defaultMethod);

            if (ret == null)
            {
                log.info("         NO DEFAULT SET");
            } else
            {
                log.info(String.format("         default: %s", String.valueOf(ret)));
            }
        }
    }

    private Object getDefaultValue(Method defaultMethod)
    {
        try
        {
            return defaultMethod.invoke(this.task, new Object[] {});
        } catch (IllegalArgumentException e)
        {
            log.warn("Unable to get default value from " + defaultMethod.getName(), e);
        } catch (IllegalAccessException e)
        {
            log.warn("Unable to get default value from " + defaultMethod.getName(), e);
        } catch (InvocationTargetException e)
        {
            log.warn("Unable to get default value from " + defaultMethod.getName(), e);
        }
        return null;
    }

    private List<String> sortedKeys(Set<String> keyCollection)
    {
        List<String> sorted = new ArrayList<String>();
        sorted.addAll(keyCollection);
        Collections.sort(sorted);
        return sorted;
    }

    private Map<String, OptionPair> collectOptions()
    {
        Map<String, OptionPair> optionMap = new HashMap<String, OptionPair>();

        for (Method method : task.getClass().getMethods())
        {
            if (method.isAnnotationPresent(TaskOption.class))
            {
                // Process TaskOption
                TaskOption taskopt = method.getAnnotation(TaskOption.class);

                OptionPair pair = optionMap.get(taskopt.key());
                if (pair == null)
                {
                    pair = new OptionPair();
                }

                pair.option = taskopt;
                pair.optionMethod = method;

                optionMap.put(taskopt.key(), pair);
            }

            if (method.isAnnotationPresent(TaskDefault.class))
            {
                // Process TaskDefault
                TaskDefault taskdef = method.getAnnotation(TaskDefault.class);

                OptionPair pair = optionMap.get(taskdef.key());
                if (pair == null)
                {
                    pair = new OptionPair();
                }

                pair.defaultOption = taskdef;
                pair.defaultMethod = method;

                optionMap.put(taskdef.key(), pair);
            }
        }

        return optionMap;
    }
}
