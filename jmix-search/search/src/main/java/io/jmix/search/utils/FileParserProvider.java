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

package io.jmix.search.utils;

import io.jmix.core.FileRef;
import io.jmix.search.exception.UnsupportedFileFormatException;
import io.jmix.search.index.fileparsing.FileParserResolver;
import io.jmix.search.index.fileparsing.FileParsingBundle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The service that searches appropriate file parsers for the supported file types.
 * A search principle is based on the sequential applying FileParserResolver objects' checks for the given file.
 */
@Component("search_FileParserProvider")
public class FileParserProvider {

    private static final String EMPTY_FILE_PARSER_RESOLVERS_LIST_MESSAGE
            = "There are no any file parser resolvers in the application.";

    protected List<FileParserResolver> fileParserResolvers;

    public FileParserProvider(List<FileParserResolver> fileParserResolvers) {
        this.fileParserResolvers = fileParserResolvers;
    }

    public FileParsingBundle getParsingBundle(FileRef fileRef) throws UnsupportedFileFormatException {
        if (fileParserResolvers.isEmpty()) {
            throw new IllegalStateException(EMPTY_FILE_PARSER_RESOLVERS_LIST_MESSAGE);
        }

        String fileName = fileRef.getFileName();

        List<String> messages = new ArrayList<>();

        for (FileParserResolver resolver : fileParserResolvers) {
            if (resolver.supports(fileRef)) {
                return resolver.getParsingBundle();
            }
            messages.add(resolver.getCriteriaDescription());
        }

        throw new UnsupportedFileFormatException(fileName, messages);
    }
}
