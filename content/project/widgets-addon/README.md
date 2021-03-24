## Widgets add-on

Widgets add-on may provide UI components inherited from Vaadin components as well as their Jmix wrappers.

## Adding an add-on to a project

* Build publish add-on to `.m2`:

```
./gradlew clean assemble publishToMavenLocal
```

* Include add-on dependency to a project:

```
implementation 'com.company:${project_name}:0.0.1-SNAPSHOT'
widgets 'com.company:${project_name}:0.0.1-SNAPSHOT'
```
