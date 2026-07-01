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

package io.jmix.searchflowui.kit.meta.component;

import io.jmix.flowui.kit.meta.*;

@StudioAPI
final class StudioSearchPropertyGroups {

    private StudioSearchPropertyGroups() {
    }

    @StudioPropertyGroup(
            properties = {
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.ENTITIES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.OPEN_MODE,
                            type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.GENERAL,
                            classFqn = "io.jmix.flowui.view.OpenMode",
                            options = {"NAVIGATION", "DIALOG"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SEARCH_SIZE,
                            type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SEARCH_STRATEGY,
                            type = StudioPropertyType.ENUMERATION,
                            classFqn = "io.jmix.search.searching.SearchStrategy",
                            options = {"anyTermAnyField", "startsWith", "phrase"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.THEME_NAMES,
                            type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            options = {"small", "align-center", "align-right", "helper-above-field",
                                    "always-float-label"}),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SEARCH_BUTTON_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true"),
                    @StudioProperty(
                            xmlAttribute = StudioXmlAttributes.SETTINGS_BUTTON_VISIBLE,
                            type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            defaultValue = "true")
            }
    )
    public interface SearchFieldComponent extends StudioPropertyGroups.SizedComponentDefaultProperties,
            StudioPropertyGroups.HasAriaLabelAndFocusableAttributes, StudioPropertyGroups.ValueChangeModeAttributes,
            StudioPropertyGroups.Title, StudioPropertyGroups.Label, StudioPropertyGroups.Autofocus,
            StudioPropertyGroups.HelperText, StudioPropertyGroups.StringValue, StudioPropertyGroups.Placeholder {
    }

    @StudioPropertyGroup(properties = @StudioProperty(
            xmlAttribute = StudioXmlAttributes.SEARCH_STRATEGY,
            type = StudioPropertyType.ENUMERATION,
            classFqn = "io.jmix.search.searching.SearchStrategy",
            options = {"anyTermAnyField", "allTermsAnyField", "allTermsSingleField", "phrase"}))
    public interface FullTextFilterComponent extends StudioPropertyGroups.BaseFilterDefaultProperties,
            StudioPropertyGroups.HasSizeAttributes, StudioPropertyGroups.HasAriaLabel, StudioPropertyGroups.AutoApply,
            StudioPropertyGroups.AlignSelf, StudioPropertyGroups.LabelWidth, StudioPropertyGroups.DataLoader,
            StudioPropertyGroups.JustifySelf, StudioPropertyGroups.LabelPosition {
    }

}
