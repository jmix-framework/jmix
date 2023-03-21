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

package io.jmix.pivottable.model.meta;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * Meta interface for support in Screen Designer only.
 */
@StudioElement(
        caption = "Renderer",
        xmlElement = "renderer",
        icon = "io/jmix/pivottable/icon/component.svg",
        xmlns = "http://jmix.io/schema/ui/pivot-table",
        xmlnsAlias = "pivot"
)
public interface Renderer {
    @StudioProperty(type = PropertyType.ENUMERATION)
    void setType(io.jmix.pivottable.model.Renderer renderer);

    @StudioProperty(type = PropertyType.ENUMERATION)
    void setDefault(io.jmix.pivottable.model.Renderer renderer);

    @StudioProperty(type = PropertyType.ENUMERATION)
    void setSelected(io.jmix.pivottable.model.Renderer renderer);
}
