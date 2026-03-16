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

package io.jmix.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

//TODO [SB4] Javadoc
@ConfigurationProperties(prefix = "spring.h2.console")
public class H2ConsoleProperties {

    boolean enabled;

    String path;

    // TODO [IVGA][SB4] handle 'settings' properties?

    public H2ConsoleProperties(boolean enabled,
                               @DefaultValue("/h2-console") String path) {
        this.enabled = enabled;
        this.path = path;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPath() {
        return path;
    }
}
