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

import io.jmix.gradle.ui.ThemeCompile
import io.jmix.gradle.ui.WidgetsCompile
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.*

class JmixPlugin implements Plugin<Project> {

    public static final String DEFAULT_JMIX_VERSION = '1.0-SNAPSHOT'

    public static final String THEMES_CONFIGURATION_NAME = 'themes'
    public static final String WIDGETS_CONFIGURATION_NAME = 'widgets'

    public static final String COMPILE_THEMES_TASK_NAME = 'compileThemes'
    public static final String COMPILE_WIDGETS_TASK_NAME = 'compileWidgets'

    @Override
    void apply(Project project) {
        project.extensions.create('jmix', JmixExtension, project)

        project.afterEvaluate {
            if (!project.hasProperty('jmixFrameworkItself') && project.jmix.useBom) {
                String jmixVersion = project.jmix.version ?: DEFAULT_JMIX_VERSION

                def platform = project.dependencies.platform("io.jmix.bom:jmix-bom:$jmixVersion")
                project.dependencies.add('implementation', platform)
                project.dependencies.add(THEMES_CONFIGURATION_NAME, platform)
                project.dependencies.add(WIDGETS_CONFIGURATION_NAME, platform)
            }

            if (project.jmix.entitiesEnhancing.enabled) {
                project.configurations.create('enhancing')
                project.dependencies.add('enhancing', 'org.eclipse.persistence:org.eclipse.persistence.jpa:2.7.7-1-jmix')

                project.tasks.findByName('compileJava').doLast(new EnhancingAction('main'))
                project.tasks.findByName('compileTestJava').doLast(new EnhancingAction('test'))
            }

            // Exclude client-side logger for each configuration except 'widgets'
            project.configurations.collect {
                if (it.getName() != 'widgets') {
                    it.exclude(group: 'ru.finam', module: 'slf4j-gwt')
                }
            }

            // Exclude second logger to prevent collisions with Logback
            if (!project.hasProperty('jmixFrameworkItself')) {
                project.configurations.collect {
                    it.exclude(group: 'org.slf4j', module: 'slf4j-jdk14')
                }
            }
        }

        setupThemeCompile(project)
        setupWidgetsCompile(project)

        project.task([type: ZipProject], 'zipProject')
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
}
