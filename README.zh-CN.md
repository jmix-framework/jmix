<h1 align="center"> <a href="https://www.jmix.io/"><img src="img/Jmix_logo.png" alt="Jmix" width="400" align="center"></a>
</h1>

<h4 align="center">企业级应用开发全栈框架</h4>
  
<p align="center">
<a href="http://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat" alt="license" title=""></a>
</p>


<div align="center">
  <h3>
    <a href="https://www.jmix.io/" target="_blank">
      网站
    </a>
    <span> | </span>
    <a href="https://www.jmix.io/learn/live-demo/" target="_blank">
      在线示例
    </a>
    <span> | </span>
    <a href="https://docs.jmix.io" target="_blank">
      文档
    </a>
  </h3>
</div>

<p align="center">
<a href="https://twitter.com/Jmix_framework" target="_blank"><img src="img/icon-tw.svg" height="36px" alt="" title=""></a>
<a href="https://www.facebook.com/JmixFramework" target="_blank"><img src="img/icon-fb.svg" height="36px" margin-left="20px" alt="" title=""></a>
<a href="https://www.linkedin.com/company/jmix-framework/" target="_blank"><img src="img/icon-link.svg" height="36px" margin-left="20px" alt="" title=""></a>
<a href="https://www.youtube.com/c/JmixFramework" target="_blank"><img src="img/icon-yt.svg" height="36px" margin-left="20px" alt="" title=""></a>
</p>

## 介绍
Jmix 是一个库和工具的集合，可以加快以数据为中心的 Spring Boot 应用程序开发过程。Jmix 提供了许多开箱即用的功能，包括类似复杂的数据安全和审计方面的高级系统功能以及类似于报表引擎或业务流程执行引擎的业务功能，只需要几行代码就可以引入这些功能。

## 使用 Jmix
[下载 Jmix Studio](https://www.jmix.io/tools) - 一个 IntelliJ IDEA 插件。它可以在应用程序开发的所有阶段提供帮助：创建和配置项目、定义数据模型、生成数据库脚本以及使用可视化编辑器开发界面。它也为 Jmix 项目提供了专有的高级导航、代码完成和代码审查功能。

按照[快速开始](https://www.jmix.io/learn/quickstart/) 指南可以在15分钟内建立并运行一个项目。该指南展示了在创建任何 Web 应用程序时都必须做的一些事情：如何设计模型、如何维护数据、如何创建业务逻辑以及如何创建用户界面。

## 从源码构建

- 检出仓库:

    ```bash
    git clone https://github.com/jmix-framework/jmix.git
    ```

- 安装 JDK 8 。

- 构建并发布框架模块到本地 Maven :

    ```bash
    cd jmix
    ./gradlew publishToMavenLocal
    ```
- 如果你要构建 Jmix Gradle 插件、 Studio 模板或框架的本地化翻译，可在相应的目录中执行 `./gradlew publishToMavenLocal` :


  - `jmix-gradle-plugin` - 用于构建 Jmix 应用程序的 Gradle 插件。
  - `jmix-build` - 一个内部 Gradle 插件，封装了框架的构建逻辑，构建应用程序时不使用它。
  - `jmix-templates` - Studio 新建项目向导中使用的项目模板。
  - `jmix-translations` - 框架[翻译](https://docs.jmix.io/jmix/localization/framework-translations.html) 。

如果你想要为 Jmix 贡献代码, 请参阅[贡献](CONTRIBUTING.md)。

## 许可
Jmix 是遵循 [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可的开源项目，框架的大部分扩展也遵循相同的许可。

框架也为人个和公司提供了商业工具，商业工具使用单独的[许可和定价](https://www.jmix.io/subscription-plans-and-prices/) 。