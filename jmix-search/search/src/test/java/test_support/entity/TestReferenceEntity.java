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

package test_support.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@JmixEntity
@Entity(name = "test_ReferenceEntity")
@Table(name = "TEST_REFERENCE_ENTITY")
public class TestReferenceEntity extends BaseEntity {

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinColumn(name = "ONE_TO_ONE_ASSOCIATION_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private TestSubReferenceEntity oneToOneAssociation;

    @Column(name = "TEXT_VALUE")
    private String textValue;

    @Column(name = "INT_VALUE")
    private Integer intValue;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_VALUE")
    protected Date dateValue;

    @Column(name = "ENUM_VALUE")
    private Integer enumValue;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oneToOneAssociation")
    private TestRootEntity testRootEntityOneToOne;

    @JoinColumn(name = "TEST_ROOT_ENTITY_M_TO_O_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TestRootEntity testRootEntityManyToOne;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "testReferenceEntityManyToOne")
    private List<TestSubReferenceEntity> oneToManyAssociation;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinTable(name = "TEST_REF_E_SREF_E_LINK",
            joinColumns = @JoinColumn(name = "TEST_REFERENCE_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "TEST_SUB_REFERENCE_ENTITY_ID"))
    @ManyToMany
    private List<TestSubReferenceEntity> manyToManyAssociation;

    public List<TestSubReferenceEntity> getManyToManyAssociation() {
        return manyToManyAssociation;
    }

    public void setManyToManyAssociation(List<TestSubReferenceEntity> manyToManyAssociation) {
        this.manyToManyAssociation = manyToManyAssociation;
    }

    public List<TestSubReferenceEntity> getOneToManyAssociation() {
        return oneToManyAssociation;
    }

    public void setOneToManyAssociation(List<TestSubReferenceEntity> oneToManyAssociation) {
        this.oneToManyAssociation = oneToManyAssociation;
    }

    public TestRootEntity getTestRootEntityManyToOne() {
        return testRootEntityManyToOne;
    }

    public void setTestRootEntityManyToOne(TestRootEntity testRootEntityManyToOne) {
        this.testRootEntityManyToOne = testRootEntityManyToOne;
    }

    public TestSubReferenceEntity getOneToOneAssociation() {
        return oneToOneAssociation;
    }

    public void setOneToOneAssociation(TestSubReferenceEntity oneToOneAssociation) {
        this.oneToOneAssociation = oneToOneAssociation;
    }

    public TestRootEntity getTestRootEntityOneToOne() {
        return testRootEntityOneToOne;
    }

    public void setTestRootEntityOneToOne(TestRootEntity testRootEntityOneToOne) {
        this.testRootEntityOneToOne = testRootEntityOneToOne;
    }

    public TestEnum getEnumValue() {
        return enumValue == null ? null : TestEnum.fromId(enumValue);
    }

    public void setEnumValue(TestEnum enumValue) {
        this.enumValue = enumValue == null ? null : enumValue.getId();
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }
}
