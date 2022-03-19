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

package jpql_constants

import io.jmix.core.DataManager
import io.jmix.core.LoadContext
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestDateTimeEntity

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class RelativeDateTimeMomentTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @PersistenceContext
    EntityManager entityManager

    @Autowired
    Metadata metadata

    private TestDateTimeEntity now
    private TestDateTimeEntity previousYearEntity
    private TestDateTimeEntity previousMonthEntity
    private TestDateTimeEntity previousWeekEntity
    private TestDateTimeEntity previousDayEntity
    private TestDateTimeEntity previousHourEntity
    private TestDateTimeEntity previousMinuteEntity
    private LoadContext<TestDateTimeEntity> loadContext


    void setup() {
        now = dataManager.save(createCurrentMomentEntity())
        previousYearEntity = dataManager.save(createPreviousYearEntity())
        previousMonthEntity = dataManager.save(createPreviousMonthEntity())
        previousWeekEntity = dataManager.save(createPreviousWeekEntity())
        previousDayEntity = dataManager.save(createPreviousDayEntity())
        previousHourEntity = dataManager.save(createPreviousHourEntity())
        previousMinuteEntity = dataManager.save(createPreviousMinuteEntity())

        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
    }

    TestDateTimeEntity createCurrentMomentEntity() {
        TestDateTimeEntity currentMoment = dataManager.create(TestDateTimeEntity)
        currentMoment.localDate = LocalDate.now()
        currentMoment.localDateTime = LocalDateTime.now()
        currentMoment.offsetDateTime = OffsetDateTime.now()
        currentMoment.nowDate = new Date()
        return currentMoment
    }

    TestDateTimeEntity createPreviousYearEntity() {
        TestDateTimeEntity previousYearEntity = createCurrentMomentEntity()
        previousYearEntity.localDate = previousYearEntity.localDate.minusYears(1)
        previousYearEntity.localDateTime = previousYearEntity.localDateTime.minusYears(1)
        previousYearEntity.offsetDateTime = previousYearEntity.offsetDateTime.minusYears(1)
        previousYearEntity.nowDate = new Date(previousYearEntity.nowDate.getTime() - TimeUnit.DAYS.toMillis(366))
        return previousYearEntity
    }

    TestDateTimeEntity createPreviousMonthEntity() {
        TestDateTimeEntity previousYearEntity = createCurrentMomentEntity()
        previousYearEntity.localDate = previousYearEntity.localDate.minusMonths(1)
        previousYearEntity.localDateTime = previousYearEntity.localDateTime.minusMonths(1)
        previousYearEntity.offsetDateTime = previousYearEntity.offsetDateTime.minusMonths(1)
        previousYearEntity.nowDate = new Date(previousYearEntity.nowDate.getTime() - TimeUnit.DAYS.toMillis(31))
        return previousYearEntity
    }

    TestDateTimeEntity createPreviousWeekEntity() {
        TestDateTimeEntity previousYearEntity = createCurrentMomentEntity()
        previousYearEntity.localDate = previousYearEntity.localDate.minusWeeks(1)
        previousYearEntity.localDateTime = previousYearEntity.localDateTime.minusWeeks(1)
        previousYearEntity.offsetDateTime = previousYearEntity.offsetDateTime.minusWeeks(1)
        previousYearEntity.nowDate = new Date(previousYearEntity.nowDate.getTime() - TimeUnit.DAYS.toMillis(7))
        return previousYearEntity
    }

    TestDateTimeEntity createPreviousDayEntity() {
        TestDateTimeEntity previousYearEntity = createCurrentMomentEntity()
        previousYearEntity.localDate = previousYearEntity.localDate.minusDays(1)
        previousYearEntity.localDateTime = previousYearEntity.localDateTime.minusDays(1)
        previousYearEntity.offsetDateTime = previousYearEntity.offsetDateTime.minusDays(1)
        previousYearEntity.nowDate = new Date(previousYearEntity.nowDate.getTime() - TimeUnit.DAYS.toMillis(1))
        return previousYearEntity
    }

    TestDateTimeEntity createPreviousHourEntity() {
        TestDateTimeEntity previousYearEntity = createCurrentMomentEntity()
        previousYearEntity.localDateTime = previousYearEntity.localDateTime.minusHours(1)
        previousYearEntity.offsetDateTime = previousYearEntity.offsetDateTime.minusHours(1)
        previousYearEntity.nowDate = new Date(previousYearEntity.nowDate.getTime() - TimeUnit.HOURS.toMillis(1))
        return previousYearEntity
    }

    TestDateTimeEntity createPreviousMinuteEntity() {
        TestDateTimeEntity previousYearEntity = createCurrentMomentEntity()
        previousYearEntity.localDateTime = previousYearEntity.localDateTime.minusMinutes(1)
        previousYearEntity.offsetDateTime = previousYearEntity.offsetDateTime.minusMinutes(1)
        previousYearEntity.nowDate = new Date(previousYearEntity.nowDate.getTime() - TimeUnit.MINUTES.toMillis(1))
        return previousYearEntity
    }

    def "working with Entity manager"() {
        when:
        def e = entityManager.createQuery("select e from test_TestDateTimeEntity e where e.localDate>FIRST_DAY_OF_CURRENT_YEAR", TestDateTimeEntity.class).getResultList();

        then:
        !e.isEmpty()
    }

    def "replacing constant"() {
        when:
        def loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate>FIRST_DAY_OF_CURRENT_YEAR")
        def e = dataManager.loadList(loadContext)

        then:
        !e.isEmpty()

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where FIRST_DAY_OF_CURRENT_YEAR<e.localDate")
        e = dataManager.loadList(loadContext)

        then:
        !e.isEmpty()
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_YEAR and LAST_DAY_OF_CURRENT_YEAR with java.util.Date type"() {
        when:
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate > FIRST_DAY_OF_CURRENT_YEAR and e.nowDate < LAST_DAY_OF_CURRENT_YEAR")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate < FIRST_DAY_OF_CURRENT_YEAR or e.nowDate > LAST_DAY_OF_CURRENT_YEAR")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_MONTH and LAST_DAY_OF_CURRENT_MONTH with java.util.Date type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate >= FIRST_DAY_OF_CURRENT_MONTH and e.nowDate <= LAST_DAY_OF_CURRENT_MONTH")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousMonthEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate < FIRST_DAY_OF_CURRENT_MONTH or e.nowDate > LAST_DAY_OF_CURRENT_MONTH")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_WEEK and LAST_DAY_OF_CURRENT_WEEK with java.util.Date type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate > FIRST_DAY_OF_CURRENT_WEEK and e.nowDate < LAST_DAY_OF_CURRENT_WEEK")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousWeekEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate < FIRST_DAY_OF_CURRENT_WEEK or e.nowDate > LAST_DAY_OF_CURRENT_WEEK")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_DAY and END_OF_CURRENT_DAY with java.util.Date type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate > START_OF_CURRENT_DAY and e.nowDate < END_OF_CURRENT_DAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousDayEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate < START_OF_CURRENT_DAY or e.nowDate > END_OF_CURRENT_DAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_TOMORROW and START_OF_YESTERDAY with java.util.Date type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate < START_OF_TOMORROW and e.nowDate > START_OF_YESTERDAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        e.contains(previousDayEntity)
        !e.contains(previousWeekEntity)
        !e.contains(previousMonthEntity)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate > START_OF_TOMORROW or e.nowDate < START_OF_YESTERDAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        !e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)

    }

    def "interaction of constants START_OF_CURRENT_HOUR and END_OF_CURRENT_HOUR with java.util.Date type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate > START_OF_CURRENT_HOUR and e.nowDate < END_OF_CURRENT_HOUR")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousHourEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate < START_OF_CURRENT_HOUR or e.nowDate > END_OF_CURRENT_HOUR")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousHourEntity)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_MINUTE and END_OF_CURRENT_MINUTE with java.util.Date type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate > START_OF_CURRENT_MINUTE and e.nowDate < END_OF_CURRENT_MINUTE")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousMinuteEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.nowDate < START_OF_CURRENT_MINUTE or e.nowDate > END_OF_CURRENT_MINUTE")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousMinuteEntity)
        e.contains(previousHourEntity)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_YEAR and LAST_DAY_OF_CURRENT_YEAR with java.time.LocalDate type"() {
        when:
        def loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate >= FIRST_DAY_OF_CURRENT_YEAR and e.localDate <= LAST_DAY_OF_CURRENT_YEAR")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate < FIRST_DAY_OF_CURRENT_YEAR or e.localDate > LAST_DAY_OF_CURRENT_YEAR")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_MONTH and LAST_DAY_OF_CURRENT_MONTH with java.time.LocalDate type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate >= FIRST_DAY_OF_CURRENT_MONTH and e.localDate <= LAST_DAY_OF_CURRENT_MONTH")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousMonthEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate < FIRST_DAY_OF_CURRENT_MONTH or e.localDate > LAST_DAY_OF_CURRENT_MONTH")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_WEEK and LAST_DAY_OF_CURRENT_WEEK with java.time.LocalDate type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate >= FIRST_DAY_OF_CURRENT_WEEK and e.localDate <= LAST_DAY_OF_CURRENT_WEEK")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousWeekEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate < FIRST_DAY_OF_CURRENT_WEEK or e.localDate > LAST_DAY_OF_CURRENT_WEEK")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_DAY and END_OF_CURRENT_DAY with java.time.LocalDate type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate >= START_OF_CURRENT_DAY and e.localDate <= END_OF_CURRENT_DAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousDayEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate < START_OF_CURRENT_DAY or e.localDate > END_OF_CURRENT_DAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_TOMORROW and START_OF_YESTERDAY with java.time.LocalDate type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate < START_OF_TOMORROW and e.localDate >= START_OF_YESTERDAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        e.contains(previousDayEntity)
        !e.contains(previousWeekEntity)
        !e.contains(previousMonthEntity)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDate > START_OF_TOMORROW or e.localDate < START_OF_YESTERDAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        !e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_YEAR and LAST_DAY_OF_CURRENT_YEAR with java.time.LocalDateTime type"() {
        when:
        def loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime > FIRST_DAY_OF_CURRENT_YEAR and e.localDateTime < LAST_DAY_OF_CURRENT_YEAR")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime < FIRST_DAY_OF_CURRENT_YEAR or e.localDateTime > LAST_DAY_OF_CURRENT_YEAR")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_MONTH and LAST_DAY_OF_CURRENT_MONTH with java.time.LocalDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime >= FIRST_DAY_OF_CURRENT_MONTH and e.localDateTime <= LAST_DAY_OF_CURRENT_MONTH")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousMonthEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime < FIRST_DAY_OF_CURRENT_MONTH or e.localDateTime > LAST_DAY_OF_CURRENT_MONTH")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_WEEK and LAST_DAY_OF_CURRENT_WEEK with java.time.LocalDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime > FIRST_DAY_OF_CURRENT_WEEK and e.localDateTime < LAST_DAY_OF_CURRENT_WEEK")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousWeekEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime < FIRST_DAY_OF_CURRENT_WEEK or e.localDateTime > LAST_DAY_OF_CURRENT_WEEK")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_DAY and END_OF_CURRENT_DAY with java.time.LocalDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime > START_OF_CURRENT_DAY and e.localDateTime < END_OF_CURRENT_DAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousDayEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime < START_OF_CURRENT_DAY or e.localDateTime > END_OF_CURRENT_DAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_TOMORROW and START_OF_YESTERDAY with java.time.LocalDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime < START_OF_TOMORROW and e.localDateTime > START_OF_YESTERDAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        e.contains(previousDayEntity)
        !e.contains(previousWeekEntity)
        !e.contains(previousMonthEntity)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime > START_OF_TOMORROW or e.localDateTime < START_OF_YESTERDAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        !e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_HOUR and END_OF_CURRENT_HOUR with java.time.LocalDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime > START_OF_CURRENT_HOUR and e.localDateTime < END_OF_CURRENT_HOUR")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousHourEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime < START_OF_CURRENT_HOUR or e.localDateTime > END_OF_CURRENT_HOUR")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousHourEntity)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_MINUTE and END_OF_CURRENT_MINUTE with java.time.LocalDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime > START_OF_CURRENT_MINUTE and e.localDateTime < END_OF_CURRENT_MINUTE")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousMinuteEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.localDateTime < START_OF_CURRENT_MINUTE or e.localDateTime > END_OF_CURRENT_MINUTE")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousMinuteEntity)
        e.contains(previousHourEntity)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)

    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_YEAR and LAST_DAY_OF_CURRENT_YEAR with java.time.OffsetDateTime type"() {
        when:
        def loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime > FIRST_DAY_OF_CURRENT_YEAR and e.offsetDateTime < LAST_DAY_OF_CURRENT_YEAR")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime < FIRST_DAY_OF_CURRENT_YEAR or e.offsetDateTime > LAST_DAY_OF_CURRENT_YEAR")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_MONTH and LAST_DAY_OF_CURRENT_MONTH with java.time.OffsetDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime >= FIRST_DAY_OF_CURRENT_MONTH and e.offsetDateTime <= LAST_DAY_OF_CURRENT_MONTH")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousMonthEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime < FIRST_DAY_OF_CURRENT_MONTH or e.offsetDateTime > LAST_DAY_OF_CURRENT_MONTH")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants FIRST_DAY_OF_CURRENT_WEEK and LAST_DAY_OF_CURRENT_WEEK with java.time.OffsetDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime > FIRST_DAY_OF_CURRENT_WEEK and e.offsetDateTime < LAST_DAY_OF_CURRENT_WEEK")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousWeekEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime < FIRST_DAY_OF_CURRENT_WEEK or e.offsetDateTime > LAST_DAY_OF_CURRENT_WEEK")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_DAY and END_OF_CURRENT_DAY with java.time.OffsetDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime > START_OF_CURRENT_DAY and e.offsetDateTime < END_OF_CURRENT_DAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousDayEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime < START_OF_CURRENT_DAY or e.offsetDateTime > END_OF_CURRENT_DAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_TOMORROW and START_OF_YESTERDAY with java.time.OffsetDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime < START_OF_TOMORROW and e.offsetDateTime > START_OF_YESTERDAY")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        e.contains(previousDayEntity)
        !e.contains(previousWeekEntity)
        !e.contains(previousMonthEntity)
        !e.contains(previousYearEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime > START_OF_TOMORROW or e.offsetDateTime < START_OF_YESTERDAY")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        !e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_HOUR and END_OF_CURRENT_HOUR with java.time.OffsetDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime > START_OF_CURRENT_HOUR and e.offsetDateTime < END_OF_CURRENT_HOUR")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousHourEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime < START_OF_CURRENT_HOUR or e.offsetDateTime > END_OF_CURRENT_HOUR")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousHourEntity)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)
    }

    def "interaction of constants START_OF_CURRENT_MINUTE and END_OF_CURRENT_MINUTE with java.time.OffsetDateTime type"() {
        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime > START_OF_CURRENT_MINUTE and e.offsetDateTime < END_OF_CURRENT_MINUTE")
        def e = dataManager.loadList(loadContext)

        then:
        e.contains(now)
        !e.contains(previousMinuteEntity)

        when:
        loadContext = new LoadContext(metadata.getClass(TestDateTimeEntity.class))
        loadContext.setQueryString("select e from test_TestDateTimeEntity e where e.offsetDateTime < START_OF_CURRENT_MINUTE or e.offsetDateTime > END_OF_CURRENT_MINUTE")
        e = dataManager.loadList(loadContext)

        then:
        !e.contains(now)
        e.contains(previousMinuteEntity)
        e.contains(previousHourEntity)
        e.contains(previousDayEntity)
        e.contains(previousWeekEntity)
        e.contains(previousMonthEntity)
        e.contains(previousYearEntity)

    }


}
