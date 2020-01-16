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

package io.jmix.core.security;

import io.jmix.core.entity.BaseUuidEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotations.MetaClass;
import io.jmix.core.metamodel.annotations.MetaProperty;

import java.util.Date;

/**
 * Non-persistent entity to show user sessions list in UI.
 *
 */
@MetaClass(name = "sec$UserSessionEntity")
@SystemLevel
public class UserSessionEntity extends BaseUuidEntity {

    private static final long serialVersionUID = 7730031482721158275L;

    @MetaProperty
    private String login;
    @MetaProperty
    private String userName;
    @MetaProperty
    private String address;
    @MetaProperty
    private String clientInfo;
    @MetaProperty
    private Date since;
    @MetaProperty
    private Date lastUsedTs;
    @MetaProperty
    private Boolean system;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getLastUsedTs() {
        return lastUsedTs;
    }

    public void setLastUsedTs(Date lastUsedTs) {
        this.lastUsedTs = lastUsedTs;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return "id=" + getId() + ", login=" + login + ", user=" + userName + ", since=" + since + ", last=" + lastUsedTs;
    }
}