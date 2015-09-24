## Introduction ##

**Branching** Signing Artifacts
**Upload to Nexus**

## Branching and Nexus ##

**Note:** need a settings.xml server configuration for jscep.googlecode.com

```
mvn release:prepare
```

Enter the new version numbers.  Defaults are usually fine.  You will then be prompted for the password for your GPG key.  If everything goes fine, then do branch and deploy by issuing the following command.

**Note:** need a settings.xml server configuration for sonatype-nexus-staging ([reference](http://www.sonatype.com/people/2010/11/what-to-do-when-nexus-returns-401/))

```
mvn release:perform
```

### Preparation Problems ###

If `release:prepare` fails, issue the following command to undo:

```
mvn release:rollback
```

## Upload Release to Google Code ##

**Note:** need a settings.xml server configuration for googlecode

Check out the new release tag, then do the following:

```
mvn clean install source:jar javadoc:jar com.googlecode.maven-gcu-plugin:maven-gcu-plugin:1.1:upload
```

This will upload the source, javadoc and jar artifacts to Google Code.

## Maven Artifact ##

  * Login to [oss.sonatype.org](https://oss.sonatype.org/).
  * Under **Build Promotion** select **Staging Repositories**
  * Select the Repository
  * Click 'Close'
  * Click 'Release'

[Nexus 401?](http://www.sonatype.com/people/2010/11/what-to-do-when-nexus-returns-401/)