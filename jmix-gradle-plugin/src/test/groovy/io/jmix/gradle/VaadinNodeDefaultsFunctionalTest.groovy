/*
 * Copyright 2026 Haulmont.
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

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static org.junit.jupiter.api.Assertions.assertTrue

class VaadinNodeDefaultsFunctionalTest {

    private static final String PRINT_NODE_SETTINGS_TASK = '''
        tasks.register('printNodeSettings') {
            doLast {
                // The effective configuration re-applies Vaadin's conventions, as Vaadin tasks do.
                def config = new com.vaadin.flow.gradle.PluginEffectiveConfiguration(project, project.vaadin)
                println "nodeVersion=${config.nodeVersion.get()}"
                println "nodeAutoUpdate=${config.nodeAutoUpdate.get()}"
            }
        }
        '''

    @TempDir
    Path testProjectDir

    @Test
    @Tag('slowTests')
    void patchedNodeUsedByDefault() {
        writeBuildFile('')

        String output = runner('printNodeSettings').build().output

        assertTrue(output.contains("nodeVersion=${JmixPlugin.VAADIN_NODE_VERSION}"), output)
        assertTrue(output.contains('nodeAutoUpdate=true'), output)
    }

    @Test
    @Tag('slowTests')
    void explicitNodeSettingsTakePrecedence() {
        writeBuildFile('''
            vaadin {
                nodeVersion = 'v22.20.0'
                nodeAutoUpdate = false
            }
            ''')

        String output = runner('printNodeSettings').build().output

        assertTrue(output.contains('nodeVersion=v22.20.0'), output)
        assertTrue(output.contains('nodeAutoUpdate=false'), output)
    }

    @Test
    @Tag('slowTests')
    void projectWithoutVaadinBuilds() {
        Files.writeString(testProjectDir.resolve('settings.gradle'), "rootProject.name = 'node-defaults'\n")
        Files.writeString(testProjectDir.resolve('build.gradle'), '''
            plugins {
                id 'io.jmix'
                id 'java'
            }

            jmix {
                useBom = false
            }
            ''')

        runner('help').build()
    }

    private void writeBuildFile(String vaadinBlock) {
        Files.writeString(testProjectDir.resolve('settings.gradle'), "rootProject.name = 'node-defaults'\n")
        Files.writeString(testProjectDir.resolve('build.gradle'), """
            plugins {
                id 'io.jmix'
                id 'java'
            }

            apply plugin: 'com.vaadin'

            jmix {
                useBom = false
            }
            ${vaadinBlock}
            ${PRINT_NODE_SETTINGS_TASK}
            """)
    }

    private GradleRunner runner(String... args) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(args.toList() + '--stacktrace')
                .withPluginClasspath()
                .forwardOutput()
    }
}
