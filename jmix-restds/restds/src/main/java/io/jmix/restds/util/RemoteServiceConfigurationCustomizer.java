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

import java.util.Optional;

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
     * @return a filter or empty {@code Optional} for no additional filtering
     */
    Optional<TypeFilter> getScannerIncludeFilter();

    /**
     * Returns the name of the data store for the given remote service.
     *
     * @param serviceInterface remote service interface
     * @return data store name or empty {@code Optional} to skip customization
     */
    Optional<String> getStoreName(Class<?> serviceInterface);

    /**
     * Returns the remote name for the given remote service.
     *
     * @param serviceInterface remote service interface
     * @return remote name or empty {@code Optional} to skip customization
     */
    Optional<String> getServiceName(Class<?> serviceInterface);
}
