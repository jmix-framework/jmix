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

package io.jmix.messagetemplatesflowui.kit.component;

import java.io.Serializable;

/**
 * Embeddable plugin for the {@link JmixGrapesJs} component. The plugin can provide additional functionality,
 * such as new HTML blocks, actions in the control panel, etc.
 */
public abstract class GrapesJsPlugin implements Serializable {

    protected final String name;
    protected String options;

    public GrapesJsPlugin(String name) {
        this.name = name;
    }

    /**
     * @return the unique name of the plugin
     */
    public String getName() {
        return name;
    }

    /**
     * @return additional options for configuring the plugin as a JSON string
     */
    public String getOptions() {
        return options;
    }

    /**
     * Sets additional options for configuring the plugin as a JSON string.
     *
     * @param options options as JSON string
     */
    public void setOptions(String options) {
        this.options = options;
    }
}
