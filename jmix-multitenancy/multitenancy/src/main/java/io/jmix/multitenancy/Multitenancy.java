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

package io.jmix.multitenancy;

import io.jmix.core.EntityStates;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.multitenancy.core.TenantEntityOperation;
import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.multitenancy.service.impl.TenantLoader;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Answers what the application's tenants are and which tenant a user or a record belongs to.
 * <p>
 * This is what callers use. {@link TenantProvider} is the other side of the same subject: it is the extension
 * point an application implements to change how the current tenant is determined, and the methods here delegate
 * to whatever it says.
 * <p>
 * Absence of a tenant is {@code null} here, never the {@link TenantProvider#NO_TENANT} sentinel. The sentinel
 * belongs to the extension point, where an implementation has no other way to say "not determined", and it is
 * translated at this boundary so that it never reaches a caller.
 * <p>
 * Marked experimental because this is the first release in which the add-on has a facade at all, and the shape of
 * one is hard to get right without consumers. What may still move: which questions belong here rather than on the
 * extension point, whether reading the tenant of a record keeps throwing two different types, and whether the
 * catalog of tenants stays a plain {@code Set} once it is cached. Code written against it keeps working within a
 * minor version; what may change is announced in release notes rather than carried through a deprecation cycle.
 */
@Experimental
@Component("mten_Multitenancy")
@NullMarked
public class Multitenancy {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected TenantLoader tenantLoader;
    @Autowired
    protected TenantProvider tenantProvider;
    @Autowired
    protected TenantEntityOperation tenantEntityOperation;

    /**
     * @return tenants of the application, empty if it has none
     */
    public Set<String> getAvailableTenants() {
        return tenantLoader.findAllWithTenantId()
                .stream()
                .map(Tenant::getTenantId)
                .collect(Collectors.toSet());
    }

    /**
     * @return tenant of the current user, or null if the user belongs to none
     */
    @Nullable
    public String getCurrentUserTenantId() {
        return withoutSentinel(tenantProvider.getCurrentUserTenantId());
    }

    /**
     * @return tenant of the given user, or null if the user belongs to none
     */
    @Nullable
    public String getTenantIdForUser(UserDetails user) {
        return withoutSentinel(tenantProvider.getTenantIdForUser(user));
    }

    /**
     * Reads the tenant a record belongs to.
     * <p>
     * The two ways of not getting an answer call for opposite reactions, so they throw different types. An
     * entity with no tenant attribute is a mistake in the calling code. An attribute that was not fetched is
     * fixed by loading the record again with it in the fetch plan. A caller prepared to do without the answer
     * asks {@link #isTenantIdReadable(Object)} first.
     *
     * @return tenant of the record, or null if the record belongs to none
     * @throws IllegalArgumentException   if the entity has no attribute annotated with
     *                                    {@link io.jmix.core.annotation.TenantId}
     * @throws TenantIdNotLoadedException if the attribute has not been fetched
     */
    @Nullable
    public String getTenantIdForEntity(Object entity) {
        MetaProperty tenantProperty = tenantProperty(entity);
        if (!entityStates.isLoaded(entity, tenantProperty.getName())) {
            throw new TenantIdNotLoadedException(String.format("%s.%s is not loaded",
                    entity.getClass().getSimpleName(), tenantProperty.getName()));
        }
        // The attribute never holds the sentinel: TenantPersistingListener leaves the value alone when the
        // current user belongs to no tenant. A record whose attribute does hold that string belongs to a tenant
        // that has been named so, and translating it here would move its data into the shared scope.
        return tenantEntityOperation.getTenant(entity);
    }

    /**
     * Tells whether {@link #getTenantIdForEntity(Object)} will answer rather than throw. False both for an entity
     * that has no tenant attribute and for one whose attribute has not been fetched.
     */
    public boolean isTenantIdReadable(Object entity) {
        MetaProperty tenantProperty = metadataTools.findTenantIdProperty(metaClassOf(entity));
        return tenantProperty != null && entityStates.isLoaded(entity, tenantProperty.getName());
    }

    protected MetaProperty tenantProperty(Object entity) {
        MetaProperty tenantProperty = metadataTools.findTenantIdProperty(metaClassOf(entity));
        if (tenantProperty == null) {
            throw new IllegalArgumentException(String.format("%s has no attribute annotated with @TenantId",
                    entity.getClass().getSimpleName()));
        }
        return tenantProperty;
    }

    protected MetaClass metaClassOf(Object entity) {
        if (!EntityValues.isEntity(entity)) {
            throw new IllegalArgumentException(String.format("%s is not an entity", entity.getClass().getName()));
        }
        return metadata.getClass(entity);
    }

    @Nullable
    protected String withoutSentinel(String tenantId) {
        return TenantProvider.NO_TENANT.equals(tenantId) ? null : tenantId;
    }
}
