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

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "ADDITIONAL_ENTITY")
@Entity
public class AdditionalEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @JoinTable(name = "PARENT_ADDITIONAL_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "ADDITIONAL_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "PARENT_ENTITY_ID"),
            foreignKey = @ForeignKey(name = "FK_PARENT_ADDITIONAL_ENTITY_LINK", value = ConstraintMode.NO_CONSTRAINT))
    @ManyToMany
    private List<ParentEntity> parentEntities;


    public List<ParentEntity> getParentEntities() {
        return parentEntities;
    }

    public void setParentEntities(List<ParentEntity> parentEntities) {
        this.parentEntities = parentEntities;
    }

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
}
