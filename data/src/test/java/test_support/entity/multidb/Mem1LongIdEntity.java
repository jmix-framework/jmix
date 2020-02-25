/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.entity.multidb;

import io.jmix.core.entity.BaseLongIdEntity;
import io.jmix.core.metamodel.annotations.MetaClass;
import io.jmix.core.metamodel.annotations.MetaProperty;
import io.jmix.core.metamodel.annotations.NamePattern;
import io.jmix.core.metamodel.annotations.Store;

@MetaClass(name = "test_Mem1LongIdEntity")
@NamePattern("%s|name")
@Store(name = "mem1")
public class Mem1LongIdEntity extends BaseLongIdEntity {

    @MetaProperty
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
