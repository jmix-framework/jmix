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

package io.jmix.searchflowui.kit.meta.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.StudioComponent;
import io.jmix.flowui.kit.meta.StudioProperty;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;
import io.jmix.flowui.kit.meta.StudioXmlElements;

@StudioUiKit
public interface StudioSearchComponents {

    @StudioComponent(
            name = "SearchField",
            classFqn = "io.jmix.searchflowui.component.SearchField",
            category = "Components",
            xmlElement = StudioXmlElements.SEARCH_FIELD,
            xmlns = "http://jmix.io/schema/search/ui",
            xmlnsAlias = "search",
            icon = "io/jmix/searchflowui/kit/meta/icon/component/searchField.svg",
            propertyGroups = StudioSearchPropertyGroups.SearchFieldComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.AUTOFOCUS, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENTITIES, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HELPER_TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.OPEN_MODE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"NAVIGATION", "DIALOG"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PLACEHOLDER, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SEARCH_SIZE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SEARCH_STRATEGY, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.search.searching.SearchStrategy",
                            options = {"anyTermAnyField", "startsWith", "phrase"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TITLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_MODE, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VALUE_CHANGE_TIMEOUT, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SEARCH_BUTTON_VISIBLE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SETTINGS_BUTTON_VISIBLE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
            }
    )
    TextField searchField();

    @StudioComponent(
            name = "Full-text filter",
            classFqn = "io.jmix.searchflowui.component.FullTextFilter",
            category = "Components",
            xmlElement = StudioXmlElements.FULL_TEXT_FILTER,
            xmlns = "http://jmix.io/schema/search/ui",
            xmlnsAlias = "search",
            icon = "io/jmix/searchflowui/kit/meta/icon/component/searchField.svg",
            propertyGroups = StudioSearchPropertyGroups.FullTextFilterComponent.class,
            properties = {
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ALIGN_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.JUSTIFY_SELF, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.kit.component.Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.AUTO_APPLY, type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABEL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ARIA_LABELLED_BY, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CSS, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.CLASS_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.COLSPAN, category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DATA_LOADER, category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF,
                            required = true),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.DEFAULT_VALUE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ENABLED, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ERROR_MESSAGE, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.HELPER_TEXT, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.ID, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL_POSITION, category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "ASIDE"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL_VISIBLE, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.LABEL_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MAX_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_HEIGHT, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.MIN_WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.PARAMETER_NAME, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.READ_ONLY, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REQUIRED, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.REQUIRED_MESSAGE, category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.SEARCH_STRATEGY, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.search.searching.SearchStrategy",
                            options = {"anyTermAnyField", "allTermsAnyField", "allTermsSingleField", "phrase"}),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.TAB_INDEX, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.THEME_NAMES, category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.FOCUS_SHORTCUT, type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.VISIBLE, category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = StudioXmlAttributes.WIDTH, category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    HorizontalLayout fullTextFilter();
}

