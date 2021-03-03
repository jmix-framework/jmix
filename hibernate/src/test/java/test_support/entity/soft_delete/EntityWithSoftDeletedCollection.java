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
import io.jmix.hibernate.impl.SoftDeletionFilterDefinition;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Table(name = "TEST_SOFT_DELETION_COLLECTION")
@JmixEntity
@Entity(name = "test_EntityWithSoftDeletedCollection")
public class EntityWithSoftDeletedCollection {
    private static final long serialVersionUID = 7016314126468585951L;

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    protected UUID id;

    @OneToMany(mappedBy = "parent")
    private List<SoftDeleteEntity> collection;

    @Column(name = "TITLE")
    private String title;

    public List<SoftDeleteEntity> getCollection() {
        return collection;
    }

    public void setCollection(List<SoftDeleteEntity> collection) {
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
