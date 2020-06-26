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

import io.jmix.data.entity.StandardEntity;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "SEC_ROW_LEVEL_POLICY_ENTITY")
@Entity(name = "sec_RowLevelPolicyEntity")
public class RowLevelPolicyEntity extends StandardEntity {
    private static final long serialVersionUID = -8009316149061437606L;

    @NotNull
    @Column(name = "TYPE_", nullable = false)
    private String type;

    @NotNull
    @Column(name = "ENTITY_NAME", nullable = false)
    private String entityName;

    @NotNull
    @Column(name = "ACTION_", nullable = false)
    private String action;

    @Lob
    @Column(name = "WHERE_CLAUSE", length = 5000)
    private String whereClause;

    @Lob
    @Column(name = "JOiN_CLAUSE", length = 5000)
    private String joinClause;

    @Lob
    @Column(name = "SCRIPT_", length = 5000)
    private String script;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROLE_ENTITY_ID")
    private RoleEntity role;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public RowLevelPolicyAction getAction() {
        return RowLevelPolicyAction.fromId(action);
    }

    public void setAction(RowLevelPolicyAction action) {
        this.action = action != null ? action.getId() : null;
    }

    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public String getJoinClause() {
        return joinClause;
    }

    public void setJoinClause(String joinClause) {
        this.joinClause = joinClause;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public RowLevelPolicyType getType() {
        return RowLevelPolicyType.fromId(type);
    }

    public void setType(RowLevelPolicyType type) {
        this.type = type != null ? type.getId() : null;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}