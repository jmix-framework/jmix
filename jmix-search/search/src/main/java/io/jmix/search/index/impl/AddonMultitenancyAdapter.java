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

import io.jmix.multitenancy.Multitenancy;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Adapter to the Multitenancy add-on: it answers the module's questions by asking the add-on and translating
 * what comes back.
 * <p>
 * This class references the add-on types, so it must not be loaded when the add-on is absent: it is registered as a
 * bean by the starter configuration conditionally.
 */
@NullMarked
public class AddonMultitenancyAdapter implements MultitenancyAdapter {

    protected final Multitenancy multitenancy;

    public AddonMultitenancyAdapter(Multitenancy multitenancy) {
        this.multitenancy = multitenancy;
    }

    @Override
    public boolean isMultitenancyActive() {
        return true;
    }

    @Override
    public Set<String> getAvailableTenants() {
        return multitenancy.getAvailableTenants();
    }

    @Nullable
    @Override
    public String getTenantIdForInstance(Object instance) {
        return multitenancy.getTenantIdForEntity(instance);
    }

    @Override
    public boolean isTenantIdReadable(Object instance) {
        return multitenancy.isTenantIdReadable(instance);
    }

    @Nullable
    @Override
    public String getCurrentUserTenantId() {
        return multitenancy.getCurrentUserTenantId();
    }

}
