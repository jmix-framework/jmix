/*
 * Copyright 2019 Haulmont.
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

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.*;
import java.util.Date;

@Table(name = "TEST_DATE_TIME_ENTITY")
@JmixEntity
@Entity(name = "test_TestDateTimeEntity")
public class TestDateTimeEntity extends BaseEntity {
    private static final long serialVersionUID = -543881549652353366L;

    @Column(name = "NAME")
    protected String name;

    @Column(name = "OFFSET_DATE_TIME")
    protected OffsetDateTime offsetDateTime;

    @Column(name = "OFFSET_TIME")
    protected OffsetTime offsetTime;

    @Column(name = "LOCAL_DATE")
    protected LocalDate localDate;

    @Column(name = "LOCAL_TIME")
    protected LocalTime localTime;

    @Column(name = "LOCAL_DATE_TIME")
    protected LocalDateTime localDateTime;

    @Column(name = "NOW_DATE")
    protected Date nowDate;

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    public OffsetTime getOffsetTime() {
        return offsetTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public Date getNowDate() {
        return nowDate;
    }

    public void setNowDate(Date date) {
        this.nowDate = date;
    }
}
