/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.core.model;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.NumberFormat;

import java.math.BigDecimal;

/**
 *
 */
@JmixEntity(name = "sys$TestNumberValuesEntity")
public class TestNumberValuesEntity extends BaseUuidEntity {

    @JmixProperty
    @NumberFormat(pattern = "#,###.###")
    private BigDecimal decimalField1;

    @JmixProperty
    @NumberFormat(pattern = "0.00")
    private BigDecimal decimalField2;

    @JmixProperty
    @NumberFormat(pattern = "#")
    private BigDecimal decimalField3;

    @JmixProperty
    @NumberFormat(pattern = "#,###.###", decimalSeparator = "_", groupingSeparator = "`")
    private BigDecimal decimalField4;

    @JmixProperty
    @NumberFormat(pattern = "#,###.###")
    private Double doubleField1;

    @JmixProperty
    @NumberFormat(pattern = "0.00")
    private Double doubleField2;

    @JmixProperty
    @NumberFormat(pattern = "#")
    private Double doubleField3;

    @JmixProperty
    @NumberFormat(pattern = "#,###.###")
    private Float floatField1;

    @JmixProperty
    @NumberFormat(pattern = "0.00")
    private Float floatField2;

    @JmixProperty
    @NumberFormat(pattern = "#")
    private Float floatField3;

    @JmixProperty
    @NumberFormat(pattern = "#")
    private Integer intField1;

    @JmixProperty
    @NumberFormat(pattern = "#,##0")
    private Integer intField2;

    @JmixProperty
    @NumberFormat(pattern = "#")
    private Long longField1;

    @JmixProperty
    @NumberFormat(pattern = "#,##0")
    private Long longField2;

    @JmixProperty
    @NumberFormat(pattern = "#%")
    private BigDecimal percentField;

    public BigDecimal getDecimalField1() {
        return decimalField1;
    }

    public void setDecimalField1(BigDecimal decimalField1) {
        this.decimalField1 = decimalField1;
    }

    public BigDecimal getDecimalField2() {
        return decimalField2;
    }

    public void setDecimalField2(BigDecimal decimalField2) {
        this.decimalField2 = decimalField2;
    }

    public BigDecimal getDecimalField3() {
        return decimalField3;
    }

    public void setDecimalField3(BigDecimal decimalField3) {
        this.decimalField3 = decimalField3;
    }

    public BigDecimal getDecimalField4() {
        return decimalField4;
    }

    public void setDecimalField4(BigDecimal decimalField4) {
        this.decimalField4 = decimalField4;
    }

    public Double getDoubleField1() {
        return doubleField1;
    }

    public void setDoubleField1(Double doubleField1) {
        this.doubleField1 = doubleField1;
    }

    public Double getDoubleField2() {
        return doubleField2;
    }

    public void setDoubleField2(Double doubleField2) {
        this.doubleField2 = doubleField2;
    }

    public Double getDoubleField3() {
        return doubleField3;
    }

    public void setDoubleField3(Double doubleField3) {
        this.doubleField3 = doubleField3;
    }

    public Float getFloatField1() {
        return floatField1;
    }

    public void setFloatField1(Float floatField1) {
        this.floatField1 = floatField1;
    }

    public Float getFloatField2() {
        return floatField2;
    }

    public void setFloatField2(Float floatField2) {
        this.floatField2 = floatField2;
    }

    public Float getFloatField3() {
        return floatField3;
    }

    public void setFloatField3(Float floatField3) {
        this.floatField3 = floatField3;
    }

    public Integer getIntField1() {
        return intField1;
    }

    public void setIntField1(Integer intField1) {
        this.intField1 = intField1;
    }

    public Integer getIntField2() {
        return intField2;
    }

    public void setIntField2(Integer intField2) {
        this.intField2 = intField2;
    }

    public Long getLongField1() {
        return longField1;
    }

    public void setLongField1(Long longField1) {
        this.longField1 = longField1;
    }

    public Long getLongField2() {
        return longField2;
    }

    public void setLongField2(Long longField2) {
        this.longField2 = longField2;
    }

    public BigDecimal getPercentField() {
        return percentField;
    }

    public void setPercentField(BigDecimal percentField) {
        this.percentField = percentField;
    }
}
