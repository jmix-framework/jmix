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

package io.jmix.security.model;

import com.google.common.base.Strings;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

/**
 * Class is used for converting {@link ResourceRole}, {@link RowLevelRole} objects
 * into non-persistent {@link ResourceRoleModel}, {@link RowLevelRoleModel} entities which may be
 * displayed in UI
 */
@Component("sec_RoleModelConverter")
public class RoleModelConverter {

    private final EntityStates entityStates;
    @Autowired
    protected Metadata metadata;

    public RoleModelConverter(EntityStates entityStates) {
        this.entityStates = entityStates;
    }

    public ResourceRoleModel createResourceRoleModel(ResourceRole role) {
        ResourceRoleModel roleModel = metadata.create(ResourceRoleModel.class);

        initBaseParameters(roleModel, role);
        roleModel.setScopes(role.getScopes());
        roleModel.setResourcePolicies(createResourcePolicyModels(role.getResourcePolicies()));

        entityStates.setNew(roleModel, false);

        return roleModel;
    }


    public RowLevelRoleModel createRowLevelRoleModel(RowLevelRole role) {
        RowLevelRoleModel roleModel = metadata.create(RowLevelRoleModel.class);

        initBaseParameters(roleModel, role);

        roleModel.setRowLevelPolicies(createRowLevelPolicyModels(role.getRowLevelPolicies()));

        entityStates.setNew(roleModel, false);

        return roleModel;
    }

    protected void initBaseParameters(BaseRoleModel roleModel, BaseRole role) {
        String databaseId = role.getCustomProperties().get("databaseId");
        if (databaseId != null) {
            roleModel.setId(UUID.fromString(databaseId));
        }
        roleModel.setCode(role.getCode());
        roleModel.setDescription(role.getDescription());
        roleModel.setName(role.getName());
        roleModel.setSource(RoleSourceType.fromId(role.getSource()));
        roleModel.setChildRoles(role.getChildRoles());
        roleModel.setCustomProperties(role.getCustomProperties());
    }

    protected List<ResourcePolicyModel> createResourcePolicyModels(Collection<ResourcePolicy> resourcePolicies) {
        return resourcePolicies.stream()
                .map(resourcePolicy -> {
                    ResourcePolicyModel model = metadata.create(ResourcePolicyModel.class);
                    String databaseId = resourcePolicy.getCustomProperties().get("databaseId");
                    if (databaseId != null) {
                        model.setId(UUID.fromString(databaseId));
                    }
                    model.setType(resourcePolicy.getType());
                    model.setResource(resourcePolicy.getResource());
                    model.setAction(resourcePolicy.getAction());
                    model.setEffect(resourcePolicy.getEffect());
                    model.setPolicyGroup(resourcePolicy.getPolicyGroup());
                    model.setCustomProperties(resourcePolicy.getCustomProperties());
                    entityStates.setNew(model, false);
                    return model;
                })
                .sorted((model1, model2) -> CASE_INSENSITIVE_ORDER.compare(
                        Strings.nullToEmpty(model1.getPolicyGroup()),
                        Strings.nullToEmpty(model2.getPolicyGroup())))
                .collect(Collectors.toList());
    }

    protected List<RowLevelPolicyModel> createRowLevelPolicyModels(Collection<RowLevelPolicy> rowLevelPolicies) {
        return rowLevelPolicies.stream()
                .map(rowLevelPolicy -> {
                    RowLevelPolicyModel model = metadata.create(RowLevelPolicyModel.class);
                    String databaseId = rowLevelPolicy.getCustomProperties().get("databaseId");
                    if (databaseId != null) {
                        model.setId(UUID.fromString(databaseId));
                    }
                    model.setType(rowLevelPolicy.getType());
                    model.setAction(rowLevelPolicy.getAction());
                    model.setEntityName(rowLevelPolicy.getEntityName());
                    model.setJoinClause(rowLevelPolicy.getJoinClause());
                    model.setWhereClause(rowLevelPolicy.getWhereClause());
                    model.setScript(rowLevelPolicy.getScript());
                    model.setCustomProperties(rowLevelPolicy.getCustomProperties());
                    entityStates.setNew(model, false);
                    return model;
                })
                .collect(Collectors.toList());
    }
}
