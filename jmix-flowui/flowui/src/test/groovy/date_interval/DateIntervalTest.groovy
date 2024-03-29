/*
 * Copyright 2024 Haulmont.
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

import date_interval.view.DateIntervalKeyValueTestView
import date_interval.view.DateIntervalTestView
import io.jmix.core.DataManager
import io.jmix.core.TimeSource
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval
import io.jmix.flowui.app.propertyfilter.dateinterval.model.DateInterval
import io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined.TodayPredefinedDateInterval
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

import java.time.LocalDate

@SpringBootTest
class DateIntervalTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager
    @Autowired
    TimeSource timeSource
    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    TodayPredefinedDateInterval todayPredefinedDateInterval

    @Override
    void setup() {
        registerViewBasePackages "date_interval.view"

        def currentDate = LocalDate.now()
        def lastDate = currentDate.minusDays 1
        def nextDate = currentDate.plusDays 2

        def item1 = dataManager.create Order
        item1.date = currentDate

        def item2 = dataManager.create Order
        item2.date = lastDate

        def item3 = dataManager.create Order
        item3.date = nextDate

        dataManager.save item1, item2, item3
    }

    @Override
    void cleanup() {
        jdbcTemplate.update "delete from TEST_ORDER"
    }

    def "DateInterval with LAST and NEXT types"() {
        when: "Open the DateIntervalTestView with a propertyFilter"
        def view = navigateToView DateIntervalTestView
        PropertyFilter<? super DateInterval> propertyFilter = view.dateFilter

        then: "All items will be loaded"
        view.items.size() == 3

        when: "Apply filter with 'LAST 2 DAY' value"
        DateInterval last2DayInterval = new DateInterval(BaseDateInterval.Type.LAST, 2, DateInterval.TimeUnit.DAY, false)

        propertyFilter.valueComponent.value = last2DayInterval
        propertyFilter.apply()

        then: "Should be loaded only one item"
        view.items.size() == 1

        when: "Apply filter with 'NEXT 2 DAY' value"
        DateInterval next2Day = new DateInterval(BaseDateInterval.Type.NEXT, 2, DateInterval.TimeUnit.DAY, false)

        propertyFilter.valueComponent.value = next2Day
        propertyFilter.apply()

        then: "Should be loaded only one item"
        view.items.size() == 1
    }

    def "DateInterval with PREDEFINED type"() {
        when: "Open the DateIntervalTestView with a propertyFilter"
        def view = navigateToView DateIntervalTestView
        PropertyFilter<? super DateInterval> propertyFilter = view.dateFilter

        then: "All items will be loaded"
        view.items.size() == 3

        when: "Apply filter with 'Today' predefined interval"
        propertyFilter.valueComponent.value = todayPredefinedDateInterval
        propertyFilter.apply()

        then: "Should be loaded only one item"
        view.items.size() == 1
    }

    def "DateInterval with LAST and NEXT type and KeyValueCollection"() {
        when: "Open the DateIntervalTestView with a propertyFilter"
        def view = navigateToView DateIntervalKeyValueTestView
        PropertyFilter<? super DateInterval> propertyFilter = view.dateFilter

        then:
        view.items.size() == 3

        when: "Apply filter with 'LAST 2 DAY' value"
        propertyFilter.valueComponent.value = new DateInterval(BaseDateInterval.Type.LAST, 2, DateInterval.TimeUnit.DAY, false)
        propertyFilter.apply()

        then: "Should be loaded only one item"
        view.items.size() == 1

        when: "Apply filter with 'NEXT 2 DAY' value"
        propertyFilter.valueComponent.value = new DateInterval(BaseDateInterval.Type.NEXT, 2, DateInterval.TimeUnit.DAY, false)
        propertyFilter.apply()

        then: "Should be loaded only one item"
        view.items.size() == 1
    }

    def "DateInterval with PREDEFINED type and KeyValueCollection"() {
        when: "Open the DateIntervalTestView with a propertyFilter"
        def view = navigateToView DateIntervalKeyValueTestView
        PropertyFilter<? super DateInterval> propertyFilter = view.dateFilter

        then: "All items will be loaded"
        view.items.size() == 3

        when: "Apply filter with 'Today' predefined interval"
        propertyFilter.valueComponent.value = todayPredefinedDateInterval
        propertyFilter.apply()

        then: "Should be loaded only one item"
        view.items.size() == 1
    }
}
