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
package com.haulmont.cuba.core.entity;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@SystemLevel
@JmixEntity(name = "sys$AbstractSearchFolder")
public abstract class AbstractSearchFolder extends Folder {

    private static final long serialVersionUID = -2234453892776090930L;

    @Column(name = "FILTER_COMPONENT")
    protected String filterComponentId;

    @Lob
    @Column(name = "FILTER_XML")
    protected String filterXml;

    @Column(name = "APPLY_DEFAULT")
    protected Boolean applyDefault = true;

    public void copyFrom(AbstractSearchFolder srcFolder) {
        setCreatedBy(srcFolder.getCreatedBy());
        setCreateTs(srcFolder.getCreateTs());
        setDeletedBy(srcFolder.getDeletedBy());
        setDeleteTs(srcFolder.getDeleteTs());
        setFilterComponentId(srcFolder.getFilterComponentId());
        setFilterXml(srcFolder.getFilterXml());
        setName(srcFolder.getCaption());
        setTabName(srcFolder.getTabName());
        setParent(srcFolder.getParent());
        setItemStyle(srcFolder.getItemStyle());
        setSortOrder(srcFolder.getSortOrder());
    }

    public String getFilterComponentId() {
        return filterComponentId;
    }

    public void setFilterComponentId(String filterComponentId) {
        this.filterComponentId = filterComponentId;
    }

    public String getFilterXml() {
        return filterXml;
    }

    public void setFilterXml(String filterXml) {
        this.filterXml = filterXml;
    }

    public Boolean getApplyDefault() {
        return applyDefault;
    }

    public void setApplyDefault(Boolean applyDefault) {
        this.applyDefault = applyDefault;
    }

    @JmixProperty
    public String getLocName() {
        if (StringUtils.isNotEmpty(name)) {
            Messages messages = AppBeans.get(Messages.class);
            return messages.getMainMessage(name);
        }
        return null;
    }
}
