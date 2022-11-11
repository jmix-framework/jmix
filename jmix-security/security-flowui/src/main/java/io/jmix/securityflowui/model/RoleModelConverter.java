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

package io.jmix.securityflowui.model;

import io.jmix.core.Metadata;
import io.jmix.security.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class is used for converting {@link ResourceRole}, {@link RowLevelRole} objects
 * into non-persistent {@link ResourceRoleModel}, {@link RowLevelRoleModel} entities which may be
 * displayed in UI
 */
@Component("sec_FlowuiRoleModelConverter")
public class RoleModelConverter {

    @Autowired
    protected Metadata metadata;

    public ResourceRoleModel createResourceRoleModel(ResourceRole role) {
        ResourceRoleModel roleModel = metadata.create(ResourceRoleModel.class);

        initBaseParameters(roleModel, role);
        roleModel.setScopes(role.getScopes());
        roleModel.setResourcePolicies(createResourcePolicyModels(role.getResourcePolicies()));

        return roleModel;
    }


    public RowLevelRoleModel createRowLevelRoleModel(RowLevelRole role) {
        RowLevelRoleModel roleModel = metadata.create(RowLevelRoleModel.class);

        initBaseParameters(roleModel, role);

        roleModel.setRowLevelPolicies(createRowLevelPolicyModels(role.getRowLevelPolicies()));

        return roleModel;
    }

    protected void initBaseParameters(BaseRoleModel roleModel, BaseRole role) {
        roleModel.setCode(role.getCode());
        roleModel.setDescription(role.getDescription());
        roleModel.setName(role.getName());
        roleModel.setSource(RoleSource.fromId(role.getSource()));
        roleModel.setChildRoles(role.getChildRoles());
        roleModel.setCustomProperties(role.getCustomProperties());
    }

    protected List<ResourcePolicyModel> createResourcePolicyModels(Collection<ResourcePolicy> resourcePolicies) {
        return resourcePolicies.stream()
                .map(resourcePolicy -> {
                    ResourcePolicyModel model = metadata.create(ResourcePolicyModel.class);
                    model.setType(ResourcePolicyType.fromId(resourcePolicy.getType()));
                    model.setResource(resourcePolicy.getResource());
                    model.setAction(resourcePolicy.getAction());
                    model.setEffect(resourcePolicy.getEffect());
                    model.setPolicyGroup(resourcePolicy.getPolicyGroup());
                    model.setCustomProperties(resourcePolicy.getCustomProperties());
                    return model;
                })
                .collect(Collectors.toList());
    }

    protected List<RowLevelPolicyModel> createRowLevelPolicyModels(Collection<RowLevelPolicy> resourcePolicies) {
        return resourcePolicies.stream()
                .map(rowLevelPolicy -> {
                    RowLevelPolicyModel model = metadata.create(RowLevelPolicyModel.class);
                    model.setType(rowLevelPolicy.getType());
                    model.setAction(rowLevelPolicy.getAction());
                    model.setEntityName(rowLevelPolicy.getEntityName());
                    model.setJoinClause(rowLevelPolicy.getJoinClause());
                    model.setWhereClause(rowLevelPolicy.getWhereClause());
                    model.setScript(rowLevelPolicy.getScript());
                    model.setCustomProperties(rowLevelPolicy.getCustomProperties());
                    return model;
                })
                .collect(Collectors.toList());
    }
}
