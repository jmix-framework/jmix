/*
 * Copyright 2026 Haulmont.
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

package test_support.entity.viewtemplate;

import io.jmix.core.FileRef;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Lob;
import test_support.entity.sales.Status;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@SuppressWarnings("unused")
public class ComponentXmlDatatypesEntity {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    private String stringValue;

    @Lob
    private String lobValue;

    private UUID uuidValue;

    private Boolean booleanValue;

    private java.sql.Date sqlDateValue;

    private LocalDate localDateValue;

    private java.sql.Time sqlTimeValue;

    private LocalTime localTimeValue;

    private OffsetTime offsetTimeValue;

    private Date dateValue;

    private LocalDateTime localDateTimeValue;

    private OffsetDateTime offsetDateTimeValue;

    private Short shortValue;

    private Integer integerValue;

    private Long longValue;

    private BigInteger bigIntegerValue;

    private BigDecimal bigDecimalValue;

    private Float floatValue;

    private Double doubleValue;

    private Character characterValue;

    private URI uriValue;

    private FileRef fileRefValue;

    private byte[] byteArrayValue;

    private Status status;

    @Composition
    private ComponentXmlDatatypesEntity compositionValue;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getLobValue() {
        return lobValue;
    }

    public void setLobValue(String lobValue) {
        this.lobValue = lobValue;
    }

    public UUID getUuidValue() {
        return uuidValue;
    }

    public void setUuidValue(UUID uuidValue) {
        this.uuidValue = uuidValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public java.sql.Date getSqlDateValue() {
        return sqlDateValue;
    }

    public void setSqlDateValue(java.sql.Date sqlDateValue) {
        this.sqlDateValue = sqlDateValue;
    }

    public LocalDate getLocalDateValue() {
        return localDateValue;
    }

    public void setLocalDateValue(LocalDate localDateValue) {
        this.localDateValue = localDateValue;
    }

    public java.sql.Time getSqlTimeValue() {
        return sqlTimeValue;
    }

    public void setSqlTimeValue(java.sql.Time sqlTimeValue) {
        this.sqlTimeValue = sqlTimeValue;
    }

    public LocalTime getLocalTimeValue() {
        return localTimeValue;
    }

    public void setLocalTimeValue(LocalTime localTimeValue) {
        this.localTimeValue = localTimeValue;
    }

    public OffsetTime getOffsetTimeValue() {
        return offsetTimeValue;
    }

    public void setOffsetTimeValue(OffsetTime offsetTimeValue) {
        this.offsetTimeValue = offsetTimeValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public LocalDateTime getLocalDateTimeValue() {
        return localDateTimeValue;
    }

    public void setLocalDateTimeValue(LocalDateTime localDateTimeValue) {
        this.localDateTimeValue = localDateTimeValue;
    }

    public OffsetDateTime getOffsetDateTimeValue() {
        return offsetDateTimeValue;
    }

    public void setOffsetDateTimeValue(OffsetDateTime offsetDateTimeValue) {
        this.offsetDateTimeValue = offsetDateTimeValue;
    }

    public Short getShortValue() {
        return shortValue;
    }

    public void setShortValue(Short shortValue) {
        this.shortValue = shortValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public BigInteger getBigIntegerValue() {
        return bigIntegerValue;
    }

    public void setBigIntegerValue(BigInteger bigIntegerValue) {
        this.bigIntegerValue = bigIntegerValue;
    }

    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Character getCharacterValue() {
        return characterValue;
    }

    public void setCharacterValue(Character characterValue) {
        this.characterValue = characterValue;
    }

    public URI getUriValue() {
        return uriValue;
    }

    public void setUriValue(URI uriValue) {
        this.uriValue = uriValue;
    }

    public FileRef getFileRefValue() {
        return fileRefValue;
    }

    public void setFileRefValue(FileRef fileRefValue) {
        this.fileRefValue = fileRefValue;
    }

    public byte[] getByteArrayValue() {
        return byteArrayValue;
    }

    public void setByteArrayValue(byte[] byteArrayValue) {
        this.byteArrayValue = byteArrayValue;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ComponentXmlDatatypesEntity getCompositionValue() {
        return compositionValue;
    }

    public void setCompositionValue(ComponentXmlDatatypesEntity compositionValue) {
        this.compositionValue = compositionValue;
    }
}
