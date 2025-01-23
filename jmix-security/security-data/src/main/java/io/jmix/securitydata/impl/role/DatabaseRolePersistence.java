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

package io.jmix.securitydata.impl.role;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.impl.jpql.ErrorRec;
import io.jmix.data.impl.jpql.JpqlSyntaxException;
import io.jmix.security.model.*;
import io.jmix.security.role.RolePersistence;
import io.jmix.securitydata.entity.*;
import io.jmix.securitydata.impl.role.provider.DatabaseRowLevelRoleProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("sec_DatabaseRolePersistence")
public class DatabaseRolePersistence implements RolePersistence {

    private final ApplicationContext applicationContext;
    private final Metadata metadata;
    private final EntityStates entityStates;
    private final DataManager dataManager;
    private final FetchPlans fetchPlans;
    private final EntityImportExport entityImportExport;
    private final EntityImportPlans entityImportPlans;
    private final QueryTransformerFactory queryTransformerFactory;
    private final DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider;

    public DatabaseRolePersistence(ApplicationContext applicationContext, Metadata metadata,
                                   EntityStates entityStates, DataManager dataManager, FetchPlans fetchPlans,
                                   EntityImportExport entityImportExport, EntityImportPlans entityImportPlans,
                                   QueryTransformerFactory queryTransformerFactory,
                                   DatabaseRowLevelRoleProvider databaseRowLevelRoleProvider) {
        this.applicationContext = applicationContext;
        this.metadata = metadata;
        this.entityStates = entityStates;
        this.dataManager = dataManager;
        this.fetchPlans = fetchPlans;
        this.entityImportExport = entityImportExport;
        this.entityImportPlans = entityImportPlans;
        this.queryTransformerFactory = queryTransformerFactory;
        this.databaseRowLevelRoleProvider = databaseRowLevelRoleProvider;
    }

