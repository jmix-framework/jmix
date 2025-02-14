/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.component.plugin;

import io.jmix.messagetemplatesflowui.kit.component.GrapesJsPlugin;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import org.springframework.stereotype.Component;

/**
 * The plugin adds simple {@code tooltip} component for {@link GrapesJs}.
 *
 * @see <a href="https://github.com/GrapesJS/components-tooltip">GrapesJS Tooltip GitHub</a>
 */
@Component("msgtmp_GrapesJsTooltipPlugin")
public class GrapesJsTooltipPlugin extends GrapesJsPlugin {

    public static final String NAME = "grapesjs-tooltip";

    public GrapesJsTooltipPlugin() {
        super(NAME);

    }
}
