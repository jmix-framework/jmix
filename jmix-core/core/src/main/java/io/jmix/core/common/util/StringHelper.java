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
package io.jmix.core.common.util;

public final class StringHelper {

    private StringHelper() {
    }

    /**
     * Removes extra (more than one) whitespace characters from any place of the string.<br>
     * Examples:<br>
     * " aaa  bbb   ccc ddd " becomes "aaa bbb ccc ddd"
     *
    */
    public static String removeExtraSpaces(String str) {
        if (str == null || str.isEmpty())
            return str;

        StringBuilder sb = new StringBuilder();

        int pos = 0;
        boolean prevWS = true;

        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i)) || i == str.length() - 1) {
                if (!prevWS) {
                    sb.append(str, pos, i).append(str.charAt(i));
                }
                prevWS = true;
            } else {
                if (prevWS)
                    pos = i;
                prevWS = false;
            }
        }
        if (Character.isWhitespace(sb.charAt(sb.length() - 1)))
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Converts a string with underscores to camel case. For example:
     * <pre>
     *     foo_bar      - fooBar
     *     FOO_BAR      - fooBar
     *     foo_bar_baz  - fooBarBaz
     *     foo__bar     - fooBar
     *     _foo_bar     - _fooBar
     *     foo          - foo
     * </pre>
     */
    public static String underscoreToCamelCase(String str) {
        if (str == null || str.isEmpty())
            return str;

        StringBuilder sb = new StringBuilder();
        String s = str;
        while (s.charAt(0) == '_') {
            sb.append('_');
            s = s.substring(1);
        }

        String[] parts = s.split("_");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.length() > 0) {
                if (i == 0) {
                    sb.append(part.toLowerCase());
                } else {
                    sb.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) {
                        sb.append(part.substring(1).toLowerCase());
                    }
                }
            }
        }
        return sb.toString();
    }
}