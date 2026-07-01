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

package test_support.entity.lazyloading.nullability;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "CHILD_ENTITY")
@Entity
public class ChildEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "CHILDNAME")
    private String childname;

    @JoinColumn(name = "PARENT_ENTITY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ParentEntity parentEntity;

    @JoinColumn(name = "ADDITIONAL_PARENT_ENTITY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ParentEntity additionalParentEntity;

    @JoinColumn(name = "ADDITIONAL_ENTITY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private AdditionalEntity additionalEntity;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getChildname() {
        return childname;
    }

    public void setChildname(String childname) {
        this.childname = childname;
    }

    public ParentEntity getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(ParentEntity parentEntity) {
        this.parentEntity = parentEntity;
    }

    public AdditionalEntity getAdditionalEntity() {
        return additionalEntity;
    }

    public void setAdditionalEntity(AdditionalEntity additionalEntity) {
        this.additionalEntity = additionalEntity;
    }

    public ParentEntity getAdditionalParentEntity() {
        return additionalParentEntity;
    }

    public void setAdditionalParentEntity(ParentEntity additionalParentEntity) {
        this.additionalParentEntity = additionalParentEntity;
    }
}