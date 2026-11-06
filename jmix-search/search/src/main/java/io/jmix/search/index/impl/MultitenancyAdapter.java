/*
 * Copyright 2026 Haulmont.
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

package io.jmix.search.index.impl;

import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Answers the questions about tenants that cannot be answered without the Multitenancy add-on.
 * <p>
 * This is the only place that asks the add-on anything. Two others know it exists for different reasons:
 * {@link io.jmix.search.listener.SearchTenantEventListener} is subscribed to its events, and the starter
 * configuration checks whether it is on the classpath. An application without the add-on gets an implementation
 * that reports no tenants at all, so the rest of the module never checks whether the add-on is there.
 * <p>
 * A tenant is either known or absent. The {@code no_tenant} sentinel never reaches this interface: the add-on
 * translates it in its own facade.
 */
public interface MultitenancyAdapter {

    /**
     * @return true if the application manages tenants, so that an entity with a tenant attribute is stored in a
     * separate index per tenant
     */
    boolean isMultitenancyActive();

    /**
     * @return tenants known to the application, empty if it manages no tenants
     */
    Set<String> getAvailableTenants();

    /**
     * @return tenant of the instance, or null if it belongs to none or the application manages no tenants
     * @throws RuntimeException if the tenant cannot be read off the instance. The add-on throws a type of its
     *                          own, which this module cannot name without depending on it; ask
     *                          {@link #isTenantIdReadable(Object)} first to avoid the throw
     */
    @Nullable
    String getTenantIdForInstance(Object instance);

    /**
     * Tells whether {@link #getTenantIdForInstance(Object)} will answer rather than throw. An instance the
     * application holds may have been loaded without the tenant attribute, and then its tenant is unknowable
     * rather than absent.
     */
    boolean isTenantIdReadable(Object instance);

    /**
     * @return tenant of the current user, or null if the user has none or the application manages no tenants
     */
    @Nullable
    String getCurrentUserTenantId();
}
