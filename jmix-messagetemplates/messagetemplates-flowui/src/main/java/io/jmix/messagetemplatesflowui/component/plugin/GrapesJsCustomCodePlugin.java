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
import org.springframework.stereotype.Component;

/**
 * The plugin adds the possibility to embed custom code as a block.
 *
 * @see <a href="https://github.com/GrapesJS/components-custom-code">GrapesJS Custom Code GitHub</a>
 */
@Component("msgtmp_GrapesJsCustomCodePlugin")
public class GrapesJsCustomCodePlugin extends GrapesJsPlugin {

    public static final String NAME = "grapesjs-custom-code";

    public GrapesJsCustomCodePlugin() {
        super(NAME);
    }
}
