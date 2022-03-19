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
package com.haulmont.cuba.security.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import io.jmix.core.entity.annotation.EnableRestore;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

/**
 * Search folder settings.
 */

@JmixEntity
@Entity(name = "sec$SearchFolder")
@Table(name = "SEC_SEARCH_FOLDER")
@PrimaryKeyJoinColumn(name = "FOLDER_ID", referencedColumnName = "ID")
@DiscriminatorValue("S")
@EnableRestore
@NamePattern("%s|name")
public class SearchFolder extends AbstractSearchFolder {

    @Column(name = "USERNAME")
    protected String username;

    @Column(name = "PRESENTATION_ID")
    protected UUID presentationId;

    @Column(name = "IS_SET")
    protected Boolean isSet = false;

    @Column(name = "ENTITY_TYPE")
    protected String entityType;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getPresentationId() {
        return presentationId;
    }

    public void setPresentationId(UUID presentationId) {
        this.presentationId = presentationId;
    }

    @Override
    public String getCaption() {
        Messages messages = AppBeans.get(Messages.class);
        return messages.getMainMessage(name);
    }

    public Boolean getIsSet() {
        return isSet;
    }

    public void setIsSet(Boolean isSet) {
        this.isSet = isSet;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @InstanceName
    @DependsOnProperties("name")
    public String getInstanceName() {
        return name;
    }
}
