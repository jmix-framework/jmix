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

package test_support.entity.number_id_generation;


import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;

@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("R")
@Table(name = "TEST_NUMBER_ID_JOINED_ROOT")
@JmixEntity
@Entity(name = "test$NumberIdJoinedRoot")
public class NumberIdJoinedRoot {
    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected Long id;

    @Column(name = "NAME")
    protected String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
