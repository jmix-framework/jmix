/*
 * Copyright 2019 Haulmont.
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

package test_support.entity.sales;


import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.datatype.impl.EnumUtils;
import test_support.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JmixEntity
@Entity(name = "sales_Customer")
@Table(name = "SALES_CUSTOMER")
@Listeners("test_TestCustomerListener")
public class Customer extends BaseEntity {
    @Column(name = "NAME")
    @InstanceName
    private String name;

    @Column(name = "STATUS")
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return EnumUtils.fromId(Status.class, status, null);
    }

    public void setStatus(Status status) {
        this.status = status.getId();
    }
}
