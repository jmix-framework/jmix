## Jmix UI Theme Add-on

A theme add-on provides SCSS files for a theme compilation. See the [documentation](https://docs.jmix.io/jmix/backoffice-ui/themes/theme_addon.html) for details.

### Using the Theme Add-on

* Build and publish the add-on to local Maven repository (`~/.m2`):

  ```
  ./gradlew clean publishToMavenLocal
  ```

* Open the project where you want to apply the theme add-on.
* Create a [custom theme](https://docs.jmix.io/jmix/backoffice-ui/themes/custom_theme.html).
* Open the `build.gradle` file and make the following changes:
  * add `mavenLocal()` to repositories
  * include the add-on dependency to the project, for example:

    ```
    implementation 'com.company:mythemeaddon:0.0.1-SNAPSHOT'
    ```
    
* Reload the Gradle project.
