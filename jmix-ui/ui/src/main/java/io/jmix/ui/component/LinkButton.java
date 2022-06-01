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
import io.jmix.ui.meta.StudioComponent;

/**
 * A button looking like hyperlink.
 */
@StudioComponent(
        caption = "LinkButton",
        category = "Components",
        xmlElement = "linkButton",
        icon = "io/jmix/ui/icon/component/linkButton.svg",
        canvasText = "New Link Button",
        canvasTextProperty = "caption",
        canvasBehaviour = CanvasBehaviour.BUTTON,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/link-button.html"
)
public interface LinkButton extends Button {
    String NAME = "linkButton";
}