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

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.TestBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "test_ProductTag")
@JmixEntity
@Table(name = "TEST_PRODUCT_TAG")
public class ProductTag extends TestBaseEntity {

    private static final long serialVersionUID = 8176440331297951916L;

    @Column(name = "NAME")
    @InstanceName
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProductTag{" +
                "id=" + id +
                ", version=" + version +
                ", name='" + name + '\'' +
                "}@" + Integer.toHexString(System.identityHashCode(this));
    }
}
