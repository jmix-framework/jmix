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

package io.jmix.restds.auth;

import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * Provides authentication for {@link io.jmix.restds.impl.RestDataStore}.
 */
public interface RestAuthenticator {

    /**
     * Creates authentication interceptor to be used by {@link io.jmix.restds.impl.RestInvoker}.
     * @param dataStoreName name of the data store
     * @return authentication interceptor instance
     */
    ClientHttpRequestInterceptor getAuthenticationInterceptor(String dataStoreName);
}
