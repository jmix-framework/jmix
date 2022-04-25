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
package test_support.entity.sec;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.TestBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * User role.
 */
@Entity(name = "sec$Role")
@JmixEntity
@Table(name = "SEC_ROLE")
public class Role extends TestBaseEntity {

    private static final long serialVersionUID = -4889116218059626402L;

    @Column(name = "NAME")
    private String name;

    @Column(name = "LOC_NAME")
    private String locName;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "ROLE_TYPE")
    private Integer type;

    @Column(name = "IS_DEFAULT_ROLE")
    private Boolean defaultRole;

    @InstanceName
    @DependsOnProperties({"locName","name"})
    public String getCaption(){
        return String.format("%s [%s]",getLocName(),getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleType getType() {
        return RoleType.fromId(type);
    }

    public void setType(RoleType type) {
        this.type = type == null ? null : type.getId();
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
}
