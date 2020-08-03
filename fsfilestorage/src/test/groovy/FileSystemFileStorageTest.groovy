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


import io.jmix.core.FileStorage
import io.jmix.core.CoreConfiguration
import io.jmix.fsfilestorage.FileSystemFileStorageConfiguration
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.FileSystemFileStorageTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, FileSystemFileStorageConfiguration,
        FileSystemFileStorageTestConfiguration])
class FileSystemFileStorageTest extends Specification {

    @Autowired
    private FileStorage<URI, String> fileStorage

    def "write/load data using file storage"() {
        URI reference = fileStorage.createReference('test.txt')

        byte[] a = 'Test output'.getBytes()
        fileStorage.saveStream(reference, new ByteArrayInputStream(a)) == a.length

        expect:
        InputStream inputStream = fileStorage.openStream(reference)
        IOUtils.toByteArray(inputStream) == a

        cleanup:
        fileStorage.removeFile(reference)
    }

    def "URI file reference format"() {

        when: "Simple URI reference"
        def uri = new URI('2020/05/04/5abf4a7e-1c99-b595-bca2-481d1e7f27a4.txt*1.txt')

        then:
        fileStorage.getFileInfo(uri) == '1.txt'

        when: "Filename is not defined"
        uri = new URI('2020/05/04/5abf4a7e-1c99-b595-bca2-481d1e7f27a4')

        then:
        fileStorage.getFileInfo(uri) == ''

        when: "Create file reference"
        uri = fileStorage.createReference('test.txt')

        then:
        fileStorage.getFileInfo(uri) == 'test.txt'
    }

}
