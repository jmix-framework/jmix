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

package io.jmix.search.utils

import io.jmix.core.CoreConfiguration
import io.jmix.core.FileRef
import io.jmix.core.FileStorage
import io.jmix.localfs.LocalFileStorageConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import test_support.FileProcessorTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, LocalFileStorageConfiguration, FileProcessorTestConfiguration])
@TestPropertySource(locations = 'classpath:/file_processor/test-app.properties')
class FileProcessorTest extends Specification {

    private static final String RESOURCE_DIR = '/file_processor'

    @Autowired
    FileProcessor fileProcessor

    @Autowired
    FileStorage fileStorage

    def "extract content from pdf file"() {
        given:
        FileRef fileRef = saveReferenceFile('sample.pdf')

        when:
        String content = fileProcessor.extractFileContent(fileRef)

        then:
        content != null

        cleanup:
        removeStoredFile(fileRef)
    }

    def "extract content from docx file"() {
        given:
        FileRef fileRef = saveReferenceFile('sample.docx')

        when:
        String content = fileProcessor.extractFileContent(fileRef)

        then:
        content != null

        cleanup:
        removeStoredFile(fileRef)
    }

    def "extract content from xlsx file"() {
        given:
        FileRef fileRef = saveReferenceFile('sample.xlsx')

        when:
        String content = fileProcessor.extractFileContent(fileRef)

        then:
        content != null

        cleanup:
        removeStoredFile(fileRef)
    }

    def "extract content from odt file"() {
        given:
        FileRef fileRef = saveReferenceFile('sample.odt')

        when:
        String content = fileProcessor.extractFileContent(fileRef)

        then:
        content != null

        cleanup:
        removeStoredFile(fileRef)
    }

    def "extract content from rtf file"() {
        given:
        FileRef fileRef = saveReferenceFile('sample.rtf')

        when:
        String content = fileProcessor.extractFileContent(fileRef)

        then:
        content != null

        cleanup:
        removeStoredFile(fileRef)
    }

    def "extract content from txt file"() {
        given:
        FileRef fileRef = saveReferenceFile('sample.txt')

        when:
        String content = fileProcessor.extractFileContent(fileRef)

        then:
        content != null

        cleanup:
        removeStoredFile(fileRef)
    }

    protected FileRef saveReferenceFile(String fileName) {
        InputStream inputStream = getClass().getResourceAsStream("${RESOURCE_DIR}/${fileName}")
        assert inputStream != null: "Test resource not found: ${fileName}"

        inputStream.withCloseable {
            fileStorage.saveStream(fileName, it)
        }
    }

    protected void removeStoredFile(FileRef fileRef) {
        if (fileRef != null && fileStorage.fileExists(fileRef)) {
            fileStorage.removeFile(fileRef)
        }
    }
}
