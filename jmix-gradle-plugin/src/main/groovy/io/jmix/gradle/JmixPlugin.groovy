/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet

import java.util.jar.Manifest

class JmixPlugin implements Plugin<Project> {

    public static final String DEFAULT_CONF_DIR = '.jmix/conf'

    public static final String PROVIDED_RUNTIME_CONFIGURATION_NAME = 'providedRuntime'
    public static final String PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME = 'productionRuntimeClasspath'

    @Override
    void apply(Project project) {
        project.extensions.create('jmix', JmixExtension, project)

        project.afterEvaluate {
            if (isJmixApp(project) && project.jmix.useBom) {
                String bomVersion = project.jmix.bomVersion ?: getBomVersion()
                def platform = project.dependencies.platform("io.jmix.bom:jmix-bom:$bomVersion")
                project.dependencies.add('implementation', platform)

                if (project.plugins.hasPlugin('war')) {
                    project.dependencies.add(PROVIDED_RUNTIME_CONFIGURATION_NAME, platform)
                }

                if (project.plugins.hasPlugin('org.springframework.boot')) {
                    project.dependencies.add(PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME, platform)
                }
            }

            if (project.plugins.hasPlugin('org.springframework.boot')) {
                project.with {
                    springBoot {
                        buildInfo()
                    }
                }
            }

            def kotlinPlugin = project.plugins.findPlugin("org.jetbrains.kotlin.jvm")
            def javaPlugin = project.plugins.findPlugin(JavaPlugin.class)
            def groovyPlugin = project.plugins.hasPlugin('groovy')

            if (kotlinPlugin) {
                setupKotlinOutputDir(project, kotlinPlugin.pluginVersion)
            }

            if (groovyPlugin) {
                setupGroovyOutputDir(project)
            }

            if (project.jmix.entitiesEnhancing.enabled) {
                project.configurations.create('enhancing')
                project.dependencies.add('enhancing', 'org.eclipse.persistence:org.eclipse.persistence.jpa:5.0.2-2-jmix')

                def enhanceMainTask = project.tasks.register('enhanceJmixMain', EnhancingTask)
                def enhanceTestTask = project.tasks.register('enhanceJmixTest', EnhancingTask)

                def mainCompileTasks = []
                def testCompileTasks = []

                if (javaPlugin) {
                    mainCompileTasks.add(project.tasks.named('compileJava'))
                    testCompileTasks.add(project.tasks.named('compileTestJava'))
                }

                /**
                 * If project 100% kotlin, compileJava will not execute and EnhancingTask will not run.
                 * So we need run EnhancingTask after compileKotlin.
                 */
                if (kotlinPlugin) {
                    mainCompileTasks.add(project.tasks.named('compileKotlin'))
                    testCompileTasks.add(project.tasks.named('compileTestKotlin'))
                }

                // Groovy classes (e.g. Spock tests) are compiled into the java output dir (see setupGroovyOutputDir),
                // so enhancing must run after them to mirror them into the enhanced output dir.
                if (groovyPlugin) {
                    mainCompileTasks.add(project.tasks.named('compileGroovy'))
                    testCompileTasks.add(project.tasks.named('compileTestGroovy'))
                }

                def enhancedMainDir = project.file(EnhancingTask.enhancedClassesDir(project, 'main'))
                def enhancedTestDir = project.file(EnhancingTask.enhancedClassesDir(project, 'test'))

                enhanceMainTask.configure { configureEnhancingTask(project, it, 'main', mainCompileTasks) }
                enhanceTestTask.configure { configureEnhancingTask(project, it, 'test', testCompileTasks) }

                project.sourceSets.main.output.classesDirs.setFrom(enhancedMainDir)
                project.sourceSets.main.output.classesDirs.builtBy(enhanceMainTask)
                project.tasks.named(project.sourceSets.main.classesTaskName) { dependsOn(enhanceMainTask) }

                project.sourceSets.test.output.classesDirs.setFrom(enhancedTestDir)
                project.sourceSets.test.output.classesDirs.builtBy(enhanceTestTask)
                project.tasks.named(project.sourceSets.test.classesTaskName) { dependsOn(enhanceTestTask) }

                registerGeneratedDescriptors(project, 'main', enhanceMainTask)
                registerGeneratedDescriptors(project, 'test', enhanceTestTask)

                if (kotlinPlugin) {
                    redirectKotlinArchiveOutput(project, 'main', enhancedMainDir)
                }
            }

            if (isJmixApp(project)) {
                def configurations = project.configurations.collect()

                //todo SB3 do we still need to exclude org.slf4j?
                // Exclude second logger to prevent collisions with Logback
                configurations.each {
                    it.exclude(group: 'org.slf4j', module: 'slf4j-jdk14')
                }
            }
        }

        project.task([type: ZipProject], 'zipProject')

        registerCleanConfTask(project)
    }

