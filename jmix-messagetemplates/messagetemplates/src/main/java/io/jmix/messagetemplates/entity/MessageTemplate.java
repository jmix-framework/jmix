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
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Entity(name = "msgtmp_MessageTemplate")
@JmixEntity
@Table(name = "MSGTMP_MESSAGE_TEMPLATE", indexes = {
        @Index(name = "IDX_MSGTMP_MESSAGE_TEMPLATE_UNQ_CODE", columnList = "CODE", unique = true),
        @Index(name = "IDX_MSGTMP_MESSAGE_TEMPLATE_GROUP", columnList = "GROUP_ID")
})
public class MessageTemplate {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @NotNull
    @Column(name = "NAME", nullable = false)
    protected String name;

    @NotNull
    @Column(name = "CODE", nullable = false, unique = true)
    protected String code;

    @Lob
    @Column(name = "HTML")
    protected String html;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    protected MessageTemplateGroup group;

    @Composition
    @OneToMany(mappedBy = "template")
    private List<MessageTemplateParameter> parameters;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public MessageTemplateGroup getGroup() {
        return group;
    }

    public void setGroup(MessageTemplateGroup group) {
        this.group = group;
    }

    public List<MessageTemplateParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<MessageTemplateParameter> parameters) {
        this.parameters = parameters;
    }

    @InstanceName
    @DependsOnProperties({"name", "code"})
    public String getInstanceName() {
        return "%s (%s)".formatted(name, code);
    }
}
