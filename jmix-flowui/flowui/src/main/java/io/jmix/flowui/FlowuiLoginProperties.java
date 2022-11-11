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

package io.jmix.flowui;

import com.google.common.base.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Optional;

@ConfigurationProperties(prefix = "jmix.flowui.login")
@ConstructorBinding
public class FlowuiLoginProperties {

    /**
     * Defines default username that is supposed to be used in login form.
     */
    String defaultUsername;

    /**
     * Defines default password that is supposed to be used in login form.
     */
    String defaultPassword;

    public FlowuiLoginProperties(
            @DefaultValue("admin") String defaultUsername,
            @DefaultValue("admin") String defaultPassword
    ) {
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }

    /**
     * @see #defaultUsername
     */
    public String getDefaultUsername() {
        return defaultUsername;
    }

    /**
     * @see #defaultPassword
     */
    public String getDefaultPassword() {
        return defaultPassword;
    }

    /**
     * @return default username if it doesn't equal to {@code <disabled>} value.
     */
    public Optional<String> getDefaultUsernameOptional() {
        return Strings.isNullOrEmpty(defaultUsername) || "<disabled>".equals(defaultUsername)
                ? Optional.empty()
                : Optional.of(defaultUsername);
    }

    /**
     * @return default password if it doesn't equal to {@code <disabled>} value.
     */
    public Optional<String> getDefaultPasswordOptional() {
        return Strings.isNullOrEmpty(defaultPassword) || "<disabled>".equals(defaultPassword)
                ? Optional.empty()
                : Optional.of(defaultPassword);
    }
}
