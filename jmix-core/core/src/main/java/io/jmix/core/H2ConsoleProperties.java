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

/**
 * Manages H2 console properties.
 *
 * @deprecated the framework reads {@code spring.h2.console.path} directly in the filter chain that opens the console
 * ({@code io.jmix.autoconfigure.security.SecurityAutoConfiguration.H2ConsoleSecurityConfiguration}); the properties
 * themselves belong to Spring Boot's {@code spring-boot-h2console} module.
 */
@Deprecated(since = "3.1", forRemoval = true)
@ConfigurationProperties(prefix = "spring.h2.console")
public class H2ConsoleProperties {

    boolean enabled;

    String path;

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