    /**
     * Kotlin classes output dir should be the same as java output dir.
     * Otherwise the current implementation of entities enhancing doesn't work properly
     */
    private void setupKotlinOutputDir(Project project, String kotlinPluginVersion) {
        try {
            def kotlinPluginVersionNumbers = kotlinPluginVersion.split('\\.')
                    .collect { it.toInteger() }
            int majorVersion = kotlinPluginVersionNumbers[0]
            int minorVersion = kotlinPluginVersionNumbers[1]
            project.tasks.getByName('compileKotlin', {task ->
                if (majorVersion == 1 && minorVersion < 7) {
                    task.destinationDir = project.sourceSets.main.java.destinationDirectory.get()
                } else {
                    task.destinationDirectory = project.sourceSets.main.java.destinationDirectory
                }
            })
            project.tasks.getByName('compileTestKotlin', {task ->
                if (majorVersion == 1 && minorVersion < 7) {
                    task.destinationDir = project.sourceSets.test.java.destinationDirectory.get()
                } else {
                    task.destinationDirectory = project.sourceSets.test.java.destinationDirectory
                }
            })
        } catch (UnknownTaskException ignored) {
            project.logger.debug("Unable to setup output directory for Kotlin. " + ignored.message)
        }
    }

    /**
     * Groovy classes output dir should be the same as java output dir.
     * Otherwise the current implementation of entities enhancing doesn't work properly: enhancing mirrors
     * only the java output dir into the enhanced output dir consumed by the test/jar tasks, so Groovy classes
     * (e.g. Spock tests) would be left out of the source set output.
     */
    private void setupGroovyOutputDir(Project project) {
        try {
            project.tasks.getByName('compileGroovy', { task ->
                task.destinationDirectory = project.sourceSets.main.java.destinationDirectory
            })
            project.tasks.getByName('compileTestGroovy', { task ->
                task.destinationDirectory = project.sourceSets.test.java.destinationDirectory
            })
        } catch (UnknownTaskException ignored) {
            project.logger.debug("Unable to setup output directory for Groovy. " + ignored.message)
        }
    }

    /**
     * Sets the properties of an entity enhancing task. All values the task needs at execution time are taken
     * from the project here, at configuration time, because the task must not use the project at execution time
     * to be compatible with the configuration cache.
     */
    private static void configureEnhancingTask(Project project, EnhancingTask task, String sourceSetName, List compileTasks) {
        def sourceSet = project.sourceSets.getByName(sourceSetName)
        def entitiesEnhancing = project.jmix.entitiesEnhancing

        task.dependsOn(compileTasks)
        dependsOnClasspathArtifacts(project, task, sourceSetName)

        task.sourceSetName.set(sourceSetName)
        task.projectDisplayName.set(project.toString())
        task.compiledClasses.set(sourceSet.java.classesDirectory)
        task.compileClasspath.from(sourceSet.compileClasspath)
        task.compileClasspathGraph.set(project.configurations.getByName(sourceSet.compileClasspathConfigurationName)
                .incoming.resolutionResult.rootComponent)
        task.runtimeClasspath.from(sourceSet.runtimeClasspath)
        task.sourceDirs.from(sourceSet.allJava.sourceDirectories)
        task.resourceDirs.from(sourceSet.resources.sourceDirectories)

        if (sourceSetName == SourceSet.TEST_SOURCE_SET_NAME) {
            def mainSourceSet = project.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            task.mainCompiledClasses.set(mainSourceSet.java.classesDirectory)
            task.mainCompileClasspath.from(mainSourceSet.compileClasspath)
            task.mainSourceDirs.from(mainSourceSet.allJava.sourceDirectories)
        }

        task.enhancingClasspath.from(project.configurations.getByName('enhancing'))

        task.skipUnmodifiedEntitiesEnhancing.set(project.provider { entitiesEnhancing.skipUnmodifiedEntitiesEnhancing })
        task.jpaConverters.set(project.provider { entitiesEnhancing.jpaConverters })
        task.nonJpaPackages.set(project.provider { entitiesEnhancing.nonJpaPackages })
        task.nonJpaClasses.set(project.provider { entitiesEnhancing.nonJpaClasses })
        task.debugEnhancing.set(project.provider {
            Boolean.parseBoolean(project.findProperty('debugEnhancing')?.toString())
        })

        task.workDir.set(project.file(EnhancingTask.enhancingWorkDir(project, sourceSetName)))
        task.enhancedClasses.set(project.file(EnhancingTask.enhancedClassesDir(project, sourceSetName)))
        task.descriptors.set(project.file(EnhancingTask.generatedDescriptorsDir(project, sourceSetName)))
    }

