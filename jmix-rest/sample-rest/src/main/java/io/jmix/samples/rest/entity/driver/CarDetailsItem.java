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


import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;

@Entity(name = "ref$CarDetailsItem")
@JmixEntity
@Table(name = "REF_CAR_DETAILS_ITEM")
public class CarDetailsItem extends StandardEntity {

    private static final long serialVersionUID = 8201548746103223718L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAR_DETAILS_ID")
    protected CarDetails carDetails;

    @Column(name = "INFO")
    protected String info;

    public CarDetails getCarDetails() {
        return carDetails;
    }

    public void setCarDetails(CarDetails carDetails) {
        this.carDetails = carDetails;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
