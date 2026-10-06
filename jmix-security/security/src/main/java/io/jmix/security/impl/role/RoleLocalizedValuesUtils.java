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

package io.jmix.security.impl.role;

import com.google.common.collect.Maps;
import io.jmix.core.LocaleResolver;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Reads and writes a bundle of the localized values of a role, its {@code localizedNames} or
 * {@code localizedDescriptions}: a {@link Properties} text whose keys are locales as
 * {@link LocaleResolver#localeToString} gives them, such as {@code en=Administrator}.
 */
@NullMarked
public final class RoleLocalizedValuesUtils {

    private RoleLocalizedValuesUtils() {
    }

    /**
     * @return the entries of the bundle, or none for {@code null} or an empty bundle
     * @throws IllegalArgumentException if the bundle cannot be read, such as one with a malformed Unicode escape
     */
    public static Map<String, String> read(@Nullable String bundle) {
        if (bundle == null || bundle.isEmpty()) {
            return Map.of();
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(bundle));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Maps.fromProperties(properties);
    }

    /**
     * @return the bundle of the entries, or {@code null} if there are none
     */
    @Nullable
    public static String write(Map<String, String> entries) {
        if (entries.isEmpty()) {
            return null;
        }

        Properties properties = new Properties();
        properties.putAll(entries);
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // The first line is the date comment that store adds; an entry never starts a line with '#', which store
        // escapes, so dropping the comment lines keeps every entry. The lines are joined the same way on every
        // platform.
        return writer.toString().lines()
                .filter(line -> !line.startsWith("#"))
                .collect(Collectors.joining("\n", "", "\n"));
    }
}
