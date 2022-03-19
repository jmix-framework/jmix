## Composite Jmix Project

This is the aggregating subproject of the composite project. It combines other projects in a single workspace using `includeBuild` directives located in `settings.gradle`.

Use *New → Subproject* actions of the Jmix tool window to create new or add existing subprojects to the composite project.

Alternatively, you can just include existing subprojects by adding relative paths to them to `settings.gradle`. For example, the following instruction includes a project from `users` directory located next to the aggregating subproject:

```
includeBuild '../users'
```
