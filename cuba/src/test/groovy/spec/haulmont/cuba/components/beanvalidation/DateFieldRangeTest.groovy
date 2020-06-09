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
import io.jmix.ui.component.DateField
import spec.haulmont.cuba.components.beanvalidation.screens.DateValidationScreen
import spec.haulmont.cuba.web.UiScreenSpec
import spock.lang.Ignore

import org.springframework.beans.factory.annotation.Autowired
import java.time.*

class DateFieldRangeTest extends UiScreenSpec {

    @Autowired
    DateTimeTransformations dateTimeTransformations

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.components.beanvalidation.screens'])
    }

    def cleanup() {
        resetScreensConfig()
    }

    def "futureDateField and futureOrPresentDateField range test"() {

        given:
        showMainScreen()

        def dateValidationScreen = screens.create(DateValidationScreen)
        dateValidationScreen.show()

        def futureDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("futureDateField")
        def futureOrPresentDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("futureOrPresentDateField")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, futureDateField.getValueSource().getType())

        expect:
        futureDateField.getRangeStart() == rangeStart
        futureOrPresentDateField.getRangeStart() == rangeStart
        futureDateField.getRangeEnd() == null
        futureOrPresentDateField.getRangeEnd() == null
    }

    def "pastDateField and pastOrPresentDateField range test"() {
        given:
        showMainScreen()

        def dateValidationScreen = screens.create(DateValidationScreen)
        dateValidationScreen.show()

        def pastDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("pastDateField")
        def pastOrPresentDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("pastOrPresentDateField")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeEnd = dateTimeTransformations.transformFromZDT(zonedDateTime, pastDateField.getValueSource().getType())

        expect:
        pastDateField.getRangeStart() == null
        pastOrPresentDateField.getRangeStart() == null
        pastDateField.getRangeEnd() == rangeEnd
        pastOrPresentDateField.getRangeEnd() == rangeEnd
    }

    def "specificFutureDateField range test"() {
        given:
        showMainScreen()

        def dateValidationScreen = screens.create(DateValidationScreen)
        dateValidationScreen.show()

        def specificFutureDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("specificFutureDateField")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        dateTime = dateTime.plusDays(1)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, specificFutureDateField.getValueSource().getType())

        expect:
        specificFutureDateField.getRangeStart() == rangeStart
        specificFutureDateField.getRangeEnd() == null
    }
}
