/*
 * Copyright 2023 Haulmont.
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

package test_support.entity.dataaware;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;
import test_support.entity.TestBaseEntity;

import java.time.OffsetTime;
import java.util.Date;

@Entity(name = "test_DateTimeRange")
@JmixEntity
@Table(name = "TEST_DATE_TIME_RANGE")
public class TestDateTimeRangeEntity extends TestBaseEntity {

    @Past
    @Column(name = "TIME_ZONE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;

    @Future
    @Column(name = "DATE_")
    @Temporal(TemporalType.DATE)
    private Date date;

    @FutureOrPresent
    @Column(name = "OFFSET_TIME")
    private OffsetTime offsetTime;

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date time) {
        this.dateTime = time;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public OffsetTime getOffsetTime() {
        return offsetTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }
}
