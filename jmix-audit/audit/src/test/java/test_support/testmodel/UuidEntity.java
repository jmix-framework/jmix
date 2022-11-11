/*
 * Copyright 2019 Haulmont.
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

package test_support.testmodel;


import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "test_UuidEntity")
@JmixEntity
@Table(name = "TEST_UUID_ENTITY")
public class UuidEntity {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Transient
    @JmixProperty
    @DependsOnProperties("db1EntityId")
    private Db1Entity db1Entity;

    @Column(name = "DB1_ENTITY_ID")
    private Long db1EntityId;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDb1Entity(Db1Entity db1Entity) {
        this.db1Entity = db1Entity;
    }

    public Db1Entity getDb1Entity() {
        return db1Entity;
    }

    public Long getDb1EntityId() {
        return db1EntityId;
    }

    public void setDb1EntityId(Long db1EntityId) {
        this.db1EntityId = db1EntityId;
    }
}

