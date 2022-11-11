# Contributing to Jmix

We welcome your contributions to Jmix! There are multiple ways to do it.

## Participating in Discussions

Join us at https://forum.jmix.io or ask questions on StackOverflow with the `jmix` tag.

## Reporting Bugs

We use Github [issues](https://github.com/jmix-framework/jmix/issues) for a backlog of bugs, improvements and feature requests related to the framework. The backlog contains issues for both open-source and premium modules. 

If you found a bug in Jmix Studio, report it on the [forum](https://forum.jmix.io) with the `studio` tag. Studio [bugtracker](https://youtrack.jmix.io/issues/JST) is read-only for the public, so we'll create an issue based on your report, and you will be able to track its progress.

## Contributing Code or Documentation

All our projects accept contributions as GitHub pull requests. The first time you create a pull request, you will be asked to electronically sign a contribution agreement (CLA).

The process:

1. Fork the repository
1. Fix an issue or create an issue and fix it
1. Create a pull request, check "Allow edits from maintainers"
1. Sign a CLA
1. Respond to review comments
1. Wait for merge and release

### Working with Code

- Install JDK 11
- Install latest IntelliJ IDEA
- Import `build.gradle` file of the project into the IDE
- Work on your issue
- When finished, run tests: `./gradlew test`
- Install to the local Maven repo: `./gradlew publishToMavenLocal` and test locally on an application.

### Working with Documentation

Fork the [jmix-docs](https://github.com/jmix-framework/jmix-docs) repository and see building instructions in its README. 
