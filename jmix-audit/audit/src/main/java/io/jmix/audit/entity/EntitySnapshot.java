/*
 * Copyright 2021 Haulmont.
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

package io.jmix.audit.entity;

import io.jmix.core.Metadata;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.entity.ReferenceToEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "AUDIT_ENTITY_SNAPSHOT")
@Entity(name = "audit_EntitySnapshot")
public class EntitySnapshot {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @TenantId
    @Column(name = "SYS_TENANT_ID")
    private String sysTenantId;

    @Column(name = "FETCH_PLAN_XML")
    private String fetchPlanXml;

    @Column(name = "SNAPSHOT_XML")
    private String snapshotXml;

    @Column(name = "ENTITY_META_CLASS")
    private String entityMetaClass;

    @Column(name = "SNAPSHOT_DATE", nullable = false)
    private Date snapshotDate;

    @Column(name = "AUTHOR_USERNAME", nullable = false)
    private String authorUsername;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private ReferenceToEntity entity;

    @Transient
    private DatatypeRegistry datatypeRegistry;

    @Transient
    private CurrentAuthentication currentAuthentication;

    @PostConstruct
    public void init(Metadata metadata, DatatypeRegistry datatypeRegistry, CurrentAuthentication currentAuthentication) {
        entity = metadata.create(ReferenceToEntity.class);
        this.datatypeRegistry = datatypeRegistry;
        this.currentAuthentication = currentAuthentication;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }

    public String getFetchPlanXml() {
        return fetchPlanXml;
    }

    public void setFetchPlanXml(String fetchPlanXml) {
        this.fetchPlanXml = fetchPlanXml;
    }

    public String getSnapshotXml() {
        return snapshotXml;
    }

    public void setSnapshotXml(String snapshotXml) {
        this.snapshotXml = snapshotXml;
    }

    public String getEntityMetaClass() {
        return entityMetaClass;
    }

    public void setEntityMetaClass(String entityMetaClass) {
        this.entityMetaClass = entityMetaClass;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public Date getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public ReferenceToEntity getEntity() {
        return entity;
    }

    public void setEntity(ReferenceToEntity entity) {
        this.entity = entity;
    }

    public void setObjectEntityId(Object entityId) {
        entity.setObjectEntityId(entityId);
    }
}