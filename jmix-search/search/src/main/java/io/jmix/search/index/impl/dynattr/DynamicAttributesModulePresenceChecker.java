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

package io.jmix.search.index.impl.dynattr;

import io.jmix.core.JmixModules;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for checking the availability of the Dynamic Attributes module in the Jmix application.
 */
@Component("search_DynamicAttributesModuleChecker")
public class DynamicAttributesModulePresenceChecker {

    private final JmixModules modules;

    public DynamicAttributesModulePresenceChecker(JmixModules modules) {
        this.modules = modules;
    }

    /**
     * Checks if the Dynamic Attributes module is available in the application.
     *
     * @return true if the Dynamic Attributes module is present, false otherwise
     */
    public boolean isModulePresent() {
        return modules.get("io.jmix.dynattr") != null;
    }
}
