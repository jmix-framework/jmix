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
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.UUID;

/**
 * Configuration properties for managing navigation-related settings in the UI module.
 */
@ConfigurationProperties(prefix = "jmix.ui.navigation")
public class UiNavigationProperties {

    /**
     * Whether {@link CrockfordUuidEncoder} must be used for encoding/decoding
     * process of {@link UUID} URL parameters.
     */
    boolean useCrockfordUuidEncoder;

    /**
     * Whether nanoseconds must be used for serializing/deserializing
     * temporal URL parameters.
     */
    boolean useNanosecondsInUrlParams;

    public UiNavigationProperties(
            @DefaultValue("false") boolean useCrockfordUuidEncoder,
            @DefaultValue("true") boolean useNanosecondsInUrlParams) {
        this.useCrockfordUuidEncoder = useCrockfordUuidEncoder;
        this.useNanosecondsInUrlParams = useNanosecondsInUrlParams;
    }

    /**
     * @see #useCrockfordUuidEncoder
     */
    public boolean isUseCrockfordUuidEncoder() {
        return useCrockfordUuidEncoder;
    }

    /**
     * @see #useNanosecondsInUrlParams
     */
    public boolean isUseNanosecondsInUrlParams() {
        return useNanosecondsInUrlParams;
    }
}
