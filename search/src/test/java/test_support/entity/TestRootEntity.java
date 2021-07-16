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

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@JmixEntity
@Entity(name = "test_RootEntity")
@Table(name = "TEST_ROOT_ENTITY")
public class TestRootEntity extends BaseEntity {

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

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinColumn(name = "ONE_TO_ONE_ASSOCIATION_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private TestReferenceEntity oneToOneAssociation;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OnDelete(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "testRootEntityManyToOne")
    private List<TestReferenceEntity> oneToManyAssociation;

    public List<TestReferenceEntity> getOneToManyAssociation() {
        return oneToManyAssociation;
    }

    public void setOneToManyAssociation(List<TestReferenceEntity> oneToManyAssociation) {
        this.oneToManyAssociation = oneToManyAssociation;
    }

    public TestReferenceEntity getOneToOneAssociation() {
        return oneToOneAssociation;
    }

    public void setOneToOneAssociation(TestReferenceEntity oneToOneAssociation) {
        this.oneToOneAssociation = oneToOneAssociation;
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
