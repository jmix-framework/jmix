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

package test_support.entity.lazyloading.eager_mapped_by;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import test_support.entity.BaseEntity;

@Table(name = "TEST_EAGER_MAPPED_BY_PASSPORT")
@JmixEntity
@Entity(name = "test_EagerMappedByPassport")
public class EagerMappedByPassport extends BaseEntity {
    @Column(name = "NUMBER_")
    protected String number;

    // intentionally declared without fetch = FetchType.LAZY: the attribute is EAGER at enhancing time,
    // so no '_persistence_person_vh' field is woven for it
    @OneToOne(mappedBy = "passport")
    protected EagerMappedByPerson person;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public EagerMappedByPerson getPerson() {
        return person;
    }

    public void setPerson(EagerMappedByPerson person) {
        this.person = person;
    }
}
