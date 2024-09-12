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

package io.jmix.search.utils.parserresolving;

import org.apache.tika.parser.Parser;

import java.util.List;

public interface FileParserResolver {
  /*  PDF("pdf", PDFParser::new),
    DOC("doc", OfficeParser::new),
    XLS("xls", OfficeParser::new),
    DOCX("docx", OOXMLParser::new),
    XLSX("xlsx", OOXMLParser::new),
    ODT("odt", OpenDocumentParser::new),
    ODS("ods", OpenDocumentParser::new),
    RTF("rtf", RTFParser::new),
    TXT("txt", TXTParser::new);*/

    /*private final String symbols;
    private final Supplier<? extends Parser> parserSupplier;

    FileParserResolver(String symbols, Supplier<? extends Parser> parserSupplier) {
        this.symbols = symbols;
        this.parserSupplier = parserSupplier;
    }
*/
    List<String> getExtension();

    Parser getParser();
}
