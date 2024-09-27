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
import io.jmix.search.exception.UnsupportedFileFormatException
import io.jmix.search.index.fileparsing.FileParserResolver
import io.jmix.search.index.fileparsing.FileParserKit
import io.jmix.search.utils.FileParserProvider
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import spock.lang.Specification
import java.util.function.Function

import static java.util.Collections.emptyList

class FileParserProviderTest extends Specification {

    def "should throw UnsupportedFileExtensionException when the given file of unsupported type"() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> fileName

        and:
        def resolver = Mock(FileParserResolver)
        resolver.supports(fileRef) >> false
        def resolver2 = Mock(FileParserResolver)
        resolver2.supports(fileRef) >> false

        and:
        def parserProvider = new FileParserProvider(List.of(resolver, resolver2))

        when:
        parserProvider.getParserKit(fileRef)

        then:
        def exception = thrown(UnsupportedFileFormatException)
        exception.getMessage().contains(fileName)

        where:
        fileName << ["abc.def", "def.zxc"]
    }

    def "should return parser of the type that is supported with exact resolver"() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> fileName

        and:
        def resolver1 = createExtensionBasedResolver("txt", parser1)
        def resolver2 = createExtensionBasedResolver("rtf", parser2)
        def resolver3 = Mock(FileParserResolver)
        resolver3.supports(_ as FileRef) >> true;
        resolver3.getParserKit() >> new FileParserKit(parser3,
                Mock(Function),
                Mock(Metadata),
                Mock(ParseContext))

        and:
        def parserProvider = new FileParserProvider(List.of(resolver1, resolver2, resolver3))

        when:
        def resolvedParser = parserProvider.getParserKit(fileRef).parser()

        then:
        resolvedParser != null
        resolvedParser == expectedResolvedParser

        where:
        fileName      | parser1      | parser2      | parser3      | expectedResolvedParser
        "file.txt"    | Mock(Parser) | null         | null         | parser1
        "file.rtf"    | null         | Mock(Parser) | null         | parser2
        "another.rtf" | null         | Mock(Parser) | null         | parser2
        "another.txt" | Mock(Parser) | null         | null         | parser1
        "file.eps"    | null         | null         | Mock(Parser) | parser3
        "file"        | null         | null         | Mock(Parser) | parser3
    }

    def "should throw an exception when there are no any resolver"() {
        given:
        FileRef fileRef = Mock()

        and:
        def resolverManager = new FileParserProvider(emptyList())

        when:
        resolverManager.getParserKit(fileRef)

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "There are no any file parser resolvers in the application."
    }

    FileParserResolver createExtensionBasedResolver(String fileExtension, Parser parser) {
        def resolver = Mock(FileParserResolver)
        resolver.supports(_ as FileRef) >> { FileRef fileRef1 ->
            {
                if (fileRef1.getFileName().contains(fileExtension)) {
                    return true
                }
                return false
            }
        }
        resolver.getParserKit() >> new FileParserKit(
                parser,
                Mock(Function),
                Mock(Metadata),
                Mock(ParseContext))
        resolver
    }
}
