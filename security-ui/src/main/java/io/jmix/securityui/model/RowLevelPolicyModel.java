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

import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;

import java.util.HashMap;
import java.util.Map;

/**
 * Non-persistent entity used to display row level policies in UI
 */
@ModelObject(name = "sec_RowLevelPolicyModel")
public class RowLevelPolicyModel extends BaseUuidEntity {

    @ModelProperty(mandatory = true)
    private RowLevelPolicyType type;

    @ModelProperty(mandatory = true)
    private RowLevelPolicyAction action;

    @ModelProperty(mandatory = true)
    private String entityName;

    @ModelProperty
    private String whereClause;

    @ModelProperty
    private String joinClause;

    @ModelProperty
    private Map<String, String> customProperties = new HashMap<>();

    public RowLevelPolicyType getType() {
        return type;
    }

    public void setType(RowLevelPolicyType type) {
        this.type = type;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getJoinClause() {
        return joinClause;
    }

    public void setJoinClause(String joinClause) {
        this.joinClause = joinClause;
    }

    public RowLevelPolicyAction getAction() {
        return action;
    }

    public void setAction(RowLevelPolicyAction action) {
        this.action = action;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
}
