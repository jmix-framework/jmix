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
import test_support.entity.TestBaseEntity;

import java.util.Date;

@Entity(name="test_TimeZoneId")
@JmixEntity
@Table(name = "TEST_TIME_ZONE_ID")
public class TestTimeZoneIdEntity extends TestBaseEntity {

    @Column(name = "TIME_ZONE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeZone;

    public Date getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(Date timeZone) {
        this.timeZone = timeZone;
    }
}
