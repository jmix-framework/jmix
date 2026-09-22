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
import io.jmix.core.security.CurrentAuthentication;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads, writes and resolves localized string values: a default value followed by {@code locale=value}
 * lines, or a {@code msg://} reference. The only class that knows the stored format.
 */
@Internal
@Component("core_LocalizedStringSupport")
public class LocalizedStringSupport {

    protected static final String LINE_SEPARATOR = "\n";

    /**
     * A locale key in the form of {@link LocaleResolver#localeToString(Locale)}.
     */
    protected static final String ENTRY_KEY_REGEX = "[A-Za-z]{2,3}(?:[_-][A-Za-z0-9]{2,8})*";

    protected static final Pattern ENTRY_KEY_PATTERN = Pattern.compile("^" + ENTRY_KEY_REGEX + "$");

    /**
     * A line that opens a locale entry: a locale key followed by {@code =}. Whitespace around the key and the
     * sign is tolerated on reading, so that a value written by hand or imported from another format is still
     * displayed correctly. Such an entry is invisible to database sorting and filtering, which match the
     * canonical {@code key=value} form only.
     */
    protected static final Pattern ENTRY_PATTERN =
            Pattern.compile("^\\s*(" + ENTRY_KEY_REGEX + ")\\s*=(.*)$");

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected MessageTools messageTools;

    public LocalizedStringValue parse(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return LocalizedStringValue.EMPTY;
        }

        StringBuilder defaultValue = new StringBuilder();
        // Tracks whether a line has been appended, so that blank leading lines are kept.
        boolean defaultValueStarted = false;
        Map<String, StringBuilder> entries = new LinkedHashMap<>();
        StringBuilder current = null;

        for (String line : splitLines(raw)) {
            Matcher matcher = ENTRY_PATTERN.matcher(line);

            if (matcher.matches()) {
                String key = matcher.group(1);

                if (entries.containsKey(key)) {
                    // The database expression takes the first occurrence of a key, so a duplicate entry and
                    // its continuation lines are collected into a detached builder and dropped.
                    current = new StringBuilder();
                } else {
                    current = new StringBuilder(matcher.group(2).stripLeading());
                    entries.put(key, current);
                }

            } else if (current == null) {
                if (defaultValueStarted) {
                    defaultValue.append(LINE_SEPARATOR);
                }

                defaultValue.append(line);
                defaultValueStarted = true;

            } else {
                current.append(LINE_SEPARATOR).append(line);
            }
        }

        Map<String, String> values = new LinkedHashMap<>();
        entries.forEach((key, text) -> values.put(key, text.toString()));
        return new LocalizedStringValue(defaultValue.toString(), values);
    }

    /**
     * Writes the canonical form: the default value on the first line, then one {@code key=value} line per
     * non-empty entry. A value without entries is written as the plain default value.
     */
    public String format(LocalizedStringValue value) {
        StringBuilder sb = new StringBuilder(value.defaultValue());

        for (Map.Entry<String, String> entry : value.values().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                sb.append(LINE_SEPARATOR)
                        .append(entry.getKey())
                        .append('=')
                        .append(entry.getValue());
            }
        }

        return sb.toString();
    }

    /**
     * Resolves the text for the locale: a {@code msg://} reference through the message bundles, otherwise the
     * first present entry of {@link #resolutionKeys(Locale)}, otherwise the default value.
     */
    public String resolve(@Nullable String raw, Locale locale) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        if (isMessageReference(raw)) {
            return messageTools.loadString(raw, locale);
        }

        LocalizedStringValue value = parse(raw);
        if (!value.isLocalized()) {
            return value.defaultValue();
        }

        for (String key : resolutionKeys(locale)) {
            String text = value.getValue(key);
            if (text != null) {
                return text;
            }
        }

        return value.defaultValue();
    }

    /**
     * Resolves the text for the current user's locale, or for the application default locale when no user
     * is authenticated.
     */
    public String resolve(@Nullable String raw) {
        return resolve(raw, getCurrentLocale());
    }

    /**
     * @return the locale keys tried in order: the exact key, the language alone, the application default locale
     */
    public List<String> resolutionKeys(Locale locale) {
        Set<String> keys = new LinkedHashSet<>();
        keys.add(localeKey(locale));

        if (!locale.getLanguage().isEmpty()) {
            keys.add(locale.getLanguage());
        }

        keys.add(localeKey(messageTools.getDefaultLocale()));
        return new ArrayList<>(keys);
    }

    public String localeKey(Locale locale) {
        return LocaleResolver.localeToString(locale);
    }

    /**
     * @return true if a line starting with this key and {@code =} would be read as a locale entry. A key that
     * fails the check cannot occur in a stored value, so nothing has to look for it.
     */
    public boolean isEntryKey(String key) {
        return ENTRY_KEY_PATTERN.matcher(key).matches();
    }

    public boolean isMessageReference(@Nullable String raw) {
        return raw != null && raw.startsWith(MessageTools.MARK);
    }

    public boolean isLocalized(@Nullable String raw) {
        return raw != null && !isMessageReference(raw) && parse(raw).isLocalized();
    }

    /**
     * @return true if any line of the text would be read as a locale entry
     */
    public boolean containsEntryLine(@Nullable String text) {
        if (text == null) {
            return false;
        }

        for (String line : splitLines(text)) {
            if (ENTRY_PATTERN.matcher(line).matches()) {
                return true;
            }
        }

        return false;
    }

    public Locale getCurrentLocale() {
        return currentAuthentication.isSet()
                ? currentAuthentication.getLocale()
                : messageTools.getDefaultLocale();
    }

    protected String[] splitLines(String text) {
        return text.replace("\r\n", LINE_SEPARATOR)
                .split(LINE_SEPARATOR, -1);
    }
}
