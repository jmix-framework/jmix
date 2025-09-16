/*
 * Copyright 2021 Haulmont.
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

package io.jmix.multitenancy.core;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Provides the tenant id for user.
 */
public interface TenantProvider {

    /**
     * Constant to be returned by {@link #getCurrentUserTenantId()} if tenant is currently not determined.
     */
    String NO_TENANT = "no_tenant";

    /**
     * Returns the current tenant id, or {@link #NO_TENANT} constant if tenant is currently not determined.
     */
    String getCurrentUserTenantId();

    /**
     * Returns tenant id for provided user, or {@link #NO_TENANT} constant if tenant is currently not determined.
     *
     * @param userDetails user
     * @return tenant id
     */
    default String getTenantIdForUser(UserDetails userDetails) {
        return NO_TENANT;
    }
}
