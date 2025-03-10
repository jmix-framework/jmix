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
 * The plugin adds some of the basic form components and blocks which help in working with forms easier.
 * Available components: {@code form}, {@code input}, {@code textarea}, {@code select}, {@code option},
 * {@code checkbox}, {@code radio}, {@code button}, {@code label}.
 *
 * @see <a href="https://github.com/GrapesJS/components-forms">GrapesJS Forms GitHub</a>
 */
@Component("msgtmp_GrapesJsFormsPlugin")
public class GrapesJsFormsPlugin extends GrapesJsPlugin {

    public static final String NAME = "grapesjs-plugin-forms";

    public GrapesJsFormsPlugin() {
        super(NAME);
    }
}
