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

package test_support.entity.multidb;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.UUID;


@JmixEntity
@Entity(name = "test_MainReport")
public class MainReport {
    @Id
    @JmixGeneratedValue
    protected UUID id;

    @InstanceName
    @Column(name = "name")
    protected String name;

    @Transient
    @JmixProperty
    @DependsOnProperties("db1OrderId")
    private Db1Order db1Order;

    @Column(name = "DB1_ORDER_ID")
    private Long db1OrderId;

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

    public Db1Order getDb1Order() {
        return db1Order;
    }

    public void setDb1Order(Db1Order db1Order) {
        this.db1Order = db1Order;
    }

    public Long getDb1OrderId() {
        return db1OrderId;
    }

    public void setDb1OrderId(Long db1OrderId) {
        this.db1OrderId = db1OrderId;
    }
}
