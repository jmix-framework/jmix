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

package spec.haulmont.cuba.web.components.datefield

import io.jmix.core.metamodel.annotations.JavaClass
import io.jmix.core.metamodel.datatypes.Datatype
import io.jmix.core.metamodel.datatypes.impl.DateDatatype
import io.jmix.core.metamodel.datatypes.impl.DateTimeDatatype
import io.jmix.core.metamodel.datatypes.impl.LocalDateDatatype
import io.jmix.core.metamodel.datatypes.impl.LocalDateTimeDatatype
import io.jmix.core.metamodel.datatypes.impl.OffsetDateTimeDatatype
import io.jmix.ui.components.DateField
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.datefield.screens.DateFieldDatatypeScreen
import spock.lang.Ignore

@SuppressWarnings("GroovyAssignabilityCheck")
class DateFieldDatatypeTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.datefield.screens', 'com.haulmont.cuba.web.app.main'])
    }

    @Ignore
    def "datatype is applied from the screen descriptor"(String id, Class<Datatype> datatypeClass) {
        showMainScreen()

        def datatypesScreen = screens.create(DateFieldDatatypeScreen)
        datatypesScreen.show()

        when:

        DateField dateField = (DateField) datatypesScreen.getWindow().getComponentNN(id)

        then:

        dateField.getDatatype().getClass() == datatypeClass
        dateField.getValue().getClass() == datatypeClass.getAnnotation(JavaClass).value()

        where:

        id                    | datatypeClass
        "dateField"           | DateDatatype
        "dateTimeField"       | DateTimeDatatype
        "localDateField"      | LocalDateDatatype
        "localDateTimeField"  | LocalDateTimeDatatype
        "offsetDateTimeField" | OffsetDateTimeDatatype
    }
}