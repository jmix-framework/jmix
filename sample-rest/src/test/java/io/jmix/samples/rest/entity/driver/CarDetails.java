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

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;
import java.util.List;

@Entity(name = "ref$CarDetails")
@JmixEntity
@Table(name = "REF_CAR_DETAILS")
public class CarDetails extends StandardEntity {

    private static final long serialVersionUID = 8201548746103223718L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAR_ID")
    protected Car car;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "carDetails")
    @Composition
    protected List<CarDetailsItem> items;

    @InstanceName
    @Column(name = "DETAILS")
    protected String details;

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<CarDetailsItem> getItems() {
        return items;
    }

    public void setItems(List<CarDetailsItem> items) {
        this.items = items;
    }
}

