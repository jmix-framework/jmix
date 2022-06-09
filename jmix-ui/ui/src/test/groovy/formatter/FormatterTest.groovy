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

package formatter

import formatter.screen.DateFormatterTestScreen
import formatter.screen.FormatterTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.FormatStringsRegistry
import io.jmix.core.security.CurrentAuthentication
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Label
import io.jmix.ui.component.formatter.DateFormatter
import io.jmix.ui.component.formatter.NumberFormatter
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.TestDateEntity

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class FormatterTest extends ScreenSpecification {

    @Autowired
    CurrentAuthentication currentAuthentication

    @Autowired
    FormatStringsRegistry formatStringsRegistry

    @Autowired
    DatatypeRegistry datatypeRegistry

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        exportScreensPackages(['formatter'])

        def dateEntity = dataManager.create(TestDateEntity)
        dateEntity.setDate(new Date())
        dateEntity.setDateTime(new Date())
        dateEntity.setLocalDate(LocalDate.now())
        dateEntity.setLocalDateTime(LocalDateTime.now())
        dateEntity.setOffsetDateTime(OffsetDateTime.now())

        dataManager.save(dateEntity)
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("delete from TEST_DATE_ENTITY");
    }

    def "Formatter is applied for field"(String id, Class<io.jmix.ui.component.formatter.Formatter> formatterClass) {
        showTestMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormatterTestScreen)
        formattersScreen.show()

        then: "Formatter is applied for the field"

        noExceptionThrown()
        ((Label) formattersScreen.getWindow().getComponent(id)).getFormatter().getClass() == formatterClass

        where:

        id                            | formatterClass
        "dateFormatterField"          | DateFormatter
        "dateTimeFormatterField"      | DateFormatter
        "numberFormatterField"        | NumberFormatter
        "defaultNumberFormatterField" | NumberFormatter
    }

    def "DateFormatter is applied for field using the XML attribute 'class'"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormatterTestScreen)
        formattersScreen.show()

        def dateFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("dateFormatterField")

        def dateFormat = new SimpleDateFormat("yyyy-MM-dd")

        then: "DateFormatter is applied for field"

        noExceptionThrown()
        dateFormatterField.getRawValue() == dateFormat.format(dateFormatterField.getValue())
    }

    def "DateFormatter is applied for field using the XML attribute 'name'"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormatterTestScreen)
        formattersScreen.show()

        def dateTimeFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("dateTimeFormatterField")

        def dateTimeFormat = new SimpleDateFormat(
                formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateTimeFormat())

        then: "DateFormatter with type attribute is applied for field"

        noExceptionThrown()
        dateTimeFormatterField.getRawValue() == dateTimeFormat.format(dateTimeFormatterField.getValue())
    }

    def "DateFormatter is applied for different date types"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def screen = screens.create(DateFormatterTestScreen)
        screen.show()

        then: "DateFormatter is applied for different date types"

        noExceptionThrown()
        !screen.datesDc.getItems().isEmpty()
    }

    def "NumberFormatter is applied for field using the XML attribute 'class'"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormatterTestScreen)
        formattersScreen.show()

        def numberFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("numberFormatterField")

        def decimalFormat = new DecimalFormat("#,###",
                formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getFormatSymbols())

        then: "NumberFormatter is applied for field"

        noExceptionThrown()
        numberFormatterField.getRawValue() == decimalFormat.format(numberFormatterField.getValue())
    }

    def "NumberFormatter without format attribute is applied for field using beanLocator"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def formattersScreen = screens.create(FormatterTestScreen)
        formattersScreen.show()

        def defaultNumberFormatterField = (Label<Date>) formattersScreen.getWindow().getComponent("defaultNumberFormatterField")

        def datatype = datatypeRegistry.get(Long.class)

        then: "NumberFormatter without format attribute for field"

        noExceptionThrown()
        defaultNumberFormatterField.getRawValue() == datatype.format(defaultNumberFormatterField.getValue(), currentAuthentication.getLocale())
    }
}
