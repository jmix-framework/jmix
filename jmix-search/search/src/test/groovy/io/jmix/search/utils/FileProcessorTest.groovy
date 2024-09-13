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
import io.jmix.search.exception.EmptyFileExtensionException
import io.jmix.search.exception.ParserResolvingException
import io.jmix.search.exception.UnsupportedFileExtensionException
import io.jmix.search.utils.parserresolving.FileParserResolverManager
import spock.lang.Specification

class FileProcessorTest extends Specification {
    def "should throw the ParserResolvingException that have been thrown by the FileParserResolver"() {
        given:
        FileStorageLocator storageLocatorMock = Mock()

        and:
        FileParserResolverManager fileParserResolver = Mock()
        FileRef fileRefMock = Mock()
        fileParserResolver.getParser(fileRefMock) >> { throw exception }
        FileProcessor fileProcessor = new FileProcessor(storageLocatorMock, fileParserResolver)

        when:
        fileProcessor.extractFileContent(fileRefMock)

        then:
        ParserResolvingException throwable = thrown()
        throwable == exception

        where:
        exception << [new UnsupportedFileExtensionException("any.name", List.of("txt, rtf")),
                      new EmptyFileExtensionException("any")]
    }
}
