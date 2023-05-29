/*
 * Copyright 2019 Haulmont.
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
package com.haulmont.cuba.core.model.common;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.ConstraintOperationType;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

/**
 * Security constraint definition entity.
 */
@Entity(name = "test$Constraint")
@JmixEntity
@Table(name = "TEST_CONSTRAINT")
@SystemLevel
public class Constraint extends StandardEntity {

    private static final long serialVersionUID = -8598548105315052474L;

    @Column(name = "CHECK_TYPE", length = 50, nullable = false)
    protected String checkType = ConstraintCheckType.DATABASE.getId();

    @Column(name = "OPERATION_TYPE", length = 50, nullable = false)
    protected String operationType = ConstraintOperationType.READ.getId();

    @Column(name = "CODE", length = 255)
    protected String code;

    @Column(name = "ENTITY_NAME", length = 255, nullable = false)
    protected String entityName;

    @Column(name = "JOIN_CLAUSE", length = 500)
    protected String joinClause;

    @Column(name = "WHERE_CLAUSE", length = 1000)
    protected String whereClause;

    @Lob
    @Column(name = "GROOVY_SCRIPT")
    protected String groovyScript;

    @Lob
    @Column(name = "FILTER_XML")
    protected String filterXml;

    @Column(name = "IS_ACTIVE")
    protected Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    protected Group group;

    @Transient
    protected boolean predefined;

    @Column(name = "SYS_TENANT_ID")
    @TenantId
    protected String sysTenantId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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

    public String getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(String groovyScript) {
        this.groovyScript = groovyScript;
    }

    public String getFilterXml() {
        return filterXml;
    }

    public void setFilterXml(String filterXml) {
        this.filterXml = filterXml;
    }

    public ConstraintCheckType getCheckType() {
        return ConstraintCheckType.fromId(checkType);
    }

    public void setCheckType(ConstraintCheckType checkType) {
        this.checkType = checkType != null ? checkType.getId() : null;
    }

    public ConstraintOperationType getOperationType() {
        return ConstraintOperationType.fromId(operationType);
    }

    public void setOperationType(ConstraintOperationType operationType) {
        this.operationType = operationType != null ? operationType.getId() : null;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }


    public boolean isPredefined() {
        return predefined;
    }

    public void setPredefined(boolean predefined) {
        this.predefined = predefined;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
