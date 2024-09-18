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
import io.jmix.search.exception.UnsupportedFileTypeException;
import io.jmix.search.index.fileparsing.FileParserResolver;
import org.apache.tika.parser.Parser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The service that searches appropriate file parsers for the supported file types.
 * Search principle is based on a file extension analysing.
 */
@Component("search_FileParserResolverManager")
public class FileParserResolverManager {

    protected List<FileParserResolver> fileParserResolvers;

    public FileParserResolverManager(List<FileParserResolver> fileParserResolvers) {
        this.fileParserResolvers = fileParserResolvers;
    }

    public Parser getParser(FileRef fileRef) throws UnsupportedFileTypeException {
        String fileName = fileRef.getFileName();

        List<String> messages = new ArrayList<>();

        for (FileParserResolver resolver : fileParserResolvers) {
            if (resolver.supports(fileRef)) {
                return resolver.getParser();
            }
            messages.add(resolver.getCriteriaDescription());
        }

        throw new UnsupportedFileTypeException(fileName, messages);
    }
}
