package io.jmix.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/*
 * Copyright (c) 2008-2019 Haulmont.
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

/**
 * In general case, entities enhancing is done using the {@link EnhancingAction} action as a part of {@code compileJava} or {@code compileKotlin}
 * tasks. However, these are cases when entities enhancing must be performed after both java and kotlin classes are compiled, e.g. when a kotlin
 * entity has a field of type that is java entity.
 *
 * In these cases {@link EnhancingAction}s should not be added to compile tasks and enhancing must be done with this task.
 */
//todo rename task - why "separate"?
abstract class SeparateEnhancingTask extends DefaultTask {

    @Internal
    String sourceSetName

    @Inject
    SeparateEnhancingTask(String sourceSetName) {
        this.sourceSetName = sourceSetName
        def javaPlugin = project.plugins.findPlugin(JavaPlugin.class)
        def kotlinPlugin = project.plugins.findPlugin("org.jetbrains.kotlin.jvm")
        if (javaPlugin) {
            def compileTaskName = sourceSetName == 'test' ? 'compileTestJava' : 'compileJava'
            def classesTaskName = sourceSetName == 'test' ? 'testClasses' : 'classes'
            dependsOn(project.tasks.getByPath(compileTaskName))
            project.tasks.getByPath(classesTaskName).dependsOn(this)
        }
        if (kotlinPlugin) {
            def compileTaskName = sourceSetName == 'test' ? 'compileTestKotlin' : 'compileKotlin'
            def classesTaskName = sourceSetName == 'test' ? 'testClasses' : 'classes'
            dependsOn(project.tasks.getByPath(compileTaskName))
            project.tasks.getByPath(classesTaskName).dependsOn(this)
        }
    }

    @TaskAction
    def enhanceClasses() {
        def enhancingAction = new EnhancingAction(sourceSetName)
        enhancingAction.performAction(project)
    }
}
