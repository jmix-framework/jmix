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
import io.jmix.ui.component.DateField
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

import java.time.*

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DateFieldRangeTest extends ScreenSpecification {

    @Autowired
    DateTimeTransformations dateTimeTransformations

    @Override
    void setup() {
        exportScreensPackages(['bean_validation'])
    }

    def "futureDateField and futureOrPresentDateField range test"() {

        when:
        showTestMainScreen()

        def dateValidationScreen = screens.create(DateValidationTestScreen)
        dateValidationScreen.show()

        def futureDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("futureDateField")
        def futureOrPresentDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("futureOrPresentDateField")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, futureDateField.getValueSource().getType())

        then:
        futureDateField.getRangeStart() == rangeStart
        futureOrPresentDateField.getRangeStart() == rangeStart
        futureDateField.getRangeEnd() == null
        futureOrPresentDateField.getRangeEnd() == null
    }

    def "pastDateField and pastOrPresentDateField range test"() {
        when:
        showTestMainScreen()

        def dateValidationScreen = screens.create(DateValidationTestScreen)
        dateValidationScreen.show()

        def pastDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("pastDateField")
        def pastOrPresentDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("pastOrPresentDateField")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeEnd = dateTimeTransformations.transformFromZDT(zonedDateTime, pastDateField.getValueSource().getType())

        then:
        pastDateField.getRangeStart() == null
        pastOrPresentDateField.getRangeStart() == null
        pastDateField.getRangeEnd() == rangeEnd
        pastOrPresentDateField.getRangeEnd() == rangeEnd
    }

    def "specificFutureDateField range test"() {
        when:
        showTestMainScreen()

        def dateValidationScreen = screens.create(DateValidationTestScreen)
        dateValidationScreen.show()

        def specificFutureDateField = (DateField<Date>) dateValidationScreen.getWindow().getComponent("specificFutureDateField")

        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        dateTime = dateTime.plusDays(1)
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())
        def rangeStart = dateTimeTransformations.transformFromZDT(zonedDateTime, specificFutureDateField.getValueSource().getType())

        then:
        specificFutureDateField.getRangeStart() == rangeStart
        specificFutureDateField.getRangeEnd() == null
    }
}
