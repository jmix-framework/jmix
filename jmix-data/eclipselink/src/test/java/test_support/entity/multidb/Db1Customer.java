/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.entity.multidb;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.Store;

import jakarta.persistence.*;
import java.util.List;

@JmixEntity
@Entity(name = "test_Db1Customer")
@Table(name = "CUSTOMER")
@Store(name = "db1")
public class Db1Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO/*, generator = "ref$Db1Customer"*/)
//    @SequenceGenerator(name = "ref$Db1Customer", sequenceName = "customer_sequence", allocationSize = 1)
    @Column(name = "ID")
    protected Long id;

    @OneToMany(mappedBy = "customer")
    protected List<Db1Order> orders;

    @Column(name = "NAME")
    @InstanceName
    private String name;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Db1Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Db1Order> orders) {
        this.orders = orders;
    }
}
