/*
 * Copyright 2022 Haulmont.
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



import io.jmix.core.CoreConfiguration
import io.jmix.core.FileRef
import io.jmix.core.FileStorage
import io.jmix.core.FileStorageException
import io.jmix.localfs.LocalFileStorage
import io.jmix.localfs.LocalFileStorageConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.LocalFileStorageTestConfiguration
import test_support.TestContextInititalizer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@ContextConfiguration(
        classes = [CoreConfiguration, LocalFileStorageConfiguration, LocalFileStorageTestConfiguration],
        initializers = [TestContextInititalizer]
)
class LocalFileStorageTraversalPathTest extends Specification {

    @Autowired
    private FileStorage fileStorage

    def "'isFileWithinRootDirectory' allows nonexistent file inside the storage root"() {
        given:
        createTraversalPrefix()
        Path storageRoot = storageRoot()
        Path inside = storageRoot.resolve('2026/04/28/new-inside.txt')

        expect:
        invokePathCheck(inside, storageRoot)
    }

    def "'isFileWithinRootDirectory' rejects nonexistent file outside of the storage root"() {
        given:
        createTraversalPrefix()
        Path storageRoot = storageRoot()
        Path outside = storageRoot.resolve('2026/04/28/../../../../outside-nonexistent/new-outside.txt')

        expect:
        !invokePathCheck( outside, storageRoot)
    }

    def "'isFileWithinRootDirectory' allows nonexistent file inside a nonexisting storage root"() {
        given:
        Path futureStorageRoot = workDir().resolve('future-filestorage')
        Path inside = futureStorageRoot.resolve('2026/04/28/new-inside.txt')

        expect:
        invokePathCheck(inside, futureStorageRoot)
    }

    def "'saveStream' doesn't allow to write outside of the storage root"() {
        given:
        createTraversalPrefix()
        Path outside = workDir().resolve('outside-save/owned.txt')
        Files.deleteIfExists(outside)
        def fileRef = traversalRef('outside-save/owned.txt')

        expect:
        !Files.exists(outside)

        when:
        ((LocalFileStorage) fileStorage).saveStream(fileRef, new ByteArrayInputStream('OWNED'.bytes))

        then:
        thrown(FileStorageException)
        !Files.exists(outside)

        cleanup:
        Files.deleteIfExists(outside)
    }

    def "'removeFile' doesn't allow to delete outside of the storage root"() {
        given:
        createTraversalPrefix()
        Path outside = workDir().resolve('outside-delete/canary.txt')
        Files.createDirectories(outside.parent)
        Files.writeString(outside, 'DO_NOT_DELETE')
        def fileRef = traversalRef('outside-delete/canary.txt')

        when:
        fileStorage.removeFile(fileRef)

        then:
        Files.exists(outside)
    }

    def "'fileExists' and 'openStream' don't found files outside of the storage root"() {
        given:
        createTraversalPrefix()
        Path outside = workDir().resolve('outside-exists/canary.txt')
        Files.createDirectories(outside.parent)
        Files.writeString(outside, 'EXISTS')
        def fileRef = traversalRef('outside-exists/canary.txt')

        expect:
        !fileStorage.fileExists(fileRef)

        when:
        fileStorage.openStream(fileRef)

        then:
        thrown(FileStorageException)

        cleanup:
        Files.deleteIfExists(outside)
    }

    private static FileRef traversalRef(String outsideRelativePath) {
        new FileRef('testFs', "2026/04/28/../../../../${outsideRelativePath}", 'x.txt')
    }

    private boolean invokePathCheck(Path filePath, Path rootDirectoryPath) {
        def method = LocalFileStorage.getDeclaredMethod('isFileWithinRootDirectory', Path, Path)
        method.accessible = true
        (boolean) method.invoke((LocalFileStorage) fileStorage, filePath, rootDirectoryPath)
    }

    private static Path storageRoot() {
        workDir().resolve('filestorage')
    }

    private static Path workDir() {
        Paths.get(System.getProperty('user.dir'), 'build/test-home/work')
    }

    private static void createTraversalPrefix() {
        Files.createDirectories(storageRoot().resolve('2026/04/28'))
    }
}