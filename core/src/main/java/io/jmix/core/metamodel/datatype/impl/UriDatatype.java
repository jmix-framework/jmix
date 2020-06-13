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

package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(id = "uri", javaClass = URI.class, defaultForClass = true, value = "core_UriDatatype")
public class UriDatatype implements Datatype<URI> {

    @Override
	public String format(@Nullable Object value) {
		return value == null ? "" : value.toString();
	}

    @Override
    public String format(@Nullable Object value, Locale locale) {
        return format(value);
    }

    @Override
    @Nullable
	public URI parse(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse " + value + " to URI", e);
        }
    }

    @Override
    @Nullable
    public URI parse(@Nullable String value, Locale locale) throws ParseException {
        return parse(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}