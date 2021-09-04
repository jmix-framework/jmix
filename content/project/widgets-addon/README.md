## Jmix UI Widgets Add-on

A widgets add-on provides custom UI components to reuse them in different applications.

### Using the Widgets Add-on

* Build and publish the add-on to local Maven repository (`~/.m2`):

  ```
  ./gradlew clean publishToMavenLocal
  ```

* Open the project where you want to apply the widgets add-on.
* Open the `build.gradle` file and make the following changes:
    * add `mavenLocal()` to repositories
    * include the add-on dependency to the project's `implementation` and `widgets` configurations, for example:
      ```
      implementation 'com.company:mywidgetsaddon:0.0.1-SNAPSHOT'
      widgets 'com.company:mywidgetsaddon:0.0.1-SNAPSHOT'
      ```
* Reload the Gradle project.
