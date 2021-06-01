/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.sys;

import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

@StudioElement(
        xmlElement = "property",
        caption = "Property Option",
        defaultProperty = "name",
        icon = "io/jmix/ui/icon/element/property.svg"
)
public class PropertyOption {

    protected final String name;
    protected final String caption;
    protected final String configurationName;
    protected final String screen;

    public PropertyOption(String name, @Nullable String caption, @Nullable String configurationName,
                          @Nullable String screen) {
        Preconditions.checkNotEmptyString(name, "Empty name for custom property option");

        this.name = name;
        this.caption = caption;
        this.configurationName = configurationName;
        this.screen = screen;
    }

    @StudioProperty(required = true)
    public String getName() {
        return name;
    }

    @Nullable
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    public String getCaption() {
        return caption;
    }

    @Nullable
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    public String getConfigurationName() {
        return configurationName;
    }

    @Nullable
    @StudioProperty(name = "screen", type = PropertyType.SCREEN_ID)
    public String getScreenId() {
        return screen;
    }
}
