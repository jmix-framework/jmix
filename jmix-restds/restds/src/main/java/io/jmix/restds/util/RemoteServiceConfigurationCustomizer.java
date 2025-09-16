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

package io.jmix.restds.util;

import io.jmix.core.JmixOrder;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.Nullable;

/**
 * Interface for customizing the configuration of services available through the REST DataStore.
 * <p>
 * Should be implemented by beans in add-ons and applications.
 * The implementation can have the {@link org.springframework.core.annotation.Order} annotation
 * with a {@link JmixOrder} value.
 */
public interface RemoteServiceConfigurationCustomizer {

    /**
     * Returns a filter to include certain classes when searching for remote service interfaces.
     *
     * @return a filter or null for no additional filtering
     */
    @Nullable
    TypeFilter getScannerIncludeFilter();

    /**
     * Returns the service parameters for the given remote service.
     *
     * @param serviceInterface remote service interface
     * @return service parameters or null to skip customization
     */
    @Nullable
    ServiceParameters getServiceParameters(Class<?> serviceInterface);

    /**
     * Service parameters: data store name and remote service name.
     */
    class ServiceParameters {

        private String storeName;
        private String serviceName;

        public ServiceParameters() {
        }

        @Nullable
        public String getStoreName() {
            return storeName;
        }

        @Nullable
        public String getServiceName() {
            return serviceName;
        }

        /**
         * Sets the data store name.
         *
         * @param storeName data store name
         * @return this instance for chaining
         */
        public ServiceParameters withStoreName(String storeName) {
            this.storeName = storeName;
            return this;
        }

        /**
         * Sets the remote service name.
         *
         * @param serviceName remote service name
         * @return this instance for chaining
         */
        public ServiceParameters withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
    }
}
