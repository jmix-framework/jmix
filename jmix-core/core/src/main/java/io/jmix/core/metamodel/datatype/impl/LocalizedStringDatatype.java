/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.LocalizedStringSupport;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

/**
 * Datatype of a {@link io.jmix.core.metamodel.annotation.LocalizedString} property. The Java class stays
 * {@code String}: {@code format(value, locale)} resolves the text of the locale, {@code format(value)} returns
 * the raw stored value for transport, {@code parse} returns the entered text as a literal.
 * <p>
 * Datatypes are instantiated while {@code DatatypeRegistry} is created for {@code Metadata}, and
 * {@link LocalizedStringSupport} depends on {@code MessageTools}, which depends on {@code Metadata}; the support
 * is therefore obtained lazily to avoid a circular dependency at startup.
 */
@Internal
@DatatypeDef(id = LocalizedStringDatatype.ID, javaClass = String.class, value = "core_LocalizedStringDatatype")
public class LocalizedStringDatatype extends StringDatatype {

    public static final String ID = "localizedString";

    @Autowired
    protected ObjectProvider<LocalizedStringSupport> localizedStringSupportProvider;

    @Override
    public String format(@Nullable Object value, Locale locale) {
        return value == null ? "" : localizedStringSupportProvider.getObject().resolve((String) value, locale);
    }
}
