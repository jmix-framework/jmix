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

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Past;
import java.util.Date;
import java.util.UUID;

@Entity(name = "ref$Seller")
@JmixEntity
@Table(name = "REF_SELLER")
public class Seller extends StandardEntity {

    private static final long serialVersionUID = 3238417347166814388L;

    @Column(name = "UUID")
    private UUID uuid;

    @InstanceName
    @Column(name = "NAME")
    protected String name;

    @Past(message = "Must be in the past")
    @Temporal(TemporalType.DATE)
    @Column(name = "CONTRACT_START_DATE")
    protected Date contractStartDate;

    @Future(message = "Must be in the future")
    @Temporal(TemporalType.DATE)
    @Column(name = "CONTRACT_END_DATE")
    protected Date contractEndDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(Date contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
