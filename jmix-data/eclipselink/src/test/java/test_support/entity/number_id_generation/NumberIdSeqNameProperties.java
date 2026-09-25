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

import java.util.UUID;

@Table(name = "TEST_NUMBER_ID_SEQ_NAME_PROPERTIES")
@JmixEntity
@Entity(name = "test_NumberIdSeqNameProperties")
public class NumberIdSeqNameProperties {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "LONG_NUMBER")
    @JmixGeneratedValue(sequenceName = "seq_number_id_props_long")
    protected Long longNumber;

    @Column(name = "INTEGER_NUMBER")
    @JmixGeneratedValue(sequenceName = "seq_number_id_props_integer")
    protected Integer integerNumber;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getLongNumber() {
        return longNumber;
    }

    public void setLongNumber(Long longNumber) {
        this.longNumber = longNumber;
    }

    public Integer getIntegerNumber() {
        return integerNumber;
    }

    public void setIntegerNumber(Integer integerNumber) {
        this.integerNumber = integerNumber;
    }
}
