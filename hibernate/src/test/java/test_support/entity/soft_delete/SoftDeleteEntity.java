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

import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.hibernate.impl.SoftDeletionFilterDefinition;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Table(name = "TEST_HARDDELETE_ENTITY")
@JmixEntity
@Entity(name = "test_SoftDeleteEntity")
public class SoftDeleteEntity {
    private static final long serialVersionUID = 7016314126468585951L;

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "TITLE")
    private String title;

    @DeletedDate
    @Column(name = "TIME_OF_DELETION")
    protected Date timeOfDeletion;

    @JoinColumn(name = "PARENT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityWithSoftDeletedCollection parent;

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

    public EntityWithSoftDeletedCollection getParent() {
        return parent;
    }

    public void setParent(EntityWithSoftDeletedCollection parent) {
        this.parent = parent;
    }


    public Date getTimeOfDeletion() {
        return timeOfDeletion;
    }

    public void setTimeOfDeletion(Date timeOfDeletion) {
        this.timeOfDeletion = timeOfDeletion;
    }


}
