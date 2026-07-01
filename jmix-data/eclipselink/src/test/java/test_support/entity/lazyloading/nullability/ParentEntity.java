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
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.eclipselink.lazyloading.NotInstantiatedList;
import io.jmix.eclipselink.lazyloading.NotInstantiatedSet;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@JmixEntity
@Table(name = "PARENT_ENTITY")
@Entity
public class ParentEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Composition
    @OneToMany(mappedBy = "parentEntity")
    // field initializer that doesn't provoke eager loading of collection without correct ref fields lazy loading initialization
    private List<ChildEntity> children = new NotInstantiatedList<>();

    @NotNull
    @JoinTable(name = "PARENT_ADDITIONAL_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "PARENT_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "ADDITIONAL_ENTITY_ID"),
            foreignKey = @ForeignKey(name = "FK_PARENT_ADDITIONAL_ENTITY_LINK", value = ConstraintMode.NO_CONSTRAINT))
    @ManyToMany
    // field initializer that doesn't provoke eager loading of collection without correct ref fields lazy loading initialization
    private Set<AdditionalEntity> relatedAdditionalEntities = new NotInstantiatedSet<>();

    public Set<AdditionalEntity> getRelatedAdditionalEntities() {
        return relatedAdditionalEntities;
    }

    public void setRelatedAdditionalEntities(Set<AdditionalEntity> relatedAdditionalEntities) {
        this.relatedAdditionalEntities = relatedAdditionalEntities;
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

    public List<ChildEntity> getChildren() {
        return children;
    }

    public void setChildren(List<ChildEntity> children) {
        this.children = children;
    }
}