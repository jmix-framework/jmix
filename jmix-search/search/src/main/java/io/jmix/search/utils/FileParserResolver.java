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
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.parser.Parser;
import org.springframework.stereotype.Component;

@Component("search_FileParserResolver")
public class FileParserResolver {

    public Parser getParser(FileRef fileRef) throws ParserResolvingException {
        String fileName = fileRef.getFileName();
        String fileExtension = FilenameUtils.getExtension(fileName);
        if (Strings.isNullOrEmpty(fileExtension)){
            throw new EmptyFileExtensionException(fileName);
        }
        for (SupportedFileExtensions extension : SupportedFileExtensions.values()) {
            if (extension.getSymbols().equals(fileExtension)) {
                return extension.getParser();
            }
        }
        throw new UnsupportedFileExtensionException(fileName);
    }
}
