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

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import java.io.File;

/**
 * A resource that is stored in the server file system.
 *
 * @see #setFile(File)
 */
@StudioElement(
        caption = "File Resource",
        xmlElement = "file",
        icon = "io/jmix/ui/icon/element/resource.svg"
)
public interface FileResource extends Resource, ResourceView.HasStreamSettings {

    /**
     * @param file file in the server file system
     * @return this FileResource instance
     */
    @StudioProperty(name = "path", type = PropertyType.STRING, required = true)
    FileResource setFile(File file);

    File getFile();
}
