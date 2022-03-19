/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.FileRef;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(id = "fileRef", javaClass = FileRef.class, defaultForClass = true, value = "core_FileRefDatatype")
public class FileRefDatatype implements Datatype<FileRef> {

    private static final Logger log = LoggerFactory.getLogger(FileRefDatatype.class);

    @Override
    public String format(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        return format(value);
    }

    @Nullable
    @Override
    public FileRef parse(@Nullable String value) throws ParseException {
        try {
            return value == null ? null : FileRef.fromString(value);
        } catch (RuntimeException e) {
            log.warn("Cannot convert " + value + " to FileRef", e);
            return null;
        }
    }

    @Nullable
    @Override
    public FileRef parse(@Nullable String value, Locale locale) throws ParseException {
        return parse(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
