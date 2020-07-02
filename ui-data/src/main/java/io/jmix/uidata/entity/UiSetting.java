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

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.uidata.UserSettingServiceImpl;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * Record for user setting.
 *
 * @see UserSettingServiceImpl
 */
@Entity(name = "ui_Setting")
@Table(name = "UI_SETTING")
@SystemLevel
public class UiSetting extends BaseUuidEntity {

    private static final long serialVersionUID = -4324101071593066529L;

    @CreatedDate
    @Column(name = "CREATE_TS")
    private Date createTs;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "USER_LOGIN")
    private String userLogin;

    @Column(name = "NAME")
    private String name;

    @Lob
    @Column(name = "VALUE_")
    private String value;

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

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}