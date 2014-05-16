# Development Guidelines

This document describes tools, tasks and workflow that one needs to be familiar with in order to effectively maintain
this project. If you use this package within your own software as is but don't plan on modifying it, this guide is
**not** for you.

## Tools

*  [Gradle](http://www.gradle.org/): used to run predefined tasks. It also manages dependencies for the project. If you
   do not have it already installed, you won't need to install it. The gradle wrapper is included in the project and you
   can invoke it using `./gradlew` substituted for `gradle`.

## Tasks

### Building

Gradle's [Java Plugin](http://www.gradle.org/docs/current/userguide/java_plugin.html) provides various tasks to build
this software. The most common tasks are:

*  `gradle build` - build jars and test
*  `gradle clean` - remove previously built artifacts.

### Testing

This project's tests are written as JUnit test cases. Common tasks:

*  `gradle check` - run the test suite.

### Generating Documentation

**TODO**

*  Github Pages
*  docs directory

### Releasing

**TODO**

*  Maven Central
*  Github Releases (jar files)
*  Github Pages upload should be a dependency

### IDE Integration

Gradle's [IDEA Plugin](http://www.gradle.org/docs/current/userguide/idea_plugin.html) and
[Eclipse Plugin](http://www.gradle.org/docs/current/userguide/eclipse_plugin.html) provide tasks to generate project
files for these IDEs. In general, neither of these are necessary to work on the project. If you prefer to use one of
these IDEs, you may find those tasks helpful.

## Workflow

### Versioning

The project uses [semantic versioning](http://semver.org/) as a policy for incrementing version numbers. For planned
work that will go into a future version, there should be a Milestone created in the Github Issues named with the version
number (e.g. "v2.2.1").

**TODO** During development the version number should end in "-pre". The version number is hardcoded into the class
`com.opentok.constants.Version`, and also specified in `build.gradle`.

### Branches

*  `master` - the main development branch.
*  `feat.foo` - feature branches. these are used for longer running tasks that cannot be accomplished in one commit.
   once merged into master, these branches should be deleted.
*  `vx.x.x` - if development for a future version/milestone has begun while master is working towards a sooner
   release, this is the naming scheme for that branch. once merged into master, these branches should be deleted.

### Tags

*  `vx.x.x` - commits are tagged with a final version number during release.

### Issues

Issues are labelled to help track their progress within the pipeline.

*  no label - these issues have not been triaged.
*  `bug` - confirmed bug. aim to have a test case that reproduces the defect.
*  `enhancement` - contains details/discussion of a new feature. it may not yet be approved or placed into a
   release/milestone.
*  `wontfix` - closed issues that were never addressed.
*  `duplicate` - closed issue that is the same to another referenced issue.
*  `question` - purely for discussion

### Management

When in doubt, find the maintainers and ask.