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

@StudioComponent(
        caption = "VerticalGroupBox",
        category = "Containers",
        xmlElement = "groupBox",
        icon = "io/jmix/ui/icon/container/verticalGroupBox.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.GROUP_BOX,
        unsupportedProperties = {"orientation"},
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/group-box-layout.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "100%")
        }
)
public interface GroupBoxLayout
        extends ExpandingLayout, OrderedContainer,
        Component.HasIcon, Component.HasCaption, HasBorder, HasSpacing, HasOuterMargin, HasOrientation,
        Collapsable, Component.BelongToFrame, ShortcutNotifier, HasContextHelp,
        HasHtmlCaption, HasHtmlDescription, SupportsExpandRatio, HasRequiredIndicator, HasHtmlSanitizer {

    String NAME = "groupBox";

    /**
     * Sets layout style as a Vaadin Panel
     *
     * @param showAsPanel whether the layout should appear as a Vaadin Panel
     */
    @StudioProperty(defaultValue = "false")
    void setShowAsPanel(boolean showAsPanel);

    /**
     * @return true if layout looks like Vaadin Panel
     */
    boolean isShowAsPanel();
}