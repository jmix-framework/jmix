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

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.Date;

@JmixEntity
@Entity(name = "test_SubReferenceEntity")
@Table(name = "TEST_SUB_REFERENCE_ENTITY")
public class TestSubReferenceEntity extends BaseEntity {

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "TEXT_VALUE")
    private String textValue;

    @Column(name = "INT_VALUE")
    private Integer intValue;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_VALUE")
    private Date dateValue;

    @Column(name = "ENUM_VALUE")
    private Integer enumValue;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oneToOneAssociation")
    private TestReferenceEntity testReferenceEntity;

    @JoinColumn(name = "TEST_REF_ENTITY_M_TO_O_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TestReferenceEntity testReferenceEntityManyToOne;

    public TestReferenceEntity getTestReferenceEntityManyToOne() {
        return testReferenceEntityManyToOne;
    }

    public void setTestReferenceEntityManyToOne(TestReferenceEntity testReferenceEntityManyToOne) {
        this.testReferenceEntityManyToOne = testReferenceEntityManyToOne;
    }

    public TestReferenceEntity getTestReferenceEntity() {
        return testReferenceEntity;
    }

    public void setTestReferenceEntity(TestReferenceEntity testReferenceEntity) {
        this.testReferenceEntity = testReferenceEntity;
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

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public TestEnum getEnumValue() {
        return enumValue == null ? null : TestEnum.fromId(enumValue);
    }

    public void setEnumValue(TestEnum enumValue) {
        this.enumValue = enumValue == null ? null : enumValue.getId();
    }
}
