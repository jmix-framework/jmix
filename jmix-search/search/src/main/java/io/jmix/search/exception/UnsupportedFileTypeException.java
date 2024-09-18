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

import java.util.List;

/**
 * An exception that is thrown when a user added some file with extension that there are no any known parser for.
 */
public class UnsupportedFileTypeException extends Exception {

    private static final String MESSAGE = "The file %s can't be parsed. " +
            "Only the following file parsing criteria are supported: %s";

    /**
     * @param fileName - the name of the file which type is not supported
     * @param supportedExtensions - the list of the supported file parsing cri
     */
    public UnsupportedFileTypeException(String fileName, List<String> supportedExtensions) {
        super(String.format(
                MESSAGE,
                fileName,
                getSupportedExtensionsString(supportedExtensions)));
    }

    protected static String getSupportedExtensionsString(List<String> supportedExtensions){
        return String.join("\n", supportedExtensions);
    }
}
