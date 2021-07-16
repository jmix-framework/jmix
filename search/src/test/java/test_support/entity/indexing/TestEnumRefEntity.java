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

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.entity.BaseEntity;
import test_support.entity.TestEnum;

import javax.persistence.*;
import java.util.List;

@JmixEntity
@Table(name = "TEST_ENUM_REF_ENTITY")
@Entity(name = "test_EnumRefEntity")
public class TestEnumRefEntity extends BaseEntity {
    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "ENUM_VALUE")
    private Integer enumValue;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinColumn(name = "ONE_TO_ONE_REF_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private TestEnumSubRefEntity oneToOneRef;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "manyToOneRef")
    private List<TestEnumSubRefEntity> oneToManyRef;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oneToOneRef")
    private TestEnumRootEntity inverseOneToOneRef;

    @JoinColumn(name = "MANY_TO_ONE_REF_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TestEnumRootEntity manyToOneRef;

    public TestEnumRootEntity getManyToOneRef() {
        return manyToOneRef;
    }

    public void setManyToOneRef(TestEnumRootEntity manyToOneRef) {
        this.manyToOneRef = manyToOneRef;
    }

    public TestEnumRootEntity getInverseOneToOneRef() {
        return inverseOneToOneRef;
    }

    public void setInverseOneToOneRef(TestEnumRootEntity inverseOneToOneRef) {
        this.inverseOneToOneRef = inverseOneToOneRef;
    }

    public List<TestEnumSubRefEntity> getOneToManyRef() {
        return oneToManyRef;
    }

    public void setOneToManyRef(List<TestEnumSubRefEntity> oneToManyRef) {
        this.oneToManyRef = oneToManyRef;
    }

    public TestEnumSubRefEntity getOneToOneRef() {
        return oneToOneRef;
    }

    public void setOneToOneRef(TestEnumSubRefEntity oneToOneRef) {
        this.oneToOneRef = oneToOneRef;
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