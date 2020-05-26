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
package test_support.entity;

import io.jmix.core.entity.annotation.TrackEditScreenHistory;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.data.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * User role.
 */
@Entity(name = "dynattr$Role")
@Table(name = "DYNATTR_ROLE")
@TrackEditScreenHistory
public class Role extends StandardEntity {

    private static final long serialVersionUID = -4889116218059626402L;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Column(name = "LOC_NAME")
    private String locName;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "IS_DEFAULT_ROLE")
    private Boolean defaultRole;

    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

    @Transient
    private boolean isPredefined = false;

    @InstanceName(relatedProperties = {"locName", "name"})
    public String getCaption() {
        return String.format("%s [%s]", getLocName(), getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(Boolean defaultRole) {
        this.defaultRole = defaultRole;
    }

    public boolean isPredefined() {
        return isPredefined;
    }

    public void setPredefined(boolean predefined) {
        isPredefined = predefined;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
