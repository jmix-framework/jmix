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

import com.google.common.base.Strings;
import io.jmix.core.FileRef;
import io.jmix.search.exception.EmptyFileExtensionException;
import io.jmix.search.exception.ParserResolvingException;
import io.jmix.search.exception.UnsupportedFileExtensionException;
import io.jmix.search.index.fileparsing.FileParserResolver;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.parser.Parser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("search_FileParserResolverManager")
public class FileParserResolverManager {

    protected List<FileParserResolver> fileParserResolvers;

    public FileParserResolverManager(List<FileParserResolver> fileParserResolvers) {
        this.fileParserResolvers = fileParserResolvers;
    }

    public Parser getParser(FileRef fileRef) throws ParserResolvingException {
        String fileName = fileRef.getFileName();
        String fileExtension = FilenameUtils.getExtension(fileName);
        if (Strings.isNullOrEmpty(fileExtension)) {
            throw new EmptyFileExtensionException(fileName, getSupportedExtensions());
        }

        for (FileParserResolver resolver : fileParserResolvers) {
            if (resolver.getExtension().contains(fileExtension)) {
                return resolver.getParser();
            }
        }

        throw new UnsupportedFileExtensionException(fileName, getSupportedExtensions());
    }

    protected List<String> getSupportedExtensions() {
        return fileParserResolvers
                .stream()
                .flatMap(fileParserResolver -> fileParserResolver.getExtension().stream())
                .toList();
    }
}
