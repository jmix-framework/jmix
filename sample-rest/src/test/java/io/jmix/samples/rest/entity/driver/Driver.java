/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.rest.entity.driver;

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;
import java.util.Set;

@Entity(name = "ref$Driver")
@JmixEntity
@Table(name = "REF_DRIVER")
public class Driver extends StandardEntity {

    private static final long serialVersionUID = -3978805138573255022L;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private Address address;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "DRIVER_GROUP_ID")
//    private DriverGroup driverGroup;

    @Column(name = "STATUS")
    private Integer status;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "PLATFORM_ENTITY_ID")
//    protected SamplePlatformEntity platformEntity;

    @OneToMany(mappedBy = "driver")
    @Composition
    private Set<DriverAllocation> allocations;

//    @ModelProperty
//    public SamplePlatformEntity getPlatformEntityName() {
//        if (!isLoaded(this, "platformEntity")
//                || !isLoaded(this, "status")) {
//            return null;
//        }
//
//        return !Objects.equals(status, DriverStatus.ACTIVE.getId()) ? platformEntity : null;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public DriverCallsign getCallsign() {
//        return callsign;
//    }
//
//    public void setCallsign(DriverCallsign callsign) {
//        this.callsign = callsign;
//    }
//
//    public DriverGroup getDriverGroup() {
//        return driverGroup;
//    }
//
//    public void setDriverGroup(DriverGroup driverGroup) {
//        this.driverGroup = driverGroup;
//    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public DriverStatus getStatus() {
        return status == null ? null : DriverStatus.fromId(status);
    }

    public void setStatus(DriverStatus status) {
        this.status = status == null ? null : status.getId();
    }

//    public SamplePlatformEntity getPlatformEntity() {
//        return platformEntity;
//    }
//
//    public void setPlatformEntity(SamplePlatformEntity platformEntity) {
//        this.platformEntity = platformEntity;
//    }

    public Set<DriverAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(Set<DriverAllocation> allocations) {
        this.allocations = allocations;
    }
}
