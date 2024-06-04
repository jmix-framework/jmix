/*
 * Copyright 2023 Haulmont.
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

package test_support.entity.dataaware;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import test_support.entity.TestBaseEntity;

@Entity(name = "test_Length")
@JmixEntity
@Table(name = "TEST_LENGTH")
public class TestLengthEntity extends TestBaseEntity {

    @Size(min = 3, max = 10)
    @Column(name = "SIZE_ATTRIBUTE")
    private String sizeAttribute;

    @Length(min = 5, max = 15)
    @Column(name = "LENGTH_ATTRIBUTE")
    private String lengthAttribute;

    public String getLengthAttribute() {
        return lengthAttribute;
    }

    public void setLengthAttribute(String lengthAttribute) {
        this.lengthAttribute = lengthAttribute;
    }

    public String getSizeAttribute() {
        return sizeAttribute;
    }

    public void setSizeAttribute(String sizeAttribute) {
        this.sizeAttribute = sizeAttribute;
    }
}
