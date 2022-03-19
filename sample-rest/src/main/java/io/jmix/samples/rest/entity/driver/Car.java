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
import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Entity(name = "ref_Car")
@JmixEntity
@Table(name = "REF_CAR")
@Listeners("jmix_CarDetachListener")
public class Car extends StandardEntity {
    private static final long serialVersionUID = -7377186515184761381L;

    @InstanceName
    @Column(name = "VIN")
    private String vin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLOUR_ID")
    private Colour colour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODEL_ID")
//    @Lookup(type = LookupType.SCREEN)
    private Model model;

    @OneToMany(mappedBy = "car")
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private Set<DriverAllocation> driverAllocations;

    @OneToMany(mappedBy = "car")
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private Set<InsuranceCase> insuranceCases;

    @OneToMany(mappedBy = "car")
    @OrderBy("createTs")
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<Repair> repairs;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "car")
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    protected CarDetails details;


    @OneToOne(fetch = FetchType.LAZY)
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @JoinColumn(name = "CAR_DOCUMENTATION_ID")
    protected CarDocumentation carDocumentation;

    // for tests only
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOKEN_ID")
    @OnDelete(DeletePolicy.CASCADE)
    protected CarToken token;

    // Test meta property enhancing in persistent entity

    @JmixProperty
    @Transient
    protected Integer repairCost;

    @Transient
    protected Integer repairPrice;

    @Transient
    protected Integer repairCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SELLER_ID")
    protected Seller seller;

    @Valid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCY_CODE")
//    @Lookup(type = LookupType.DROPDOWN, actions = {"lookup", "open"})
    protected Currency currency;

    // This attribute is set by CarDetachListener
    @Transient
    @JmixProperty
    protected String currencyCode;

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Colour getColour() {
        return colour;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Set<DriverAllocation> getDriverAllocations() {
        return driverAllocations;
    }

    public void setDriverAllocations(Set<DriverAllocation> driverAllocations) {
        this.driverAllocations = driverAllocations;
    }

    public List<Repair> getRepairs() {
        return repairs;
    }

    public void setRepairs(List<Repair> repairs) {
        this.repairs = repairs;
    }

    public CarDetails getDetails() {
        return details;
    }

    public void setDetails(CarDetails details) {
        this.details = details;
    }

    public CarToken getToken() {
        return token;
    }

    public void setToken(CarToken token) {
        this.token = token;
    }

    // Test meta property enhancing in persistent entity

    public Integer getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(Integer repairCost) {
        this.repairCost = repairCost;
    }

    public Integer getRepairPrice() {
        return repairPrice;
    }

    public void setRepairPrice(Integer repairPrice) {
        this.repairPrice = repairPrice;
    }

    @JmixProperty
    public Integer getRepairCount() {
        return repairCount;
    }

    @JmixProperty
    public void setRepairCount(Integer repairCount) {
        this.repairCount = repairCount;
    }

    // References with non-UUID primary keys

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Set<InsuranceCase> getInsuranceCases() {
        return insuranceCases;
    }

    public void setInsuranceCases(Set<InsuranceCase> insuranceCases) {
        this.insuranceCases = insuranceCases;
    }

    public CarDocumentation getCarDocumentation() {
        return carDocumentation;
    }

    public void setCarDocumentation(CarDocumentation carDocumentation) {
        this.carDocumentation = carDocumentation;
    }
}
