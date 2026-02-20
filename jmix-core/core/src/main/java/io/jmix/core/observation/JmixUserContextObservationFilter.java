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

package io.jmix.core.observation;

import io.jmix.core.security.CurrentAuthentication;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * An {@link ObservationFilter} that enriches all spans with Jmix user-specific context:
 * {@code jmix.user.username} - username of the current user
 */
public class JmixUserContextObservationFilter implements ObservationFilter {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Override
    public Observation.Context map(Observation.Context context) {
        if (currentAuthentication.isSet()) {
            UserDetails user = currentAuthentication.getUser();
            context.addHighCardinalityKeyValue(KeyValue.of("jmix.user.username", user.getUsername()));
        }

        return context;
    }
}
