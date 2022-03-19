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
import java.util.List;


@JmixEntity
@Entity(name = "test_WildParentEntity")
@Table(name = "TEST_WILD_PARENT_ENTITY")
public class WildParentEntity extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    //@Transient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    private List<? extends WildBaseEntity> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<? extends WildBaseEntity> getChildren() {
        return children;
    }

    public void setChildren(List<? extends WildBaseEntity> children) {
        this.children = children;
    }
}
