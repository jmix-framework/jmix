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

package io.jmix.securityui.model;

import io.jmix.core.Metadata;
import io.jmix.security.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class is used for converting {@link Role} objects into non-persistent {@link RoleModel} entities which may be
 * displayed in UI
 */
@Component("sec_RoleModelConverter")
public class RoleModelConverter {

    @Autowired
    protected Metadata metadata;

    public RoleModel createRoleModel(Role role) {
        RoleModel roleModel = metadata.create(RoleModel.class);
        roleModel.setCode(role.getCode());
        roleModel.setName(role.getName());
        roleModel.setSource(role.getSource());
        roleModel.setCustomProperties(role.getCustomProperties());

        List<ResourcePolicyModel> resourcePolicyModels = role.getResourcePolicies().stream()
                .map(resourcePolicy -> {
                    ResourcePolicyModel model = metadata.create(ResourcePolicyModel.class);
                    model.setType(resourcePolicy.getType());
                    model.setResource(resourcePolicy.getResource());
                    model.setAction(resourcePolicy.getAction());
                    model.setEffect(resourcePolicy.getEffect());
                    model.setCustomProperties(resourcePolicy.getCustomProperties());
                    return model;
                })
                .collect(Collectors.toList());
        roleModel.setResourcePolicies(resourcePolicyModels);

        List<RowLevelPolicyModel> rowLevelPolicyModels = role.getRowLevelPolicies().stream()
                .map(rowLevelPolicy -> {
                    RowLevelPolicyModel model = metadata.create(RowLevelPolicyModel.class);
                    model.setType(rowLevelPolicy.getType());
                    model.setAction(rowLevelPolicy.getAction());
                    model.setEntityName(rowLevelPolicy.getEntityName());
                    model.setJoinClause(rowLevelPolicy.getJoinClause());
                    model.setWhereClause(rowLevelPolicy.getWhereClause());
                    model.setCustomProperties(rowLevelPolicy.getCustomProperties());
                    return model;
                })
                .collect(Collectors.toList());
        roleModel.setRowLevelPolicies(rowLevelPolicyModels);

        return roleModel;
    }
}
