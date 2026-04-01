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

package io.jmix.flowui.kit.meta.component;

import io.jmix.flowui.kit.meta.StudioAPI;
import io.jmix.flowui.kit.meta.StudioPropertyGroup;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;

@StudioAPI
public final class StudioHtmlComponentPropertyGroups {

    private StudioHtmlComponentPropertyGroups() {
    }

    @StudioPropertyGroup
    public interface TextHtmlComponentDefaultProperties extends StudioPropertyGroups.AlignSelfAndJustifySelfAndColspan,
            StudioPropertyGroups.ClassNamesAndCss, StudioPropertyGroups.IdAndVisible,
            StudioPropertyGroups.EnabledWithTrueDefaultValue, StudioPropertyGroups.Size,
            StudioPropertyGroups.ClickShortcut, StudioPropertyGroups.CollectionOrInstanceDataContainer,
            StudioPropertyGroups.Property, StudioPropertyGroups.Text, StudioPropertyGroups.HtmlComponentThemeNames,
            StudioPropertyGroups.Title, StudioPropertyGroups.WhiteSpace {
    }

    @StudioPropertyGroup
    public interface AccessibleTextHtmlComponentDefaultProperties extends TextHtmlComponentDefaultProperties,
            StudioPropertyGroups.AriaLabel, StudioPropertyGroups.AriaLabelledBy {
    }

    @StudioPropertyGroup
    public interface TitleHtmlComponentDefaultProperties extends StudioPropertyGroups.AlignSelfAndJustifySelfAndColspan,
            StudioPropertyGroups.ClassNamesAndCss, StudioPropertyGroups.IdAndVisible, StudioPropertyGroups.Size,
            StudioPropertyGroups.Title {
    }
}
