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

package io.jmix.core;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parsed form of a localized string: the default value and the values of the locale entries,
 * keyed by the string form of the locale (see {@link LocaleResolver#localeToString(java.util.Locale)}).
 * Entries keep the order in which they were parsed or supplied.
 */
@Internal
public record LocalizedStringValue(String defaultValue, Map<String, String> values) {

    public static final LocalizedStringValue EMPTY = new LocalizedStringValue("", Map.of());

    public LocalizedStringValue {
        Preconditions.checkNotNullArgument(defaultValue, "defaultValue is null");
        Preconditions.checkNotNullArgument(values, "values is null");

        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    /**
     * @return true if at least one locale entry exists, even an empty one
     */
    public boolean isLocalized() {
        return !values.isEmpty();
    }

    /**
     * @return the text of the locale entry, or null if the entry is absent or empty
     */
    @Nullable
    public String getValue(String localeKey) {
        String value = values.get(localeKey);
        return value == null || value.isEmpty() ? null : value;
    }
}
