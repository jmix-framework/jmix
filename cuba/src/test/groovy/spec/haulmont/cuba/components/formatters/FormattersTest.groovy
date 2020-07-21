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

package spec.haulmont.cuba.components.formatters


import com.haulmont.chile.core.datatypes.Datatypes
import io.jmix.core.security.CurrentAuthentication
import io.jmix.ui.component.Label
import io.jmix.ui.component.formatter.DateFormatter
import io.jmix.ui.component.formatter.NumberFormatter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spec.haulmont.cuba.components.formatters.screens.FormattersScreen
import spec.haulmont.cuba.web.UiScreenSpec

import java.text.DecimalFormat
import java.text.SimpleDateFormat

// todo: move to jmix-ui module
class FormattersTest extends UiScreenSpec {

    @Autowired
    CurrentAuthentication currentAuthentication

    @Autowired
    ApplicationContext applicationContext

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.components.formatters.screens'])
    }

    def "Formatter is applied for field"(String id, Class<io.jmix.ui.component.formatter.Formatter> formatterClass) {
        showMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormattersScreen)
        formattersScreen.show()

        then: "Formatter is applied for the field"

        noExceptionThrown()
        ((Label) formattersScreen.getWindow().getComponent(id)).getFormatter().getClass() == formatterClass

        where:

        id                            | formatterClass
        "dateFormatterField"          | DateFormatter.class
        "dateTimeFormatterField"      | DateFormatter.class
        "numberFormatterField"        | NumberFormatter.class
        "defaultNumberFormatterField" | NumberFormatter.class
    }

    def "DateFormatter is applied for field using the XML attribute 'class'"() {
        showMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormattersScreen)
        formattersScreen.show()

        def dateFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("dateFormatterField")

        def dateFormat = new SimpleDateFormat("yyyy-MM-dd")

        then: "DateFormatter is applied for field"

        noExceptionThrown()
        dateFormatterField.getRawValue() == dateFormat.format(dateFormatterField.getValue())
    }

    def "DateFormatter is applied for field using the XML attribute 'name'"() {
        showMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormattersScreen)
        formattersScreen.show()

        def dateTimeFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("dateTimeFormatterField")

        def dateTimeFormat = new SimpleDateFormat(Datatypes.getFormatStrings(currentAuthentication.getLocale()).getDateTimeFormat())

        then: "DateFormatter with type attribute is applied for field"

        noExceptionThrown()
        dateTimeFormatterField.getRawValue() == dateTimeFormat.format(dateTimeFormatterField.getValue())
    }

    def "NumberFormatter is applied for field using the XML attribute 'class'"() {
        showMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormattersScreen)
        formattersScreen.show()

        def numberFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("numberFormatterField")

        def decimalFormat = new DecimalFormat("#,###", Datatypes.getFormatStrings(currentAuthentication.getLocale()).getFormatSymbols())

        then: "NumberFormatter is applied for field"

        noExceptionThrown()
        numberFormatterField.getRawValue() == decimalFormat.format(numberFormatterField.getValue())
    }

    def "NumberFormatter without format attribute is applied for field using beanLocator"() {
        showMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormattersScreen)
        formattersScreen.show()

        def defaultNumberFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("defaultNumberFormatterField")

        def datatype = Datatypes.getNN(Long.class)

        then: "NumberFormatter without format attribute for field"

        noExceptionThrown()
        defaultNumberFormatterField.getRawValue() == datatype.format(defaultNumberFormatterField.getValue(), currentAuthentication.getLocale())
    }
}
