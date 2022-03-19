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

package io.jmix.ui.component.mainwindow;

import io.jmix.ui.component.Component;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.StudioComponent;

@StudioComponent(
        caption = "NewWindowButton",
        category = "Main window",
        xmlElement = "newWindowButton",
        icon = "io/jmix/ui/icon/mainwindow/newWindowButton.svg",
        unsupportedProperties = {"box.expandRatio", "css", "responsive"},
        canvasBehaviour = CanvasBehaviour.BUTTON
)
public interface NewWindowButton extends Component.BelongToFrame, Component.HasIcon, Component.HasCaption {

    String NAME = "newWindowButton";
}
