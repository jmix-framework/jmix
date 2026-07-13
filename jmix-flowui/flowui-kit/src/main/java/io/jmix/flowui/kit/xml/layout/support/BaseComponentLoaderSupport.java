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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.HasAutocapitalize;
import com.vaadin.flow.component.textfield.HasAutocomplete;
import com.vaadin.flow.component.textfield.HasAutocorrect;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.HasAutofocus;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.time.Duration;
import java.util.Optional;

/**
 * Spring-independent helpers for loading common component attributes from XML elements.
 * Shared by the runtime component loaders in the flowui module
 * and the Studio preview component loaders in flowui-kit.
 */
public final class BaseComponentLoaderSupport {

    private BaseComponentLoaderSupport() {
    }

    public static void loadSpacing(ThemableLayout layout, Element element) {
        BaseLoaderSupport.loadBoolean(element, "spacing", layout::setSpacing);
    }

    public static void loadMargin(ThemableLayout layout, Element element) {
        BaseLoaderSupport.loadBoolean(element, "margin", layout::setMargin);
    }

    public static void loadPadding(ThemableLayout layout, Element element) {
        BaseLoaderSupport.loadBoolean(element, "padding", layout::setPadding);
    }

    public static void loadBoxSizing(ThemableLayout layout, Element element) {
        BaseLoaderSupport.loadEnum(element, BoxSizing.class, "boxSizing", layout::setBoxSizing);
    }

    public static void loadThemableAttributes(ThemableLayout layout, Element element) {
        loadSpacing(layout, element);
        loadMargin(layout, element);
        loadPadding(layout, element);
        loadBoxSizing(layout, element);
    }

    public static void loadAlignItems(FlexComponent component, Element element) {
        BaseLoaderSupport.loadEnum(element, FlexComponent.Alignment.class, "alignItems", component::setAlignItems);
    }

    public static void loadJustifyContent(FlexComponent component, Element element) {
        BaseLoaderSupport.loadEnum(element, FlexComponent.JustifyContentMode.class, "justifyContent", component::setJustifyContentMode);
    }

    public static void loadFlexibleAttributes(FlexComponent component, Element element) {
        loadAlignItems(component, element);
        loadJustifyContent(component, element);
        loadEnabled(component, element);
        loadClassNames(component, element);
        loadSizeAttributes(component, element);
    }

    public static void loadValueChangeMode(HasValueChangeMode component, Element element) {
        BaseLoaderSupport.loadEnum(element, ValueChangeMode.class, "valueChangeMode", component::setValueChangeMode);
        BaseLoaderSupport.loadInteger(element, "valueChangeTimeout", component::setValueChangeTimeout);
    }

    public static void loadThemeNames(HasTheme component, Element element) {
        BaseLoaderSupport.loadString(element, "themeNames")
                .ifPresent(themesString -> BaseLoaderSupport.split(themesString, component::addThemeName));
    }

    public static void loadClassNames(HasStyle component, Element element) {
        BaseLoaderSupport.loadString(element, "classNames")
                .ifPresent(classNamesString -> BaseLoaderSupport.split(classNamesString, component::addClassName));
    }

    public static void loadThemeList(Component component, Element element) {
        BaseLoaderSupport.loadString(element, "themeNames")
                .ifPresent(themeNamesString -> BaseLoaderSupport.split(themeNamesString, component.getElement().getThemeList()::add));
    }

    public static void loadValueAndElementAttributes(HasValueAndElement<?, ?> component, Element element) {
        BaseLoaderSupport.loadBoolean(element, "readOnly", component::setReadOnly);
    }

    public static void loadAutofocus(HasAutofocus component, Element element) {
        BaseLoaderSupport.loadBoolean(element, "autofocus", component::setAutofocus);
    }

    public static void loadAutocomplete(HasAutocomplete component, Element element) {
        BaseLoaderSupport.loadEnum(element, Autocomplete.class, "autocomplete", component::setAutocomplete);
    }

