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

package test_support.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.List;

@Table(name = "TEST_MANY_TO_MANY_SECOND_ENTITY")
@Entity(name = "test_ManyToManySecondEntity")
@JmixEntity
public class ManyToManySecondEntity extends BaseEntity {
    @Column(name = "NAME")
    protected String name;

    @JoinTable(name = "TEST_MANY_TO_MANY_FIRST_ENTITY_MANY_TO_MANY_SECOND_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "MANY_TO_MANY_SECOND_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "MANY_TO_MANY_FIRST_ENTITY_ID"))
    @ManyToMany(fetch = FetchType.LAZY)
    protected List<ManyToManyFirstEntity> manyToManyFirstEntities;

    public List<ManyToManyFirstEntity> getManyToManyFirstEntities() {
        return manyToManyFirstEntities;
    }

    public void setManyToManyFirstEntities(List<ManyToManyFirstEntity> manyToManyFirstEntities) {
        this.manyToManyFirstEntities = manyToManyFirstEntities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}