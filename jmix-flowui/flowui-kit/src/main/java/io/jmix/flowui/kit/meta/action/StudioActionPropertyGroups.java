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

package io.jmix.flowui.kit.meta.action;

import io.jmix.flowui.kit.meta.StudioAPI;
import io.jmix.flowui.kit.meta.StudioPropertyGroup;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;

@StudioAPI
public final class StudioActionPropertyGroups {

    private StudioActionPropertyGroups() {
    }

    @StudioPropertyGroup
    public interface ActionDefaultProperties extends StudioPropertyGroups.ActionVariantWithDefaultDefaultValue,
            StudioPropertyGroups.Description, StudioPropertyGroups.EnabledWithTrueDefaultValue,
            StudioPropertyGroups.ShortcutCombination, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface TextActionDefaultProperties extends ActionDefaultProperties, StudioPropertyGroups.Text {
    }

    @StudioPropertyGroup
    public interface IconActionDefaultProperties extends ActionDefaultProperties, StudioPropertyGroups.LookAndFeelIcon {
    }

    @StudioPropertyGroup
    public interface IconTextActionDefaultProperties extends IconActionDefaultProperties, StudioPropertyGroups.Text {
    }

    @StudioPropertyGroup
    public interface RequiredIconTextActionDefaultProperties extends IconTextActionDefaultProperties,
            StudioPropertyGroups.RequiredId {
    }

    @StudioPropertyGroup
    public interface ActionDefaultPropertiesWithoutShortcutCombination extends
            StudioPropertyGroups.ActionVariantWithDefaultDefaultValue, StudioPropertyGroups.Description,
            StudioPropertyGroups.EnabledWithTrueDefaultValue, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface RequiredIconTextActionDefaultPropertiesWithoutShortcutCombination extends
            ActionDefaultPropertiesWithoutShortcutCombination, StudioPropertyGroups.LookAndFeelIcon,
            StudioPropertyGroups.RequiredId, StudioPropertyGroups.Text {
    }

    @StudioPropertyGroup
    public interface DangerActionDefaultProperties extends StudioPropertyGroups.ActionVariantWithDangerDefaultValue,
            StudioPropertyGroups.Description, StudioPropertyGroups.EnabledWithTrueDefaultValue,
            StudioPropertyGroups.ShortcutCombination, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface PrimaryActionDefaultProperties extends StudioPropertyGroups.ActionVariantWithPrimaryDefaultValue,
            StudioPropertyGroups.Description, StudioPropertyGroups.EnabledWithTrueDefaultValue,
            StudioPropertyGroups.ShortcutCombination, StudioPropertyGroups.Visible {
    }

    @StudioPropertyGroup
    public interface ActionCommonPropertiesWithoutCategory extends StudioPropertyGroups.DescriptionWithoutCategory,
            StudioPropertyGroups.EnabledWithTrueDefaultValueWithoutCategory,
            StudioPropertyGroups.ShortcutCombination, StudioPropertyGroups.VisibleWithDefaultValueTrue {
    }

    @StudioPropertyGroup
    public interface PrimaryActionDefaultPropertiesWithCreateIdAndPlusIcon extends
            PrimaryActionDefaultProperties, StudioPropertyGroups.RequiredIdWithCreateInitialValue,
            StudioPropertyGroups.LookAndFeelIconWithPlusDefaultValue {
    }
}
