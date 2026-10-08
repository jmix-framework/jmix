/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securitydata.impl.role.provider;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.RoleProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Base role provider that gets resource roles/row level roles from the database.
 */
public abstract class BaseDatabaseRoleProvider<T extends BaseRole> implements RoleProvider<T> {

    private static final Logger log = LoggerFactory.getLogger(BaseDatabaseRoleProvider.class);

    protected UnconstrainedDataManager dataManager;
    protected Metadata metadata;
    protected AccessManager accessManager;

    // The localized values that could not be read and were reported, by the code of the role and the property.
    protected final Map<String, String> reportedLocalizedValues = new ConcurrentHashMap<>();

    @Override
    @NonNull
    public Collection<T> getAllRoles() {
        return getAllRoles(true);
    }

    @Override
    @NonNull
    public Collection<T> getAllRoles(boolean includePolicies) {
        List<T> roles = dataManager.load(getRoleClass())
                .all()
                .fetchPlan(fetchPlanBuilder -> buildFetchPlan(fetchPlanBuilder, includePolicies))
                .list()
                .stream()
                .map(entity -> buildRole(entity, includePolicies))
                .collect(Collectors.toList());

        roles.forEach(this::checkLocalizedValues);

        return roles;
    }

    @Nullable
    @Override
    public T findRoleByCode(@NonNull String code) {
        T role = dataManager.load(getRoleClass())
                .query(buildFindByCodeQuery())
                .parameter("code", code)
                .fetchPlan(this::buildFetchPlan)
                .optional()
                .map(this::buildRole)
                .orElse(null);

        if (role != null) {
            checkLocalizedValues(role);
        }

        return role;
    }

    @Override
    public boolean deleteRole(@NonNull T role) {
        CrudEntityContext entityContext = new CrudEntityContext(metadata.getClass(getRoleClass()));
        accessManager.applyRegisteredConstraints(entityContext);
        if (!entityContext.isDeletePermitted()) {
            return false;
        }

        String roleDatabaseId = role.getCustomProperties().get("databaseId");
        Object roleEntity;
        if (Strings.isNullOrEmpty(roleDatabaseId)) {
            throw new IllegalArgumentException(String.format("Database ID of role with code \"%s\" is empty", role.getCode()));
        } else {
            UUID roleEntityId = UUID.fromString(roleDatabaseId);
            roleEntity = dataManager.getReference(getRoleClass(), roleEntityId);
            dataManager.remove(roleEntity);
        }
        return true;
    }

    @Autowired
    public void setDataManager(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    protected abstract T buildRole(Object entity);

    protected T buildRole(Object entity, boolean includePolicies) {
        return buildRole(entity);
    }

    protected abstract Class<?> getRoleClass();

    protected abstract void buildFetchPlan(FetchPlanBuilder fetchPlanBuilder);

    protected void buildFetchPlan(FetchPlanBuilder fetchPlanBuilder, boolean includePolicies) {
        buildFetchPlan(fetchPlanBuilder);
    }

    protected String buildFindByCodeQuery() {
        return "where e.code = :code";
    }

    /**
     * Logs the localized values of the role that cannot be read. The role keeps them as they are stored and is shown
     * with its default texts instead. The role is reported here, where its code is known, rather than every time it is
     * shown, and at {@code WARN} once for the same values: lists build their roles on every load, so the values are
     * logged at {@code DEBUG} afterwards until they change.
     */
    protected void checkLocalizedValues(T role) {
        checkLocalizedValuesInternal(role, "localizedNames", role.getLocalizedNames());
        checkLocalizedValuesInternal(role, "localizedDescriptions", role.getLocalizedDescriptions());
    }

    protected void checkLocalizedValuesInternal(T role, String property, @Nullable String localizedValues) {
        String key = role.getCode() + "/" + property;
        try {
            RoleLocalizedValuesUtils.read(localizedValues);
            reportedLocalizedValues.remove(key);
        } catch (IllegalArgumentException e) {
            // Values that cannot be read are not null, since null is read as no values.
            String unreadableValues = Objects.requireNonNull(localizedValues);
            boolean reported = unreadableValues.equals(reportedLocalizedValues.put(key, unreadableValues));

            log.atLevel(reported ? Level.DEBUG : Level.WARN)
                    .log("Cannot read the {} of role '{}', its default text is shown instead: {}",
                            property, role.getCode(), e.getMessage());
        }
    }
}
