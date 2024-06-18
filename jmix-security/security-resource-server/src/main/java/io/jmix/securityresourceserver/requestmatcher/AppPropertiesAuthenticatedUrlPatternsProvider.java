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

package io.jmix.securityresourceserver.requestmatcher;

import com.google.common.base.Splitter;
import io.jmix.core.JmixModules;

import java.util.List;

/**
 * An implementation of the {@link AuthenticatedUrlPatternsProvider} that gets the patterns from the
 * {@code jmix.resource-server.authenticated-url-patterns} application property.
 *
 * @see AuthenticatedUrlPatternsProvider
 */
public class AppPropertiesAuthenticatedUrlPatternsProvider implements AuthenticatedUrlPatternsProvider {

    private final JmixModules jmixModules;

    public AppPropertiesAuthenticatedUrlPatternsProvider(JmixModules jmixModules) {
        this.jmixModules = jmixModules;
    }

    @Override
    public List<String> getAuthenticatedUrlPatterns() {
        //the value of the jmix.resource-server.authenticated-url-patterns in each module may contain a comma separated
        //list of URL patterns
        List<String> propertyValues = jmixModules.getPropertyValues("jmix.resource-server.authenticated-url-patterns");
        return propertyValues.stream()
                .flatMap(propertyValue -> Splitter.on(",")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToStream(propertyValue))
                .toList();
    }
}
