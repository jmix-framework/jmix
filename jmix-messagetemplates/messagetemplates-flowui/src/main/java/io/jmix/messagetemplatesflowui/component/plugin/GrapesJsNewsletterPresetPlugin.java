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

@Component("msgtmp_GrapesJsNewsletterPresetPlugin")
public class GrapesJsNewsletterPresetPlugin extends GrapesJsPlugin {

    public static final String NAME = "grapesjs-preset-newsletter";

    public GrapesJsNewsletterPresetPlugin() {
        super(NAME);

        initOptions();
    }

    protected void initOptions() {
        setOptions("""
                {
                    "modalLabelImport": "Paste all your code here below and click import",
                    "modalLabelExport": "Copy the code and use it wherever you want",
                    "categoryLabel": "Basic",
                    "cellStyle": {
                        "font-size": "12px",
                        "font-weight": 300,
                        "vertical-align": "top",
                        "color": "rgb(111, 119, 125)",
                        "margin": 0,
                        "padding": 0
                    }
                }
                """);
    }
}
