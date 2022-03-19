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

package test_support.entity.common;

import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;

import javax.persistence.*;

@JmixEntity
@Entity(name = "test_WildBaseEntity")
@Table(name = "TEST_WILD_BASE_ENTITY")
public class WildBaseEntity extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID", nullable = false)
    private WildParentEntity parent;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WildParentEntity getParent() {
        return parent;
    }

    public void setParent(WildParentEntity entity) {
        this.parent = entity;
    }
}
