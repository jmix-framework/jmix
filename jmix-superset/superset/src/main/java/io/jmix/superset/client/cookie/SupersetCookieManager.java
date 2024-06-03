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

package io.jmix.superset.client.cookie;

import io.jmix.core.annotation.Experimental;
import io.jmix.superset.client.SupersetClient;

import java.net.CookieManager;
import java.net.http.HttpClient;

/**
 * The class for configuring cookie management for {@link HttpClient}. In {@link SupersetClient} it is injected as a
 * Spring bean and can be replaced by custom implementation.
 * <p>
 * When CSRF token request is sent, it returns Set-Cookie header alongside the token. This cookie should also be sent
 * while getting guest token. The {@link CookieManager} by default uses
 * {@link java.net.CookiePolicy#ACCEPT_ORIGINAL_SERVER} for requests and sends cookie when it is necessary.
 * <p>
 * This class can be extended for configuring more advanced cookie policy or cookie store.
 */
@Experimental
public class SupersetCookieManager extends CookieManager {
}
