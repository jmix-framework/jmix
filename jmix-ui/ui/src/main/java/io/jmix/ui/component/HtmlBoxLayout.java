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
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

/**
 * A container component with freely designed layout and style. The layout consists of items with textually represented
 * locations. Each item contains one sub-component, which can be any component, such as a layout. The adapter and theme
 * are responsible for rendering the layout with a given style by placing the items in the defined locations.
 */
@StudioComponent(
        caption = "HtmlBox",
        category = "Containers",
        xmlElement = "htmlBox",
        icon = "io/jmix/ui/icon/container/htmlBox.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.VERTICAL,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/html-box-layout.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "100%")
        }
)
public interface HtmlBoxLayout extends ComponentContainer, Component.BelongToFrame, Component.HasIcon,
        Component.HasCaption, HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasRequiredIndicator,
        HasHtmlSanitizer {

    String NAME = "htmlBox";

    /**
     * Returns filename of the related HTML template.
     */
    @Nullable
    String getTemplateName();

    /**
     * Sets filename of the related HTML template inside theme/layouts directory.
     */
    @StudioProperty(name = "template", type = PropertyType.FILE_REF, options = {"html", "themes/${themeName}/layouts/"})
    void setTemplateName(@Nullable String templateName);

    /**
     * @return the contents of the template
     */
    @Nullable
    String getTemplateContents();

    /**
     * Sets the contents of the template used to draw the custom layout.
     */
    @StudioProperty(type = PropertyType.HTML)
    void setTemplateContents(@Nullable String templateContents);
}