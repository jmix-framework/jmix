/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.entity.multidb;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.core.metamodel.annotation.Store;
import io.jmix.data.entity.BaseUuidEntity;

@ModelObject(name = "test_Mem1Customer")
@Store(name = "mem1")
public class Mem1Customer extends BaseUuidEntity {

    @ModelProperty
    @InstanceName
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
