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

package component.calendar

import component.calendar.screen.CalendarTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.metamodel.annotation.DatatypeDef
import io.jmix.core.metamodel.datatype.Datatype
import io.jmix.core.metamodel.datatype.impl.*
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Calendar
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class CalendarTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.calendar"])
    }

    def "Datatype is applied from the screen descriptor"(String id, Class<Datatype> datatypeClass) {
        showTestMainScreen()

        def calendarScreen = screens.create(CalendarTestScreen)
        calendarScreen.show()

        when:

        def calendar = calendarScreen.getWindow().getComponentNN(id) as Calendar

        then:

        noExceptionThrown()
        calendar.getDatatype().getClass() == datatypeClass
        calendar.getStartDate().getClass() == datatypeClass.getAnnotation(DatatypeDef).javaClass()
        calendar.getEndDate().getClass() == datatypeClass.getAnnotation(DatatypeDef).javaClass()

        where:

        id                       | datatypeClass
        "calendarDate"           | DateDatatype
        "calendarDefault"        | DateTimeDatatype
        "calendarDateTime"       | DateTimeDatatype
        "calendarLocalDate"      | LocalDateDatatype
        "calendarLocalDateTime"  | LocalDateTimeDatatype
        "calendarOffsetDateTime" | OffsetDateTimeDatatype
    }

    def "StartDateProperty and EndDateProperty values are propagated to Calendar from ValueSource"() {
        showTestMainScreen()

        def calendarScreen = screens.create(CalendarTestScreen)
        calendarScreen.show()

        def item = calendarScreen.ordersDc.getItems().get(0)
        def calendar = calendarScreen.getWindow().getComponentNN("calendarWithContainer") as Calendar

        when: 'StartDateProperty and EndDateProperty values are set to ValueSource'
        // StartDateProperty and EndDateProperty
        item.setDate(new Date())

        def events = calendar.getEventProvider().getEvents()

        then: 'Calendar contains events'
        !events.isEmpty()

        and: 'Calendar StartDateProperty is updated'
        item.getDate() == events.get(0).getStart()

        and: 'Calendar EndDateProperty is updated'
        item.getDate() == events.get(0).getEnd()
    }
}
