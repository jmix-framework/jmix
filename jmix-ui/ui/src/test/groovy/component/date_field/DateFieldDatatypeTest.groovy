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

package component.date_field

import component.date_field.screen.DateFieldDatatypeTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.metamodel.annotation.DatatypeDef
import io.jmix.core.metamodel.datatype.Datatype
import io.jmix.core.metamodel.datatype.impl.*
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.DateField
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DateFieldDatatypeTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.date_field"])
    }

    def "datatype is applied from the screen descriptor"(String id, Class<Datatype> datatypeClass) {
        showTestMainScreen()

        def datatypeScreen = screens.create(DateFieldDatatypeTestScreen)
        datatypeScreen.show()

        when:

        DateField dateField = (DateField) datatypeScreen.getWindow().getComponentNN(id)

        then:

        dateField.getDatatype().getClass() == datatypeClass
        dateField.getValue().getClass() == datatypeClass.getAnnotation(DatatypeDef).javaClass()

        where:

        id                    | datatypeClass
        "dateField"           | DateDatatype
        "dateTimeField"       | DateTimeDatatype
        "localDateField"      | LocalDateDatatype
        "localDateTimeField"  | LocalDateTimeDatatype
        "offsetDateTimeField" | OffsetDateTimeDatatype
    }
}
