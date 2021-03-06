Warwick Utils [![Build Status](https://travis-ci.com/UniversityofWarwick/warwick-utils.svg?branch=master)](https://travis-ci.com/UniversityofWarwick/warwick-utils)
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

    $ mvn versions:set -DnewVersion=$(date +%Y%m%d)
    $ mvn versions:commit
    
This should set <version>the-current-date</version> in all the pom.xml files.

Commit and push to master; you can then run the manual goal from the Bamboo build once it's built.
    
Multiple versions on the same day
-------------

If you get a message like this:

    [ERROR] Failed to execute goal [...] Failed to deploy artifacts: Could not transfer artifact [...] from/to nexus [...] Return code is: 400, ReasonPhrase:Bad Request
    
It probably means that the artifact already exists on Nexus, and Nexus doesn't support overwriting of artifacts
with the same version. The easiest way to fix this is to add an hour/minute component to your versioning:

    $ mvn versions:set -DnewVersion=$(date +%Y%m%d-%H%M)
    $ mvn versions:commit

You should then be able to deploy.    
    
Generating distributables without pushing to Nexus
-------------

The dist profile will put JARs in a dist directory off the project root:

    $ mvn -Pdist

Installing into the local Maven repository for testing
------------------------------------------------------

```
$ mvn versions:set -DnewVersion=20190114-SNAPSHOT
$ mvn install -DskipTests
```
