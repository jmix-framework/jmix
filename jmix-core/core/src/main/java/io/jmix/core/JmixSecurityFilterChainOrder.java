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

package io.jmix.core;

/**
 * A set of constants with order values of {@link org.springframework.security.web.SecurityFilterChain} used in Jmix
 * modules.
 */
public interface JmixSecurityFilterChainOrder {
    int WEBDAV_REST_BASIC = JmixOrder.HIGHEST_PRECEDENCE + 80;
    int WEBDAV_REST_DIGEST = JmixOrder.HIGHEST_PRECEDENCE + 80;
    int AUTHSERVER_AUTHORIZATION_SERVER = JmixOrder.HIGHEST_PRECEDENCE + 100;
    int AUTHSERVER_LOGIN_FORM = JmixOrder.HIGHEST_PRECEDENCE + 110;
    int OIDC_RESOURCE_SERVER = JmixOrder.HIGHEST_PRECEDENCE + 150;
    int AUTHSERVER_RESOURCE_SERVER = JmixOrder.HIGHEST_PRECEDENCE + 150;
    int OIDC_LOGIN = JmixOrder.HIGHEST_PRECEDENCE + 200;
    int CORE_SECURITY = JmixOrder.HIGHEST_PRECEDENCE + 300;
    int LDAP = JmixOrder.HIGHEST_PRECEDENCE + 300;
    int LDAP_ACTIVE_DIRECTORY = JmixOrder.HIGHEST_PRECEDENCE + 300;
    int STANDARD_SECURITY = JmixOrder.HIGHEST_PRECEDENCE + 300;
    int FLOWUI = JmixOrder.HIGHEST_PRECEDENCE + 300;
}
