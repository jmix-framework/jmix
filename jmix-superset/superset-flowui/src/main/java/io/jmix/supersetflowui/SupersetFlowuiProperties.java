/*
 * Copyright 2024 Haulmont.
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

package io.jmix.supersetflowui;

import io.jmix.supersetflowui.component.SupersetDashboard;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("jmix.supersetflowui")
public class SupersetFlowuiProperties {

    /**
     * Defines a timeout of background task that fetches guest token for {@link SupersetDashboard} component
     * in {@link DefaultGuestTokenProvider}.
     */
    Duration backgroundFetchingGuestTokenTimeout;

    public SupersetFlowuiProperties(@DefaultValue("1m") Duration backgroundFetchingGuestTokenTimeout) {
        this.backgroundFetchingGuestTokenTimeout = backgroundFetchingGuestTokenTimeout;
    }

    /**
     * @return a timeout of background task that fetches guest token
     * @see #backgroundFetchingGuestTokenTimeout
     */
    public Duration getBackgroundFetchingGuestTokenTimeout() {
        return backgroundFetchingGuestTokenTimeout;
    }
}
