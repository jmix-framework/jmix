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
package com.haulmont.cuba.security.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

/**
 * A filter component settings.
 */
@JmixEntity
@Entity(name = "sec$Filter")
@Table(name = "SEC_FILTER")
@NamePattern("%s|name")
@SystemLevel
public class FilterEntity extends StandardEntity {

    @Column(name = "COMPONENT")
    protected String componentId;

    @Column(name = "NAME")
    protected String name;

    @Column(name = "CODE", length = 200)
    protected String code;

    @Column(name = "XML")
    @Lob
    protected String xml;

    @Column(name = "USERNAME")
    protected String username;

    @Column(name = "GLOBAL_DEFAULT")
    protected Boolean globalDefault = false;

    @TenantId
    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

    @Transient
    protected Boolean isDefault = false;

    @Transient
    protected Boolean applyDefault = false;

    @Transient
    protected AbstractSearchFolder folder;

    @Transient
    protected Boolean isSet = false;

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Boolean getApplyDefault() {
        return applyDefault;
    }

    public void setApplyDefault(Boolean applyDefault) {
        this.applyDefault = applyDefault;
    }

    public AbstractSearchFolder getFolder() {
        return folder;
    }

    public void setFolder(AbstractSearchFolder folder) {
        this.folder = folder;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getIsSet() {
        return isSet;
    }

    public void setIsSet(Boolean isSet) {
        this.isSet = isSet;
    }

    public Boolean getGlobalDefault() {
        return globalDefault;
    }

    public void setGlobalDefault(Boolean globalDefault) {
        this.globalDefault = globalDefault;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
