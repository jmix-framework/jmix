/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.model.common;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Creatable;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import io.jmix.core.Metadata;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.data.entity.ReferenceToEntity;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.Date;

/**
 * Snapshot for entity.
 */
@Entity(name = "test$EntitySnapshot")
@JmixEntity
@Table(name = "TEST_ENTITY_SNAPSHOT")
@SystemLevel
public class EntitySnapshot extends BaseUuidEntity implements Creatable {

    private static final long serialVersionUID = 4835363127711391591L;

    @Column(name = "CREATE_TS")
    protected Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

    @Column(name = "VIEW_XML")
    private String viewXml;

    @Column(name = "SNAPSHOT_XML")
    private String snapshotXml;

    @Column(name = "ENTITY_META_CLASS")
    private String entityMetaClass;

    @Column(name = "SNAPSHOT_DATE", nullable = false)
    private Date snapshotDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AUTHOR_ID")
    private User author;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private ReferenceToEntity entity;

    @PostConstruct
    public void init() {
        Metadata metadata = AppBeans.get(Metadata.class);
        entity = metadata.create(ReferenceToEntity.class);
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

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }

    public String getViewXml() {
        return viewXml;
    }

    public void setViewXml(String viewXml) {
        this.viewXml = viewXml;
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

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getAuthor() {
        return author;
    }

    @JmixProperty
    @DependsOnProperties({"snapshotDate", "author"})
    public String getLabel() {
        String name = "";
        if (author != null && StringUtils.isNotEmpty(this.author.getCaption())) {
            name += this.author.getCaption() + " ";
        }

        Datatype datatype = Datatypes.getNN(Date.class);

        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);
        if (userSessionSource != null && userSessionSource.checkCurrentUserSession()) {
            name += datatype.format(snapshotDate, userSessionSource.getLocale());
        }

        return StringUtils.trim(name);
    }

    @JmixProperty
    @DependsOnProperties("snapshotDate")
    public Date getChangeDate() {
        return this.snapshotDate;
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
