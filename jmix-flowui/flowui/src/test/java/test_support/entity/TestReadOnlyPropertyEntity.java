/*
 * Copyright 2022 Haulmont.
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

package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity(name = "test_ReadOnlyPropertyEntity")
@JmixEntity
@Table(name = "TEST_READONLY_PROPERTY_ENTITY")
public class TestReadOnlyPropertyEntity {

    private static final long serialVersionUID = -8628312032421692288L;

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "NAME")
    private String name;

    @JmixProperty
    private String roName;

    @JmixProperty
    private List<Foo> roList;

    public TestReadOnlyPropertyEntity() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    @JmixProperty
    public Foo getRoFoo() {
        return roList.isEmpty() ? null : roList.get(0);
    }

    public void initReadOnlyProperties() {
        roName = "roValue";
        roList = Collections.singletonList(new Foo());
    }
}
