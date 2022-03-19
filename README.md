# Jmix Templates

This repository contains templates for creating new application projects with the [Jmix](https://jmix.io) framework.

For more information see:

* Jmix Core project source [repository](https://github.com/Haulmont/jmix-core).
* Jmix [documentation](https://docs.jmix.io).

Templates can use the following variables:

|           Variable           |                 Description                 |                 Example                 |
|:----------------------------:|:-------------------------------------------:|:---------------------------------------:|
|         project_name         |                Project name                 |             sample-project              |
|         project_path         | Path to the project in the operating system |          /../../sample-project          |
|       project_rootPath       |   Path to the root folder of the project    |           com/company/sample            |
|     project_rootPackage      |   Path to the root package of the project   |           com.company.sample            |
|        project_group         |                Project group                |               com.company               |
|     project_classPrefix      |       Prefix of the application class       |              SampleProject              |
| project_projectPrintableName |           Printable project name            |             Sample Project              |
|          project_id          |                 Project ID                  |                   foo                   |
|       project_locales        |               Project locales               | [Locale(code=en), <br/>Locale(code=ru)] |
|       project_version        |               Project version               |             0.0.1-SNAPSHOT              |

The template for Single Module Add-On can also use the following variables:

|             Variable             |                            Description                            |                Example                 |
|:--------------------------------:|:-----------------------------------------------------------------:|:--------------------------------------:|
|           module_name            |                            Module name                            |             sample-module              |
|  project_autoConfigurationPath   |               Path to the auto-configuration class                | com/company/autoconfigure/samplemodule |
| project_autoConfigurationPackage | Path to the package where the auto-configuration class is located | com.company.autoconfigure.samplemodule |

