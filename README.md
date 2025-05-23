<h1 align="center"> <a href="https://www.jmix.io/"><img src="img/Jmix_logo.png" alt="Jmix" width="400" align="center"></a>
</h1>

<h4 align="center">A Full-Stack Framework for Business Applications</h4>
  
<p align="center">
<a href="http://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat" alt="license" title=""></a>
</p>


<div align="center">
  <h3>
    <a href="https://www.jmix.io/" target="_blank">
      Website
    </a>
    <span> | </span>
    <a href="https://www.jmix.io/live-demo/" target="_blank">
      Live Demo
    </a>
    <span> | </span>
    <a href="https://docs.jmix.io" target="_blank">
      Documentation
    </a>
  </h3>
</div>

<p align="center">
<a href="https://twitter.com/JmixPlatform" target="_blank"><img src="img/icon-tw.svg" height="36px" alt="" title=""></a>
<a href="https://www.facebook.com/JmixFramework" target="_blank"><img src="img/icon-fb.svg" height="36px" margin-left="20px" alt="" title=""></a>
<a href="https://www.linkedin.com/company/jmix-platform/" target="_blank"><img src="img/icon-link.svg" height="36px" margin-left="20px" alt="" title=""></a>
<a href="https://www.youtube.com/channel/UCEmWc8OwhgHnAV7vVVxtglQ" target="_blank"><img src="img/icon-yt.svg" height="36px" margin-left="20px" alt="" title=""></a>
</p>

## Description
Jmix is a high-level full-stack framework for enterprise web applications. It comes with advanced tooling and a rich set of functional modules. You can plug in advanced system functionality like complex data access control and audit as well as business functionality like reporting or BPM with just a few lines of code.

Jmix is based on Spring Boot, which means that you can use a lot of third-party libraries and frameworks with minimal configuration, in addition to the functionality provided by Jmix.

## Using Jmix
To get started, install [Jmix Studio](https://www.jmix.io/tools) - a plugin for IntelliJ IDEA. It helps you at all stages of application development: creating and configuring a project, defining its data model, generating database migration scripts, developing UI views in a visual editor. It provides advanced navigation, code completion and inspections specific to Jmix projects.

To learn Jmix, complete the [tutorial](https://docs.jmix.io/jmix/tutorial/index.html) which walks you through the process of building a full-stack web application. It covers the entire development process, from data modeling to creating UI and managing access rights.

## Building From Source

- Clone the repository:

    ```bash
    git clone https://github.com/jmix-framework/jmix.git
    ```

- Install JDK 17.

- Build and publish the framework modules to the local Maven:

    ```bash
    cd jmix
    ./gradlew publishToMavenLocal
    ```

- If you want to build Gradle plugins, Studio templates or framework translations, execute `./gradlew publishToMavenLocal` in the respective directories:

  - `jmix-gradle-plugin` - a Gradle plugin for building Jmix applications.
  - `jmix-build` - an internal Gradle plugin which encapsulates the framework build logic. It's not used when building applications.
  - `jmix-templates` - templates used by Studio new project wizard.
  - `jmix-translations` - framework [translations](https://docs.jmix.io/jmix/localization/framework-translations.html).

If you want to contribute your changes to Jmix, see [CONTRIBUTING](CONTRIBUTING.md).

## License
Jmix is an open-source project distributed under the [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) license. The same licensing is applied to most of the framework’s add-ons.

The framework is complemented with commercial tools with separate [licensing and pricing](https://www.jmix.io/subscription-plans-and-prices/) for individuals and companies.
