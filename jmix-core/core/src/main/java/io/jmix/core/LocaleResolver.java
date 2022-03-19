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

package io.jmix.core;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * The LocaleResolver class transforms locales to strings and vice versa to support messages localization.
 */
public class LocaleResolver {

    /**
     * @param localeString the locale String or language tag.
     * @return The locale that best represents the language tag or locale string.
     * @throws NullPointerException if {@code localeString} is {@code null}
     */
    public static Locale resolve(String localeString) {
        Locale result;
        if (localeString.contains("-")) {
            result = Locale.forLanguageTag(localeString);
        } else {
            result = LocaleUtils.toLocale(localeString);
        }
        return result;
    }

    /**
     * @return A string representation of the Locale without {@code Extension}
     * or a BCP47 language tag if locale object contains {@code Script}
     */
    public static String localeToString(Locale locale) {
        if (locale == null) {
            return null;
        }
        Locale strippedLocale = locale.stripExtensions();
        return StringUtils.isEmpty(strippedLocale.getScript()) ?
                strippedLocale.toString() : strippedLocale.toLanguageTag();
    }
}
