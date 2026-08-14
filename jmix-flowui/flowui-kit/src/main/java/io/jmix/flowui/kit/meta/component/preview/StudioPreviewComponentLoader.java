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

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.Tooltip;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import io.jmix.flowui.kit.xml.layout.support.ComponentLoaderUtils;
import io.jmix.flowui.kit.xml.layout.support.LoaderUtils;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

/**
 * Vaadin component loader for Studio view designer preview.
 * <p>
 * <b>Register new loaders via SPI in
 * {@code META-INF/services/io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader}</b>
 * </p>
 *
 * @see StudioPreviewComponentProvider
 */
public interface StudioPreviewComponentLoader {

    String MAIN_VIEW_SCHEMA = "http://jmix.io/schema/flowui/main-view";
    String MAIN_VIEW_TAB_MODE_SCHEMA = "http://jmix.io/schema/tabmod/main-view";

    String VIEW_SCHEMA = "http://jmix.io/schema/flowui/view";
    String FRAGMENT_SCHEMA = "http://jmix.io/schema/flowui/fragment";

    List<String> VIEW_OR_FRAGMENT_SCHEMAS = List.of(
            VIEW_SCHEMA, FRAGMENT_SCHEMA,
            MAIN_VIEW_SCHEMA, MAIN_VIEW_TAB_MODE_SCHEMA
    );

    default boolean hasViewOrFragmentSchema(Element element) {
        return VIEW_OR_FRAGMENT_SCHEMAS.contains(element.getNamespaceURI());
    }

    /**
     * Define the element that this loader can load.
     */
    boolean isSupported(Element element);

    /**
     * Create vaadin component from component xml element.
     *
     * @param componentElement xml element of component
     * @param viewElement      xml element of view containing {@code componentElement}
     * @see Element
     */
    @Nullable
    Component load(Element componentElement, Element viewElement);

