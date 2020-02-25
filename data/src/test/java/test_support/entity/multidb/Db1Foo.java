/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.entity.multidb;

import io.jmix.core.entity.BaseLongIdEntity;
import io.jmix.core.metamodel.annotations.NamePattern;
import io.jmix.core.metamodel.annotations.Store;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "test_Db1Foo")
@Table(name = "FOO")
@NamePattern("%s|name")
@Store(name = "db1")
public class Db1Foo extends BaseLongIdEntity {

    @Column(name = "NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
