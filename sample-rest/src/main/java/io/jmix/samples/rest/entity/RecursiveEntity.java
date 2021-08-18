/*
 * Copyright 2021 Haulmont.
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

package io.jmix.samples.rest.entity;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity(name = "rest_RecursiveEntity")
@JmixEntity
@Table(name = "REST_RECURSIVE_ENTITY")
public class RecursiveEntity extends StandardEntity {

    @NotNull
    @Column
    private String name;

    @Valid
    @Composition
    @ManyToOne
    @JoinColumn(name = "recursive_entity_id")
    private RecursiveEntity recursiveEntity;

    @Valid
    @OneToMany(mappedBy = "recursiveEntity")
    @Composition
    private List<RecursiveEntity> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecursiveEntity getRecursiveEntity() {
        return recursiveEntity;
    }

    public void setRecursiveEntity(RecursiveEntity recursiveEntity) {
        this.recursiveEntity = recursiveEntity;
    }

    public List<RecursiveEntity> getChildren() {
        return children;
    }

    public void setChildren(List<RecursiveEntity> children) {
        this.children = children;
    }
}
