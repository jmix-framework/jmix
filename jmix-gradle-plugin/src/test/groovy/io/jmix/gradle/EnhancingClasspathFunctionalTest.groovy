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

import static org.junit.jupiter.api.Assertions.assertTrue

class EnhancingClasspathFunctionalTest {

    @TempDir
    Path testProjectDir

    @Test
    @Tag('slowTests')
    void aggregateBuildGeneratesPersistenceXmlWithDependencyEntities() {
        copyFixture('multiproject-persistence-descriptor', testProjectDir)

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments('clean', ':feature:classes', "-PjmixBomVersion=${System.getProperty('jmixBomVersion')}", '--stacktrace')
                .withPluginClasspath()
                .forwardOutput()
                .build()

        Path persistenceXml = testProjectDir.resolve('feature/build/generated/jmix-descriptors/main/sample/feature/persistence.xml')
        assertTrue(Files.exists(persistenceXml), "Expected generated persistence.xml at ${persistenceXml}")

        String xml = Files.readString(persistenceXml)
        assertTrue(xml.contains('<class>sample.base.BaseEntity</class>'), "Expected base module entity in:\n${xml}")
        assertTrue(xml.contains('<class>sample.feature.FeatureEntity</class>'), "Expected feature module entity in:\n${xml}")

        int jarIndex = result.output.indexOf(':base:jar')
        int enhanceIndex = result.output.indexOf(':feature:enhanceJmixMain')
        assertTrue(jarIndex >= 0, "Expected :base:jar to run:\n${result.output}")
        assertTrue(enhanceIndex >= 0, "Expected :feature:enhanceJmixMain to run:\n${result.output}")
        assertTrue(jarIndex < enhanceIndex, "Expected :base:jar to run before :feature:enhanceJmixMain:\n${result.output}")
    }

    private static void copyFixture(String name, Path target) {
        Path source = Path.of(EnhancingClasspathFunctionalTest.getResource("/fixtures/${name}").toURI())

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
