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

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.ui.entity.LogicalFilterCondition;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.UUID;

@JmixEntity
@Entity(name = "ui_FilterConfiguration")
@Table(name = "UI_FILTER_CONFIGURATION")
@SystemLevel
public class FilterConfiguration {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "COMPONENT_ID", nullable = false)
    protected String componentId;

    @Column(name = "CODE", nullable = false)
    protected String code;

    @Column(name = "USERNAME")
    protected String username;

    @Column(name = "ROOT_CONDITION")
    @Lob
    @Convert(converter = FilterConditionConverter.class)
    protected LogicalFilterCondition rootCondition;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LogicalFilterCondition getRootCondition() {
        return rootCondition;
    }

    public void setRootCondition(LogicalFilterCondition rootCondition) {
        this.rootCondition = rootCondition;
    }
}
