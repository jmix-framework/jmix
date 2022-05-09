/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * A resource located in the current UI theme.
 *
 * @see #setPath(String)
 */
@StudioElement(
        caption = "Theme Resource",
        xmlElement = "theme",
        icon = "io/jmix/ui/icon/element/resource.svg"
)
public interface ThemeResource extends Resource {

    /**
     * @param path relative path to the theme resource, e.g. "images/image.png" for the resource
     *             located at {@code src/main/themes/mytheme/images/image.png} if the current theme is "mytheme"
     * @return this ThemeResource instance
     */
    @StudioProperty(required = true)
    ThemeResource setPath(String path);

    String getPath();
}
