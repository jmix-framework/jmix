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
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class BuildReliabilityFunctionalTest {

    @TempDir
    Path testProjectDir

    private GradleRunner runner(String... args) {
        def all = (args.toList() + ["-PjmixBomVersion=${System.getProperty('jmixBomVersion')}".toString(), '--stacktrace'])
        return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(all)
                .withPluginClasspath()
                .forwardOutput()
    }

    private Path persistenceXml() {
        // Descriptors are a generated source-set output, not reprocessed by processResources.
        return testProjectDir.resolve('build/generated/jmix-descriptors/main/sample/app/persistence.xml')
    }

    private Path entityClassFile() {
        // Enhancement writes to the dedicated enhanced output dir, not compileJava's output dir.
        return testProjectDir.resolve('build/classes/jmix/main/sample/app/AppEntity.class')
    }

    private static boolean isEnhanced(Path classFile) {
        return isEnhanced(Files.readAllBytes(classFile))
    }

    private static boolean isEnhanced(byte[] classBytes) {
        String asLatin1 = new String(classBytes, StandardCharsets.ISO_8859_1)
        // Jmix enhancing adds the io.jmix.core.Entity interface; EclipseLink weaving adds _persistence_ members.
        return asLatin1.contains('io/jmix/core/Entity') || asLatin1.contains('_persistence_')
    }

    @Test
    @Tag('slowTests')
    void entitiesEnhancedWhenClassesCompiledInEarlierInvocation() {
        copyFixture('single-project-enhancing', testProjectDir)

        // Simulate an IDE/partial build: compile WITHOUT running the enhance task.
        runner('clean', 'compileJava').build()

        // Next invocation finds compileJava UP-TO-DATE. Enhancement must still ensure the
        // on-disk classes are enhanced, regardless of whether compile did work in this invocation.
        runner('classes').build()

        assertTrue(isEnhanced(entityClassFile()),
                "Entity left un-enhanced when classes were compiled in an earlier invocation")
    }

    @Test
    @Tag('slowTests')
    void descriptorsSurviveResourceOnlyRebuild() {
        copyFixture('single-project-enhancing', testProjectDir)

        runner('clean', 'classes').build()
        assertTrue(Files.exists(persistenceXml()), "persistence.xml missing after first build")

        // Change ONLY a resource; entity sources untouched -> compileJava UP-TO-DATE, enhance skipped.
        Path props = testProjectDir.resolve('src/main/resources/application.properties')
        Files.write(props, '\nsample.app.greeting2=world\n'.bytes, StandardOpenOption.APPEND)

        runner('classes').build()

        assertTrue(Files.exists(persistenceXml()),
                "persistence.xml was lost after a resource-only incremental rebuild")
        String xml = Files.readString(persistenceXml())
        assertTrue(xml.contains('<class>sample.app.AppEntity</class>'), "Expected entity in:\n${xml}")
    }

    @Test
    @Tag('slowTests')
    void descriptorsProducedByEnhanceTask() {
        copyFixture('single-project-enhancing', testProjectDir)

        // Descriptors are produced by the enhance task as a generated source-set output, independently of
        // processResources, so they don't force processResources to run after compileJava (which would
        // create a circular dependency in modules that order compileJava after processResources).
        runner('clean', 'enhanceJmixMain').build()

        assertTrue(Files.exists(persistenceXml()),
                "persistence.xml must be produced by the enhance task into the generated descriptors dir")
        String xml = Files.readString(persistenceXml())
        assertTrue(xml.contains('<class>sample.app.AppEntity</class>'), "Expected entity in:\n${xml}")
    }

    @Test
    @Tag('slowTests')
    void descriptorsPresentAfterCleanWithBuildCache() {
        copyFixture('single-project-enhancing', testProjectDir)

        // Populate the cache.
        runner('--build-cache', 'classes').build()
        assertTrue(Files.exists(persistenceXml()), "persistence.xml missing after first cached build")

        // Clean and rebuild from cache only.
        runner('--build-cache', 'clean').build()
        runner('--build-cache', 'classes').build()

        assertTrue(Files.exists(persistenceXml()),
                "persistence.xml missing after clean + build-cache restore")
    }

    @Test
    @Tag('slowTests')
    void enhancedClassesWrittenToSeparateDirectory() {
        copyFixture('single-project-enhancing', testProjectDir)

        runner('clean', 'classes').build()

        Path compiled = testProjectDir.resolve('build/classes/java/main/sample/app/AppEntity.class')
        Path enhanced = testProjectDir.resolve('build/classes/jmix/main/sample/app/AppEntity.class')

        // compileJava must own its output dir exclusively: the class it produces stays un-enhanced.
        assertTrue(Files.exists(compiled), "compiled class must exist in build/classes/java/main")
        assertTrue(!isEnhanced(compiled),
                "class in build/classes/java/main must NOT be enhanced (compileJava owns the dir exclusively)")

        // Enhancement writes to a separate, dedicated output dir.
        assertTrue(Files.exists(enhanced), "enhanced class must exist in build/classes/jmix/main")
        assertTrue(isEnhanced(enhanced), "class in build/classes/jmix/main must be enhanced")
    }

    @Test
    @Tag('slowTests')
    void compileJavaUpToDateOnSecondBuild() {
        copyFixture('single-project-enhancing', testProjectDir)

        runner('clean', 'classes').build()
        def result = runner('classes').build()

        assertEquals(TaskOutcome.UP_TO_DATE, result.task(':compileJava').outcome,
                "compileJava must be UP-TO-DATE on a no-change rebuild; in-place enhancement modifying its " +
                        "output dir makes Gradle recompile every build (overlapping outputs)")
    }

    @Test
    @Tag('slowTests')
    void kotlinEntityEnhancedAndNotDuplicatedInJar() {
        copyFixture('single-project-enhancing-kotlin', testProjectDir)

        runner('clean', 'jar').build()

        Path jar = builtJar()

        // The Kotlin plugin adds the raw compile dir to the jar separately from sourceSet.output.
        // Without the enhanced-output redirect, each class is packaged twice (and the raw, un-enhanced
        // copy may win), which also breaks the build with a duplicate-entry error.
        assertEquals(1, classEntryCount(jar, 'sample/app/AppEntity.class'),
                "Kotlin entity class must appear exactly once in the jar")
        assertEquals(1, classEntryCount(jar, 'sample/app/AppConfiguration.class'),
                "Kotlin @JmixModule class must appear exactly once in the jar")

        assertTrue(isEnhanced(readJarEntry(jar, 'sample/app/AppEntity.class')),
                "The entity class shipped in the jar must be the enhanced copy")
    }

    @Test
    @Tag('slowTests')
    void kotlinTestSourcesCompileAgainstEnhancedMainOutput() {
        copyFixture('single-project-enhancing-kotlin', testProjectDir)

        // The fixture's main set declares a top-level Kotlin extension function and a test source calls it.
        // sourceSets.main.output is redirected to the enhanced output dir, so the test compiler resolves
        // main's top-level declarations only if the enhanced dir also carries the Kotlin module metadata
        // (META-INF/*.kotlin_module). Without mirroring it, compilation fails with "Unresolved reference".
        def result = runner('clean', 'compileTestKotlin').build()

        assertEquals(TaskOutcome.SUCCESS, result.task(':compileTestKotlin').outcome,
                "Kotlin test sources must compile against the enhanced main output")
    }

    @Test
    @Tag('slowTests')
    void kotlinModuleMetadataMirroredIntoEnhancedDir() {
        copyFixture('single-project-enhancing-kotlin', testProjectDir)

        runner('clean', 'classes').build()

        // The Kotlin module metadata file is the mechanism behind top-level-declaration resolution.
        // Enhancement mirrors the compiled classes into a separate output dir that replaces
        // sourceSets.main.output, so the metadata must be mirrored alongside the classes.
        Path metaInf = testProjectDir.resolve('build/classes/jmix/main/META-INF')
        boolean hasModuleMetadata = Files.exists(metaInf) &&
                Files.list(metaInf).withCloseable { stream ->
                    stream.anyMatch { it.fileName.toString().endsWith('.kotlin_module') }
                }
        assertTrue(hasModuleMetadata,
                "Kotlin module metadata (*.kotlin_module) must be mirrored into the enhanced output dir")
    }

    @Test
    @Tag('slowTests')
    void enhancedDirSelfHealsWhenClearedWithChecksumKept() {
        copyFixture('single-project-enhancing', testProjectDir)

        runner('clean', 'classes').build()
        assertTrue(isEnhanced(entityClassFile()), "entity must be enhanced after the first build")

        // Clear the enhanced output while the saved checksum survives (e.g. `rm -rf build/classes`,
        // an IDE rebuild, or a partial clean). The enhanced dir and the checksum live in different
        // build subtrees, so they can get out of sync.
        deleteRecursively(testProjectDir.resolve('build/classes/jmix/main'))
        Path checksum = testProjectDir.resolve('build/tmp/entitiesEnhancing/main/entities.checksum')
        assertTrue(Files.exists(checksum), "precondition: the checksum file must survive")
        assertTrue(!Files.exists(entityClassFile()), "precondition: the enhanced class must be removed")

        runner('classes').build()

        assertTrue(isEnhanced(entityClassFile()),
                "enhanced dir must self-heal: enhancement must re-run when an enhanced entity is missing, " +
                        "even when the checksum is unchanged")
    }

    @Test
    @Tag('slowTests')
    void testEnhancementIsNoOpWithoutTestSources() {
        copyFixture('single-project-enhancing', testProjectDir)

        // The fixture has main entities but no test sources. enhanceJmixTest must be a harmless no-op,
        // not fail by invoking the persistence weaver with no compiled test classes.
        def result = runner('clean', 'testClasses').build()

        assertEquals(TaskOutcome.SUCCESS, result.task(':enhanceJmixMain').outcome,
                "main enhancement must still run")
        assertTrue(isEnhanced(entityClassFile()), "main entity must be enhanced")
        // The test enhance task must not fail the build when there are no compiled test classes.
        TaskOutcome testEnhanceOutcome = result.task(':enhanceJmixTest')?.outcome
        assertTrue(testEnhanceOutcome == null || testEnhanceOutcome == TaskOutcome.SUCCESS,
                "enhanceJmixTest must not fail without test sources (was ${testEnhanceOutcome})")
    }

    @Test
    @Tag('slowTests')
    void generatedEntityClassesSurviveNonEntityRebuild() {
        copyFixture('single-project-enhancing', testProjectDir)

        runner('clean', 'classes').build()
        Path generatedInner = testProjectDir.resolve('build/classes/jmix/main/sample/app/AppEntity$JmixEntityEntry.class')
        assertTrue(Files.exists(generatedInner),
                'precondition: the enhancer must generate AppEntity$JmixEntityEntry')

        // Change a NON-entity class so the enhance task re-runs (compiled input changed) but skips weaving
        // (entity checksum unchanged). Stale-removal must not drop the enhancer-generated inner classes.
        Path config = testProjectDir.resolve('src/main/java/sample/app/AppConfiguration.java')
        String src = Files.readString(config)
                .replace('public class AppConfiguration {',
                        'public class AppConfiguration {\n    @SuppressWarnings("unused") private void probe() {}')
        Files.writeString(config, src)

        runner('classes').build()

        assertTrue(Files.exists(generatedInner),
                'enhancer-generated classes must survive an incremental rebuild that changes only a non-entity class')
    }

    private static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return
        }
        Files.walk(dir).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
    }

    private Path builtJar() {
        Path libs = testProjectDir.resolve('build/libs')
        Files.list(libs).withCloseable { stream ->
            return stream.filter { it.fileName.toString().endsWith('.jar') }
                    .findFirst()
                    .orElseThrow { new AssertionError("No jar produced in ${libs}".toString()) }
        }
    }

    private static int classEntryCount(Path jar, String entryName) {
        new ZipFile(jar.toFile()).withCloseable { zip ->
            return Collections.list(zip.entries())*.name.count { it == entryName }
        }
    }

    private static byte[] readJarEntry(Path jar, String entryName) {
        new ZipFile(jar.toFile()).withCloseable { zip ->
            def entry = zip.getEntry(entryName)
            assertTrue(entry != null, "Entry ${entryName} not found in ${jar}".toString())
            return zip.getInputStream(entry).bytes
        }
    }

    private static void copyFixture(String name, Path target) {
        Path source = Path.of(BuildReliabilityFunctionalTest.getResource("/fixtures/${name}").toURI())
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
