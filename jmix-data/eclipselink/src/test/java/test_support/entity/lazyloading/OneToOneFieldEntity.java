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

package test_support.entity.lazyloading;

import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;

import javax.persistence.*;

@Table(name = "TEST_ONE_TO_ONE_FIELD_ENTITY")
@JmixEntity
@Entity(name = "test_OneToOneFieldEntity")
public class OneToOneFieldEntity extends BaseEntity {
    @Column(name = "NAME")
    protected String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEST_ONE_TO_ONE_NO_FIELD_ENTITY_ID")
    protected OneToOneNoFieldEntity oneToOneNoFieldEntity;

    public OneToOneNoFieldEntity getOneToOneNoFieldEntity() {
        return oneToOneNoFieldEntity;
    }

    public void setOneToOneNoFieldEntity(OneToOneNoFieldEntity oneToOneNoFieldEntity) {
        this.oneToOneNoFieldEntity = oneToOneNoFieldEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}