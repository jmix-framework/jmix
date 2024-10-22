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
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;

@StudioUiKit
public interface StudioSearchComponents {

    @StudioComponent(
            name = "SearchField",
            classFqn = "io.jmix.searchflowui.component.SearchField",
            category = "Components",
            xmlElement = "searchField",
            xmlns = "http://jmix.io/schema/search/ui",
            xmlnsAlias = "search",
            icon = "io/jmix/searchflowui/kit/meta/icon/component/searchField.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "entities", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "openMode", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"NAVIGATION", "DIALOG"}),
                    @StudioProperty(xmlAttribute = "placeholder", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "searchSize", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "searchStrategy", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.search.searching.SearchStrategy",
                            options = {"anyTermAnyField", "startsWith", "phrase"}),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "focusShortcut", type = StudioPropertyType.SHORTCUT_COMBINATION),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    TextField searchField();

    @StudioComponent(
            name = "Full-text filter",
            classFqn = "io.jmix.searchflowui.component.FullTextFilter",
            category = "Components",
            xmlElement = "fullTextFilter",
            xmlns = "http://jmix.io/schema/search/ui",
            xmlnsAlias = "search",
            icon = "io/jmix/searchflowui/kit/meta/icon/component/searchField.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "autoApply", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", category = StudioProperty.Category.POSITION, type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataLoader", category = StudioProperty.Category.DATA_BINDING, type = StudioPropertyType.DATA_LOADER_REF,
                            required = true),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "helperText", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "label", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelPosition", category = StudioProperty.Category.POSITION, type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "ASIDE"),
                    @StudioProperty(xmlAttribute = "labelVisible", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "labelWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "maxWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minHeight", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "minWidth", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"}),
                    @StudioProperty(xmlAttribute = "parameterName", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "readOnly", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", category = StudioProperty.Category.VALIDATION, type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "searchStrategy", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.search.searching.SearchStrategy",
                            options = {"anyTermAnyField", "allTermsAnyField", "allTermsSingleField", "phrase"}),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "themeNames", category = StudioProperty.Category.LOOK_AND_FEEL, type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", category = StudioProperty.Category.GENERAL, type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", category = StudioProperty.Category.SIZE, type = StudioPropertyType.SIZE, options = {"AUTO", "100%"})
            }
    )
    HorizontalLayout fullTextFilter();
}

