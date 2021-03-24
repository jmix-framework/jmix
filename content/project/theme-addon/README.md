## Theme add-on

Theme add-on may provide SCSS files for a theme compilation.

## Adding an add-on to a project

* Build publish add-on to `.m2`:

```
./gradlew clean assemble publishToMavenLocal
```

* Include add-on dependency to a project:

```
implementation 'com.company:${project_name}:0.0.1-SNAPSHOT'
```
