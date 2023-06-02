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

import java.util.jar.Manifest

class JmixPlugin implements Plugin<Project> {

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

            if (kotlinPlugin) {
                setupKotlinOutputDir(project, kotlinPlugin.pluginVersion)
            }

            if (project.jmix.entitiesEnhancing.enabled) {
                project.configurations.create('enhancing')
                project.dependencies.add('enhancing', 'org.eclipse.persistence:org.eclipse.persistence.jpa:4.0.1-15-jmix')

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

                //todo SB3 do we still need to exclude org.slf4j?
                // Exclude second logger to prevent collisions with Logback
                configurations.each {
                    it.exclude(group: 'org.slf4j', module: 'slf4j-jdk14')
                }
            }
        }

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
}
