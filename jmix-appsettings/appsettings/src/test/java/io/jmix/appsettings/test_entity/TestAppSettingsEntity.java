package io.jmix.appsettings.test_entity;

import io.jmix.appsettings.defaults.*;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Date;

@JmixEntity
@Entity(name = "testAppSettingsEntity")
@Table(name = "TEST_APP_SETTINGS")
public class TestAppSettingsEntity extends AppSettingsEntity {

    @DeletedDate
    @Column(name = "DELETED_DATE")
    private Date deletedDate;

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

    @SystemLevel
    @Column(name = "TEST_SYSTEM_LEVEL_VAL")
    @AppSettingsDefault("systemDef")
    private String testSystemLevelValue;

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

    public String getTestSystemLevelValue() {
        return testSystemLevelValue;
    }

    public void setTestSystemLevelValue(String testSystemLevelValue) {
        this.testSystemLevelValue = testSystemLevelValue;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

}
