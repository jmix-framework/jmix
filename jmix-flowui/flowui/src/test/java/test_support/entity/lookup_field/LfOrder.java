/*
 * Copyright 2026 Haulmont.
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

package test_support.entity.lookup_field;

import io.jmix.core.entity.annotation.LookupField;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;

@JmixEntity
@Entity(name = "test_LfOrder")
@Table(name = "TEST_LF_ORDER")
public class LfOrder extends TestBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRY_ID")
    private LfCountry country;

    @LookupField(type = LookupType.VIEW)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VIEW_COUNTRY_ID")
    private LfCountry viewCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITY_ID")
    private LfCity city;

    @LookupField(type = LookupType.DROPDOWN)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DROPDOWN_CITY_ID")
    private LfCity dropdownCity;

    @LookupField(type = LookupType.VIEW, actions = {"entity_open"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VIEW_CITY_ID")
    private LfCity viewCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private LfProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private LfPerson person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID")
    private LfEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPPLIER_ID")
    private LfSupplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BROKEN_ID")
    private LfBroken broken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_ID")
    private LfTag tag;

    public LfCountry getCountry() {
        return country;
    }

    public void setCountry(LfCountry country) {
        this.country = country;
    }

    public LfCountry getViewCountry() {
        return viewCountry;
    }

    public void setViewCountry(LfCountry viewCountry) {
        this.viewCountry = viewCountry;
    }

    public LfCity getCity() {
        return city;
    }

    public void setCity(LfCity city) {
        this.city = city;
    }

    public LfCity getDropdownCity() {
        return dropdownCity;
    }

    public void setDropdownCity(LfCity dropdownCity) {
        this.dropdownCity = dropdownCity;
    }

    public LfCity getViewCity() {
        return viewCity;
    }

    public void setViewCity(LfCity viewCity) {
        this.viewCity = viewCity;
    }

    public LfProduct getProduct() {
        return product;
    }

    public void setProduct(LfProduct product) {
        this.product = product;
    }

    public LfPerson getPerson() {
        return person;
    }

    public void setPerson(LfPerson person) {
        this.person = person;
    }

    public LfEvent getEvent() {
        return event;
    }

    public void setEvent(LfEvent event) {
        this.event = event;
    }

    public LfSupplier getSupplier() {
        return supplier;
    }

    public void setSupplier(LfSupplier supplier) {
        this.supplier = supplier;
    }

    public LfBroken getBroken() {
        return broken;
    }

    public void setBroken(LfBroken broken) {
        this.broken = broken;
    }

    public LfTag getTag() {
        return tag;
    }

    public void setTag(LfTag tag) {
        this.tag = tag;
    }
}
