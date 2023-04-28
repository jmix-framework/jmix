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
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.RoleProvider;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Base role provider that gets resource roles/row level roles from the database.
 */
public abstract class BaseDatabaseRoleProvider<T extends BaseRole> implements RoleProvider<T> {

    protected UnconstrainedDataManager dataManager;
    protected Metadata metadata;
    protected AccessManager accessManager;

    @Override
    public Collection<T> getAllRoles() {
        return dataManager.load(getRoleClass())
                .all()
                .fetchPlan(this::buildFetchPlan)
                .list()
                .stream()
                .map(this::buildRole)
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public T findRoleByCode(String code) {
        return dataManager.load(getRoleClass())
                .query(buildFindByCodeQuery())
                .parameter("code", code)
                .fetchPlan(this::buildFetchPlan)
                .optional()
                .map(this::buildRole)
                .orElse(null);
    }

    @Override
    public boolean deleteRole(T role) {
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

    protected abstract Class<?> getRoleClass();

    protected abstract void buildFetchPlan(FetchPlanBuilder fetchPlanBuilder);

    protected String buildFindByCodeQuery() {
        return "where e.code = :code";
    }
}
