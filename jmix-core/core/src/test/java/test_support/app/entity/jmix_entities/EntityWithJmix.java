/*
 * Copyright 2026 Haulmont.
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

package test_support.app.entity.jmix_entities;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.*;
import java.util.UUID;

@JmixEntity
@Entity(name = "test_entityJmix")
@Table(name = "TEST_ENTITY_JMIX")
public class EntityWithJmix {
    @Id
    @Column(name = "UUID")
    @JmixGeneratedValue
    private UUID uuid;

    @Column(name = "NAME")
    protected String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPPED_WITH_JMIX_ID")
    protected MappedWithJmix mappedWithJmix;

    @Embedded
    protected EmbeddableWithJmix embeddableWithJmix;

    private String calculatedId;

    private String fieldWithoutGetter;

    @Transient
    private int transientField;


    @Transient
    @JmixProperty
    private Integer consideredTransientField;


    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MappedWithJmix getMappedWithJmix() {
        return mappedWithJmix;
    }

    public void setMappedWithJmix(MappedWithJmix mappedWithJmix) {
        this.mappedWithJmix = mappedWithJmix;
    }

    public EmbeddableWithJmix getEmbeddableWithJmix() {
        return embeddableWithJmix;
    }

    public void setEmbeddableWithJmix(EmbeddableWithJmix embeddableWithJmix) {
        this.embeddableWithJmix = embeddableWithJmix;
    }

    public String getCalculatedId() {
        return calculatedId;
    }

    public Integer getConsideredTransientField() {
        return consideredTransientField;
    }

    public void setConsideredTransientField(Integer consideredTransientField) {
        this.consideredTransientField = consideredTransientField;
    }
}
