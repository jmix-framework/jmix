/*
 * Copyright 2022 Haulmont.
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

package test_support.entity.entity_extension;

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.*;
import java.util.UUID;

/**
 * Entity to check that loaded state of method-based attributes determined correctly
 */
@JmixEntity
@Entity(name = "exttest_Client")
@Table(name = "EXTTEST_CLIENT")
public class Client {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "NAME")
    @InstanceName
    protected String name;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private Address address;


    @JmixProperty
    @DependsOnProperties({"address", "name"})
    public String getLabel1() {
        return name + " - " + address.getCity();
    }

    @JmixProperty
    @DependsOnProperties({"name", "address"})
    public String getLabel2() {
        return name + " - " + address.getCity();
    }

    @JmixProperty
    @DependsOnProperties({"address", "name"})
    public String getLabel3() {
        return name + " - " + address.getStreet();
    }

    @JmixProperty
    @DependsOnProperties({"address"})
    public String getAddressStreet() {
        return ">" + address.getStreet() + "<";
    }

    @JmixProperty
    @DependsOnProperties({"address"})
    public String getAddressCity() {
        return ">" + address.getCity() + "<";
    }


    @JmixProperty
    @DependsOnProperties({"name"})
    public String getHtmlName() {
        return "</b>" + name + "</b>";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
