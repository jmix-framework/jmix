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
@Entity(name = "test_SimpleRootEntity")
@Table(name = "TEST_SIMPLE_ROOT_ENTITY")
public class TestSimpleRootEntity extends BaseEntity {

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "FIRST_TEXT_VALUE")
    private String firstTextValue;

    @Column(name = "SECOND_TEXT_VALUE")
    private String secondTextValue;

    @Column(name = "INT_VALUE")
    private Integer intValue;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_VALUE")
    private Date dateValue;

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

    public String getFirstTextValue() {
        return firstTextValue;
    }

    public void setFirstTextValue(String firstTextValue) {
        this.firstTextValue = firstTextValue;
    }

    public String getSecondTextValue() {
        return secondTextValue;
    }

    public void setSecondTextValue(String secondTextValue) {
        this.secondTextValue = secondTextValue;
    }
}
