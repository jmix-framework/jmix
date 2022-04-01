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

package jpql_macros

import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import test_support.DataSpec
import test_support.entity.TestDateTimeEntity

import java.time.*
@Ignore("Till Haulmont/jmix-data#124 will be fixed")
class DateTimeMacroTest extends DataSpec {

    @Autowired
    DataManager dataManager

    private TestDateTimeEntity entity

    private LocalDate localDate
    private LocalDateTime localDateTime
    private OffsetDateTime offsetDateTime
    private LocalTime localTime;
    private OffsetTime offsetTime;
    private Date nowDate

    void setup() {

        localDate = LocalDate.now()
        localDateTime = LocalDateTime.now()
        offsetDateTime = OffsetDateTime.now()
        localTime = LocalTime.now()
        offsetTime = OffsetTime.now()

        nowDate = new Date()

        entity = dataManager.create(TestDateTimeEntity)

        entity.localDate = localDate
        entity.localDateTime = localDateTime
        entity.offsetDateTime = offsetDateTime
        entity.nowDate = nowDate
        entity.localTime = localTime
        entity.offsetTime = offsetTime;

        dataManager.save(entity)
    }

    //----------@between--------
    def "@between for LocalDate"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDate, now, now + 1, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDate, now - 1, now, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDate, now - 1, now, DAY)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDate, now, now + 1, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDate, now - 1, now, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('where @between(e.localDate, now - 1, now, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDate, now - 1, now, MINUTE)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    def "@between for OffsetTime"() {

        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetTime, now - 1, now, hour)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetTime, now, now + 1, hour)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetTime, now - 1, now, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetTime, now, now + 1, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetTime, now - 10, now - 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetTime, now - 5, now + 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@between for LocalTime"() {

        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localTime, now - 1, now, hour)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localTime, now, now + 1, hour)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localTime, now - 1, now, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localTime, now, now + 1, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localTime, now - 10, now - 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localTime, now - 5, now + 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@between for LocalDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now, now + 1, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now - 1, now, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now, now + 1, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now - 1, now, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now - 1, now, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now, now + 1, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now - 10, now - 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.localDateTime, now - 5, now + 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@between for OffsetDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now, now + 1, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now - 1, now, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now, now + 1, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now - 1, now, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now - 1, now, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now, now + 1, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now - 10, now - 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.offsetDateTime, now - 5, now + 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@between for Date"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now, now + 1, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now - 1, now, day)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)

        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now, now + 1, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now - 1, now, month)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now - 1, now, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now, now + 1, minute)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now - 10, now - 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@between(e.nowDate, now - 5, now + 5, second)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    //----------@dateBefore and @dateAfter--------


    def "@dateBefore for DateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.localDate, :param)')
                .parameter('param', LocalDate.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.localDate, :param)')
                .parameter('param', LocalDate.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@dateAfter for DateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.localDate, :param)')
                .parameter('param', LocalDate.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.localDate, :param)')
                .parameter('param', LocalDate.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    def "@dateBefore for LocalDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.localDateTime, :param)')
                .parameter('param', LocalDateTime.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.localDateTime, :param)')
                .parameter('param', LocalDateTime.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@dateAfter for LocalDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.localDateTime, :param)')
                .parameter('param', LocalDateTime.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.localDateTime, :param)')
                .parameter('param', LocalDateTime.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    def "@dateBefore for OffsetDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.offsetDateTime, :param)')
                .parameter('param', OffsetDateTime.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.offsetDateTime, :param)')
                .parameter('param', OffsetDateTime.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@dateAfter for OffsetDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.offsetDateTime, :param)')
                .parameter('param', OffsetDateTime.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.offsetDateTime, :param)')
                .parameter('param', OffsetDateTime.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    def "@dateBefore for Date"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.nowDate, now)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateBefore(e.nowDate, now+1)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@dateAfter for Date"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.nowDate, now)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateAfter(e.nowDate, now+1)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    //----------@dateEquals--------

    def "@dateEquals for DateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.localDate, :param)')
                .parameter('param', LocalDate.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.localDate, :param)')
                .parameter('param', LocalDate.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }


    def "@dateEquals for LocalDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.localDateTime, :param)')
                .parameter('param', LocalDateTime.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.localDateTime, :param)')
                .parameter('param', LocalDateTime.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    def "@dateEquals for OffsetDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.offsetDateTime, :param)')
                .parameter('param', OffsetDateTime.now())
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.offsetDateTime, :param)')
                .parameter('param', OffsetDateTime.now().plusDays(1))
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    def "@dateEquals for Date"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.nowDate, now)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity

        when:
        e = dataManager.load(TestDateTimeEntity)
                .query('@dateEquals(e.nowDate, now+1)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == null
    }

    //----------@dateEquals--------

    def "@today for DateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@today(e.localDate)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@today for LocalDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@today(e.localDateTime)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@today for OffsetDateTime"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@today(e.offsetDateTime)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }

    def "@today for Date"() {
        when:
        def e = dataManager.load(TestDateTimeEntity)
                .query('@today(e.nowDate)')
                .fetchPlan(FetchPlan.LOCAL).optional().orElse(null)
        then:
        e == entity
    }
}
