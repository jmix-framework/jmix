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


import io.jmix.core.CoreConfiguration
import io.jmix.core.CoreProperties
import io.jmix.core.FileIntegrityChecker
import io.jmix.core.FileIntegrityStatus
import io.jmix.core.FileRef
import io.jmix.core.FileStorage
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
class LocalFileStorageIntegrityTest extends Specification {

    static final String TEST_OUTPUT_SHA_256 = 'e6e383e65dabb3ed7c623557f7967ed8edd1c4e4afc09828f97c8ef3d794e65f'
    static final String EMPTY_SHA_256 = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'

    @Autowired
    private FileStorage fileStorage

    @Autowired
    private FileIntegrityChecker integrityChecker

    @Autowired
    private CoreProperties coreProperties

    def "saveStream adds the SHA-256 digest of the content"() {
        when:
        FileRef fileRef = fileStorage.saveStream('test.txt', new ByteArrayInputStream('Test output'.bytes))

        then:
        fileRef.parameters['jmix-sha-256'] == TEST_OUTPUT_SHA_256

        cleanup:
        fileStorage.removeFile(fileRef)
    }

    def "saveStream keeps other parameters"() {
        when:
        FileRef fileRef = fileStorage.saveStream('test.txt', new ByteArrayInputStream('Test output'.bytes),
                [size: 11, 'sha-256': 'app'])

        then:
        fileRef.parameters == [size: '11', 'sha-256': 'app', 'jmix-sha-256': TEST_OUTPUT_SHA_256]

        cleanup:
        fileStorage.removeFile(fileRef)
    }

    def "saveStream rejects a reserved parameter"() {
        when:
        fileStorage.saveStream('test.txt', new ByteArrayInputStream('Test output'.bytes), [(key): 'x'])

        then:
        thrown(IllegalArgumentException)

        where:
        key << ['jmix-sha-256', 'jmix-other']
    }

    def "check of a saved file is VALID"() {
        given:
        FileRef fileRef = fileStorage.saveStream('test.txt', new ByteArrayInputStream('Test output'.bytes))

        expect:
        integrityChecker.check(fileRef).status == FileIntegrityStatus.VALID

        cleanup:
        fileStorage.removeFile(fileRef)
    }

    def "check of an empty file is VALID"() {
        given:
        FileRef fileRef = fileStorage.saveStream('empty.txt', new ByteArrayInputStream(new byte[0]))

        expect:
        fileRef.parameters['jmix-sha-256'] == EMPTY_SHA_256
        integrityChecker.check(fileRef).status == FileIntegrityStatus.VALID

        cleanup:
        fileStorage.removeFile(fileRef)
    }

    def "check of a file changed on disk is MODIFIED"() {
        given:
        FileRef fileRef = fileStorage.saveStream('test.txt', new ByteArrayInputStream('Test output'.bytes))

        when:
        Files.write(storageRoot().resolve(fileRef.path), 'Changed'.bytes)

        then:
        integrityChecker.check(fileRef).status == FileIntegrityStatus.MODIFIED

        cleanup:
        fileStorage.removeFile(fileRef)
    }

    def "check of a deleted file is MISSING"() {
        given:
        FileRef fileRef = fileStorage.saveStream('test.txt', new ByteArrayInputStream('Test output'.bytes))

        when:
        Files.delete(storageRoot().resolve(fileRef.path))

        then:
        integrityChecker.check(fileRef).status == FileIntegrityStatus.MISSING
    }

    private Path storageRoot() {
        Paths.get(coreProperties.workDir, 'filestorage')
    }
}
