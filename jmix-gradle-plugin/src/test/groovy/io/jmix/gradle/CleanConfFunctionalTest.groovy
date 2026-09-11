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
import java.nio.file.StandardCopyOption

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * The 'cleanConf' task must delete the directory that the application actually uses at runtime.
 * The runtime resolves 'jmix.core.conf-dir' against its working directory, which for 'bootRun'
 * is the application module dir - not the build root dir.
 */
class CleanConfFunctionalTest {

    private static final String APP_MODULE_PATH = 'modules/backend/app'

    @TempDir
    Path testProjectDir

    @Test
    @Tag('slowTests')
    void defaultConfDirIsResolvedAgainstApplicationModule() {
        copyFixture('nonstandard-layout-conf-dir', testProjectDir)

        Path rootConfDir = createConfDir('.jmix/conf')
        Path appConfDir = createAppConfDir('.jmix/conf')

        runCleanConf()

        assertFalse(Files.exists(appConfDir), "conf dir of the application module must be deleted: ${appConfDir}")
        assertTrue(Files.exists(rootConfDir), "conf dir of the build root must be left alone: ${rootConfDir}")
    }

    @Test
    @Tag('slowTests')
    void relativeConfDirPropertyIsResolvedAgainstApplicationModule() {
        copyFixture('nonstandard-layout-conf-dir', testProjectDir)
        appendAppProperty('jmix.core.conf-dir = custom-conf')

        Path rootConfDir = createConfDir('custom-conf')
        Path appConfDir = createAppConfDir('custom-conf')

        runCleanConf()

        assertFalse(Files.exists(appConfDir), "conf dir of the application module must be deleted: ${appConfDir}")
        assertTrue(Files.exists(rootConfDir), "conf dir of the build root must be left alone: ${rootConfDir}")
    }

    @Test
    @Tag('slowTests')
    void userDirPlaceholderInConfDirPropertyIsExpanded() {
        copyFixture('nonstandard-layout-conf-dir', testProjectDir)
        appendAppProperty('jmix.core.conf-dir = ${user.dir}/.jmix/conf')

        Path rootConfDir = createConfDir('.jmix/conf')
        Path appConfDir = createAppConfDir('.jmix/conf')

        runCleanConf()

        assertFalse(Files.exists(appConfDir), "conf dir of the application module must be deleted: ${appConfDir}")
        assertTrue(Files.exists(rootConfDir), "conf dir of the build root must be left alone: ${rootConfDir}")
    }

    @Test
    @Tag('slowTests')
    void absoluteConfDirPropertyIsUsedAsIs() {
        copyFixture('nonstandard-layout-conf-dir', testProjectDir)

        Path absoluteConfDir = Files.createDirectories(testProjectDir.resolve('outside/conf'))
        Files.writeString(absoluteConfDir.resolve('marker.txt'), 'marker')
        appendAppProperty("jmix.core.conf-dir = ${absoluteConfDir.toString().replace('\\', '/')}")

        Path appConfDir = createAppConfDir('.jmix/conf')

        runCleanConf()

        assertFalse(Files.exists(absoluteConfDir), "conf dir given as an absolute path must be deleted: ${absoluteConfDir}")
        assertTrue(Files.exists(appConfDir), "default conf dir must be left alone when the property is set: ${appConfDir}")
    }

    private void runCleanConf() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(':app:cleanConf', '--stacktrace')
                .withPluginClasspath()
                .forwardOutput()
                .build()
    }

    private Path createConfDir(String relativePath) {
        return createMarkedDir(testProjectDir.resolve(relativePath))
    }

    private Path createAppConfDir(String relativePath) {
        return createMarkedDir(testProjectDir.resolve(APP_MODULE_PATH).resolve(relativePath))
    }

    private static Path createMarkedDir(Path dir) {
        Files.createDirectories(dir)
        Files.writeString(dir.resolve('marker.txt'), 'marker')
        return dir
    }

    private void appendAppProperty(String line) {
        Path propertiesFile = testProjectDir.resolve("${APP_MODULE_PATH}/src/main/resources/application.properties")
        Files.writeString(propertiesFile, "${Files.readString(propertiesFile)}\n${line}\n")
    }

    private static void copyFixture(String name, Path target) {
        Path source = Path.of(CleanConfFunctionalTest.getResource("/fixtures/${name}").toURI())

        Files.walk(source).forEach { sourcePath ->
            Path targetPath = target.resolve(source.relativize(sourcePath).toString())
            if (Files.isDirectory(sourcePath)) {
                Files.createDirectories(targetPath)
            } else {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
