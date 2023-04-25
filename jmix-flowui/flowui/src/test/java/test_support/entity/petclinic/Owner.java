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

package test_support.entity.petclinic;

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.TestBaseEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

@Entity(name = "pc_Owner")
@JmixEntity
@Table(name = "PC_OWNER")
public class Owner extends TestBaseEntity {

    private static final long serialVersionUID = 3567970938271564819L;

    @InstanceName
    @Column(name = "NAME")
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    protected OwnerCategory category;

    @Email
    @Column(name = "EMAIL")
    protected String email;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    @AssociationOverrides({
            @AssociationOverride(name = "country", joinColumns = @JoinColumn(name = "ADDRESS_COUNTRY_ID")),
            @AssociationOverride(name = "city", joinColumns = @JoinColumn(name = "ADDRESS_CITY_ID"))
    })
    @AttributeOverrides({
            @AttributeOverride(name = "postcode", column = @Column(name = "ADDRESS_POSTCODE")),
            @AttributeOverride(name = "line1", column = @Column(name = "ADDRESS_LINE1")),
            @AttributeOverride(name = "line2", column = @Column(name = "ADDRESS_LINE2"))
    })
    protected Address address;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public OwnerCategory getCategory() {
        return category;
    }

    public void setCategory(OwnerCategory category) {
        this.category = category;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
