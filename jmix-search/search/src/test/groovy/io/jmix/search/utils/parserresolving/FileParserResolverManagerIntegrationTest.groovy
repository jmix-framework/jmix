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
import io.jmix.search.exception.UnsupportedFileTypeException
import io.jmix.search.index.fileparsing.FileParserResolver
import io.jmix.search.index.fileparsing.resolvers.MSOfficeDocumentsParserResolver
import io.jmix.search.index.fileparsing.resolvers.OldMSOfficeDocumentsParserResolver
import io.jmix.search.index.fileparsing.resolvers.OpenOfficeDocumentsParserResolver
import io.jmix.search.index.fileparsing.resolvers.PDFParserResolver
import io.jmix.search.index.fileparsing.resolvers.RTFParserResolver
import io.jmix.search.index.fileparsing.resolvers.TXTParserResolver
import io.jmix.search.utils.FileParserResolverManager
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.rtf.RTFParser
import org.apache.tika.parser.txt.TXTParser
import spock.lang.Specification

class FileParserResolverManagerIntegrationTest extends Specification {

    def "there is appropriate resolver for the file"() {
        given:
        def manager = new FileParserResolverManager(getResolvers())

        and:
        def fileRef = Mock(FileRef)
        fileRef.getFileName() >> "filename." + extension

        expect:
        manager.getParser(fileRef).getClass() == theClass

        where:
        extension | theClass
        "txt"     | TXTParser
        "pdf"     | PDFParser
        "rtf"     | RTFParser
        "odt"     | OpenDocumentParser
        "ods"     | OpenDocumentParser
        "doc"     | OfficeParser
        "xls"     | OfficeParser
        "docx"    | OOXMLParser
        "xlsx"    | OOXMLParser
    }

    def "there is not appropriate resolver for the file"() {
        given:
        def manager = new FileParserResolverManager(getResolvers())

        and:
        def fileRef = Mock(FileRef)
        fileRef.getFileName() >> "filename." + extension

        when:
        manager.getParser(fileRef)

        then:
        thrown(UnsupportedFileTypeException)

        where:
        extension << ["txt1", "ems", "", "od", "ods2"]
    }

    List<FileParserResolver> getResolvers() {
        List.of(
                new MSOfficeDocumentsParserResolver(),
                new OldMSOfficeDocumentsParserResolver(),
                new OpenOfficeDocumentsParserResolver(),
                new PDFParserResolver(),
                new RTFParserResolver(),
                new TXTParserResolver()
        )
    }
}
