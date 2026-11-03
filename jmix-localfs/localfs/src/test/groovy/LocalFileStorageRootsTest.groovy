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
import io.jmix.core.FileDigest
import io.jmix.core.FileRef
import io.jmix.localfs.LocalFileStorage
import io.jmix.localfs.LocalFileStorageConfiguration
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.concurrent.PollingConditions
import test_support.LocalFileStorageTestConfiguration
import test_support.TestContextInititalizer

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@ContextConfiguration(
        classes = [CoreConfiguration, LocalFileStorageConfiguration, LocalFileStorageTestConfiguration],
        initializers = [TestContextInititalizer]
)
class LocalFileStorageRootsTest extends Specification {

    static final String SUBSTITUTED_SHA_256 = '7a583d43dd29b1fc1bcb15a1683675706161b179088535bb2d29f46cffb02dc9'

    @Autowired
    private ApplicationContext applicationContext

    @TempDir
    private Path primary

    @TempDir
    private Path secondary

    private LocalFileStorage storage

    void setup() {
        storage = new LocalFileStorage('rootsFs', "$primary,$secondary")
        applicationContext.autowireCapableBeanFactory.autowireBean(storage)
    }

    void cleanup() {
        storage.stopWriteExecutor()
    }

    def "openStream reads the primary root when the secondary copy differs"() {
        given:
        FileRef fileRef = saveAndAwaitCopy('Primary')

        when:
        Files.writeString(secondary.resolve(fileRef.path), 'Stale')

        then:
        readContent(fileRef) == 'Primary'
    }

    def "openStream falls back to the secondary root"() {
        given:
        FileRef fileRef = saveAndAwaitCopy('Primary')

        when:
        Files.delete(primary.resolve(fileRef.path))

        then:
        readContent(fileRef) == 'Primary'
    }

    def "a substituted primary is seen although the secondary is intact"() {
        given:
        FileRef fileRef = saveAndAwaitCopy('Primary')

        when:
        Files.writeString(primary.resolve(fileRef.path), 'Substituted')
        FileDigest digest = storage.calculateDigest(fileRef, FileDigest.SHA_256)

        then:
        digest == FileDigest.of(FileDigest.SHA_256, SUBSTITUTED_SHA_256)
        digest != FileDigest.fromFileRef(fileRef)
    }

    private FileRef saveAndAwaitCopy(String content) {
        FileRef fileRef = storage.saveStream('test.txt', new ByteArrayInputStream(content.bytes))

        new PollingConditions(timeout: 5).eventually {
            Path copy = secondary.resolve(fileRef.path)
            assert Files.exists(copy) && Files.readString(copy) == content
        }
        fileRef
    }

    private String readContent(FileRef fileRef) {
        storage.openStream(fileRef).withCloseable { IOUtils.toString(it, StandardCharsets.UTF_8) }
    }
}
