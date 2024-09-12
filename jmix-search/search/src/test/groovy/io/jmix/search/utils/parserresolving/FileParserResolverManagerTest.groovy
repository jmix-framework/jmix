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

package io.jmix.search.utils.parserresolving

import io.jmix.core.FileRef
import io.jmix.search.exception.EmptyFileExtensionException
import io.jmix.search.exception.UnsupportedFileExtensionException
import org.apache.tika.parser.Parser
import spock.lang.Specification

class FileParserResolverManagerTest extends Specification {
    def "should throw EmptyFileExtensionException when the given file name has no extension"() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> fileName

        and:
        def parserResolver = new FileParserResolverManager(Collections.emptyList())

        when:
        parserResolver.getParser(fileRef)

        then:
        thrown(EmptyFileExtensionException)

        where:
        fileName << ["abc", "def", "abc.", "abc.."]
    }

    def "should throw UnsupportedFileExtensionException when the given file name with unsupported extension"() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> fileName

        and:
        def resolver = Mock(FileParserResolver)
        resolver.getExtension() >> List.of("docx", "xlsx")
        def resolver2 = Mock(FileParserResolver)
        resolver2.getExtension() >> List.of("doc", "xls")

        and:
        def parserResolver = new FileParserResolverManager(List.of(resolver, resolver2))

        when:
        parserResolver.getParser(fileRef)

        then:
        def exception = thrown(UnsupportedFileExtensionException)
        exception.getMessage().contains(fileName)
        where:
        fileName << ["abc.def", "def.zxc"]
    }

    def "should return parser of the type that corresponds to the file extension"() {
        given:
        def resolver = Mock(FileParserResolver)
        resolver.getExtension() >> List.of("docx", "xlsx")
        def parser1 = Mock(Parser)
        resolver.getParser() >> parser1
        def resolver2 = Mock(FileParserResolver)
        resolver2.getExtension() >> List.of("doc", "xls")
        def parser2 = Mock(Parser)
        resolver2.getParser() >> parser2

        and:
        def parserResolver = new FileParserResolverManager(List.of(resolver, resolver2))

        expect:
        parserResolver.getParser(createFileRefMock("docx")) == parser1
        parserResolver.getParser(createFileRefMock("xlsx")) == parser1
        parserResolver.getParser(createFileRefMock("doc")) == parser2
        parserResolver.getParser(createFileRefMock("xls")) == parser2

    }

    private FileRef createFileRefMock(String extension) {
        def fileRef = Mock(FileRef)
        fileRef.getFileName() >> "filename." + extension
        fileRef
    }
}
