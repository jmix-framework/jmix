/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core.model.self_reference;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.List;

@Entity(name = "test_SelfReferencedEntity")
@JmixEntity
@Table(name = "TEST_SELF_REFERENCED_ENTITY")
public class SelfReferencedEntity extends BaseDictEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_CODE")
    private SelfReferencedEntity parent;

    @OneToMany(mappedBy = "parent")
    private List<SelfReferencedEntity> children;

    public List<SelfReferencedEntity> getChildren() {
        return children;
    }

    public void setChildren(List<SelfReferencedEntity> children) {
        this.children = children;
    }

    public SelfReferencedEntity getParent() {
        return parent;
    }

    public void setParent(SelfReferencedEntity parent) {
        this.parent = parent;
    }
}
