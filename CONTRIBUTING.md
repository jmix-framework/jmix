# Contributing to Jmix

We welcome your contributions to Jmix! There are multiple ways to do it.

## Participating in Discussions

Join us at https://forum.jmix.io or ask questions on StackOverflow with the `jmix` tag.

## Reporting Bugs

The framework is located in [multiple repositories](https://github.com/Haulmont?q=jmix) on GitHub, so if you know what repository is relevant, create an issue in that repository. If not, just create it in [jmix-core](https://github.com/Haulmont/jmix-core), and we will transfer it later if needed. 

If you found a bug in Jmix Studio, report it on the [forum](https://forum.jmix.io) with the `studio` tag. Studio [bugtracker](https://youtrack.jmix.io/issues/JST) is read-only for the public, so we'll create an issue based on your report, and you will be able to track its progress.

## Contributing Code or Documentation

All our projects accept contributions as GitHub pull requests. The first time you create a pull request, you will be asked to electronically sign a contribution agreement (CLA).

The process:

1. Fork a repository
1. Fix an issue or create an issue and fix it
1. Create a pull request, check "Allow edits from maintainers"
1. Sign a CLA
1. Respond to review comments
1. Wait for merge and release

### Working with Code

- Install JDK 8
- Install IntelliJ IDEA
- Import `build.gradle` file of the project into the IDE
- Work on your issue
- When finished, run tests: `./gradlew test`
- Install to the local Maven repo: `./gradlew publishToMavenLocal` and test locally on an application.

### Working with Documentation

Fork the [jmix-docs](https://github.com/Haulmont/jmix-docs) repository and see building instructions in its README. 
