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
 * A static resource that is served by the application.
 * By default, static content is served from {@code /static}, {@code /public}, {@code /resources}, or {@code /META-INF/resources}
 * directories of the classpath.
 *
 * @see #setPath(String)
 */
@StudioElement(
        caption = "RelativePath Resource",
        xmlElement = "relativePath",
        icon = "io/jmix/ui/icon/element/resource.svg"
)
public interface RelativePathResource extends Resource, ResourceView.HasMimeType {

    /**
     * @param path relative path to the resource, e.g. "images/image.png" for a resource
     *             located at {@code src/main/resources/static/images/image.png}
     * @return this RelativePathResource instance
     */
    @StudioProperty(required = true)
    RelativePathResource setPath(String path);

    String getPath();
}
