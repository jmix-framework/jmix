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
 * CssLayout is a layout component renders components and their captions into a same DIV element.
 * Component layout can then be adjusted with CSS.
 */
@StudioComponent(
        caption = "CssLayout",
        category = "Containers",
        xmlElement = "cssLayout",
        icon = "io/jmix/ui/icon/container/cssLayout.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.VERTICAL,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/css-layout.html"
)
public interface CssLayout extends OrderedContainer, Component.BelongToFrame, Component.HasCaption,
        Component.HasIcon, HasContextHelp, LayoutClickNotifier, ShortcutNotifier, HasHtmlCaption, HasHtmlDescription,
        HasRequiredIndicator, HasHtmlSanitizer {

    String NAME = "cssLayout";
}