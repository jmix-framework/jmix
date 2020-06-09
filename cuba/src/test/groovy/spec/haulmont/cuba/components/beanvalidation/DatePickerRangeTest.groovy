/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.components.beanvalidation

import io.jmix.core.DateTimeTransformations
import io.jmix.ui.component.DatePicker
import spec.haulmont.cuba.components.beanvalidation.screens.DateValidationScreen
import spec.haulmont.cuba.web.UiScreenSpec
import spock.lang.Ignore

import org.springframework.beans.factory.annotation.Autowired
import java.time.*

class DatePickerRangeTest extends UiScreenSpec {

    @Autowired
    DateTimeTransformations dateTimeTransformations

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.components.beanvalidation.screens'])
    }

    def cleanup() {
        resetScreensConfig()
    }

    def "futureDatePicker and futureOrPresentDatePicker range test"() {
        given:
        showMainScreen()

        def dateValidationScreen = screens.create(DateValidationScreen)
        dateValidationScreen.show()

        def futureDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("futureDatePicker")
        def futureOrPresentDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("futureOrPresentDatePicker")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, futureDatePicker.getValueSource().getType())

        expect:
        futureDatePicker.getRangeStart() == rangeStart
        futureOrPresentDatePicker.getRangeStart() == rangeStart
        futureDatePicker.getRangeEnd() == null
        futureOrPresentDatePicker.getRangeEnd() == null
    }


    def "pastDatePicker and pasteOrPresentDatePicker range test"() {
        given:
        showMainScreen()

        def dateValidationScreen = screens.create(DateValidationScreen)
        dateValidationScreen.show()

        def pastDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("pastDatePicker")
        def pastOrPresentDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("pastOrPresentDatePicker")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeEnd = dateTimeTransformations.transformFromZDT(zonedDateTime, pastDatePicker.getValueSource().getType())

        expect:
        pastDatePicker.getRangeStart() == null
        pastOrPresentDatePicker.getRangeStart() == null
        pastDatePicker.getRangeEnd() == rangeEnd
        pastOrPresentDatePicker.getRangeEnd() == rangeEnd
    }

    def "specificFutureDatePicker range test"() {
        given:
        showMainScreen()

        def dateValidationScreen = screens.create(DateValidationScreen)
        dateValidationScreen.show()

        def specificFutureDatePicker = (DatePicker<Date>) dateValidationScreen.getWindow().getComponent("specificFutureDatePicker")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        dateTime = dateTime.plusDays(1)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, specificFutureDatePicker.getValueSource().getType())

        expect:
        specificFutureDatePicker.getRangeStart() == rangeStart
        specificFutureDatePicker.getRangeEnd() == null
    }
}
