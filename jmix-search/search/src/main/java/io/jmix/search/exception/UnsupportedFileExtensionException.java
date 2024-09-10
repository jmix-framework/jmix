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

package io.jmix.search.exception;

import io.jmix.search.utils.SupportedFileExtensions;
import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/**
 * An exception that is thrown when a user added some file with extension that there are no any known parser for.
 */
public class UnsupportedFileExtensionException extends ParserResolvingException {

    private static final String MESSAGE = "The file %s with '%s' extension is not supported. " +
            "Only following file extensions are supported %s.";

    public UnsupportedFileExtensionException(String fileName) {
        super(String.format(
                MESSAGE,
                fileName,
                FilenameUtils.getExtension(fileName),
                Arrays.stream(SupportedFileExtensions.values())
                        .map(SupportedFileExtensions::getSymbols)
                        .collect(joining(", "))));
    }
}