    /**
     * Create vaadin component from component xml element, with access to Studio-resolved context
     * (e.g. localized messages, entity property captions).
     *
     * @param componentElement xml element of component
     * @param viewElement      xml element of view containing {@code componentElement}
     * @param environment      Studio-side environment; {@link StudioPreviewEnvironment#NOOP} when unavailable
     * @see Element
     */
    @Nullable
    default Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        return load(componentElement, viewElement);
    }

    default Optional<String> loadString(Element element, String attributeName) {
        return LoaderUtils.loadString(element, attributeName);
    }

    default Optional<String> loadString(Element element, String attributeName, boolean emptyToNull) {
        return LoaderUtils.loadString(element, attributeName, emptyToNull);
    }

    default Optional<Boolean> loadBoolean(Element element, String attributeName) {
        return LoaderUtils.loadBoolean(element, attributeName);
    }

    default Optional<Integer> loadInteger(Element element, String attributeName) {
        return LoaderUtils.loadInteger(element, attributeName);
    }

    default Optional<Double> loadDouble(Element element, String attributeName) {
        return LoaderUtils.loadDouble(element, attributeName);
    }

    default <T extends Enum<T>> Optional<T> loadEnum(Element element, Class<T> type, String attributeName) {
        return LoaderUtils.loadEnum(element, type, attributeName);
    }

    default void loadString(Element element, String attributeName, Consumer<String> setter) {
        LoaderUtils.loadString(element, attributeName, setter);
    }

    default void loadBoolean(Element element, String attributeName, Consumer<Boolean> setter) {
        LoaderUtils.loadBoolean(element, attributeName, setter);
    }

    default void loadInteger(Element element, String attributeName, Consumer<Integer> setter) {
        LoaderUtils.loadInteger(element, attributeName, setter);
    }

    default void loadDouble(Element element, String attributeName, Consumer<Double> setter) {
        LoaderUtils.loadDouble(element, attributeName, setter);
    }

    default void loadLocalizedString(Element element, String attributeName,
                                     StudioPreviewEnvironment environment, Consumer<String> setter) {
        LoaderUtils.loadString(element, attributeName)
                .map(value -> PreviewActionSupport.resolveText(environment, value))
                .ifPresent(setter);
    }

    default <T extends Enum<T>> void loadEnum(Element element, Class<T> type, String attributeName, Consumer<T> setter) {
        LoaderUtils.loadEnum(element, type, attributeName, setter);
    }

    default void loadWidth(HasSize component, Element element) {
        ComponentLoaderUtils.loadWidth(component, element);
    }

    default void loadMaxWidth(HasSize component, Element element) {
        ComponentLoaderUtils.loadMaxWidth(component, element);
    }

    default void loadMinWidth(HasSize component, Element element) {
        ComponentLoaderUtils.loadMinWidth(component, element);
    }

    default void loadHeight(HasSize component, Element element) {
        ComponentLoaderUtils.loadHeight(component, element);
    }

    default void loadMaxHeight(HasSize component, Element element) {
        ComponentLoaderUtils.loadMaxHeight(component, element);
    }

    default void loadMinHeight(HasSize component, Element element) {
        ComponentLoaderUtils.loadMinHeight(component, element);
    }

    default void loadSizeAttributes(HasSize component, Element element) {
        ComponentLoaderUtils.loadSizeAttributes(component, element);
    }

    default void loadEnabled(HasEnabled component, Element element) {
        ComponentLoaderUtils.loadEnabled(component, element);
    }

    default void loadClassNames(HasStyle component, Element element) {
        ComponentLoaderUtils.loadClassNames(component, element);
    }

    default void loadThemeNames(HasTheme component, Element element) {
        ComponentLoaderUtils.loadThemeNames(component, element);
    }

    default void split(String names, Consumer<String> setter) {
        LoaderUtils.split(names, setter);
    }

    default List<String> split(String names) {
        return LoaderUtils.split(names);
    }

    /**
     * Applies the attributes that are common to all preview components,
     * based on the interfaces implemented by the {@code component}.
     */
    default void loadComponentBaseAttributes(Component component, Element element) {
        LoaderUtils.loadString(element, "id", component::setId);
        LoaderUtils.loadBoolean(element, "visible", component::setVisible);
        loadCss(component, element);
        loadSelfAlignment(component, element, "alignSelf", "align-self");
        loadSelfAlignment(component, element, "justifySelf", "justify-self");
        if (component instanceof HasSize hasSize) {
            ComponentLoaderUtils.loadSizeAttributes(hasSize, element);
        }
        if (component instanceof HasEnabled hasEnabled) {
            ComponentLoaderUtils.loadEnabled(hasEnabled, element);
        }
        // Component implements HasStyle, so class names are loaded unconditionally.
        ComponentLoaderUtils.loadClassNames(component, element);
        if (component instanceof HasTheme hasTheme) {
            ComponentLoaderUtils.loadThemeNames(hasTheme, element);
        }
        if (component instanceof ThemableLayout themableLayout) {
            ComponentLoaderUtils.loadThemableAttributes(themableLayout, element);
        }
        if (component instanceof FlexComponent flexComponent) {
            ComponentLoaderUtils.loadFlexibleAttributes(flexComponent, element);
        }
    }

    /**
     * Applies the input-field attributes shared by all field-like components, based on the
     * interfaces the {@code component} implements: {@code label} (falling back to the entity
     * property caption for data-bound fields), {@code placeholder}, {@code helperText},
     * {@code readOnly}, {@code required} and the {@code <tooltip>} sub-element.
     */
    default void loadFieldAttributes(Component component, Element element, StudioPreviewEnvironment environment) {
        if (component instanceof HasLabel hasLabel) {
            loadLocalizedString(element, "label", environment, hasLabel::setLabel);
            if (hasLabel.getLabel() == null) {
                propertyCaption(element, environment).ifPresent(hasLabel::setLabel);
            }
        }
        if (component instanceof HasPlaceholder hasPlaceholder) {
            loadLocalizedString(element, "placeholder", environment, hasPlaceholder::setPlaceholder);
        }
        if (component instanceof HasHelper hasHelper) {
            loadLocalizedString(element, "helperText", environment, hasHelper::setHelperText);
        }
        if (component instanceof HasValueAndElement<?, ?> hasValue) {
            loadBoolean(element, "readOnly", hasValue::setReadOnly);
            loadBoolean(element, "required", hasValue::setRequiredIndicatorVisible);
        }
        if (component instanceof HasTooltip hasTooltip) {
            loadTooltip(hasTooltip, element, environment);
        }
    }

    default void loadTooltip(HasTooltip component, Element element, StudioPreviewEnvironment environment) {
        Element tooltipElement = element.element("tooltip");
        if (tooltipElement == null) {
            return;
        }
        LoaderUtils.loadString(tooltipElement, "text")
                .map(text -> PreviewActionSupport.resolveText(environment, text))
                .ifPresent(text -> {
                    Tooltip tooltip = component.setTooltipText(text);
                    LoaderUtils.loadEnum(tooltipElement, Tooltip.TooltipPosition.class, "position")
                            .ifPresent(tooltip::setPosition);
                });
    }

    /**
     * Resolves the display caption of a data-bound component (one with a {@code property}
     * attribute) via {@link StudioPreviewEnvironment#propertyCaption}. The {@code dataContainer}
     * is looked up on the element itself and then up the ancestor chain, mirroring how the
     * runtime inherits it (e.g. from an enclosing {@code formLayout}).
     */
    default Optional<String> propertyCaption(Element element, StudioPreviewEnvironment environment) {
        return LoaderUtils.loadString(element, "property")
                .map(property -> {
                    String dataContainer = PreviewActionSupport.resolveDataContainer(element);
                    String metaClass = LoaderUtils.loadString(element, "metaClass")
                            // fragments: their containers are invisible to Studio (it only sees the
                            // host descriptor), so pass the entity class from the local <data> section
                            .orElseGet(() -> PreviewActionSupport.containerEntityClass(element, dataContainer));
                    return environment.propertyCaption(dataContainer, metaClass, property);
                });
    }

    private static void loadCss(Component component, Element element) {
        LoaderUtils.loadString(element, "css").ifPresent(css -> {
            for (String statement : css.split(";")) {
                int separator = statement.indexOf(':');
                if (separator <= 0) {
                    continue;
                }
                String name = statement.substring(0, separator).trim();
                String value = statement.substring(separator + 1).trim();
                if (!name.isEmpty()) {
                    component.getStyle().set(name, value);
                }
            }
        });
    }

    private static void loadSelfAlignment(Component component, Element element,
                                          String attributeName, String cssProperty) {
        LoaderUtils.loadString(element, attributeName)
                .map(value -> value.trim().replace('_', '-').toLowerCase(Locale.ROOT))
                .ifPresent(value -> component.getStyle().set(cssProperty, value));
    }
}
