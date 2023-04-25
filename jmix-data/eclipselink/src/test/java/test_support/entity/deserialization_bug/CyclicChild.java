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

package test_support.entity.deserialization_bug;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.entity_extension.Address;

import jakarta.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "JMIXALL.DATA.TEST_CYCLIC_CHILD")
@Entity(name = "jmixall.data.test_CyclicChild")
public class CyclicChild {
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private CyclicParent parent;

    @JmixGeneratedValue
    @Column(name = "SOME_GEN_ID")
    private UUID someGenId;

    @Embedded
    private Address address;

    public CyclicParent getParent() {
        return parent;
    }

    public void setParent(CyclicParent parent) {
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSomeGenId() {
        return someGenId;
    }

    public void setSomeGenId(UUID someGenId) {
        this.someGenId = someGenId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}