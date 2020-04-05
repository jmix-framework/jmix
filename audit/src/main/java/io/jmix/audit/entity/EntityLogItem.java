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

import io.jmix.core.AppBeans;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.entity.Creatable;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotations.ModelProperty;
import io.jmix.core.metamodel.datatypes.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.data.entity.ReferenceToEntity;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Record containing information about entity lifecycle event.
 * Created by <code>EntityLog</code> bean.
 */
@Entity(name = "sec$EntityLog")
@Table(name = "SEC_ENTITY_LOG")
@Listeners("jmix_EntityLogItemDetachListener")
@SystemLevel
public class EntityLogItem extends BaseUuidEntity implements Creatable {

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

    @Column(name = "CREATE_TS")
    private Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "EVENT_TS")
    private Date eventTs;

    @Column(name = "USER_LOGIN")
    private String userLogin;

    @Column(name = "CHANGE_TYPE", length = 1)
    private String type;

    @Column(name = "ENTITY", length = 100)
    private String entity;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private ReferenceToEntity entityRef;

    @Column(name = "ENTITY_INSTANCE_NAME")
    private String entityInstanceName;

    @Transient
    private transient io.jmix.core.Entity dbGeneratedIdEntity;

    @Transient
    @ModelProperty
    private Set<EntityLogAttr> attributes;

    @Column(name = "CHANGES")
    private String changes;

    @PostConstruct
    public void init() {
        Metadata metadata = AppBeans.get(Metadata.NAME);
        entityRef = metadata.create(ReferenceToEntity.class);
    }

    @Override
    public Date getCreateTs() {
        return createTs;
    }

    @Override
    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @ModelProperty
    public String getDisplayedEntityName() {
        Metadata metadata = AppBeans.get(Metadata.NAME);
        ExtendedEntities extendedEntities = AppBeans.get(ExtendedEntities.NAME);
        MessageTools messageTools = AppBeans.get(MessageTools.NAME);
        MetaClass metaClass = metadata.getSession().getClass(entity);
        if (metaClass != null) {
            metaClass = extendedEntities.getEffectiveMetaClass(metaClass);
            return messageTools.getEntityCaption(metaClass);
        }
        return entity;
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

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
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

    public io.jmix.core.Entity getDbGeneratedIdEntity() {
        return dbGeneratedIdEntity;
    }

    public void setDbGeneratedIdEntity(io.jmix.core.Entity dbGeneratedIdEntity) {
        this.dbGeneratedIdEntity = dbGeneratedIdEntity;
    }

    public String getEntityInstanceName() {
        return entityInstanceName;
    }

    public void setEntityInstanceName(String entityInstanceName) {
        this.entityInstanceName = entityInstanceName;
    }

    public void setObjectEntityId(Object entity) {
        if (entityRef == null) {
            entityRef = AppBeans.get(Metadata.class).create(ReferenceToEntity.class);
        }
        entityRef.setObjectEntityId(entity);
    }

    public Object getObjectEntityId() {
        return entityRef == null ? null : entityRef.getObjectEntityId();
    }
}
