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
import io.jmix.search.index.fileparsing.resolvers.MSOfficeDocumentsParserResolver
import io.jmix.search.index.fileparsing.resolvers.LegacyMSOfficeDocumentsParserResolver
import io.jmix.search.index.fileparsing.resolvers.OpenOfficeDocumentsParserResolver
import io.jmix.search.index.fileparsing.resolvers.PDFParserResolver
import io.jmix.search.index.fileparsing.resolvers.RTFParserResolver
import io.jmix.search.index.fileparsing.resolvers.TXTParserResolver
import io.jmix.search.utils.FileParserProvider
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.rtf.RTFParser
import org.apache.tika.parser.txt.TXTParser
import spock.lang.Specification

class FileParserProviderIntegrationTest extends Specification {

    def "there is appropriate resolver for the file"() {
        given:
        def provider = new FileParserProvider(getResolvers())

        and:
        def fileRef = Mock(FileRef)
        fileRef.getFileName() >> "filename." + extension

        expect:
        provider.getParsingBundle(fileRef).parser().getClass() == theClass

        where:
        extension | theClass
        "txt"     | TXTParser
        "TXT"     | TXTParser
        "pdf"     | PDFParser
        "PDF"     | PDFParser
        "rtf"     | RTFParser
        "RTF"     | RTFParser
        "odt"     | OpenDocumentParser
        "ODT"     | OpenDocumentParser
        "ods"     | OpenDocumentParser
        "ODS"     | OpenDocumentParser
        "doc"     | OfficeParser
        "DOC"     | OfficeParser
        "xls"     | OfficeParser
        "XLS"     | OfficeParser
        "docx"    | OOXMLParser
        "DOCX"    | OOXMLParser
        "xlsx"    | OOXMLParser
        "XLSX"    | OOXMLParser
    }

    def "there is not appropriate resolver for the file"() {
        given:
        def provider = new FileParserProvider(getResolvers())

        and:
        def fileRef = Mock(FileRef)
        fileRef.getFileName() >> "filename." + extension

        when:
        provider.getParsingBundle(fileRef)

        then:
        thrown(UnsupportedFileFormatException)

        where:
        extension << ["txt1", "ems", "", "od", "ods2"]
    }

    List<FileParserResolver> getResolvers() {
        List.of(
                new MSOfficeDocumentsParserResolver(),
                new LegacyMSOfficeDocumentsParserResolver(),
                new OpenOfficeDocumentsParserResolver(),
                new PDFParserResolver(),
                new RTFParserResolver(),
                new TXTParserResolver()
        )
    }
}
