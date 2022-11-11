/*
 * Copyright 2022 Haulmont.
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

import io.jmix.flowui.view.navigation.CrockfordUuidEncoder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.UUID;

@ConfigurationProperties(prefix = "jmix.flowui.navigation")
@ConstructorBinding
public class FlowuiNavigationProperties {

    /**
     * Whether {@link CrockfordUuidEncoder} must be used for encoding/decoding
     * process of {@link UUID} URL parameters.
     */
    boolean useCrockfordUuidEncoder;

    public FlowuiNavigationProperties(boolean useCrockfordUuidEncoder) {
        this.useCrockfordUuidEncoder = useCrockfordUuidEncoder;
    }

    /**
     * @see #useCrockfordUuidEncoder
     */
    public boolean isUseCrockfordUuidEncoder() {
        return useCrockfordUuidEncoder;
    }
}
