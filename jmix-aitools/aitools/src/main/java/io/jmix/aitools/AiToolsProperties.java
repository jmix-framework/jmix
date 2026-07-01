/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties of the AI Tools add-on, bound from the {@code jmix.aitools} prefix.
 */
@ConfigurationProperties("jmix.aitools")
public class AiToolsProperties {

    /**
     * Whether the AI Tools autoconfiguration is enabled.
     */
    Boolean enabled;

    public AiToolsProperties(@DefaultValue("true") Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }
}
