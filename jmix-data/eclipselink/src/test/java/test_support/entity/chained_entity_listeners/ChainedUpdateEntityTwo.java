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

package test_support.entity.chained_entity_listeners;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "TEST_CHAINEDUPDATEENTITYTWO", indexes = {
        @Index(name = "IDX_CHAINEDUPDATEENTITYTWO", columnList = "ENTITY_ONE_ID")
})
@Entity(name = "test_ChainedUpdateEntityTwo")
public class ChainedUpdateEntityTwo {

    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "AMOUNT")
    private Integer amount;

    @JoinColumn(name = "ENTITY_ONE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChainedUpdateEntityOne entityOne;

    public ChainedUpdateEntityOne getEntityOne() {
        return entityOne;
    }

    public void setEntityOne(ChainedUpdateEntityOne entityOne) {
        this.entityOne = entityOne;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}