    @Override
    public void save(ResourceRoleModel roleModel) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);

        if (entityStates.isNew(roleModel)) {
            ResourceRoleEntity roleEntity = resourceRoleModelToEntity(roleModel);
            saveContext.saving(roleEntity);

            if (roleModel.getResourcePolicies() != null) {
                for (ResourcePolicyModel policyModel : roleModel.getResourcePolicies()) {
                    ResourcePolicyEntity policyEntity = resourcePolicyModelToEntity(policyModel);
                    policyEntity.setRole(roleEntity);
                    saveContext.saving(policyEntity);
                }
            }

        } else {
            ResourceRoleEntity roleEntity = dataManager.load(ResourceRoleEntity.class).id(roleModel.getId()).one();
            resourceRoleModelToEntity(roleModel, roleEntity);
            saveContext.saving(roleEntity);

            Map<UUID, ResourcePolicyEntity> existingPolicies = roleEntity.getResourcePolicies().stream()
                    .collect(Collectors.toMap(ResourcePolicyEntity::getId, Function.identity()));

            for (ResourcePolicyModel policyModel : roleModel.getResourcePolicies()) {
                ResourcePolicyEntity policyEntity = existingPolicies.get(policyModel.getId());
                if (policyEntity == null) {
                    policyEntity = resourcePolicyModelToEntity(policyModel);
                    policyEntity.setRole(roleEntity);
                    saveContext.saving(policyEntity);
                } else {
                    if (!Objects.equals(policyModel.getResource(), policyEntity.getResource())
                            || !Objects.equals(policyModel.getAction(), policyEntity.getAction())
                            || !Objects.equals(policyModel.getEffect(), policyEntity.getEffect())
                            || !Objects.equals(policyModel.getPolicyGroup(), policyEntity.getPolicyGroup())) {
                        resourcePolicyModelToEntity(policyModel, policyEntity);
                        saveContext.saving(policyEntity);
                    }
                }
            }

            for (ResourcePolicyEntity policyEntity : roleEntity.getResourcePolicies()) {
                if (roleModel.getResourcePolicies().stream().noneMatch(model -> model.getId().equals(policyEntity.getId()))) {
                    saveContext.removing(policyEntity);
                }
            }
        }

        dataManager.save(saveContext);
    }

    @Override
    public void save(RowLevelRoleModel roleModel) {
        SaveContext saveContext = new SaveContext().setDiscardSaved(true);

        if (entityStates.isNew(roleModel)) {
            RowLevelRoleEntity roleEntity = rowLevelRoleModelToEntity(roleModel);
            saveContext.saving(roleEntity);

            if (roleModel.getRowLevelPolicies() != null) {
                for (RowLevelPolicyModel policyModel : roleModel.getRowLevelPolicies()) {
                    RowLevelPolicyEntity policyEntity = rowLevelPolicyModelToEntity(policyModel);
                    policyEntity.setRole(roleEntity);
                    saveContext.saving(policyEntity);
                }
            }

        } else {
            RowLevelRoleEntity roleEntity = dataManager.load(RowLevelRoleEntity.class).id(roleModel.getId()).one();
            rowLevelRoleModelToEntity(roleModel, roleEntity);
            saveContext.saving(roleEntity);

            Map<UUID, RowLevelPolicyEntity> existingPolicies = roleEntity.getRowLevelPolicies().stream()
                    .collect(Collectors.toMap(RowLevelPolicyEntity::getId, Function.identity()));

            for (RowLevelPolicyModel policyModel : roleModel.getRowLevelPolicies()) {
                RowLevelPolicyEntity policyEntity = existingPolicies.get(policyModel.getId());
                if (policyEntity == null) {
                    policyEntity = rowLevelPolicyModelToEntity(policyModel);
                    policyEntity.setRole(roleEntity);
                } else {
                    rowLevelPolicyModelToEntity(policyModel, policyEntity);
                }
                saveContext.saving(policyEntity);
            }

            for (RowLevelPolicyEntity policyEntity : roleEntity.getRowLevelPolicies()) {
                if (roleModel.getRowLevelPolicies().stream().noneMatch(model -> model.getId().equals(policyEntity.getId()))) {
                    saveContext.removing(policyEntity);
                }
            }
        }

        dataManager.save(saveContext);
    }

    @Override
    public void removeRoles(Collection<? extends BaseRoleModel> roleModels) {
        List<Object> entitiesToRemove = roleModels.stream()
                .map(model ->
                        model instanceof ResourceRoleModel resourceRoleModel ?
                                resourceRoleModelToEntity(resourceRoleModel) :
                                rowLevelRoleModelToEntity((RowLevelRoleModel) model))
                .collect(Collectors.toCollection(ArrayList::new));

        List<RoleAssignmentEntity> roleAssignments = dataManager.load(RoleAssignmentEntity.class)
                .query("e.roleCode IN :codes")
                .parameter("codes", roleModels.stream()
                        .map(BaseRoleModel::getCode)
                        .collect(Collectors.toList()))
                .list();

        entitiesToRemove.addAll(roleAssignments);

        dataManager.remove(entitiesToRemove);
    }

    @Override
    public byte[] exportResourceRoles(List<ResourceRoleModel> roleModels, boolean zip) {
        List<ResourceRoleEntity> roleEntities = roleModels.stream()
                .map(this::resourceRoleModelToEntity)
                .toList();
        return zip ?
                entityImportExport.exportEntitiesToZIP(roleEntities, createResourceRoleExportFetchPlan()) :
                entityImportExport.exportEntitiesToJSON(roleEntities, createResourceRoleExportFetchPlan()).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportRowLevelRoles(List<RowLevelRoleModel> roleModels, boolean zip) {
        List<RowLevelRoleEntity> roleEntities = roleModels.stream()
                .map(this::rowLevelRoleModelToEntity)
                .toList();
        return zip ?
                entityImportExport.exportEntitiesToZIP(roleEntities, createRowLevelRoleExportFetchPlan()) :
                entityImportExport.exportEntitiesToJSON(roleEntities, createRowLevelRoleExportFetchPlan()).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<Object> importResourceRoles(byte[] data, boolean zip) {
        Collection<Object> importedEntities;
        if (zip) {
            importedEntities = entityImportExport.importEntitiesFromZIP(data, createResourceRoleEntityImportPlan());
        } else {
            importedEntities = entityImportExport.importEntitiesFromJson(new String(data, StandardCharsets.UTF_8), createResourceRoleEntityImportPlan());
        }
        return new ArrayList<>(importedEntities);
    }

    @Override
    public List<Object> importRowLevelRoles(byte[] data, boolean zip) {
        Collection<Object> importedEntities;
        if (zip) {
            importedEntities = entityImportExport.importEntitiesFromZIP(data, createRowLevelRoleEntityImportPlan());
        } else {
            importedEntities = entityImportExport.importEntitiesFromJson(new String(data, StandardCharsets.UTF_8), createRowLevelRoleEntityImportPlan());
        }
        return new ArrayList<>(importedEntities);
    }

    @Override
    public List<String> checkRowLevelJpqlPolicySyntax(String entityName, String joinClause, String whereClause) {
        String baseQueryString = "select e from " + entityName + " e";
        try {
            QueryTransformer transformer = queryTransformerFactory.transformer(baseQueryString);
            if (StringUtils.isNotBlank(joinClause)) {
                transformer.addJoinAndWhere(joinClause, whereClause);
            } else {
                transformer.addWhere(whereClause);
            }

            String jpql = transformer.getResult();
            dataManager.load(metadata.getClass(entityName).getJavaClass())
                    .query(jpql)
                    .maxResults(0)
                    .list();

            return List.of();
        } catch (JpqlSyntaxException e) {
            return e.getErrorRecs().stream().map(ErrorRec::toString).toList();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause == null) {
                rootCause = e;
            }
            return List.of(rootCause.toString());
        }
    }

    @Override
    @Nullable
    public String checkRowLevelPredicatePolicySyntax(String entityName, String script) {
        RowLevelBiPredicate<Object, ApplicationContext> predicate = databaseRowLevelRoleProvider.createPredicateFromScript(script);
        Object entity = metadata.create(entityName);
        try {
            predicate.test(entity, applicationContext);
            return null;
        } catch (ScriptCompilationException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause == null) {
                rootCause = e;
            }
            return rootCause.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private FetchPlan createResourceRoleExportFetchPlan() {
        return fetchPlans.builder(ResourceRoleEntity.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("resourcePolicies", FetchPlan.BASE)
                .build();
    }

    private FetchPlan createRowLevelRoleExportFetchPlan() {
        return fetchPlans.builder(RowLevelRoleEntity.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("rowLevelPolicies", FetchPlan.BASE)
                .build();
    }

    private EntityImportPlan createResourceRoleEntityImportPlan() {
        return entityImportPlans.builder(ResourceRoleEntity.class)
                .addLocalProperties()
                .addProperty(new EntityImportPlanProperty(
                        "resourcePolicies",
                        entityImportPlans.builder(ResourcePolicyEntity.class).addLocalProperties().build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                )
                .build();
    }

    private EntityImportPlan createRowLevelRoleEntityImportPlan() {
        return entityImportPlans.builder(RowLevelRoleEntity.class)
                .addLocalProperties()
                .addProperty(new EntityImportPlanProperty(
                        "rowLevelPolicies",
                        entityImportPlans.builder(RowLevelPolicyEntity.class).addLocalProperties().build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS)
                )
                .build();
    }

    private ResourceRoleEntity resourceRoleModelToEntity(ResourceRoleModel model) {
        ResourceRoleEntity entity = dataManager.create(ResourceRoleEntity.class);
        resourceRoleModelToEntity(model, entity);
        entityStates.setNew(entity, entityStates.isNew(model));
        return entity;
    }

    private RowLevelRoleEntity rowLevelRoleModelToEntity(RowLevelRoleModel model) {
        RowLevelRoleEntity entity = dataManager.create(RowLevelRoleEntity.class);
        rowLevelRoleModelToEntity(model, entity);
        entityStates.setNew(entity, entityStates.isNew(model));
        return entity;
    }

    private void resourceRoleModelToEntity(ResourceRoleModel model, ResourceRoleEntity entity) {
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setCode(model.getCode());
        entity.setDescription(model.getDescription());
        entity.setScopes(model.getScopes());
        entity.setChildRoles(model.getChildRoles());
    }

    private void rowLevelRoleModelToEntity(RowLevelRoleModel model, RowLevelRoleEntity entity) {
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setCode(model.getCode());
        entity.setDescription(model.getDescription());
        entity.setChildRoles(model.getChildRoles());
    }

    private ResourcePolicyEntity resourcePolicyModelToEntity(ResourcePolicyModel model) {
        ResourcePolicyEntity entity = dataManager.create(ResourcePolicyEntity.class);
        resourcePolicyModelToEntity(model, entity);
        entityStates.setNew(entity, entityStates.isNew(model));
        return entity;
    }

    private void resourcePolicyModelToEntity(ResourcePolicyModel model, ResourcePolicyEntity entity) {
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setResource(model.getResource());
        entity.setAction(model.getAction());
        entity.setEffect(model.getEffect());
        entity.setPolicyGroup(model.getPolicyGroup());
    }

    private RowLevelPolicyEntity rowLevelPolicyModelToEntity(RowLevelPolicyModel model) {
        RowLevelPolicyEntity entity = dataManager.create(RowLevelPolicyEntity.class);
        rowLevelPolicyModelToEntity(model, entity);
        entityStates.setNew(entity, entityStates.isNew(model));
        return entity;
    }

    private void rowLevelPolicyModelToEntity(RowLevelPolicyModel model, RowLevelPolicyEntity entity) {
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setEntityName(model.getEntityName());
        entity.setJoinClause(model.getJoinClause());
        entity.setWhereClause(model.getWhereClause());
        entity.setScript(model.getScript());
        entity.setAction(model.getAction());
    }

}
