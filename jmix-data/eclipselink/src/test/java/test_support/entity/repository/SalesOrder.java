/*
 * Copyright 2021 Haulmont.
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

package test_support.entity.repository;


import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;

import jakarta.persistence.*;
import java.util.Date;

@JmixEntity
@Table(name = "REPOSITORY_SALES_ORDER")
@Entity(name = "repository$SalesOrder")
public class SalesOrder extends BaseEntity {
    private static final long serialVersionUID = -2862329198474356617L;

    @Column(name = "COUNT")
    protected Integer count;

    @Column(name = "NUMBER_")
    protected String number;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_")
    protected Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    protected Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    protected Product product;

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Integer getCount() {
        return count;
    }

    public SalesOrder setCount(Integer count) {
        this.count = count;
        return this;
    }

    public Product getProduct() {
        return product;
    }

    public SalesOrder setProduct(Product product) {
        this.product = product;
        return this;
    }
}