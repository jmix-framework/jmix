/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.testmodel;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.Store;

import javax.persistence.*;

@JmixEntity
@Entity(name = "test_Db1Entity")
@Table(name = "TEST_DB1_ENTITY")
@Store(name = "db1")
public class Db1Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO/*, generator = "ref$Db1Customer"*/)
    @Column(name = "ID")
    protected Long id;

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
}
