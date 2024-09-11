/*
 * Copyright 2024 Haulmont.
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

import io.jmix.core.FileRef
import io.jmix.core.FileStorageLocator
import io.jmix.search.exception.UnsupportedFileFormatException
import spock.lang.Specification

class FileProcessorTest2 extends Specification {
    def "ExtractFileContent"() {
        given:
        FileStorageLocator storageLocatorMock = Mock()
        FileRef fileRefMock = Mock()
        fileRefMock.getFileName() >> fileName
        FileProcessor fileProcessor = new FileProcessor(storageLocatorMock)

        when:
        fileProcessor.extractFileContent(fileRefMock)

        then:
        def exception = thrown(UnsupportedFileFormatException)
        exception.getMessage() == message

        where:
        fileName                     | message
        "file-name.sql"              | "The file file-name.sql with 'sql' extension is not supported."
        "any-file.abc"               | "The file any-file.abc with 'abc' extension is not supported."
        "any-file-without-extension" | "The file any-file-without-extension with '' extension is not supported."
    }
}
