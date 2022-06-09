/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.mainwindow;

import io.jmix.ui.component.Component;
import io.jmix.ui.component.OrderedContainer;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.ContainerType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperty;


/**
 * A panel that can be collapsed to the left hand side.
 * <p>
 * Typically used as a side menu container.
 */
@StudioComponent(
        caption = "Drawer",
        category = "Main window",
        xmlElement = "drawer",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.VERTICAL,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/drawer.html",
        unsupportedProperties = {"align", "enable", "expand", "responsive", "width", "height", "visible"},
        icon = "io/jmix/ui/icon/mainwindow/drawer.svg"
)
public interface Drawer extends OrderedContainer, Component.BelongToFrame {

    String NAME = "drawer";

    /**
     * Collapses the drawer.
     */
    void collapse();

    /**
     * Expands the drawer.
     */
    void expand();

    /**
     * Toggles the drawer.
     */
    void toggle();

    /**
     * Sets a mode when drawer is expanded on hover.
     * Note that collapse state is not changed.
     * {@code false} by default.
     *
     * @param expandOnHover a boolean value specifying if drawer should be expanded on hover.
     */
    @StudioProperty(defaultValue = "false")
    void setExpandOnHover(boolean expandOnHover);

    /**
     * @return whether the drawer should be expanded on hover.
     */
    boolean isExpandOnHover();

    /**
     * @return whether the drawer is collapsed.
     */
    boolean isCollapsed();

}
