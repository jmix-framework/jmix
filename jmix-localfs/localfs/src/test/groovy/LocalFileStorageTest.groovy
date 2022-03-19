/*
 * Copyright 2020 Haulmont.
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


import io.jmix.core.FileRef
import io.jmix.core.FileStorage
import io.jmix.core.CoreConfiguration
import io.jmix.localfs.LocalFileStorageConfiguration
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.LocalFileStorageTestConfiguration
import test_support.TestContextInititalizer

@ContextConfiguration(
        classes = [CoreConfiguration, LocalFileStorageConfiguration, LocalFileStorageTestConfiguration],
        initializers = [TestContextInititalizer]
)
class LocalFileStorageTest extends Specification {

    @Autowired
    private FileStorage fileStorage

    def "write/load data using file storage"() {
        byte[] a = 'Test output'.getBytes()
        FileRef fileRef = fileStorage.saveStream("test.txt", new ByteArrayInputStream(a))

        expect:
        InputStream inputStream = fileStorage.openStream(fileRef)
        IOUtils.toByteArray(inputStream) == a

        cleanup:
        fileStorage.removeFile(fileRef)
    }

    def "FileRef format"() {
        when: "FileRef as URI string"
        def fileRefString = 'fs://2021/01/25/60680137-5d4a-69a0-999e-526acf141308.png?name=1.txt&testParam=foo'
        def fileRef = FileRef.fromString(fileRefString)

        then:
        fileRef.getStorageName() == 'fs'
        fileRef.getPath() == '2021/01/25/60680137-5d4a-69a0-999e-526acf141308.png'
        fileRef.getFileName() == '1.txt'
        fileRef.getParameters().get('testParam') == 'foo'

        fileRef.toString() == fileRefString
    }

}
