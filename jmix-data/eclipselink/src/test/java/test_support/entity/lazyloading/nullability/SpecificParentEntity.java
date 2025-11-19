/*
 * Copyright 2025 Haulmont.
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


import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.eclipselink.lazyloading.NotInstantiatedList;
import jakarta.persistence.*;

import java.util.List;


@JmixEntity
@Entity
public class SpecificParentEntity extends ParentEntity {

    @Composition
    @OneToMany(mappedBy = "additionalParentEntity")
    private List<ChildEntity> additionalChildren = new NotInstantiatedList<>();

    public List<ChildEntity> getAdditionalChildren() {
        return additionalChildren;
    }

    public void setAdditionalChildren(List<ChildEntity> additionalChildren) {
        this.additionalChildren = additionalChildren;
    }
}
