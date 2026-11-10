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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Checks that the entity enhancing tasks work with the Gradle configuration cache
 * (jmix-framework/jmix#5913).
 */
class ConfigurationCacheFunctionalTest {

    @TempDir
    Path testProjectDir

    @Test
    @Tag('slowTests')
    void enhancingTasksWorkWithConfigurationCache() {
        copyFixture('single-project-enhancing', testProjectDir)
        addTestEntity()

        // The first build stores the configuration cache entry and runs the tasks.
        BuildResult first = build('clean', 'testClasses')
        assertTrue(first.output.contains('Configuration cache entry stored'),
                "Expected the configuration cache entry to be stored:\n${first.output}")
        assertEnhanced(first)

        // The second build loads the tasks from the configuration cache entry and runs them again.
        BuildResult second = build('clean', 'testClasses')
        assertTrue(second.output.contains('Configuration cache entry reused'),
                "Expected the configuration cache entry to be reused:\n${second.output}")
        assertEnhanced(second)
    }

    @Test
    @Tag('slowTests')
    void enhancingTaskUsesDependencyModuleEntitiesWithConfigurationCache() {
        copyFixture('multiproject-persistence-descriptor', testProjectDir)

        build('clean', ':feature:classes')
        BuildResult second = build('clean', ':feature:classes')
        assertTrue(second.output.contains('Configuration cache entry reused'),
                "Expected the configuration cache entry to be reused:\n${second.output}")
        assertEquals(TaskOutcome.SUCCESS, second.task(':feature:enhanceJmixMain').outcome)

        String xml = Files.readString(
                testProjectDir.resolve('feature/build/generated/jmix-descriptors/main/sample/feature/persistence.xml'))
        assertTrue(xml.contains('<class>sample.base.BaseEntity</class>'), "Expected base module entity in:\n${xml}")
        assertTrue(xml.contains('<class>sample.feature.FeatureEntity</class>'), "Expected feature module entity in:\n${xml}")
    }

    private BuildResult build(String... tasks) {
        def args = tasks.toList() + [
                '--configuration-cache',
                "-PjmixBomVersion=${System.getProperty('jmixBomVersion')}".toString(),
                '--stacktrace'
        ]
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(args)
                .withPluginClasspath()
                .forwardOutput()
                .build()
    }

    private void assertEnhanced(BuildResult result) {
        assertEquals(TaskOutcome.SUCCESS, result.task(':enhanceJmixMain').outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(':enhanceJmixTest').outcome)

        assertTrue(isEnhanced(testProjectDir.resolve('build/classes/jmix/main/sample/app/AppEntity.class')),
                'main entity must be enhanced')
        assertTrue(isEnhanced(testProjectDir.resolve('build/classes/jmix/test/sample/app/TestEntity.class')),
                'test entity must be enhanced')

        String testXml = Files.readString(
                testProjectDir.resolve('build/generated/jmix-descriptors/test/sample/app/persistence.xml'))
        assertTrue(testXml.contains('<class>sample.app.AppEntity</class>'), "Expected main entity in:\n${testXml}")
        assertTrue(testXml.contains('<class>sample.app.TestEntity</class>'), "Expected test entity in:\n${testXml}")
    }

    private void addTestEntity() {
        Path dir = testProjectDir.resolve('src/test/java/sample/app')
        Files.createDirectories(dir)
        Files.writeString(dir.resolve('TestEntity.java'), '''\
            package sample.app;

            import io.jmix.core.metamodel.annotation.JmixEntity;
            import jakarta.persistence.Column;
            import jakarta.persistence.Entity;
            import jakarta.persistence.Id;
            import jakarta.persistence.Table;

            import java.util.UUID;

            @JmixEntity
            @Table(name = "SAMPLE_TEST_ENTITY")
            @Entity(name = "sample_TestEntity")
            public class TestEntity {

                @Id
                @Column(name = "ID", nullable = false)
                private UUID id;

                public UUID getId() {
                    return id;
                }

                public void setId(UUID id) {
                    this.id = id;
                }
            }
            '''.stripIndent())
    }

    private static boolean isEnhanced(Path classFile) {
        String asLatin1 = new String(Files.readAllBytes(classFile), StandardCharsets.ISO_8859_1)
        // Jmix enhancing adds the io.jmix.core.Entity interface; EclipseLink weaving adds _persistence_ members.
        return asLatin1.contains('io/jmix/core/Entity') && asLatin1.contains('_persistence_')
    }

    private static void copyFixture(String name, Path target) {
        Path source = Path.of(ConfigurationCacheFunctionalTest.getResource("/fixtures/${name}").toURI())
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
