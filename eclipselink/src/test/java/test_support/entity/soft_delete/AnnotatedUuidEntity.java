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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Table(name = "TEST_ANNOTATED_UUID_ENTITY")
@JmixEntity
@Entity(name = "test_AnnotatedUuidEntity")
public class AnnotatedUuidEntity {
    @Id
    @JmixGeneratedValue
    @Column(name = "ID")
    protected Long id;

    @Column(name = "SOME_NOT_PRIMARY_ID")
    @JmixGeneratedValue
    protected UUID someNotPrimaryId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSomeNotPrimaryId() {
        return someNotPrimaryId;
    }

    public void setSomeNotPrimaryId(UUID someNotPrimaryId) {
        this.someNotPrimaryId = someNotPrimaryId;
    }
}
