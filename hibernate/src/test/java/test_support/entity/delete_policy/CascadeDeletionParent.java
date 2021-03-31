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

package test_support.entity.delete_policy;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@JmixEntity
@Table(name = "CASCADE_DELETION_PARENT")
@Entity(name = "CascadeDeletionParent")
public class CascadeDeletionParent extends BaseEntity {

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToMany(mappedBy = "parent")
    @OnDelete(DeletePolicy.CASCADE)
    protected Set<CascadeDeletionChild> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<CascadeDeletionChild> getChildren() {
        return children;
    }

    public void setChildren(Set<CascadeDeletionChild> children) {
        this.children = children;
    }
}