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

@Entity(name = "ref$CarToken")
@JmixEntity
@Table(name = "REF_CAR_TOKEN")
public class CarToken extends StandardEntity {

    private static final long serialVersionUID = -3020931348846190506L;

    @Column(name = "TOKEN")
    protected String token;

    @JoinColumn(name = "REPAIR_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    protected Repair repair;

    @JoinColumn(name = "GARAGE_TOKEN_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    protected CarGarageToken garageToken;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Repair getRepair() {
        return repair;
    }

    public void setRepair(Repair repair) {
        this.repair = repair;
    }

    public CarGarageToken getGarageToken() {
        return garageToken;
    }

    public void setGarageToken(CarGarageToken garageToken) {
        this.garageToken = garageToken;
    }
}
