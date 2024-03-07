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

package io.jmix.flowui.kit.meta.component.preview;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Vaadin component loader for Studio view designer preview.
 * <h4>
 *     Register new loaders via SPI in {@code META-INF/services/io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader}
 * </h4>
 * @see StudioPreviewComponentProvider
 */
public interface StudioPreviewComponentLoader {

    /**
     * Define the element that this loader can load.
     */
    boolean isSuitable(Element elements);

    /**
     * Create vaadin component from component xml element.
     * @param componentElement xml element of component
     * @param viewElement xml element of view containing {@code componentElement}
     * @see Element
     */
    @Nullable
    Component load(Element componentElement, Element viewElement);

    default Optional<String> loadString(Element element, String attributeName) {
        return loadString(element, attributeName, true);
    }

    default Optional<String> loadString(Element element, String attributeName, boolean emptyToNull) {
        String attributeValue = element.attributeValue(attributeName);
        return Optional.ofNullable(emptyToNull ? Strings.emptyToNull(attributeValue) : attributeValue);
    }

    default Optional<Boolean> loadBoolean(Element element, String attributeName) {
        return loadString(element, attributeName).map(Boolean::parseBoolean);
    }

    default Optional<Integer> loadInteger(Element element, String attributeName) {
        return loadString(element, attributeName).map(Integer::parseInt);
    }

    default Optional<Double> loadDouble(Element element, String attributeName) {
        return loadString(element, attributeName).map(Double::parseDouble);
    }

    default <T extends Enum<T>> Optional<T> loadEnum(Element element, Class<T> type, String attributeName) {
        return loadString(element, attributeName).map(stringValue -> Enum.valueOf(type, stringValue));
    }

    default void loadString(Element element, String attributeName, Consumer<String> setter) {
        loadString(element, attributeName).ifPresent(setter);
    }

    default void loadBoolean(Element element, String attributeName, Consumer<Boolean> setter) {
        loadBoolean(element, attributeName).ifPresent(setter);
    }

    default void loadInteger(Element element, String attributeName, Consumer<Integer> setter) {
        loadInteger(element, attributeName).ifPresent(setter);
    }

    default void loadDouble(Element element, String attributeName, Consumer<Double> setter) {
        loadDouble(element, attributeName).ifPresent(setter);
    }

    default <T extends Enum<T>> void loadEnum(Element element, Class<T> type, String attributeName, Consumer<T> setter) {
        loadEnum(element, type, attributeName).ifPresent(setter);
    }

    default  void loadWidth(HasSize component, Element element) {
        loadString(element, "width").ifPresent(component::setWidth);
    }

    default void loadMaxWidth(HasSize component, Element element) {
        loadString(element, "maxWidth").ifPresent(component::setMaxWidth);
    }

    default void loadMinWidth(HasSize component, Element element) {
        loadString(element, "minWidth").ifPresent(component::setMinWidth);
    }

    default void loadHeight(HasSize component, Element element) {
        loadString(element, "height").ifPresent(component::setHeight);
    }

    default void loadMaxHeight(HasSize component, Element element) {
        loadString(element, "maxHeight").ifPresent(component::setMaxHeight);
    }

    default void loadMinHeight(HasSize component, Element element) {
        loadString(element, "minHeight").ifPresent(component::setMinHeight);
    }

    default void loadSizeAttributes(HasSize component, Element element) {
        loadWidth(component, element);
        loadMaxWidth(component, element);
        loadMinWidth(component, element);
        loadHeight(component, element);
        loadMaxHeight(component, element);
        loadMinHeight(component, element);
    }

    default void loadEnabled(HasEnabled component, Element element) {
        loadBoolean(element, "enabled", component::setEnabled);
    }

    default void loadClassNames(HasStyle component, Element element) {
        loadString(element, "classNames")
                .ifPresent(classNamesString -> split(classNamesString, component::addClassName));
    }

    default void split(String names, Consumer<String> setter) {
        split(names).forEach(setter);
    }

    default List<String> split(String names) {
        return Arrays.stream(names.split("[\\s,]+"))
                .filter(split -> !Strings.isNullOrEmpty(split))
                .toList();
    }
}
