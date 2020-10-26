/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.entity.multidb;


import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.core.metamodel.annotation.Store;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity(name = "ref$Db1Order")
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
    @ModelProperty
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
