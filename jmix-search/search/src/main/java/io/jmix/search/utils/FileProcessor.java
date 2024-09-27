/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.utils;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.exception.FileParseException;
import io.jmix.search.exception.UnsupportedFileFormatException;
import io.jmix.search.index.fileparsing.FileParserKit;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.OfficeParserConfig;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.StringWriter;

@Component
public class FileProcessor {

    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);

    protected FileStorageLocator fileStorageLocator;
    protected FileParserProvider fileParserProvider;

    public FileProcessor(FileStorageLocator fileStorageLocator, FileParserProvider fileParserProvider) {
        this.fileStorageLocator = fileStorageLocator;
        this.fileParserProvider = fileParserProvider;
    }

    public String extractFileContent(FileRef fileRef) throws FileParseException, UnsupportedFileFormatException {
        Preconditions.checkNotNullArgument(fileRef);
        log.debug("Extract content of file {}", fileRef);
        FileStorage fileStorage = fileStorageLocator.getByName(fileRef.getStorageName());
        FileParserKit parsingBundle = getParserKit(fileRef);
        Parser parser = parsingBundle.parser();
        log.debug("Parser for file {}: {}", fileRef, parser);

        StringWriter stringWriter = new StringWriter();
        ParseContext parseContext = createParseContext();
        try (InputStream stream = fileStorage.openStream(fileRef)) {
            parser.parse(
                    stream,
                    parsingBundle.contentHandlerGenerator().apply(stringWriter),
                    parsingBundle.metadata(),
                    parsingBundle.parseContext());
        } catch (OfficeXmlFileException e) {
            //Protection from Office 2007 documents with old .doc extension.
            if (parser instanceof OfficeParser) {
                parser = new OOXMLParser();
                try (InputStream secondStream = fileStorage.openStream(fileRef)) {
                    stringWriter = new StringWriter();
                    parser.parse(secondStream, new BodyContentHandler(stringWriter), new Metadata(), parseContext);
                } catch (Exception e1) {
                    log.error("Unable to parse OOXML file '{}'", fileRef.getFileName(), e1);
                    throw new FileParseException(fileRef.getFileName(), "Fail to parse OOXML file via OOXMLParser", e);
                }
            } else {
                throw new FileParseException(fileRef.getFileName(), "Wrong parser for OOXML file", e);
            }
        } catch (Exception e) {
            throw new FileParseException(fileRef.getFileName(), e);
        }
        return stringWriter.toString();
    }

    protected FileParserKit getParserKit(FileRef fileRef) throws UnsupportedFileFormatException {
        return fileParserProvider.getParserKit(fileRef);
    }
}
