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

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;

import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Non-persistent entity used to display row level policies in UI
 */
@JmixEntity(name = "sec_RowLevelPolicyModel")
public class RowLevelPolicyModel {

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty(mandatory = true)
    private RowLevelPolicyType type;

    @JmixProperty(mandatory = true)
    private RowLevelPolicyAction action;

    @JmixProperty(mandatory = true)
    private String entityName;

    @JmixProperty
    private String whereClause;

    @JmixProperty
    private String joinClause;

    @JmixProperty
    private String script;

    @JmixProperty
    private Map<String, String> customProperties = new HashMap<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
}
