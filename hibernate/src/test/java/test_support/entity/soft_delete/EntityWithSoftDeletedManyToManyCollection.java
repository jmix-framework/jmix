/*
 * Copyright 2020 Haulmont.
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

package test_support.entity.soft_delete;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Table(name = "TEST_SOFT_DELETION_MANY_COLLECTION")
@JmixEntity
@Entity(name = "test_EntityWithSoftDeletedManyToManyCollection")
public class EntityWithSoftDeletedManyToManyCollection {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    protected UUID id;

    @ManyToMany
    @JoinTable(name = "TEST_SOFT_DELETION_MANY_COLLECTION_TO_SOFT",
            joinColumns = @JoinColumn(name = "PARENT_ID"),
            inverseJoinColumns = @JoinColumn(name = "ENTITY_ID"))
    protected Set<SoftDeleteEntity> collection;

    @Column(name = "TITLE")
    private String title;

    public Set<SoftDeleteEntity> getCollection() {
        return collection;
    }

    public void setCollection(Set<SoftDeleteEntity> collection) {
        this.collection = collection;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
