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
import io.jmix.ui.meta.StudioComponent;

/**
 * Component container, which shows the subcomponents in the order of their addition (horizontally).
 */
@StudioComponent(
        caption = "HBox",
        category = "Containers",
        xmlElement = "hbox",
        icon = "io/jmix/ui/icon/container/hbox.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.HORIZONTAL,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/box-layout.html#hbox"
)
public interface HBoxLayout extends BoxLayout {
    String NAME = "hbox";
}