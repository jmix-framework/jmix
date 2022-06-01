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

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.ContainerType;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

/**
 * Component container, which shows the subcomponents in the order of their addition (vertically).
 */
@StudioComponent(
        caption = "VBox",
        category = "Containers",
        xmlElement = "vbox",
        icon = "io/jmix/ui/icon/container/vbox.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.VERTICAL,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/box-layout.html#vbox"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "100%")
        }
)
@StudioElement(
        caption = "InitialLayout",
        xmlElement = "initialLayout",
        icon = "io/jmix/ui/icon/element/initialLayout.svg",
        unsupportedProperties = {"box.expandRatio", "align", "caption", "captionAsHtml", "contextHelpText",
                "contextHelpTextHtmlEnabled", "css", "description", "descriptionAsHtml", "height",
                "htmlSanitizerEnabled", "icon", "requiredIndicatorVisible", "responsive", "width"}
)
public interface VBoxLayout extends BoxLayout {
    String NAME = "vbox";
}