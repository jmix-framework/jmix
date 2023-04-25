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

package io.jmix.audit.entity;

import io.jmix.core.Metadata;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.data.entity.ReferenceToEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Record containing information about entity lifecycle event.
 * Created by <code>EntityLog</code> bean.
 */
@JmixEntity
@Entity(name = "audit_EntityLog")
@Table(name = "AUDIT_ENTITY_LOG")
@Listeners("audit_EntityLogItemDetachListener")
@SystemLevel
public class EntityLogItem implements Serializable {

    private static final long serialVersionUID = 5859030306889056606L;

    public enum Type implements EnumClass<String> {
        CREATE("C"),
        MODIFY("M"),
        DELETE("D"),
        RESTORE("R");

        private String id;

        Type(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        public static Type fromId(String value) {
            if ("C".equals(value))
                return CREATE;
            else if ("M".equals(value))
                return MODIFY;
            else if ("D".equals(value))
                return DELETE;
            else if ("R".equals(value))
                return RESTORE;
            else
                return null;
        }
    }

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "CREATE_TS")
    @CreatedDate
    private Date createTs;

    @Column(name = "CREATED_BY")
    @CreatedBy
    private String createdBy;

    @Column(name = "EVENT_TS")
    private Date eventTs;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "CHANGE_TYPE", length = 1)
    private String type;

    @Column(name = "ENTITY", length = 100)
    private String entity;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private ReferenceToEntity entityRef;

    @Column(name = "ENTITY_INSTANCE_NAME", length = 1000)
    private String entityInstanceName;

    @Transient
    private transient Object dbGeneratedIdEntity;

    @Transient
    @JmixProperty
    private Set<EntityLogAttr> attributes;

    @Lob
    @Column(name = "CHANGES")
    private String changes;

    @Column(name = "SYS_TENANT_ID")
    @TenantId
    protected String sysTenantId;

    @PostConstruct
    public void init(Metadata metadata) {
        entityRef = metadata.create(ReferenceToEntity.class);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCreateTs() {
        return createTs;
    }


    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }


    public String getCreatedBy() {
        return createdBy;
    }


    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Date getEventTs() {
        return eventTs;
    }

    public void setEventTs(Date eventTs) {
        this.eventTs = eventTs;
    }

    public Type getType() {
        return Type.fromId(type);
    }

    public void setType(Type type) {
        this.type = type.getId();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<EntityLogAttr> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<EntityLogAttr> attributes) {
        this.attributes = attributes;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public ReferenceToEntity getEntityRef() {
        return entityRef;
    }

    public void setEntityRef(ReferenceToEntity entityRef) {
        this.entityRef = entityRef;
    }

    public Object getDbGeneratedIdEntity() {
        return dbGeneratedIdEntity;
    }

    public void setDbGeneratedIdEntity(Object dbGeneratedIdEntity) {
        this.dbGeneratedIdEntity = dbGeneratedIdEntity;
    }

    public String getEntityInstanceName() {
        return entityInstanceName;
    }

    public void setEntityInstanceName(String entityInstanceName) {
        this.entityInstanceName = entityInstanceName;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
