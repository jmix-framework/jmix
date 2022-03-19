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


import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "ref$DriverAllocation")
@JmixEntity
@Table(name = "REF_DRIVER_ALLOC")
public class DriverAllocation extends StandardEntity {

    private static final long serialVersionUID = 8101497971694305079L;

    @ManyToOne
    @JoinColumn(name = "DRIVER_ID")
    @OnDeleteInverse(DeletePolicy.DENY)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "CAR_ID")
    private Car car;

    @InstanceName
    public String getCaption() {
        return String.format("%s:(%s)", getDriver(), getCar());
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
