/*
 * Copyright 2026 Haulmont.
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

package component.datepicker

import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.datepicker.TypedDatePicker
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DatePickerI18nTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    def "DatePicker takes the calendar overlay name from messages"() {
        when: "A DatePicker is created"
        def datePicker = uiComponents.create(TypedDatePicker)

        then: "The calendar overlay name is the localized default"
        datePicker.i18n.dialogAccessibleName == "Calendar"
    }

    def "DateTimePicker takes the calendar overlay name from messages"() {
        when: "A DateTimePicker is created"
        def dateTimePicker = uiComponents.create(TypedDateTimePicker)

        then: "The calendar overlay name is the localized default"
        dateTimePicker.datePickerI18n.dialogAccessibleName == "Calendar"
    }
}
