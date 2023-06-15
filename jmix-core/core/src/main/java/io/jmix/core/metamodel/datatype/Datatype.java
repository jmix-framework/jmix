/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.metamodel.datatype;

import io.jmix.core.metamodel.datatype.impl.DatatypeDefUtils;

import org.springframework.lang.Nullable;
import java.text.ParseException;
import java.util.Locale;

/**
 * Represents a data type of an entity property.
 */
public interface Datatype<T> {

    /** Converts value to String. Returns an empty string for null value.  */
    String format(@Nullable Object value);

    /** Converts value to String taking into account local formats. Returns an empty string for null value. */
    String format(@Nullable Object value, Locale locale);

    /** Parses value from String */
    @Nullable
    T parse(@Nullable String value) throws ParseException;

    /** Parses value from String taking into account local formats */
    @Nullable
    T parse(@Nullable String value, Locale locale) throws ParseException;

    /** Unique ID of the Datatype used for registration */
    default String getId() {
        return DatatypeDefUtils.getId(this);
    }

    /** Java class representing this Datatype */
    default Class getJavaClass() {
        return DatatypeDefUtils.getJavaClass(this);
    }
}