/*
 * Copyright 2022 Haulmont.
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

package formatter

import formatter.screen.FormatterView
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.FormatStringsRegistry
import io.jmix.core.security.CurrentAuthentication
import io.jmix.flowui.component.formatter.DateFormatter
import io.jmix.flowui.component.formatter.NumberFormatter
import io.jmix.flowui.kit.component.valuepicker.ValuePicker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import java.text.DecimalFormat
import java.text.SimpleDateFormat

@SpringBootTest
class FormattersTest extends FlowuiTestSpecification {

    @Autowired
    CurrentAuthentication currentAuthentication

    @Autowired
    FormatStringsRegistry formatStringsRegistry

    @Autowired
    DatatypeRegistry datatypeRegistry

    @Override
    void setup() {
        registerScreenBasePackages("formatter")
    }

    def "Formatter from XML is applied for field"() {
        when: "Open the screen"
        def formatterView = openScreen(FormatterView.class)

        then: "Formatter is applied for the field"
        noExceptionThrown()
        ((ValuePicker<?>) formatterView."$id").getFormatter().getClass() == formatterClass

        where:
        id                            | formatterClass
        "dateFormatterField"          | DateFormatter
        "dateTimeFormatterField"      | DateFormatter
        "numberFormatterField"        | NumberFormatter
        "defaultNumberFormatterField" | NumberFormatter
    }

    def "DateFormatter is applied for field using the XML attribute 'class'"() {
        given: "The screen with a component"
        def formatterView = openScreen(FormatterView.class)

        when: "Component is loaded"
        def dateFormatterField = formatterView.dateFormatterField
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd")

        then: "DateFormatter is applied for field"
        noExceptionThrown()
        dateFormatterField.element.getProperty("value") == dateFormat.format(dateFormatterField.value)
    }

    def "DateFormatter is applied for field using the XML attribute 'type'"() {
        given: "The screen with a component"
        def formatterView = openScreen(FormatterView.class)

        when: "Component is loaded"
        def dateTimeFormatterField = formatterView.dateTimeFormatterField
        def dateTimeFormat = new SimpleDateFormat(
                formatStringsRegistry.getFormatStrings(currentAuthentication.locale).dateTimeFormat)

        then: "DateFormatter with type attribute is applied for field"
        noExceptionThrown()
        dateTimeFormatterField.element.getProperty("value") == dateTimeFormat.format(dateTimeFormatterField.value)
    }

    def "NumberFormatter is applied for field using the XML attribute 'class'"() {
        given: "The screen with a component"
        def formatterView = openScreen(FormatterView.class)

        when: "Component is loaded"
        def numberFormatterField = formatterView.numberFormatterField

        def decimalFormat = new DecimalFormat("#,###",
                formatStringsRegistry.getFormatStrings(currentAuthentication.locale).formatSymbols)

        then: "NumberFormatter is applied for field"
        noExceptionThrown()
        numberFormatterField.element.getProperty("value") == decimalFormat.format(numberFormatterField.value)
    }

    def "NumberFormatter without format attribute is applied for field using beanLocator"() {
        given: "The screen with a component"
        def formatterView = openScreen(FormatterView.class)

        when: "Component is loaded"
        def defaultNumberFormatterField = formatterView.defaultNumberFormatterField
        def datatype = datatypeRegistry.get(Long.class)

        then: "NumberFormatter without format attribute for field"
        noExceptionThrown()
        defaultNumberFormatterField.element.getProperty("value") ==
                datatype.format(defaultNumberFormatterField.value, currentAuthentication.locale)
    }
}
