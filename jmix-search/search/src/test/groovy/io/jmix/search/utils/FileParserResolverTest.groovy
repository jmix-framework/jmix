/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain fileName copy of the License at
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
import io.jmix.search.exception.EmptyFileExtensionException
import io.jmix.search.exception.UnsupportedFileExtensionException
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.rtf.RTFParser
import org.apache.tika.parser.txt.TXTParser
import spock.lang.Specification

class FileParserResolverTest extends Specification {
    def "should throw EmptyFileExtensionException when the given file name has no extension"() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> fileName

        and:
        def parserResolver = new FileParserResolver()

        when:
        parserResolver.getParser(fileRef)

        then:
        thrown(EmptyFileExtensionException)

        where:
        fileName << ["abc", "def", "abc.", "abc.."]
    }

    def "should throw EmptyFileExtensionException when the given file name with unsupported extension"() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> fileName

        and:
        def parserResolver = new FileParserResolver()

        when:
        parserResolver.getParser(fileRef)

        then:
        thrown(UnsupportedFileExtensionException)

        where:
        fileName << ["abc.def", "def.zxc"]
    }

    def "should throw EmptyFileExtensionException with detailed description of the problem"() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> "abc.def"

        and:
        def parserResolver = new FileParserResolver()

        when:
        parserResolver.getParser(fileRef)

        then:
        def exception = thrown(UnsupportedFileExtensionException)
        exception.getMessage() == "The file abc.def with 'def' extension is not supported. " +
                "Only following file extensions are supported pdf, doc, xls, docx, xlsx, odt, ods, rtf, txt."
    }

    def "should return parser of "() {
        given:
        FileRef fileRef = Mock()
        fileRef.getFileName() >> "filename." + fileExtension

        and:
        def parserResolver = new FileParserResolver()

        when:
        def parser = parserResolver.getParser(fileRef)

        then:
        parser.getClass() == parserClass

        where:
        fileExtension | parserClass
        "pdf"         | PDFParser
        "doc"         | OfficeParser
        "xls"         | OfficeParser
        "docx"        | OOXMLParser
        "xlsx"        | OOXMLParser
        "odt"         | OpenDocumentParser
        "ods"         | OpenDocumentParser
        "rtf"         | RTFParser
        "txt"         | TXTParser
    }
}
