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

package io.jmix.audit.snapshot.model;

import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JmixEntity(name = "audit_EntitySnapshotModel")
public class EntitySnapshotModel implements Serializable {
    private static final long serialVersionUID = -8135122262155535927L;

    @JmixGeneratedValue
    @Id
    private UUID id;

    @JmixProperty
    private Map<String, String> customProperties = new HashMap<>();

    @JmixProperty
    private String createdBy;

    @JmixProperty
    private Date createdDate;

    @TenantId
    @JmixProperty
    private String sysTenantId;

    @JmixProperty
    private String fetchPlanXml;

    @JmixProperty
    private String snapshotXml;

    @JmixProperty
    private String entityMetaClass;

    @JmixProperty(mandatory = true)
    private Date snapshotDate;

    @JmixProperty(mandatory = true)
    private String authorUsername;

    @JmixProperty
    private UUID entityId;

    @JmixProperty
    private String stringEntityId;

    @JmixProperty
    private Integer intEntityId;

    @JmixProperty
    private Long longEntityId;


    private transient DatatypeRegistry datatypeRegistry;
    private transient CurrentAuthentication currentAuthentication;

    @PostConstruct
    public void init(DatatypeRegistry datatypeRegistry, CurrentAuthentication currentAuthentication) {
        this.datatypeRegistry = datatypeRegistry;
        this.currentAuthentication = currentAuthentication;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
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

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getStringEntityId() {
        return stringEntityId;
    }

    public void setStringEntityId(String stringEntityId) {
        this.stringEntityId = stringEntityId;
    }

    public Integer getIntEntityId() {
        return intEntityId;
    }

    public void setIntEntityId(Integer intEntityId) {
        this.intEntityId = intEntityId;
    }

    public Long getLongEntityId() {
        return longEntityId;
    }

    public void setLongEntityId(Long longEntityId) {
        this.longEntityId = longEntityId;
    }

    @JmixProperty
    @DependsOnProperties({"snapshotDate","authorUsername"})
    public String getLabel() {
        String name = "";
        if (authorUsername != null && StringUtils.isNotEmpty(authorUsername)) {
            name += this.authorUsername + " ";
        }

        Datatype datatype = datatypeRegistry.get(Date.class);

        if (currentAuthentication != null && currentAuthentication.isSet()) {
            name += datatype.format(snapshotDate, currentAuthentication.getLocale());
        }

        return StringUtils.trim(name);
    }

    @JmixProperty
    @DependsOnProperties({"snapshotDate"})
    public Date getChangeDate() {
        return this.snapshotDate;
    }

    public Date getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public void setObjectEntityId(Object referenceId) {
        if (referenceId instanceof UUID) {
            setEntityId((UUID) referenceId);
            setLongEntityId(null);
            setIntEntityId(null);
            setStringEntityId(null);
        } else if (referenceId instanceof Long) {
            setEntityId(null);
            setLongEntityId((Long) referenceId);
            setIntEntityId(null);
            setStringEntityId(null);
        } else if (referenceId instanceof Integer) {
            setEntityId(null);
            setLongEntityId(null);
            setIntEntityId((Integer) referenceId);
            setStringEntityId(null);
        } else if (referenceId instanceof String) {
            setEntityId(null);
            setLongEntityId(null);
            setIntEntityId(null);
            setStringEntityId((String) referenceId);
        } else if (referenceId == null) {
            setEntityId(null);
            setLongEntityId(null);
            setIntEntityId(null);
            setStringEntityId(null);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported primary key type: %s", referenceId.getClass().getSimpleName()));
        }
    }
}
