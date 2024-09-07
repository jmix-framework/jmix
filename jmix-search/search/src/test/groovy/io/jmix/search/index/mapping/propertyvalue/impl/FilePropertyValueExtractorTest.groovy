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

package io.jmix.search.index.mapping.propertyvalue.impl

import io.jmix.core.FileRef
import io.jmix.search.exception.EmptyFileExtensionException
import io.jmix.search.exception.UnsupportedFileExtensionException
import io.jmix.search.utils.FileProcessor
import spock.lang.Specification

class FilePropertyValueExtractorTest extends Specification {


    def "nothing should be thrown if fileProcessor throws a ParserResolvingException"() {
        given:
        FileRef fileRef = Mock()

        and:
        FileProcessor fileProcessor = Mock()
        fileProcessor.extractFileContent(fileRef) >> {throw exception}

        and:
        FilePropertyValueExtractor extractor = new FilePropertyValueExtractor(fileProcessor)

        when:
        extractor.addFileContent(null, fileRef)

        then:
        true

        where:
        exception<<[new UnsupportedFileExtensionException("any.file"), new EmptyFileExtensionException("any")]
    }
}
