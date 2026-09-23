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

package io.jmix.flowui.kit.xml.layout.support;

import com.google.common.base.Strings;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Spring-independent helpers for reading typed values from XML attributes.
 * Shared by the runtime component loaders in the flowui module
 * and the Studio preview component loaders in flowui-kit.
 */
public final class LoaderUtils {

    private LoaderUtils() {
    }

    public static Optional<String> loadString(Element element, String attributeName) {
        return loadString(element, attributeName, true);
    }

    public static Optional<String> loadString(Element element, String attributeName, boolean emptyToNull) {
        String attributeValue = element.attributeValue(attributeName);
        return Optional.ofNullable(emptyToNull ? Strings.emptyToNull(attributeValue) : attributeValue);
    }

    public static Optional<Boolean> loadBoolean(Element element, String attributeName) {
        return loadString(element, attributeName).map(Boolean::parseBoolean);
    }

    public static Optional<Integer> loadInteger(Element element, String attributeName) {
        return loadString(element, attributeName).map(Integer::parseInt);
    }

    public static Optional<Double> loadDouble(Element element, String attributeName) {
        return loadString(element, attributeName).map(Double::parseDouble);
    }

    public static <T extends Enum<T>> Optional<T> loadEnum(Element element, Class<T> type, String attributeName) {
        return loadString(element, attributeName).map(stringValue -> Enum.valueOf(type, stringValue));
    }

    public static void loadString(Element element, String attributeName, Consumer<String> setter) {
        loadString(element, attributeName).ifPresent(setter);
    }

    public static void loadBoolean(Element element, String attributeName, Consumer<Boolean> setter) {
        loadBoolean(element, attributeName).ifPresent(setter);
    }

    public static void loadInteger(Element element, String attributeName, Consumer<Integer> setter) {
        loadInteger(element, attributeName).ifPresent(setter);
    }

    public static void loadDouble(Element element, String attributeName, Consumer<Double> setter) {
        loadDouble(element, attributeName).ifPresent(setter);
    }

    public static <T extends Enum<T>> void loadEnum(Element element, Class<T> type, String attributeName,
                                                    Consumer<T> setter) {
        loadEnum(element, type, attributeName).ifPresent(setter);
    }

    public static void split(String names, Consumer<String> setter) {
        split(names).forEach(setter);
    }

    public static List<String> split(String names) {
        return Arrays.stream(names.split("[\\s,]+"))
                .filter(split -> !Strings.isNullOrEmpty(split))
                .toList();
    }

    /**
     * Parses CSS declarations separated by semicolons and passes every property to the given setter.
     *
     * @param css    CSS declarations separated by semicolons
     * @param setter consumer of a property name and its value
     * @throws IllegalArgumentException if a declaration has no separator or an empty property name
     */
    public static void applyCss(String css, BiConsumer<String, String> setter) {
        for (String propertyStatement : css.split(";")) {
            if (propertyStatement.isBlank()) {
                continue;
            }

            int separatorIndex = propertyStatement.indexOf(':');
            if (separatorIndex < 0) {
                throw new IllegalArgumentException("Incorrect CSS string: " + css);
            }

            String propertyName = propertyStatement.substring(0, separatorIndex).trim();
            String propertyValue = propertyStatement.substring(separatorIndex + 1).trim();

            if (propertyName.isEmpty()) {
                throw new IllegalArgumentException("Incorrect CSS string, empty property name: " + css);
            }

            setter.accept(propertyName, propertyValue);
        }
    }
}
