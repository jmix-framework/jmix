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

import io.jmix.core.UuidProvider;
import io.jmix.core.Entity;
import io.jmix.core.entity.HasUuid;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotations.ModelObject;
import io.jmix.core.metamodel.annotations.ModelProperty;

import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

/**
 * Non-persistent entity to show user sessions list in UI.
 */
@ModelObject(name = "sec$UserSessionEntity")
@SystemLevel
public class UserSessionEntity implements Entity<UUID>, HasUuid {

    private static final long serialVersionUID = 7730031482721158275L;

    @Id
    private UUID id;
    @ModelProperty
    private String login;
    @ModelProperty
    private String userName;
    @ModelProperty
    private String address;
    @ModelProperty
    private String clientInfo;
    @ModelProperty
    private Date since;
    @ModelProperty
    private Date lastUsedTs;
    @ModelProperty
    private Boolean system;

    public UserSessionEntity() {
        id = UuidProvider.createUuid();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getUuid() {
        return id;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.id = uuid;
    }

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
