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
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "jmix.security.bruteforce")
@ConstructorBinding
public class BruteForceProperties {
    private final boolean enabled;
    private final Duration blockInterval;
    private final int maxLoginAttemptsNumber;

    public BruteForceProperties(
            @DefaultValue("false") boolean enabled,
            @DurationUnit(ChronoUnit.SECONDS)
            @DefaultValue("60") Duration blockInterval,
            @DefaultValue("5") int maxLoginAttemptsNumber) {
        this.enabled = enabled;
        this.blockInterval = blockInterval;
        this.maxLoginAttemptsNumber = maxLoginAttemptsNumber;
    }

    /**
     * @return a time interval for which a user is blocked after a series of
     * unsuccessful login attempts
     */
    public Duration getBlockInterval() {
        return blockInterval;
    }

    /**
     * Whether the brute-force protection on user authentication is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return a maximum number of unsuccessful authentication attempts
     */
    public int getMaxLoginAttemptsNumber() {
        return maxLoginAttemptsNumber;
    }
}
