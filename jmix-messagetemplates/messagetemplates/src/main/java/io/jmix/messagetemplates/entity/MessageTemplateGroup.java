/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplates.entity;

import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity(name = "msgtmp_MessageTemplateGroup")
@JmixEntity
@Table(name = "MSGTMP_MESSAGE_TEMPLATE_GROUP", indexes = {
        @Index(name = "IDX_MSGTMP_MESSAGE_TEMPLATE_GROUP_UNQ_NAME", columnList = "NAME, SYS_TENANT_ID", unique = true)
})
public class MessageTemplateGroup {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @NotNull
    @Column(name = "NAME", nullable = false)
    @InstanceName
    protected String name;

    @SystemLevel
    @Column(name = "SYS_TENANT_ID")
    @TenantId
    protected String sysTenantId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
