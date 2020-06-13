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

package io.jmix.uidata.entity;

import io.jmix.core.entity.Creatable;
import io.jmix.core.entity.Updatable;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.ui.presentation.model.TablePresentation;

import javax.persistence.*;
import java.util.Date;

/**
 * A screen presentation settings.
 */
@Entity(name = "ui_TablePresentation")
@Table(name = "UI_TABLE_PRESENTATION")
@SystemLevel
public class UiTablePresentation extends BaseUuidEntity implements Creatable, Updatable,
        TablePresentation {

    @Column(name = "CREATE_TS")
    protected Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @Column(name = "COMPONENT")
    private String componentId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SETTINGS", length = 4000)
    private String settings;

    @Column(name = "USER_LOGIN")
    private String userLogin;

    @Column(name = "IS_AUTO_SAVE")
    private Boolean autoSave;

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "UPDATED_BY", length = 50)
    protected String updatedBy;

    @Transient
    private Boolean isDefault;

    @Override
    public Date getCreateTs() {
        return createTs;
    }

    @Override
    public void setCreateTs(Date date) {
        this.createTs = date;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Date getUpdateTs() {
        return updateTs;
    }

    @Override
    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSettings() {
        return settings;
    }

    @Override
    public void setSettings(String settings) {
        this.settings = settings;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public Boolean getDefault() {
        return isDefault;
    }

    @Override
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public Boolean getAutoSave() {
        return autoSave;
    }

    @Override
    public void setAutoSave(Boolean autoSave) {
        this.autoSave = autoSave;
    }

    public String getComponentId() {
        return componentId;
    }

    @Override
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
}
