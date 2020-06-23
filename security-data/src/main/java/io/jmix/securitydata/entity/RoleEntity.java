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

package io.jmix.securitydata.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.data.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;

@Table(name = "SEC_ROLE_ENTITY")
@Entity(name = "sec_RoleEntity")
public class RoleEntity extends StandardEntity {
    private static final long serialVersionUID = -1587602133446436634L;

    @Column(name = "NAME", nullable = false)
    @InstanceName
    private @NotNull String name;

    @Column(name = "CODE", nullable = false)
    private @NotNull String code;

    @Column(name = "SCOPE")
    private @NotNull String scope;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "role")
    private List<ResourcePolicyEntity> resourcePolicies;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "role")
    private List<RowLevelPolicyEntity> rowLevelPolicies;

    public List<ResourcePolicyEntity> getResourcePolicies() {
        return resourcePolicies;
    }

    public void setResourcePolicies(List<ResourcePolicyEntity> resourcePolicies) {
        this.resourcePolicies = resourcePolicies;
    }

    public List<RowLevelPolicyEntity> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    public void setRowLevelPolicies(List<RowLevelPolicyEntity> rowLevelPolicies) {
        this.rowLevelPolicies = rowLevelPolicies;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}