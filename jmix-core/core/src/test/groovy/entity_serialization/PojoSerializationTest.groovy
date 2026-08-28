/*
 * Copyright 2025 Haulmont.
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

package entity_serialization

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.jmix.core.CoreConfiguration
import io.jmix.core.EntitySerialization
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration])
class PojoSerializationTest extends Specification {

    @Autowired
    EntitySerialization entitySerialization

    def "POJO with date and time attributes"() {

        def pojo = new PojoWithDates(
                date: new SimpleDateFormat('yyyy-MM-dd HH:mm').parse('2025-04-26 18:44'),
                localDate: LocalDate.parse('2025-04-26'),
                localDateTime: LocalDateTime.parse('2025-04-26T18:44'),
                localTime: LocalTime.parse('18:44'),
                offsetDateTime: OffsetDateTime.parse('2025-04-26T18:44+04:00'),
                offsetTime: OffsetTime.parse('18:44+04:00'))

        when:

        def json = entitySerialization.objectToJson(pojo)

        then:

        Map jsonFields = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {}.getType())
        jsonFields['localDate'] == '2025-04-26'
        jsonFields['localDateTime'] == '2025-04-26T18:44:00'
        jsonFields['localTime'] == '18:44:00'
        jsonFields['offsetDateTime'] == '2025-04-26T18:44:00+04:00'
        jsonFields['offsetTime'] == '18:44:00+04:00'

        when:

        def restoredPojo = entitySerialization.objectFromJson(json, PojoWithDates)

        then:

        restoredPojo.date == pojo.date
        restoredPojo.localDate == pojo.localDate
        restoredPojo.localDateTime == pojo.localDateTime
        restoredPojo.localTime == pojo.localTime
        restoredPojo.offsetDateTime == pojo.offsetDateTime
        restoredPojo.offsetTime == pojo.offsetTime
    }

    static class PojoWithDates {
        Date date
        LocalDate localDate
        LocalDateTime localDateTime
        LocalTime localTime
        OffsetDateTime offsetDateTime
        OffsetTime offsetTime
    }
}
