**jscep** uses [Apache Maven](http://maven.apache.org/) as its build tool.

## Prerequisites ##

  1. Install [Maven](http://maven.apache.org/)
  1. Get the Source

### Downloading Source Packages ###

Source packages are available from the [downloads](http://code.google.com/p/jscep/downloads/list?q=*sources.jar) page.

### Checking Out from Subversion ###

You can checkout the trunk by running the following commands on your command-line:

```
user@host:~$ svn checkout http://jscep.googlecode.com/svn/trunk/ jscep-read-only
user@host:~$ cd jscep-read-only
```

## Maven Commands ##

To build all modules:

```
mvn clean install
```