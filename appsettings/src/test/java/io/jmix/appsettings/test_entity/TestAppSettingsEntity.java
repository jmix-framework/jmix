package io.jmix.appsettings.test_entity;

import io.jmix.appsettings.defaults.*;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@JmixEntity
@Entity(name = "testAppSettingsEntity")
@Table(name = "TEST_APP_SETTINGS")
public class TestAppSettingsEntity extends AppSettingsEntity {

    @Column(name = "TEST_BOOLEAN_VAL")
    @AppSettingsDefaultBoolean(true)
    private Boolean testBooleanValue;

    @Column(name = "TEST_DOUBLE_VAL")
    @AppSettingsDefaultDouble(3.1415926535)
    private Double testDoubleValue;

    @Column(name = "TEST_INTEGER_VAL")
    @AppSettingsDefaultInt(123)
    private Integer testIntegerValue;

    @Column(name = "TEST_LONG_VAL")
    @AppSettingsDefaultLong(100500L)
    private Long testLongValue;

    @Column(name = "TEST_STRING_VAL")
    @AppSettingsDefault("defVal")
    private String testStringValue;

    public Boolean getTestBooleanValue() {
        return testBooleanValue;
    }

    public void setTestBooleanValue(Boolean testBooleanValue) {
        this.testBooleanValue = testBooleanValue;
    }

    public Integer getTestIntegerValue() {
        return testIntegerValue;
    }

    public void setTestIntegerValue(Integer testIntegerValue) {
        this.testIntegerValue = testIntegerValue;
    }

    public Long getTestLongValue() {
        return testLongValue;
    }

    public void setTestLongValue(Long testLongValue) {
        this.testLongValue = testLongValue;
    }

    public Double getTestDoubleValue() {
        return testDoubleValue;
    }

    public void setTestDoubleValue(Double testDoubleValue) {
        this.testDoubleValue = testDoubleValue;
    }

    public String getTestStringValue() {
        return testStringValue;
    }

    public void setTestStringValue(String testStringValue) {
        this.testStringValue = testStringValue;
    }

}
