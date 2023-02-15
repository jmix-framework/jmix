/*
 * Copyright (c) 2020 Haulmont.
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

package bean_validation

import bean_validation.screen.DateValidationTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DateTimeTransformations
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.DatePicker
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

import java.time.*

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DatePickerRangeTest extends ScreenSpecification {

    @Autowired
    DateTimeTransformations dateTimeTransformations

    @Override
    void setup() {
        exportScreensPackages(['bean_validation'])
    }

    def "futureDatePicker and futureOrPresentDatePicker range test"() {
        when:
        showTestMainScreen()

        def dateValidationScreen = screens.create(DateValidationTestScreen)
        dateValidationScreen.show()

        def futureDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("futureDatePicker")
        def futureOrPresentDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("futureOrPresentDatePicker")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, futureDatePicker.getValueSource().getType())

        then:
        futureDatePicker.getRangeStart() == rangeStart
        futureOrPresentDatePicker.getRangeStart() == rangeStart
        futureDatePicker.getRangeEnd() == null
        futureOrPresentDatePicker.getRangeEnd() == null
    }


    def "pastDatePicker and pasteOrPresentDatePicker range test"() {
        when:
        showTestMainScreen()

        def dateValidationScreen = screens.create(DateValidationTestScreen)
        dateValidationScreen.show()

        def pastDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("pastDatePicker")
        def pastOrPresentDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("pastOrPresentDatePicker")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeEnd = dateTimeTransformations.transformFromZDT(zonedDateTime, pastDatePicker.getValueSource().getType())

        then:
        pastDatePicker.getRangeStart() == null
        pastOrPresentDatePicker.getRangeStart() == null
        pastDatePicker.getRangeEnd() == rangeEnd
        pastOrPresentDatePicker.getRangeEnd() == rangeEnd
    }

    def "specificFutureDatePicker range test"() {
        when:
        showTestMainScreen()

        def dateValidationScreen = screens.create(DateValidationTestScreen)
        dateValidationScreen.show()

        def specificFutureDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("specificFutureDatePicker")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        dateTime = dateTime.plusDays(1)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, specificFutureDatePicker.getValueSource().getType())

        then:
        specificFutureDatePicker.getRangeStart() == rangeStart
        specificFutureDatePicker.getRangeEnd() == null
    }
}