    public static void loadAutocapitalize(HasAutocapitalize component, Element element) {
        BaseLoaderSupport.loadEnum(element, Autocapitalize.class, "autocapitalize", component::setAutocapitalize);
    }

    public static void loadAutocorrect(HasAutocorrect component, Element element) {
        BaseLoaderSupport.loadBoolean(element, "autocorrect", component::setAutocorrect);
    }

    public static void loadEnabled(HasEnabled component, Element element) {
        BaseLoaderSupport.loadBoolean(element, "enabled", component::setEnabled);
    }

    public static void loadWhiteSpace(HasText component, Element element) {
        BaseLoaderSupport.loadEnum(element, HasText.WhiteSpace.class, "whiteSpace", component::setWhiteSpace);
    }

    public static void loadWidth(HasSize component, Element element) {
        BaseLoaderSupport.loadString(element, "width")
                .ifPresent(component::setWidth);
    }

    public static void loadMaxWidth(HasSize component, Element element) {
        BaseLoaderSupport.loadString(element, "maxWidth")
                .ifPresent(component::setMaxWidth);
    }

    public static void loadMinWidth(HasSize component, Element element) {
        BaseLoaderSupport.loadString(element, "minWidth")
                .ifPresent(component::setMinWidth);
    }

    public static void loadHeight(HasSize component, Element element) {
        BaseLoaderSupport.loadString(element, "height")
                .ifPresent(component::setHeight);
    }

    public static void loadMaxHeight(HasSize component, Element element) {
        BaseLoaderSupport.loadString(element, "maxHeight")
                .ifPresent(component::setMaxHeight);
    }

    public static void loadMinHeight(HasSize component, Element element) {
        BaseLoaderSupport.loadString(element, "minHeight")
                .ifPresent(component::setMinHeight);
    }

    public static void loadSizeAttributes(HasSize component, Element element) {
        loadWidth(component, element);
        loadMaxWidth(component, element);
        loadMinWidth(component, element);
        loadHeight(component, element);
        loadMaxHeight(component, element);
        loadMinHeight(component, element);
    }

    public static Optional<Duration> loadDuration(Element element, String attributeName) {
        return BaseLoaderSupport.loadString(element, attributeName)
                .map(stepString -> {
                    Duration step;

                    if (stepString.endsWith("h")) {
                        step = Duration.ofHours(Long.parseLong(StringUtils.chop(stepString)));
                    } else if (stepString.endsWith("m")) {
                        step = Duration.ofMinutes(Long.parseLong(StringUtils.chop(stepString)));
                    } else if (stepString.endsWith("s")) {
                        step = Duration.ofSeconds(Long.parseLong(StringUtils.chop(stepString)));
                    } else {
                        step = Duration.ofMinutes(Long.parseLong(stepString));
                    }

                    return step;
                });
    }

    /**
     * Loads an {@link Icon} from the given {@link Element}.
     * The method tries to retrieve the "icon" attribute value from the element and,
     * if present, parses it into an {@link Icon}.
     *
     * @param element the XML element from which to load the icon
     * @return an {@link Optional} containing the parsed {@link Icon} if the "icon" attribute
     * is present and valid, or an empty {@link Optional} otherwise
     */
    public static Optional<Icon> loadIconSetIcon(Element element) {
        return loadIconSetIcon(element, "icon");
    }

    /**
     * Loads an {@link Icon} from the given {@link Element} for the attribute
     * with the given name and, if present, parses it into an {@link Icon}.
     *
     * @param element       the XML element from which to load the icon
     * @param attributeName the name of the attribute value to be returned
     * @return an {@link Optional} containing the parsed {@link Icon} if the
     * attribute value is present and valid, or an empty {@link Optional} otherwise
     */
    public static Optional<Icon> loadIconSetIcon(Element element, String attributeName) {
        return BaseLoaderSupport.loadString(element, attributeName)
                .map(ComponentUtils::parseIcon);
    }
}
