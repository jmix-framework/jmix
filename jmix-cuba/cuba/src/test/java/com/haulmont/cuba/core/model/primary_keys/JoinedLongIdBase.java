/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.core.model.primary_keys;

import com.haulmont.cuba.core.entity.BaseLongIdEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

@Entity(name = "test$JoinedLongIdBase")
@JmixEntity
@Table(name = "TEST_JOINED_LONGID_BASE")
@Inheritance(strategy = InheritanceType.JOINED)
public class JoinedLongIdBase extends BaseLongIdEntity {

    @Column(name = "NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
