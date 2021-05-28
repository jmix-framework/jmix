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

package uitest.date_interval

import io.jmix.core.DataManager
import io.jmix.ui.app.propertyfilter.dateinterval.model.RelativeDateInterval
import org.apache.commons.lang3.time.DateUtils
import org.springframework.beans.factory.annotation.Autowired
import test_support.UiDataTestSpecification
import test_support.entity.Project
import uitest.date_interval.screen.RelativeDateIntervalTestScreen

import static io.jmix.ui.app.propertyfilter.dateinterval.model.RelativeDateInterval.Operation.GREATER_OR_EQUAL

class RelativeDateIntervalTest extends UiDataTestSpecification {

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        exportScreensPackages(["uitest.date_interval.screen"])

        def currentDate = new Date()
        def nextDate = DateUtils.addMonths(currentDate, -1)

        def project1 = dataManager.create(Project)
        project1.setStartDate(currentDate)

        def project2 = dataManager.create(Project)
        project2.setStartDate(nextDate)

        dataManager.save(project1, project2)
    }

    @Override
    void cleanup() {
        jdbcTemplate.update("delete from TEST_UIDATA_PROJECT")
    }

    def "RelativeDateInterval test"() {
        showTestMainScreen()

        when: "Open screen and load data"
        def screen = (RelativeDateIntervalTestScreen) screens.create(RelativeDateIntervalTestScreen)
        screen.show()

        then:
        screen.items.size() == 2

        when: "Apply filter with '> START_OF_YESTERDAY'"
        screen.dateFilter.setValue(new RelativeDateInterval(GREATER_OR_EQUAL, "START_OF_YESTERDAY"))
        screen.dateFilter.apply()

        then: "Should be loaded only one item"
        screen.items.size() == 1
    }
}
