## Theme add-on

Theme add-on may provide SCSS files for a theme compilation. See the [documentation](https://docs.jmix.io/jmix/0.x/backoffice-ui/themes/theme_addon.html) for more details.

### Using the Theme add-on

* Build publish add-on to `.m2`:

_Windows:_
```
gradlew clean assemble publishToMavenLocal
```

_Linux & macOS:_
```
./gradlew clean assemble publishToMavenLocal
```

* Open the project you want to apply the theme add-on.
* Create a [custom theme](https://docs.jmix.io/jmix/0.x/backoffice-ui/themes/custom_theme.html).
* Open the `build.gradle` file and make the following changes:
  * add `mavenLocal()` to repositories
  * include add-on dependency to the project:
```
implementation 'com.company:${project_name}:0.0.1-SNAPSHOT'
```
* Reload the project.
