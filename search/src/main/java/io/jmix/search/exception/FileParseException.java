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

package io.jmix.search.exception;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class FileParseException extends Exception {

    private static final long serialVersionUID = 984427696757522707L;

    private static final String template = "Unable to parse file '%s'%s";

    public FileParseException(String fileName, Throwable cause) {
        super(createMessage(fileName, null), cause);
    }

    public FileParseException(String fileName) {
        super(createMessage(fileName, null));
    }

    public FileParseException(String fileName, @Nullable String details) {
        super(createMessage(fileName, details));
    }

    public FileParseException(String fileName, @Nullable String details, Throwable cause) {
        super(createMessage(fileName, details), cause);
    }

    private static String createMessage(String fileName, @Nullable String details) {
        details = StringUtils.isNotBlank(details) ? ": " + details : "";
        return String.format(template, fileName, details);
    }
}
