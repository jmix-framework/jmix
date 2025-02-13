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

package io.jmix.tabbedmode;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.tabmod")
public class TabbedModeProperties {

    /**
     * Maximum number of opened tabs. {@code 0} for unlimited.
     */
    int maxTabCount;

    public TabbedModeProperties(@DefaultValue("20") int maxTabCount) {
        this.maxTabCount = maxTabCount;
    }

    /**
     * @see #maxTabCount
     */
    public int getMaxTabCount() {
        return maxTabCount;
    }
}
