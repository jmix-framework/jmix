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

package test_support.entity.number_id_generation;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "TEST_NUMBER_ID_SEQ_NAME_ID_AND_PROPERTY")
@JmixEntity
@Entity(name = "test_NumberIdSeqNameIdAndProperty")
public class NumberIdSeqNameIdAndProperty {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue(sequenceName = "seq_number_id_id_and_prop_id")
    protected Long id;

    @Column(name = "NUMBER_")
    @JmixGeneratedValue(sequenceName = "seq_number_id_id_and_prop_number")
    protected Long number;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }
}
