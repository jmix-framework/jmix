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

package io.jmix.flowui.kit.meta.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.kit.meta.*;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.search:jmix-search-flowui-starter")
public interface StudioSearchComponents {

    @StudioComponent(
            name = "SearchField",
            classFqn = "io.jmix.searchflowui.component.SearchField",
            category = "Components",
            xmlElement = "searchField",
            xmlns = "http://jmix.io/schema/search/ui",
            xmlnsAlias = "search",
            icon = "io/jmix/flowui/kit/meta/icon/component/searchField.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "openMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"NAVIGATION", "DIALOG"}),
                    @StudioProperty(xmlAttribute = "searchStrategy", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.search.searching.SearchStrategy",
                            options = {"anyTermAnyField", "allTermsAnyField", "allTermsSingleField", "phrase"}),
                    @StudioProperty(xmlAttribute = "entities", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "allowedCharPattern", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "autocapitalize", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocapitalize", defaultValue = "NONE",
                            options = {"NONE", "SENTENCES", "WORDS", "CHARACTERS"}),
                    @StudioProperty(xmlAttribute = "autocomplete", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.component.textfield.Autocomplete", defaultValue = "OFF",
                            options = {"OFF", "ON", "NAME", "HONORIFIC_PREFIX", "GIVEN_NAME", "ADDITIONAL_NAME",
                                    "FAMILY_NAME", "HONORIFIC_SUFFIX", "NICKNAME", "EMAIL", "USERNAME", "NEW_PASSWORD",
                                    "CURRENT_PASSWORD", "ORGANIZATION_TITLE", "ORGANIZATION", "STREET_ADDRESS",
                                    "ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "ADDRESS_LEVEL1",
                                    "ADDRESS_LEVEL2", "ADDRESS_LEVEL3", "ADDRESS_LEVEL4", "COUNTRY", "COUNTRY_NAME",
                                    "POSTAL_CODE", "CC_NAME", "CC_GIVEN_NAME", "CC_ADDITIONAL_NAME", "CC_FAMILY_NAME",
                                    "CC_NUMBER", "CC_EXP", "CC_EXP_MONTH", "CC_EXP_YEAR", "CC_CSC", "CC_TYPE",
                                    "TRANSACTION_CURRENCY", "TRANSACTION_AMOUNT", "LANGUAGE", "BDAY", "BDAY_DAY",
                                    "BDAY_MONTH", "BDAY_YEAR", "SEX", "TEL", "TEL_COUNTRY_CODE", "TEL_NATIONAL",
                                    "TEL_AREA_CODE", "TEL_LOCAL", "TEL_LOCAL_PREFIX", "TEL_LOCAL_SUFFIX",
                                    "TEL_EXTENSION", "URL", "PHOTO",}),
                    @StudioProperty(xmlAttribute = "autocorrect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autofocus", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "autoselect", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "clearButtonVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataContainer", type = StudioPropertyType.DATA_CONTAINER_REF),
                    @StudioProperty(xmlAttribute = "datatype", type = StudioPropertyType.DATATYPE_ID,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxLength", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minLength", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "pattern", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "placeholder", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "property", type = StudioPropertyType.PROPERTY_REF,
                            typeParameter = "V"),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible",
                            type = StudioPropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(xmlAttribute = "title", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "value", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "valueChangeMode", type = StudioPropertyType.ENUMERATION,
                            classFqn = "com.vaadin.flow.data.value.ValueChangeMode", options = {"EAGER", "LAZY",
                            "TIMEOUT", "ON_BLUR", "ON_CHANGE"}),
                    @StudioProperty(xmlAttribute = "valueChangeTimeout", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            },
            propertiesBindings = {
                    @StudioPropertiesBinding(
                            source = "dataContainer",
                            item = "property"
                    )
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
            icon = "io/jmix/flowui/kit/meta/icon/component/searchField.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "searchStrategy", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.search.searching.SearchStrategy",
                            options = {"anyTermAnyField", "allTermsAnyField", "allTermsSingleField", "phrase"}),
                    @StudioProperty(xmlAttribute = "autoApply", type = StudioPropertyType.BOOLEAN),
                    @StudioProperty(xmlAttribute = "ariaLabel", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "ariaLabelledBy", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "dataLoader", type = StudioPropertyType.DATA_LOADER_REF,
                            required = true),
                    @StudioProperty(xmlAttribute = "defaultValue", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "errorMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "hasInExpression", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "helperText", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID),
                    @StudioProperty(xmlAttribute = "invalid", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "label", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "labelPosition", type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.flowui.component.SupportsLabelPosition$LabelPosition",
                            options = {"ASIDE", "TOP"}, defaultValue = "ASIDE"),
                    @StudioProperty(xmlAttribute = "labelVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "labelWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE),
                    @StudioProperty(xmlAttribute = "parameterClass", type = StudioPropertyType.STRING,
                            typeParameter = "V", required = true),
                    @StudioProperty(xmlAttribute = "parameterName", type = StudioPropertyType.STRING),
                    @StudioProperty(xmlAttribute = "readOnly", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "required", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredIndicatorVisible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "requiredMessage", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "tabIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "themeNames", type = StudioPropertyType.VALUES_LIST),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE)
            }
    )
    HorizontalLayout fullTextFilter();
}

