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

import javax.persistence.*;

@JmixEntity
@Table(name = "TEST_ENUM_SUB_REF_ENTITY")
@Entity(name = "test_EnumSubRefEntity")
public class TestEnumSubRefEntity extends BaseEntity {
    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "ENUM_VALUE")
    private Integer enumValue;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oneToOneRef")
    private TestEnumRefEntity inverseOneToOneRef;

    @JoinColumn(name = "MANY_TO_ONE_REF_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TestEnumRefEntity manyToOneRef;

    public TestEnumRefEntity getManyToOneRef() {
        return manyToOneRef;
    }

    public void setManyToOneRef(TestEnumRefEntity manyToOneRef) {
        this.manyToOneRef = manyToOneRef;
    }

    public TestEnumRefEntity getInverseOneToOneRef() {
        return inverseOneToOneRef;
    }

    public void setInverseOneToOneRef(TestEnumRefEntity inverseOneToOneRef) {
        this.inverseOneToOneRef = inverseOneToOneRef;
    }

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