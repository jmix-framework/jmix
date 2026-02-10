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

package io.jmix.securityresourceserver.authentication;

import com.google.common.base.Splitter;
import io.jmix.core.JmixModules;
import org.springframework.core.env.Environment;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Provides properties for forcing of API security scope.
 */
public class ForceApiSecurityScopePropertiesProvider {

    private static final String SECURITY_FILTER_CHAIN_NAMES_PROPERTY = "jmix.resource-server.force-api-security-scope.security-filter-chain-names";
    private static final String IS_ENABLED_PROPERTY = "jmix.resource-server.force-api-security-scope.enabled";

    protected final JmixModules jmixModules;
    protected final Environment environment;

    public ForceApiSecurityScopePropertiesProvider(JmixModules jmixModules, Environment environment) {
        this.jmixModules = jmixModules;
        this.environment = environment;
    }

    /**
     * Gets list of SecurityFilterChain bean-names specified in 'jmix.resource-server.force-api-security-scope.security-filter-chain-names'
     * property.
     *
     * @return List of SecurityFilterChain bean-names collected from all modules
     */
    public List<String> getSecurityFilterChainNames() {
        return getStringValuesFromProperties(SECURITY_FILTER_CHAIN_NAMES_PROPERTY);
    }

    /**
     * Returns true if API security scope is forced during access via REST API.
     *
     * @return true if property 'jmix.resource-server.force-api-security-scope.enabled' is set to true, false otherwise
     */
    public boolean isEnabled() {
        return getBooleanValueFromProperties(IS_ENABLED_PROPERTY, true);
    }

    protected List<String> getStringValuesFromProperties(String propertyName) {
        List<String> propertyValues = jmixModules.getPropertyValues(propertyName);
        return propertyValues.stream()
                .flatMap(propertyValue ->
                        Splitter.on(",")
                                .omitEmptyStrings()
                                .trimResults()
                                .splitToStream(propertyValue))
                .toList();
    }

    protected boolean getBooleanValueFromProperties(String propertyName, boolean defaultValue) {
        String propertyValue = getEnvironmentPropertyValue(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(propertyValue);
    }

    @Nullable
    protected String getEnvironmentPropertyValue(String propertyName) {
        return environment.getProperty(propertyName);
    }
}