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

package io.jmix.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.security")
public class SecurityProperties {

    /**
     * Whether hot deploy of annotated (i.e., design-time) roles is enabled.
     */
    boolean annotatedRolesHotDeployEnabled;

    public SecurityProperties(@DefaultValue("true") boolean annotatedRolesHotDeployEnabled) {
        this.annotatedRolesHotDeployEnabled = annotatedRolesHotDeployEnabled;
    }

    /**
     * @see #annotatedRolesHotDeployEnabled
     */
    public boolean isAnnotatedRolesHotDeployEnabled() {
        return annotatedRolesHotDeployEnabled;
    }
}
