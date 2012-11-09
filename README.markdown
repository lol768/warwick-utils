Warwick Utils
=============

This is a set of (mostly Java) tools that can be used as dependencies from other projects.

Setting up for development
-------------

Go to Import -> Existing projects in Eclipse, select the "modules" folder and add all the projects there. 
Once that's done, you can add the top-level project.

Pushing a release onto Nexus
-------------

Examples are for command line in UNIX.

First, set the version number:

    $ mvn versions:set -DnewVersion=`date +%Y%m%d`
    $ mvn versions:commit
    
This should set <version>the-current-date</version> in all the pom.xml files.

You _can_ then just run the deploy goal from the command line, but it's better to use the deploy *profile* as
this may trigger other changes:

    $ mvn -Pdeploy
    
Generating distributables without pushing to Nexus
-------------

The dist profile will put JARs in a dist directory off the project root:

    $ mvn -Pdist