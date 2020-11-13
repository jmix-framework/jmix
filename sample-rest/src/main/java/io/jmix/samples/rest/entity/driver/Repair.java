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
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;
import java.sql.Date;
import java.util.Set;

@Entity(name = "ref$Repair")
@JmixEntity
@Table(name = "REF_REPAIR")
public class Repair extends StandardEntity {

    private static final long serialVersionUID = 1785737195382529798L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAR_ID")
    private Car car;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSURANCE_CASE_ID")
    private InsuranceCase insuranceCase;

    @InstanceName
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "REPAIR_DATE")
    private Date date;

    @OneToMany(mappedBy = "repair")
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private Set<CarToken> carTokens;

//    @Transient
//    @ModelProperty(related = "db1CustomerId")
//    private Db1Customer db1Customer;

    @SystemLevel
    @Column(name = "DB1_CUSTOMER_ID")
    private Long db1CustomerId;

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<CarToken> getCarTokens() {
        return carTokens;
    }

    public void setCarTokens(Set<CarToken> carTokens) {
        this.carTokens = carTokens;
    }

    public InsuranceCase getInsuranceCase() {
        return insuranceCase;
    }

    public void setInsuranceCase(InsuranceCase insuranceCase) {
        this.insuranceCase = insuranceCase;
    }

//    public Db1Customer getDb1Customer() {
//        return db1Customer;
//    }
//
//    public void setDb1Customer(Db1Customer db1Customer) {
//        this.db1Customer = db1Customer;
//    }

    public Long getDb1CustomerId() {
        return db1CustomerId;
    }

    public void setDb1CustomerId(Long db1CustomerId) {
        this.db1CustomerId = db1CustomerId;
    }
}
