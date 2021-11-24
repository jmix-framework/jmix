/*
 * Copyright 2021 Haulmont.
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

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;
import test_support.entity.entity_extension.Address;

import javax.persistence.*;

@JmixEntity
@Table(name = "TEST_SELFREF_EMPLOYEE")
@Entity(name = "test_SelfReferencedEmployee")
public class SelfReferencedEmployee extends BaseEntity {
    private static final long serialVersionUID = 7096367933297948363L;

    @Column(name = "NAME")
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPERVISOR_ID")
    private SelfReferencedEmployee supervisor;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)

    private Address homeAddress;


    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public SelfReferencedEmployee getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(SelfReferencedEmployee supervisor) {
        this.supervisor = supervisor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
