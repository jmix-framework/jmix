/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.entity.multidb;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.Store;
import io.jmix.data.entity.BaseLongIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "test_Db1Foo")
@Table(name = "FOO")
@Store(name = "db1")
public class Db1Foo extends BaseLongIdEntity {

    @Column(name = "NAME")
    @InstanceName
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
