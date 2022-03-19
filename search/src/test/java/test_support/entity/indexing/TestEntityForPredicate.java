/*
 * Copyright 2021 Haulmont.
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

package test_support.entity.indexing;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;
import test_support.entity.TestEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@JmixEntity
@Table(name = "TEST_ENTITY_FOR_PREDICATE")
@Entity(name = "test_EntityForPredicate")
public class TestEntityForPredicate extends BaseEntity {
    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "ENUM_VALUE")
    private Integer enumValue;

    public TestEnum getEnumValue() {
        return enumValue == null ? null : TestEnum.fromId(enumValue);
    }

    public void setEnumValue(TestEnum enumValue) {
        this.enumValue = enumValue == null ? null : enumValue.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}