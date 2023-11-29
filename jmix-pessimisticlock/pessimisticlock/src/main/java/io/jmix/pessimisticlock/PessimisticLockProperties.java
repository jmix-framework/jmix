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

package io.jmix.pessimisticlock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.pslock")
public class PessimisticLockProperties {

    /**
     * CRON expression that is used by default pessimistic lock expiration scheduling configuration.
     */
    String expirationCron;

    /**
     * Whether the default pessimistic lock expiration scheduling configuration is used.
     */
    boolean useDefaultQuartzConfiguration;

    public PessimisticLockProperties(@DefaultValue("0 * * * * ?") String expirationCron,
                           @DefaultValue("true") boolean useDefaultQuartzConfiguration) {
        this.expirationCron = expirationCron;
        this.useDefaultQuartzConfiguration = useDefaultQuartzConfiguration;
    }

    /**
     * @see #expirationCron
     */
    public String getExpirationCron() {
        return expirationCron;
    }

    /**
     * @see #useDefaultQuartzConfiguration
     */
    public boolean isUseDefaultQuartzConfiguration() {
        return useDefaultQuartzConfiguration;
    }
}