/*
 * Copyright 2026 Haulmont.
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

package test_support.entity.lookup_field;

import io.jmix.core.entity.annotation.LookupField;
import io.jmix.core.entity.annotation.LookupItemsQuery;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import test_support.entity.TestBaseEntity;

@LookupField(type = LookupType.DROPDOWN, itemsQuery = @LookupItemsQuery(byInstanceName = true))
@JmixEntity
@Entity(name = "test_LfPerson")
@Table(name = "TEST_LF_PERSON")
public class LfPerson extends TestBaseEntity {

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @InstanceName
    @DependsOnProperties({"firstName", "lastName"})
    public String getCaption() {
        return firstName + " " + lastName;
    }
}
