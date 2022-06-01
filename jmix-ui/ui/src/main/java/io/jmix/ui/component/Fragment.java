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

import io.jmix.ui.UiComponents;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.CanvasIconSize;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.screen.ScreenFragment;

/**
 * Reusable part of {@link Window} with separate UI controller.
 *
 * @see ScreenFragment
 */
@StudioComponent(
        caption = "Fragment",
        category = "Containers",
        xmlElement = "fragment",
        icon = "io/jmix/ui/icon/container/fragment.svg",
        canvasBehaviour = CanvasBehaviour.BOX,
        canvasIconSize = CanvasIconSize.LARGE,
        unsupportedProperties = {"spacing", "margin"},
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/screens.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "screen", type = PropertyType.SCREEN_ID, required = true,
                        options = {"io.jmix.ui.screen.ScreenFragment"}),
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "100%")
        }
)
public interface Fragment extends Frame {
    /**
     * Name that is used to register a client type specific screen implementation in
     * {@link UiComponents}
     */
    String NAME = "fragment";

    @Override
    ScreenFragment getFrameOwner();

    @StudioElementsGroup(
            caption = "Properties",
            xmlElement = "properties",
            elementClass = "io.jmix.ui.sys.UiControllerProperty",
            icon = "io/jmix/ui/icon/element/properties.svg"
    )
    @Override
    FrameContext getContext();
}