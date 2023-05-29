/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core.model;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;

@Entity(name = "test_ReadOnlyPropertyEntity")
@JmixEntity
public class TestReadOnlyPropertyEntity extends BaseUuidEntity {

    @Column(name = "NAME")
    private String name;

    @JmixProperty
    private String roName;

    @JmixProperty
    private List<Foo> roList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoName() {
        return roName;
    }

    public List<Foo> getRoList() {
        return roList;
    }

    public void initReadOnlyProperties() {
        roName = "roValue";
        roList = Collections.singletonList(new Foo());
    }
}
