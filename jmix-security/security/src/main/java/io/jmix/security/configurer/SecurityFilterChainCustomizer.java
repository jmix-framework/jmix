/*
 * Copyright 2025 Haulmont.
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

package io.jmix.security.configurer;

import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Interface to be implemented by beans that customize Spring Security filter chains.
 */
public interface SecurityFilterChainCustomizer {

    /**
     * Returns the names of the security filter chains that this customizer applies to.
     */
    List<String> getChainBeanNames();

    /**
     * Performs customizations to the specified filter chain.
     *
     * @param chainName name of the security filter chain
     * @param chain security filter chain instance
     */
    void customize(String chainName, SecurityFilterChain chain);

    /**
     * Whether this customizer is enabled.
     */
    default boolean isEnabled() {
        return true;
    }
}