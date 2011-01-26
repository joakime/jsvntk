JSVNTK
======

This is a simple project for managing various project wide subversion details.
It has a few maven specific options to help with large project sets.

Once compiled, just execute the default to see the help details.

    $ java -jar jsvntk-1.1.jar
    [ERROR Main] - Invalid Command Line: No task specified.
    com.erdfelt.joakim.jsvntk.InvalidCommandLineException: No task specified.
	    at com.erdfelt.joakim.jsvntk.Main.exec(Main.java:183)
        at com.erdfelt.joakim.jsvntk.Main.main(Main.java:34)
    [INFO  Main] - $java -jar jsvntk.jar [task] [options]
    [INFO  TaskUsage] - 
    [INFO  TaskUsage] - copy-props (task)
    [INFO  TaskUsage] -   Copy properties from one subversion working copy to another subversion working copy.
    [INFO  TaskUsage] -      --dest=[File]
    [INFO  TaskUsage] -          Destination path (directory)
    [INFO  TaskUsage] -          NO DEFAULT SET
    [INFO  TaskUsage] -      --merge-props=[boolean]
    [INFO  TaskUsage] -          Merge Properties (true merges, false overwrites)
    [INFO  TaskUsage] -          default: false
    [INFO  TaskUsage] -      --source=[File]
    [INFO  TaskUsage] -          Source path (directory)
    [INFO  TaskUsage] -          default: /home/joakim/code/erdfelt/jsvntk
    [INFO  TaskUsage] - 
    [INFO  TaskUsage] - set-eol-prop (task)
    [INFO  TaskUsage] -   Set the svn:eol-style property on files that need it throughout the working copy.
    [INFO  TaskUsage] -      --dir=[File]
    [INFO  TaskUsage] -          Directory to set properties in
    [INFO  TaskUsage] -          default: /home/joakim/code/erdfelt/jsvntk
    [INFO  TaskUsage] - 
    [INFO  TaskUsage] - set-keyword-prop (task)
    [INFO  TaskUsage] -   Set the svn:keywords property on all files that need it in the working copy
    [INFO  TaskUsage] -      --dir=[File]
    [INFO  TaskUsage] -          Directory to set properties in
    [INFO  TaskUsage] -          default: /home/joakim/code/erdfelt/jsvntk
    [INFO  TaskUsage] - 
    [INFO  TaskUsage] - set-maven-project-ignores (task)
    [INFO  TaskUsage] -   Set the svn:ignore values on directories that are also have a pom.xml file
    [INFO  TaskUsage] -      --dir=[File]
    [INFO  TaskUsage] -          Directory to set properties in
    [INFO  TaskUsage] -          default: /home/joakim/code/erdfelt/jsvntk
    [INFO  TaskUsage] -      --ignore-eclipse=[boolean]
    [INFO  TaskUsage] -          Apply ignore for Eclipse Files (.project, .classpath, etc...)
    [INFO  TaskUsage] -          default: true
    [INFO  TaskUsage] -      --ignore-intellij=[boolean]
    [INFO  TaskUsage] -          Ignore Intellij IDEA Files (*.ipr, *.iml, *.iws)
    [INFO  TaskUsage] -          default: true
    [INFO  TaskUsage] -      --ignore-netbeans=[boolean]
    [INFO  TaskUsage] -          Ignore Netbeans Files (nbproject, build, dist, build.xml, etc...)
    [INFO  TaskUsage] -          default: true
    [INFO  TaskUsage] -      --ignore-temp=[boolean]
    [INFO  TaskUsage] -          Apply ignore for Temporary Files (*.swp, *.diff, etc...)
    [INFO  TaskUsage] -          default: true
    [INFO  TaskUsage] - 
    [INFO  TaskUsage] - rm-empty-dirs (task)
    [INFO  TaskUsage] -   Remove empty directories throughout the working copy.
    [INFO  TaskUsage] -      --dir=[File]
    [INFO  TaskUsage] -          Directory to remove empty dirs from
    [INFO  TaskUsage] -          default: /home/joakim/code/erdfelt/jsvntk



