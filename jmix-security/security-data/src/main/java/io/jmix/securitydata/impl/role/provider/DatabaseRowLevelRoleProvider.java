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

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.security.model.*;
import io.jmix.security.role.RowLevelRoleProvider;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import org.springframework.context.ApplicationContext;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Role provider that gets row level roles from the database from the {@link RowLevelRoleEntity}.
 */
@Component("sec_DatabaseRowLevelRoleProvider")
public class DatabaseRowLevelRoleProvider extends BaseDatabaseRoleProvider<RowLevelRole>
        implements RowLevelRoleProvider {

    private final ScriptEvaluator scriptEvaluator;
    private final ApplicationContext applicationContext;

    public DatabaseRowLevelRoleProvider(ScriptEvaluator scriptEvaluator, ApplicationContext applicationContext) {
        this.scriptEvaluator = scriptEvaluator;
        this.applicationContext = applicationContext;
    }

    @Override
    protected Class<?> getRoleClass() {
        return RowLevelRoleEntity.class;
    }

    @Override
    protected void buildFetchPlan(FetchPlanBuilder fetchPlanBuilder) {
        fetchPlanBuilder
                .addAll("name", "code", "description", "childRoles", "sysTenantId")
                .add("rowLevelPolicies", FetchPlan.BASE);
    }

    protected RowLevelRole buildRole(Object entity) {
        RowLevelRoleEntity roleEntity = (RowLevelRoleEntity) entity;

        RowLevelRole role = new RowLevelRole();
        role.setName(roleEntity.getName());
        role.setCode(roleEntity.getCode());
        role.setDescription(roleEntity.getDescription());
        role.setSource(RoleSource.DATABASE);
        role.setChildRoles(roleEntity.getChildRoles());
        role.getCustomProperties().put("databaseId", roleEntity.getId().toString());
        role.setTenantId(roleEntity.getSysTenantId());

        List<RowLevelPolicyEntity> rowLevelPolicyEntities = roleEntity.getRowLevelPolicies();
        if (rowLevelPolicyEntities != null) {
            List<RowLevelPolicy> rowLevelPolicies = rowLevelPolicyEntities.stream()
                    .map(policyEntity -> {
                                String id = policyEntity.getId().toString();
                                Map<String, String> customProperties = new HashMap<>();
                                customProperties.put("databaseId", id);
                                switch (policyEntity.getType()) {
                                    case JPQL:
                                        return new RowLevelPolicy(policyEntity.getEntityName(),
                                                policyEntity.getWhereClause(),
                                                policyEntity.getJoinClause(),
                                                customProperties);
                                    case PREDICATE:
                                        return new RowLevelPolicy(policyEntity.getEntityName(),
                                                policyEntity.getAction(),
                                                policyEntity.getScript(),
                                                createPredicateFromScript(policyEntity.getScript()),
                                                customProperties);
                                    default:
                                        throw new RuntimeException("Unknown row level policy type " + policyEntity.getType());
                                }
                            }
                    )
                    .collect(Collectors.toList());
            role.setRowLevelPolicies(rowLevelPolicies);
        }
        return role;
    }

    public RowLevelBiPredicate<Object, ApplicationContext> createPredicateFromScript(String script) {
        return (entity, applicationContext) -> {
            String modifiedScript = script.replace("{E}", "__entity__");
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("__entity__", entity);
            arguments.put("applicationContext", applicationContext);
            return Boolean.TRUE.equals(scriptEvaluator.evaluate(new StaticScriptSource(modifiedScript), arguments));
        };
    }
}