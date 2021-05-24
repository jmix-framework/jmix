## Widgets add-on

Widgets add-on may provide UI components inherited from Vaadin components as well as their Jmix wrappers.

### Using the Widgets add-on

* Build publish add-on to `.m2`:

_Windows:_
```
gradlew clean assemble publishToMavenLocal
```

_Linux & macOS:_
```
./gradlew clean assemble publishToMavenLocal
```

* Open the project you want to apply the widgets add-on.
* Open the `build.gradle` file and make the following changes:
    * add `mavenLocal()` to repositories
    * include add-on dependency to the project:
```
implementation 'com.company:${project_name}:0.0.1-SNAPSHOT'
widgets 'com.company:${project_name}:0.0.1-SNAPSHOT'
```
* Reload the project.
