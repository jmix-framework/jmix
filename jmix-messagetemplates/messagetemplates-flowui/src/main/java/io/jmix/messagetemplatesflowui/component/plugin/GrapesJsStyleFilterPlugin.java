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
 * The plugin adds a new filter built-in style property which can be used for CSS properties like {@code filter} and
 * {@code backdrop-filter}.
 * <p>
 * <b>Note: </b> incompatible with {@link GrapesJsNewsletterPresetPlugin}.
 * </p>
 *
 * @see <a href="https://github.com/GrapesJS/style-filter">GrapesJS Style Filter GitHub</a>
 */
@Component("msgtmp_GrapesJsStyleFilterPlugin")
public class GrapesJsStyleFilterPlugin extends GrapesJsPlugin {

    public static final String NAME = "grapesjs-style-filter";

    public GrapesJsStyleFilterPlugin() {
        super(NAME);

        initOptions();
    }

    protected void initOptions() {
        setOptions("""
                {
                    "styleManager": {
                        "sectors": [
                            {
                                "id": "extra",
                                "name": "Extra",
                                "properties": [
                                    { "extend": "filter" },
                                    { "extend": "filter", "property": "backdrop-filter" }
                                ]
                            }
                        ]
                    }
                }
                """);
    }
}
