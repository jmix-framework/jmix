/*
 * Copyright 2016 Haulmont.
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

package test_support.testmodel;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.Store;

import jakarta.persistence.*;

@JmixEntity
@Entity(name = "test_Db1Entity")
@Table(name = "TEST_DB1_ENTITY")
@Store(name = "db1")
public class Db1Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO/*, generator = "ref$Db1Customer"*/)
    @Column(name = "ID")
    protected Long id;

    @Column(name = "NAME")
    @InstanceName
    private String name;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
