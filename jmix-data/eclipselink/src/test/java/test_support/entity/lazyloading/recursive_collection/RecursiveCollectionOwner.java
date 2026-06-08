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

package test_support.entity.lazyloading.recursive_collection;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.eclipselink.lazyloading.NotInstantiatedList;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import test_support.entity.BaseEntity;

import java.util.List;

@JmixEntity
@Entity(name = "test_RcOwner")
@Table(name = "TEST_LL_RC_OWNER")
public class RecursiveCollectionOwner extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "owner")
    private List<RecursiveCollectionChild> children = new NotInstantiatedList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RecursiveCollectionChild> getChildren() {
        return children;
    }

    public void setChildren(List<RecursiveCollectionChild> children) {
        this.children = children;
    }
}
