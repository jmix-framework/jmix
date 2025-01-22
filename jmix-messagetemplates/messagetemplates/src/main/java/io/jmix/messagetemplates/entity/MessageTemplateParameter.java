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

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity(name = "msgtmp_MessageTemplateParameter")
@JmixEntity
@SystemLevel
@Table(name = "MSGTMP_MESSAGE_TEMPLATE_PARAMETER", indexes = {
        @Index(name = "IDX_MSGTMP_MESSAGE_TEMPLATE_PARAMETER_TEMPLATE", columnList = "TEMPLATE_ID")
})
public class MessageTemplateParameter {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    protected MessageTemplate template;

    @NotNull
    @Column(name = "TYPE_", nullable = false)
    protected Integer type;

    @NotNull
    @InstanceName
    @Column(name = "NAME", nullable = false)
    protected String name;

    @NotNull
    @Column(name = "ALIAS", nullable = false)
    protected String alias;

    @Column(name = "REQUIRED")
    protected Boolean required;

    @Column(name = "HIDDEN")
    protected Boolean hidden;

    @Column(name = "ENTITY_META_CLASS")
    protected String entityMetaClass;

    @Column(name = "ENUMERATION_CLASS")
    protected String enumerationClass;

    @Column(name = "DEFAULT_VALUE")
    protected String defaultValue;

    @Column(name = "DEFAULT_DATE_IS_CURRENT")
    protected Boolean defaultDateIsCurrent;

    @Column(name = "LOCALIZATION")
    protected String localization;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MessageTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
    }

    public ParameterType getType() {
        return type == null ? null : ParameterType.fromId(type);
    }

    public void setType(ParameterType type) {
        this.type = type == null ? null : type.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getEntityMetaClass() {
        return entityMetaClass;
    }

    public void setEntityMetaClass(String entityMetaClass) {
        this.entityMetaClass = entityMetaClass;
    }

    public String getEnumerationClass() {
        return enumerationClass;
    }

    public void setEnumerationClass(String enumerationClass) {
        this.enumerationClass = enumerationClass;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getDefaultDateIsCurrent() {
        return defaultDateIsCurrent;
    }

    public void setDefaultDateIsCurrent(Boolean defaultDateIsCurrent) {
        this.defaultDateIsCurrent = defaultDateIsCurrent;
    }

    public String getLocalization() {
        return localization;
    }

    public void setLocalization(String localization) {
        this.localization = localization;
    }
}
