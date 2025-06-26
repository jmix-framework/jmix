/*
 * Copyright 2022 Haulmont.
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
package io.jmix.flowui.sys;

import io.jmix.core.annotation.Internal;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to format and parse component paths.
 */
@Internal
public final class ValuePathHelper {

    private ValuePathHelper() {
    }

    /**
     * Formats an array of string elements into a single string that represents a component path.
     *
     * @param elements the array of strings to be formatted
     * @return the formatted string representation of the input array.
     */
    public static String format(String[] elements) {
        StringBuilder builder = new StringBuilder();

        int i = 1;
        for (String element : elements) {
            builder.append(element.contains(".") ? "[" + element + "]" : element);
            if (i != elements.length) {
                builder.append(".");
            }
            i++;
        }

        return builder.toString();
    }

    /**
     * Parses the given string path into an array of elements using specific delimiters
     * such as {@code "."}, {@code ".@"}, and {@code "["}.
     * <p>
     * The method handles the following scenarios:
     * <ul>
     *   <li>If the {@code path} contains neither {@code "."}, {@code "["}, nor {@code ".@"},
     *       the result will be a single-element array containing the input path.</li>
     *   <li>If the {@code path} contains {@code "."} but neither {@code "["} nor {@code ".@"},
     *       the input is split by {@code "."}.</li>
     *   <li>If the {@code path} contains {@code ".@"} but not {@code "["},
     *       the input is split by {@code ".@"}.</li>
     *   <li>If {@code "["} is present, the method properly handles nested structures
     *       while splitting by {@code "."} outside the brackets.</li>
     * </ul>
     *
     * @param path the input string representing a structured path
     * @return an array of {@code String} elements derived from the input path
     * @throws IllegalStateException if the path contains an invalid format
     */
    public static String[] parse(String path) {
        if (!path.contains(".") && !path.contains("[") && !path.contains(".@")) {
            return new String[]{path};
        }

        if (!path.contains("[") && !path.contains(".@")) {
            return path.split("\\.");
        }

        if (!path.contains("[")) {
            return path.split("\\.@");
        }

        List<String> elements = new ArrayList<>();

        int bracketCount = 0;

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '[') {
                bracketCount++;
                continue;
            }

            if (c == ']') {
                bracketCount--;
                continue;
            }

            if ('.' != c || bracketCount > 0)
                buffer.append(c);

            if ('.' == c && bracketCount == 0) {
                String element = buffer.toString();
                if (!StringUtils.isEmpty(element)) {
                    elements.add(element);
                } else {
                    throw new IllegalStateException("Wrong value path format");
                }
                buffer = new StringBuilder();
            }
        }
        elements.add(buffer.toString());

        return elements.toArray(new String[0]);
    }

    /**
     * Extracts a subarray of elements from the input array, starting from the second element,
     * and formats it into a single string that represents a component path.
     *
     * @param elements the input array of elements from which the subarray will be extracted
     * @return the formatted string representation of the subarray
     */
    public static String pathSuffix(String[] elements) {
        String[] subArray = ArrayUtils.subarray(elements, 1, elements.length);
        return ValuePathHelper.format(subArray);
    }

    /**
     * Extracts a prefix from the provided array of path elements by excluding
     * the last {@code 1} element. The resulting elements are then formatted
     * into a single string.
     *
     * @param elements an array of path elements
     * @return the formatted string representation of the prefix extracted from
     * the input array
     */
    public static String pathPrefix(String[] elements) {
        return pathPrefix(elements, 1);
    }

    /**
     * Extracts a prefix from the provided array of path elements by excluding the specified number of
     * elements from the end. The resulting subset of elements is then formatted into a single string.
     *
     * @param elements           an array of strings representing the path elements to process
     * @param beforeLastElements the number of elements to exclude from the end of the array
     *                           when extracting the prefix
     * @return the formatted string representation of the prefix derived from the input array
     */
    public static String pathPrefix(String[] elements, int beforeLastElements) {
        String[] subPath = ArrayUtils.subarray(elements, 0, elements.length - beforeLastElements);
        return ValuePathHelper.format(subPath);
    }
}