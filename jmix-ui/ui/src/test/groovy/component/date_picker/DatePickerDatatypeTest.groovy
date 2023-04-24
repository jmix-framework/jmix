/*
 * Copyright 2020 Haulmont.
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

package component.date_picker

import component.date_picker.screen.DatePickerDatatypeTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.metamodel.annotation.DatatypeDef
import io.jmix.core.metamodel.datatype.Datatype
import io.jmix.core.metamodel.datatype.impl.*
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.DatePicker
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DatePickerDatatypeTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.date_picker"])
    }

    def "datatype is applied from the screen descriptor"(String id, Class<Datatype> datatypeClass) {
        showTestMainScreen()

        def datatypesScreen = screens.create(DatePickerDatatypeTestScreen)
        datatypesScreen.show()

        when:

        DatePicker datePicker = (DatePicker) datatypesScreen.getWindow().getComponentNN(id)

        then:

        datePicker.getDatatype().getClass() == datatypeClass
        datePicker.getValue().getClass() == datatypeClass.getAnnotation(DatatypeDef).javaClass()

        where:

        id                     | datatypeClass
        "datePicker"           | DateDatatype
        "dateTimePicker"       | DateTimeDatatype
        "localDatePicker"      | LocalDateDatatype
        "localDateTimePicker"  | LocalDateTimeDatatype
        "offsetDateTimePicker" | OffsetDateTimeDatatype
    }
}