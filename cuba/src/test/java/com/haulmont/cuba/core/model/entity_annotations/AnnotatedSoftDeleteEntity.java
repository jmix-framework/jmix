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

package com.haulmont.cuba.core.model.entity_annotations;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "TEST_ANNOTATED_SOFT_DELETE_ENTITY")
@Entity(name = "test$AnnotatedSoftDeleteEntity")
@JmixEntity
public class AnnotatedSoftDeleteEntity extends BaseUuidEntity {
    private static final long serialVersionUID = -428994652664330271L;

    @Column(name = "DUMMY_COLUMN", length = 50)
    protected String dummy;

    @DeletedBy
    @Column(name = "WHO_DELETED", length = 50)
    protected String whoDeleted;

    @DeletedDate
    @Column(name = "WHEN_DELETED", length = 50)
    protected Date whenDeleted;


    public String getDummy() {
        return dummy;
    }

    public void setDummy(String dummy) {
        this.dummy = dummy;
    }

    public String getWhoDeleted() {
        return whoDeleted;
    }

    public void setWhoDeleted(String whoDeleted) {
        this.whoDeleted = whoDeleted;
    }

    public Date getWhenDeleted() {
        return whenDeleted;
    }

    public void setWhenDeleted(Date whenDeleted) {
        this.whenDeleted = whenDeleted;
    }
}