    private static void registerGeneratedDescriptors(Project project, String sourceSetName, enhanceTask) {
        def sourceSet = project.sourceSets.getByName(sourceSetName)
        def generatedDir = project.file(EnhancingTask.generatedDescriptorsDir(project, sourceSetName))

        // Generated persistence.xml/orm.xml are registered as a first-class generated output of the source
        // set: they reach the runtime classpath and the jar without being reprocessed by processResources.
        sourceSet.output.dir([builtBy: enhanceTask], generatedDir)
    }

    /**
     * The Kotlin JVM plugin adds the raw compile output directory to archive tasks (e.g. 'jar')
     * through a source path that bypasses sourceSet.output. Since enhancement produces a separate
     * enhanced output directory, that raw path would put un-enhanced (and duplicate) classes into
     * the archive. Redirect any archive source that points at the raw compiled-classes directory
     * to the enhanced directory.
     */
    private static void redirectKotlinArchiveOutput(Project project, String sourceSetName, File enhancedDir) {
        def sourceSet = project.sourceSets.getByName(sourceSetName)
        def rawDir = sourceSet.java.classesDirectory.get().asFile

        def archiveTaskNames = [sourceSet.jarTaskName]
        if (project.plugins.hasPlugin('org.springframework.boot')) {
            archiveTaskNames.add('bootJar')
        }

        archiveTaskNames.each { taskName ->
            try {
                project.tasks.named(taskName).configure { task ->
                    redirectArchiveSources(task, rawDir, enhancedDir)
                }
            } catch (UnknownTaskException ignored) {
                // No such archive task in this project.
            }
        }
    }

    private static void redirectArchiveSources(task, File rawDir, File enhancedDir) {
        // Walk the archive's CopySpec tree and redirect directory sources pointing at the raw dir.
        // Relies on the internal CopySpec structure (rootSpec/children/sourcePaths); guarded so that
        // a non-directory or read-only source is skipped rather than failing the build.
        def walk
        walk = { spec ->
            spec.sourcePaths.each { sourcePath ->
                try {
                    if (sourcePath.respondsTo('getOrNull') && sourcePath.getOrNull()?.asFile == rawDir) {
                        sourcePath.set(enhancedDir)
                    }
                } catch (ignored) {
                    // Source path is not a redirectable directory property.
                }
            }
            spec.children.each { child -> walk(child) }
        }
        walk(task.rootSpec)
    }

    private static void dependsOnClasspathArtifacts(Project project, task, String sourceSetName) {
        def sourceSet = project.sourceSets.getByName(sourceSetName)

        [
                sourceSet.compileClasspathConfigurationName,
                sourceSet.runtimeClasspathConfigurationName
        ].each { configurationName ->
            task.dependsOn(project.configurations.getByName(configurationName))
        }
    }

    private boolean isJmixApp(Project project) {
        !project.plugins.hasPlugin('io.jmix.build')
    }

    String getBomVersion() {
        String result = null
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF")
        while (resources.hasMoreElements()) {
            try {
                Manifest manifest = new Manifest(resources.nextElement().openStream())
                String bomVersion = manifest.mainAttributes.getValue('Jmix-BOM-Version')
                if (bomVersion) {
                    if (result && result != bomVersion) {
                        throw new IllegalStateException("More than one manifest in plugin's classpath define Jmix-BOM-Version, and they are different." +
                                " Set jmix.bomVersion property in the project.")
                    }
                    result = bomVersion
                }
            } catch (IOException e) {
                throw new RuntimeException(e)
            }
        }
        return result ?: 'unspecified'
    }

    private static void registerCleanConfTask(Project project) {
        project.tasks.register('cleanConf', CleanConfTask) { task ->
            task.cleanupEnabled.set(project.provider { project.jmix.confDirCleanupEnabled })
            task.projectDirectory.set(project.layout.projectDirectory)
            task.activeProfiles.set(project.provider { project.findProperty('spring.profiles.active')?.toString() })
        }
        project.pluginManager.withPlugin("org.springframework.boot") {
            project.tasks.named("bootRun") {
                dependsOn("cleanConf")
            }
        }
    }
}
