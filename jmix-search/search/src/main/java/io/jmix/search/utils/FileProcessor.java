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

import com.google.common.base.Strings;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.exception.FileParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

@Component
public class FileProcessor {

    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    public String extractFileContent(FileRef fileRef) throws FileParseException {
        Preconditions.checkNotNullArgument(fileRef);
        log.debug("Extract content of file {}", fileRef);
        FileStorage fileStorage = fileStorageLocator.getByName(fileRef.getStorageName());
        Parser parser = getParser(fileRef);
        log.debug("Parser for file {}: {}", fileRef, parser);

        StringWriter stringWriter = new StringWriter();
        try (InputStream stream = fileStorage.openStream(fileRef)) {
            parser.parse(stream, new BodyContentHandler(stringWriter), new Metadata(), new ParseContext());
        } catch (OfficeXmlFileException e) {
            if (parser instanceof OfficeParser) {
                parser = new OOXMLParser();
                try (InputStream secondStream = fileStorage.openStream(fileRef)) {
                    stringWriter = new StringWriter();
                    parser.parse(secondStream, new BodyContentHandler(stringWriter), new Metadata(), new ParseContext());
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

    protected Parser getParser(FileRef fileRef) throws FileParseException {
        Optional<Parser> parserOpt = getParserOpt(fileRef);
        return parserOpt.orElseThrow(() -> new FileParseException(fileRef.getFileName(), "Parser not found"));
    }

    protected Optional<Parser> getParserOpt(FileRef fileRef) {
        Parser parser;
        String ext = FilenameUtils.getExtension(fileRef.getFileName());
        if (Strings.isNullOrEmpty(ext)) {
            log.warn("Unable to create a parser for a file without extension");
            parser = null;
        } else {
            switch (ext) {
                case "pdf":
                    parser = new PDFParser();
                    break;
                case "doc":
                case "xls":
                    parser = new OfficeParser();
                    break;
                case "docx":
                case "xlsx":
                    parser = new OOXMLParser();
                    break;
                case "odt":
                case "ods":
                    parser = new OpenDocumentParser();
                    break;
                case "rtf":
                    parser = new RTFParser();
                    break;
                case "txt":
                    parser = new TXTParser();
                    break;
                default:
                    log.warn("Unsupported file extension: {}", ext);
                    parser = null;
            }
        }
        return Optional.ofNullable(parser);
    }
}
