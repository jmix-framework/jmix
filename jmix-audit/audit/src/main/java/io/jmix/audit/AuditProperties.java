/*
 * Copyright 2020 Haulmont.
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

package io.jmix.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("jmix.audit")
public class AuditProperties {

    boolean enabled;
    //ToDo: make system user name globally configurable?
    String systemUsername;

    public AuditProperties(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("system") String systemUsername
    ) {
        this.enabled = enabled;
        this.systemUsername = systemUsername;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSystemUsername() {
        return systemUsername;
    }
}
