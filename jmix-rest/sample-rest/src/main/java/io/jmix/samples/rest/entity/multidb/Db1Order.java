/*
 * Copyright 2016 Haulmont.
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

package io.jmix.samples.rest.entity.multidb;


import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.Store;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity(name = "ref$Db1Order")
@JmixEntity
@Table(name = "ORDER_")
@Store(name = "db1")
public class Db1Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    protected Long id;

    @Column(name = "ORDER_DATE")
    private Date orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Db1Customer customer;

    @Transient
    @JmixProperty
    @DependsOnProperties("mem1CustomerId")
    private Mem1Customer mem1Customer;

    @Column(name = "MEM_CUST_ID")
    private UUID mem1CustomerId;

    public void setId(Long dbId) {
        this.id = dbId;
    }

    public Long getId() {
        return id;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Db1Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Db1Customer customer) {
        this.customer = customer;
    }

    public Mem1Customer getMem1Customer() {
        return mem1Customer;
    }

    public void setMem1Customer(Mem1Customer mem1Customer) {
        this.mem1Customer = mem1Customer;
    }

    public UUID getMem1CustomerId() {
        return mem1CustomerId;
    }

    public void setMem1CustomerId(UUID mem1CustomerId) {
        this.mem1CustomerId = mem1CustomerId;
    }

}
