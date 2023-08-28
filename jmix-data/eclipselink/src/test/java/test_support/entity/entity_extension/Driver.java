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
package test_support.entity.entity_extension;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@JmixEntity
@Entity
@Table(name = "EXTTEST_DRIVER")
public class Driver {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "NAME")
    @InstanceName
    protected String name;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "CALLSIGN_ID")
    protected DriverCallsign callsign;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLATFORM_ENTITY_ID", unique = true)
    protected SamplePlatformEntity platformEntity;

    @OneToMany(mappedBy = "driver")
    @Composition
    private Set<DriverAllocation> allocations;

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

    public DriverCallsign getCallsign() {
        return callsign;
    }

    public void setCallsign(DriverCallsign callsign) {
        this.callsign = callsign;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public SamplePlatformEntity getPlatformEntity() {
        return platformEntity;
    }

    public void setPlatformEntity(SamplePlatformEntity platformEntity) {
        this.platformEntity = platformEntity;
    }

    public Set<DriverAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(Set<DriverAllocation> allocations) {
        this.allocations = allocations;
    }
}
