/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowuirestds.genericfilter;

import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;

import java.util.UUID;

@JmixEntity(name = "flowui_FilterConfiguration")
@SystemLevel
public class FilterConfiguration {

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    protected String componentId;

    protected String configurationId;

    @InstanceName
    protected String name;

    protected String username;

    protected Boolean defaultForAll = false;

    protected LogicalFilterCondition rootCondition;

    @TenantId
    protected String sysTenantId;

    protected Boolean defaultForMe = false;

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

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
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

    public Boolean getDefaultForAll() {
        return defaultForAll;
    }

    public void setDefaultForAll(Boolean defaultForAll) {
        this.defaultForAll = defaultForAll;
    }

    public LogicalFilterCondition getRootCondition() {
        return rootCondition;
    }

    public void setRootCondition(LogicalFilterCondition rootCondition) {
        this.rootCondition = rootCondition;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }

    public Boolean getDefaultForMe() {
        return defaultForMe;
    }

    public void setDefaultForMe(Boolean defaultForMe) {
        this.defaultForMe = defaultForMe;
    }
}
