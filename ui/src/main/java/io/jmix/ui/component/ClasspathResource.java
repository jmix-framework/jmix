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
 * A resource that is located in classpath with the given <code>path</code>.
 * <p>
 * For obtaining resources the {@link io.jmix.core.Resources} infrastructure interface is using.
 * <p>
 * For example if your resource is located in the web module and has the following path: "com/company/app/web/images/image.png",
 * ClassPathResource's path should be: "/com/company/app/web/images/image.png".
 */
@StudioElement(
        caption = "Classpath Resource",
        xmlElement = "classpath",
        icon = "io/jmix/ui/icon/element/resource.svg"
)
public interface ClasspathResource extends Resource, ResourceView.HasMimeType, ResourceView.HasStreamSettings {

    @StudioProperty(name = "path", required = true)
    ClasspathResource setPath(String path);

    String getPath();
}
