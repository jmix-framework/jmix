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

package io.jmix.dynattr.utils;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

/**
 * Utility class for manipulating strings in various case formats.
 */
public final class DynAttrStringUtils {

    /** Regular expression for matching camelCase strings. */
    public static final String CAMEL_CASE_REGEXP = "[a-z]+[a-z|0-9]*([A-Z][a-z|0-9]*)*";

    /** Regular expression for matching PascalCase strings. */
    public static final String PASCAL_CASE_REGEXP = "[A-Z]+[a-z|0-9]*([A-Z][a-z|0-9]*)*";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DynAttrStringUtils() {
    }

    /**
     * Converts a string to camelCase format.
     * <p>
     * This method ensures that the camelCase or PascalCase format is preserved when transforming a string into camelCase.
     * For example, 'camelCase' will not be transformed into 'camelcase'.
     *
     * @apiNote This method is designed to prevent unintentional conversion of camelCase or PascalCase strings into lowercase.
     * It is recommended for cases where maintaining the original case is crucial.
     *
     * @param str        The input string to convert.
     * @param delimiters Optional delimiters for splitting the input string.
     * @return The input string converted to camelCase format.
     */
    public static String toCamelCase(final String str, char... delimiters) {
        if (Strings.isNullOrEmpty(str) || isCamelCase(str)) {
            return str;
        } else if (isPascalCase(str)) {
            return StringUtils.uncapitalize(str);
        } else {
            return CaseUtils.toCamelCase(str, false, delimiters);
        }
    }



    /**
     * Checks if a string is in camelCase format.
     *
     * @param str The input string to check.
     * @return {@code true} if the input string is in camelCase format, {@code false} otherwise.
     */
    public static boolean isCamelCase(final String str) {
        if (str == null) {
            return false;
        }
        return str.matches(CAMEL_CASE_REGEXP);
    }

    /**
     * Checks if a string is in PascalCase format.
     *
     * @param str The input string to check.
     * @return {@code true} if the input string is in PascalCase format, {@code false} otherwise.
     */
    public static boolean isPascalCase(final String str) {
        if (str == null) {
            return false;
        }
        return str.matches(PASCAL_CASE_REGEXP);
    }
}
