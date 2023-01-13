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

import io.jmix.gradle.flowui.KitCompile
import io.jmix.gradle.ui.ThemeCompile
import io.jmix.gradle.ui.WidgetsCompile
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.*
import org.gradle.api.plugins.JavaPlugin

import java.util.jar.Manifest

class JmixPlugin implements Plugin<Project> {

    public static final String THEMES_CONFIGURATION_NAME = 'themes'
    public static final String WIDGETS_CONFIGURATION_NAME = 'widgets'
    public static final String PROVIDED_RUNTIME_CONFIGURATION_NAME = 'providedRuntime'
    public static final String PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME = 'productionRuntimeClasspath'
    public static final String KIT_CONFIGURATION_NAME = 'kit'

    public static final String COMPILE_THEMES_TASK_NAME = 'compileThemes'
    public static final String COMPILE_WIDGETS_TASK_NAME = 'compileWidgets'
    public static final String COMPILE_KIT_TASK_NAME = 'compileKit'

    @Override
    void apply(Project project) {
        project.extensions.create('jmix', JmixExtension, project)

        project.afterEvaluate {
            if (isJmixApp(project) && project.jmix.useBom) {
                String bomVersion = project.jmix.bomVersion ?: getBomVersion()
                def platform = project.dependencies.platform("io.jmix.bom:jmix-bom:$bomVersion")
                project.dependencies.add('implementation', platform)
                project.dependencies.add(THEMES_CONFIGURATION_NAME, platform)
                project.dependencies.add(WIDGETS_CONFIGURATION_NAME, platform)
                project.dependencies.add(KIT_CONFIGURATION_NAME, platform)

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

            if (kotlinPlugin) {
                setupKotlinOutputDir(project, kotlinPlugin.pluginVersion)
            }

            if (project.jmix.entitiesEnhancing.enabled) {
                project.configurations.create('enhancing')
                project.dependencies.add('enhancing', 'org.eclipse.persistence:org.eclipse.persistence.jpa:2.7.9-6-jmix')

                if (javaPlugin) {
                    project.tasks.findByName('compileJava').doLast(new EnhancingAction('main'))
                    project.tasks.findByName('compileTestJava').doLast(new EnhancingAction('test'))
                }

                /**
                 * If project 100% kotlin, compileJava will not execute and EnhancingAction will not run.
                 * So we need run EnhancingAction after compileKotlin.
                 */
                if (kotlinPlugin) {
                    project.tasks.findByName('compileKotlin').doLast(new EnhancingAction('main'))
                    project.tasks.findByName('compileTestKotlin').doLast(new EnhancingAction('test'))
                }

                project.tasks.findByName('classes').doLast({ EnhancingAction.copyGeneratedFiles(project, 'main') })
                project.tasks.findByName('testClasses').doLast({ EnhancingAction.copyGeneratedFiles(project, 'test') })
            }

            if (isJmixApp(project)) {
                def configurations = project.configurations.collect()

                // Exclude client-side logger for each configuration except 'widgets' if it's added
                if (configurations.any { it.getName() == WIDGETS_CONFIGURATION_NAME }) {
                    configurations.each {
                        if (it.getName() != WIDGETS_CONFIGURATION_NAME) {
                            it.exclude(group: 'ru.finam', module: 'slf4j-gwt')
                        }
                    }
                }

                // Exclude second logger to prevent collisions with Logback
                configurations.each {
                    it.exclude(group: 'org.slf4j', module: 'slf4j-jdk14')
                }
            }
        }

        setupThemeCompile(project)
        setupWidgetsCompile(project)
        setupKitConfiguration(project)

        project.task([type: ZipProject], 'zipProject')
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
                    task.destinationDir = project.sourceSets.main.java.outputDir
                } else {
                    task.destinationDirectory = project.sourceSets.main.java.outputDir
                }
            })
            project.tasks.getByName('compileTestKotlin', {task ->
                if (majorVersion == 1 && minorVersion < 7) {
                    task.destinationDir = project.sourceSets.test.java.outputDir
                } else {
                    task.destinationDirectory = project.sourceSets.test.java.outputDir
                }
            })
        } catch (UnknownTaskException ignored) {
            project.logger.debug("Unable to setup output directory for Kotlin. " + ignored.message)
        }
    }

    private boolean isJmixApp(Project project) {
        !project.plugins.hasPlugin('io.jmix.build')
    }

    protected void setupThemeCompile(Project project) {
        project.ext.ThemeCompile = ThemeCompile.class
        def themesConfiguration = project.configurations.create(THEMES_CONFIGURATION_NAME)

        def compileClasspathConfiguration = project.configurations.findByName('compileClasspath')
        if (compileClasspathConfiguration != null) {
            // dependency resolution for multi-module projects
            compileClasspathConfiguration.extendsFrom(themesConfiguration)
        }

        def compileThemes = project.tasks.create(COMPILE_THEMES_TASK_NAME, ThemeCompile.class)
        compileThemes.enabled = false
        project.afterEvaluate {
            DependencySet deps = themesConfiguration.getDependencies()
            if (!(deps.isEmpty() || (deps.size() == 1 && deps[0].group == 'io.jmix.bom'))) {
                project.sourceSets.main.output.dir(compileThemes.outputDirectory, builtBy: compileThemes)
                compileThemes.enabled = true
            }
        }
    }

    protected void setupWidgetsCompile(Project project) {
        project.ext.WidgetsCompile = WidgetsCompile.class
        def widgetsConfiguration = project.configurations.create(WIDGETS_CONFIGURATION_NAME)

        ExternalDependency dependency = (ExternalDependency) project.dependencies
                .add('widgets', 'javax.validation:validation-api:1.0.0.GA')
        dependency.version(new Action<MutableVersionConstraint>() {
            @Override
            void execute(MutableVersionConstraint versionConstraint) {
                versionConstraint.strictly('1.0.0.GA')
            }
        })

        widgetsConfiguration.exclude(group: 'org.hibernate.validator', module: 'hibernate-validator')

        def compileWidgetsTask = project.tasks.create(COMPILE_WIDGETS_TASK_NAME, WidgetsCompile.class)
        compileWidgetsTask.enabled = false
        project.afterEvaluate {
            compileWidgetsTask.dependsOn(project.tasks.findByName('compileJava'))

            if (widgetsConfiguration.size() > 1) {
                project.sourceSets.main.output.dir(compileWidgetsTask.outputDirectory, builtBy: compileWidgetsTask)
                compileWidgetsTask.enabled = true

                // dependency resolution for multi-module projects
                for (Dependency d : widgetsConfiguration.getAllDependencies()) {
                    if (d instanceof ProjectDependency) {
                        project.logger.info("Project $project.name will depend on ${d.getDependencyProject().name}.jar")

                        def jarTask = d.getDependencyProject().getTasks().findByName('jar')
                        if (jarTask != null) {
                            compileWidgetsTask.dependsOn(jarTask)
                        }
                    }
                }
            }
        }
    }

    protected void setupKitConfiguration(Project project) {
        project.ext.KitCompile = KitCompile.class
        def kitConfiguration = project.configurations.create(KIT_CONFIGURATION_NAME)

        def compileKit = project.tasks.create(COMPILE_KIT_TASK_NAME, KitCompile.class)

        compileKit.enabled = false
        project.afterEvaluate {
            if (kitConfiguration.size() > 1) {
                project.sourceSets.main.output.dir(compileKit.outputDirectory, builtBy: compileKit)
                compileKit.enabled = true
            } else {
                project.logger.debug("Unable to find 'kit' configuration to compile files for screen designer")
            }
        }
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
}
