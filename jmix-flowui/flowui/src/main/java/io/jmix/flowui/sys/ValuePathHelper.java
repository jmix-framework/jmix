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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to format and parse component paths.
 */
public final class ValuePathHelper {

    private ValuePathHelper() {
    }

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

    public static String[] parse(String path) {
        if (!path.contains(".") && !path.contains("[")) {
            return new String[] {path};
        }

        if (!path.contains("[")) {
            return path.split("\\.");
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

    public static String pathSuffix(String[] elements) {
        String[] subArray = ArrayUtils.subarray(elements, 1, elements.length);
        return ValuePathHelper.format(subArray);
    }

    public static String pathPrefix(String[] elements) {
        return pathPrefix(elements, 1);
    }

    public static String pathPrefix(String[] elements, int beforeLastElements) {
        String[] subPath = ArrayUtils.subarray(elements, 0, elements.length - beforeLastElements);
        return ValuePathHelper.format(subPath);
    }
}