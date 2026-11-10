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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Adapter of an application that manages no tenants: it stands in for the add-on when the add-on is absent, so
 * that the rest of the module never asks whether it is there.
 * <p>
 * An entity with a tenant attribute is stored in a single index shared by everyone, so there is no tenant to
 * report for an instance or for the current user.
 */
@NullMarked
public class NoopMultitenancyAdapter implements MultitenancyAdapter {

    @Override
    public boolean isMultitenancyActive() {
        return false;
    }

    @Override
    public Set<String> getAvailableTenants() {
        return Set.of();
    }

    @Nullable
    @Override
    public String getTenantIdForInstance(Object instance) {
        return null;
    }

    /**
     * Always true: there is nothing here that could throw, so the tenant of any instance can be read. The answer
     * is {@code null}, and it means "belongs to no tenant" rather than "could not be read".
     */
    @Override
    public boolean isTenantIdReadable(Object instance) {
        return true;
    }

    @Nullable
    @Override
    public String getCurrentUserTenantId() {
        return null;
    }
}
