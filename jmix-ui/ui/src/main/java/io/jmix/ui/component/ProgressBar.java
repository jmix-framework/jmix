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

import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.meta.CanvasIconSize;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperty;

/**
 * Progress bar is a component that visually displays the progress of some task.
 * <br>
 * Component accepts float values from 0.0f to 1.0f. 0 means no progress, 1.0 - full progress.
 * <br>
 * To indicate that a task of unknown length is executing, you can put a progress bar into indeterminate mode.
 */
@StudioComponent(
        caption = "ProgressBar",
        category = "Components",
        xmlElement = "progressBar",
        icon = "io/jmix/ui/icon/component/progressBar.svg",
        canvasIconSize = CanvasIconSize.LARGE,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/progress-bar.html"
)
public interface ProgressBar extends Component, Component.BelongToFrame,
        HasValue<Double>, HasValueSource<Double>,
        Component.HasIcon, Component.HasCaption, HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {

    String NAME = "progressBar";

    boolean isIndeterminate();

    @StudioProperty(defaultValue = "false")
    void setIndeterminate(boolean indeterminate);
}