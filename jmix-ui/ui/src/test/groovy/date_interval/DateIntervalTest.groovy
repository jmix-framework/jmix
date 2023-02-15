/*
 * Copyright 2021 Haulmont.
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

package date_interval

import date_interval.screen.DateIntervalKeyValueTestScreen
import date_interval.screen.DateIntervalTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.TimeSource
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.app.propertyfilter.dateinterval.model.DateInterval
import io.jmix.ui.app.propertyfilter.dateinterval.model.predefined.TodayPredefinedDateInterval
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.apache.commons.lang3.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Order

import static io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval.Type.LAST
import static io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval.Type.NEXT
import static io.jmix.ui.app.propertyfilter.dateinterval.model.DateInterval.TimeUnit.DAY

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DateIntervalTest extends ScreenSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    TimeSource source

    @Autowired
    TodayPredefinedDateInterval todayDateInterval;

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        exportScreensPackages(["date_interval.screen"])

        def currentDate = new Date()
        def lastDate = DateUtils.addDays(currentDate, -1)
        def nextDate = DateUtils.addDays(currentDate, 2)

        def item1 = dataManager.create(Order)
        item1.setDate(currentDate)

        def item2 = dataManager.create(Order)
        item2.setDate(lastDate)

        def item3 = dataManager.create(Order)
        item3.setDate(nextDate)

        dataManager.save(item1, item2, item3)
    }

    @Override
    void cleanup() {
        jdbcTemplate.update("delete from TEST_ORDER")
    }

    def "DateInterval with LAST and NEXT types"() {
        showTestMainScreen()

        when: "Open screen and load data"
        def screen = (DateIntervalTestScreen) screens.create(DateIntervalTestScreen)
        screen.show()

        then:
        screen.items.size() == 3

        when: "Apply filter with 'LAST 2 DAY' value"
        screen.dateFilter.setValue(new DateInterval(LAST, 2, DAY, false))
        screen.dateFilter.apply()

        then: "Should be loaded only one item"
        screen.items.size() == 1

        when: "Apply filter with 'NEXT 2 DAY' value"
        screen.dateFilter.setValue(new DateInterval(NEXT, 2, DAY, false))
        screen.dateFilter.apply()

        then: "Should be loaded only one item"
        screen.items.size() == 1
    }

    def "DateInterval with PREDEFINED type"() {
        showTestMainScreen()

        when: "Open screen and load data"
        def screen = (DateIntervalTestScreen) screens.create(DateIntervalTestScreen)
        screen.show()

        then:
        screen.items.size() == 3

        when: "Apply filter with 'Today' predefined interval"
        screen.dateFilter.setValue(todayDateInterval)
        screen.dateFilter.apply()

        then: "Should be loaded only one item"
        screen.items.size() == 1
    }

    def "DateInterval NEXT, LAST with KeyValueCollection"() {
        showTestMainScreen()

        when: "Open screen and load data"
        def screen = (DateIntervalKeyValueTestScreen) screens.create(DateIntervalKeyValueTestScreen)
        screen.show()

        then:
        screen.items.size() == 3

        when: "Apply filter with 'LAST 2 DAY' value"
        screen.dateFilter.setValue(new DateInterval(LAST, 2, DAY, false))
        screen.dateFilter.apply()

        then: "Should be loaded only one item"
        screen.items.size() == 1

        when: "Apply filter with 'NEXT 2 DAY' value"
        screen.dateFilter.setValue(new DateInterval(NEXT, 2, DAY, false))
        screen.dateFilter.apply()

        then: "Should be loaded only one item"
        screen.items.size() == 1
    }

    def "DateInterval PREDEFINED with KeyValueCollection"() {
        showTestMainScreen()

        when: "Open screen and load data"
        def screen = (DateIntervalKeyValueTestScreen) screens.create(DateIntervalKeyValueTestScreen)
        screen.show()

        then:
        screen.items.size() == 3

        when: "Apply filter with 'Today' predefined interval"
        screen.dateFilter.setValue(todayDateInterval)
        screen.dateFilter.apply()

        then: "Should be loaded only one item"
        screen.items.size() == 1
    }
}
