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

package io.jmix.authorizationserver.client;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.List;

/**
 * Interface for providing a list of registered clients for OAuth2 authorization server.
 */
public interface RegisteredClientProvider {

    /**
     * Returns a list of {@link RegisteredClient} that will be added to a default
     * {@link org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository}
     */
    List<RegisteredClient> getRegisteredClients();
}
