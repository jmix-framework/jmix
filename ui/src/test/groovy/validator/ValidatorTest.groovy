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

package validator

import io.jmix.core.CoreConfiguration
import io.jmix.core.TimeSource
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.datatype.impl.DateTimeDatatype
import io.jmix.core.metamodel.datatype.impl.LocalTimeDatatype
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.*
import io.jmix.ui.component.validation.*
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import validator.screen.ValidatorTestScreen

import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.Calendar

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ValidatorTest extends ScreenSpecification {

    @Autowired
    DatatypeRegistry datatypeRegistry

    @Autowired
    TimeSource timeSource

    @Autowired
    ObjectProvider<SizeValidator> sizeValidatorObjectProvider
    @Autowired
    ObjectProvider<RegexpValidator> regexpValidatorObjectProvider
    @Autowired
    ObjectProvider<PositiveValidator> positiveValidatorObjectProvider
    @Autowired
    ObjectProvider<PositiveOrZeroValidator> positiveOrZeroValidatorObjectProvider
    @Autowired
    ObjectProvider<PastValidator> pastValidatorObjectProvider
    @Autowired
    ObjectProvider<PastOrPresentValidator> pastOrPresentValidatorObjectProvider
    @Autowired
    ObjectProvider<NotNullValidator> notNullValidatorObjectProvider
    @Autowired
    ObjectProvider<NotEmptyValidator> notEmptyValidatorObjectProvider
    @Autowired
    ObjectProvider<NotBlankValidator> notBlankValidatorObjectProvider
    @Autowired
    ObjectProvider<NegativeValidator> negativeValidatorObjectProvider
    @Autowired
    ObjectProvider<NegativeOrZeroValidator> negativeOrZeroValidatorObjectProvider
    @Autowired
    ObjectProvider<MinValidator> minValidatorObjectProvider
    @Autowired
    ObjectProvider<MaxValidator> maxValidatorObjectProvider
    @Autowired
    ObjectProvider<FutureValidator> futureValidatorObjectProvider
    @Autowired
    ObjectProvider<FutureOrPresentValidator> futureOrPresentValidatorObjectProvider
    @Autowired
    ObjectProvider<DigitsValidator> digitsValidatorObjectProvider
    @Autowired
    ObjectProvider<DecimalMinValidator> decimalMinValidatorObjectProvider
    @Autowired
    ObjectProvider<DecimalMaxValidator> decimalMaxValidatorObjectProvider
    @Autowired
    ObjectProvider<DoubleMinValidator> doubleMinValidatorObjectProvider
    @Autowired
    ObjectProvider<DoubleMaxValidator> doubleMaxValidatorObjectProvider

    @Override
    void setup() {
        exportScreensPackages(['validator'])
    }

    def "load validators from screen"() {
        showTestMainScreen()

        when:
        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        then:
        noExceptionThrown()
    }

    def "size validator string test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def sizeValidator = sizeValidatorObjectProvider.getObject()
        sizeValidator.setSize(2, 4)

        def textField = (TextField<String>) validatorTestScreen.getWindow().getComponent("stringField")
        textField.addValidator(sizeValidator)

        when: "invalid value"
        def invalidValue = "invalidValue"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        def validValue = "vali"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()

        when: "set custom message"
        def customMessage = 'min = ${min} max = ${max}'
        sizeValidator.setMessage(customMessage)
        textField.setValue(invalidValue)
        textField.validate()

        then:
        def e = thrown(ValidationException)
        e.getDetailsMessage() == 'min = 2 max = 4'
    }

    def "size validator collection test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def customMessage = "custom message"
        def sizeValidator = sizeValidatorObjectProvider.getObject(customMessage)
        sizeValidator.setMin(2)
        sizeValidator.setMax(4)

        def twinColumn = (TwinColumn<String>) validatorTestScreen.getWindow().getComponent("twinColumn")
        twinColumn.addValidator(sizeValidator)
        twinColumn.setOptionsList(Arrays.asList("one", "two", "three"))

        when: "invalid value"
        twinColumn.setValue(Arrays.asList("one"))
        twinColumn.validate()

        then:
        def e = thrown(ValidationException)
        e.getDetailsMessage() == customMessage

        when: "valid value"
        twinColumn.setValue(Arrays.asList("one", "two"))
        twinColumn.validate()

        then:
        noExceptionThrown()

        when: "null value"
        twinColumn.setValue(null)
        twinColumn.validate()

        then:
        noExceptionThrown()
    }

    def "regexp validator text"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        RegexpValidator regexpValidator = regexpValidatorObjectProvider.getObject('^\\w*$')

        def textField = (TextField<String>) validatorTestScreen.getWindow().getComponent("stringField")
        textField.addValidator(regexpValidator)

        when: "invalid value"
        textField.setValue("^%")
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue("abcdefg123")
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "positive validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def validValue = 10
        def invalidValue = 0 // and less

        def positiveValidator = positiveValidatorObjectProvider.getObject()

        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.addValidator(positiveValidator)
        textField.setDatatype(datatypeRegistry.find(Integer))

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "positiveOrZero validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def validValue = new BigDecimal(0)
        def invalidValue = new BigDecimal(-1)
        def positiveOrZeroValidator = positiveOrZeroValidatorObjectProvider.getObject()

        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.addValidator(positiveOrZeroValidator)
        textField.setDatatype(datatypeRegistry.find(BigDecimal))

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()
    }

    // CAUTION test depends on time duration, so if you try to debug test can be failed
    def "past validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def pastValidator = pastValidatorObjectProvider.getObject()

        def dateField = (DateField) validatorTestScreen.getWindow().getComponent("dateField")
        dateField.setDatatype(datatypeRegistry.find(DateTimeDatatype))
        dateField.addValidator(pastValidator)

        when: "invalid value"
        def invalidValue = addDayToCurrentDate(2)
        dateField.setValue(new Date(invalidValue))
        dateField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        def validValue = addDayToCurrentDate(-2)
        dateField.setValue(new Date(validValue))
        dateField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        dateField.setValue(null)
        dateField.validate()

        then:
        noExceptionThrown()

        // check with seconds
        pastValidator.setCheckSeconds(true)

        when: "invalid seconds value"
        def invalidSeconds = addSecondsToCurrentDate(5)
        dateField.setValue(new Date(invalidSeconds))
        dateField.validate()

        then:
        thrown(ValidationException)

        when: "valid seconds value"
        def validSeconds = addSecondsToCurrentDate(-1)
        dateField.setValue(new Date(validSeconds))
        dateField.validate()

        then:
        noExceptionThrown()
    }

    // CAUTION test depends on time duration, so if you try to debug test can be failed
    def "pastOrPresent validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def pastOrPresentValidator = pastOrPresentValidatorObjectProvider.getObject()
        pastOrPresentValidator.setCheckSeconds(true)

        def timeField = (TimeField) validatorTestScreen.getWindow().getComponent("timeField")
        timeField.addValidator(pastOrPresentValidator)
        timeField.setDatatype(datatypeRegistry.find(LocalTimeDatatype))

        when: "invalid value"
        def currentTime = (LocalTime) timeSource.now().toLocalTime()
        timeField.setValue(currentTime.plusSeconds(5))
        timeField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        timeField.setValue(timeSource.now().toLocalTime())
        timeField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        timeField.setValue(null)
        timeField.validate()

        then:
        noExceptionThrown()
    }

    protected Long addDayToCurrentDate(int amount) {
        def timeInMillis = timeSource.currentTimeMillis()
        def calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timeInMillis)
        calendar.add(Calendar.DAY_OF_MONTH, amount)
        return calendar.getTimeInMillis()
    }

    protected Long addSecondsToCurrentDate(int amount) {
        def timeInMillis = timeSource.currentTimeMillis()
        def calendar = Calendar.getInstance()
        calendar.setTimeInMillis(timeInMillis)
        calendar.add(Calendar.SECOND, amount)
        return calendar.getTimeInMillis()
    }

    def "notNull validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def notNullValidator = notNullValidatorObjectProvider.getObject()
        def dateField = (DateField) validatorTestScreen.getWindow().getComponent("dateField")
        dateField.addValidator(notNullValidator)

        when: "null value"
        dateField.setValue(null)
        dateField.validate()

        then:
        thrown(ValidationException)

        when: "not null value"
        dateField.setValue(new Date())
        dateField.validate()

        then:
        noExceptionThrown()
    }

    def "notEmpty validator string test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def notEmptyValidator = notEmptyValidatorObjectProvider.getObject()
        def textField = (TextField<String>) validatorTestScreen.getWindow().getComponent("stringField")
        textField.addValidator(notEmptyValidator)

        when: "empty value"
        textField.setValue("")
        textField.validate()

        then:
        thrown(ValidationException)

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "not empty value"
        textField.setValue("not empty value")
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "notEmpty validator collection test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def notEmptyValidator = notEmptyValidatorObjectProvider.getObject()
        def twinColumn = (TwinColumn<String>) validatorTestScreen.getWindow().getComponent("twinColumn")
        twinColumn.addValidator(notEmptyValidator)
        twinColumn.setOptionsList(Arrays.asList("one", "two", "three"))

        when: "empty value"
        twinColumn.setValue(Collections.emptyList())
        twinColumn.validate()

        then:
        thrown(ValidationException)

        when: "null value"
        twinColumn.setValue(null)
        twinColumn.validate()

        then:
        thrown(ValidationException)

        when: "not empty value"
        twinColumn.setValue(Arrays.asList("one"))
        twinColumn.validate()

        then:
        noExceptionThrown()
    }

    def "notBlank validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def notBlankValidator = notBlankValidatorObjectProvider.getObject()
        def textField = (TextField<String>) validatorTestScreen.getWindow().getComponent("stringField")
        textField.addValidator(notBlankValidator)

        def invalidValue = "   \t   "
        def validValue = "   \t   t"

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        thrown(ValidationException)
    }

    def "negative validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def negativeValidator = negativeValidatorObjectProvider.getObject()
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Long))
        textField.addValidator(negativeValidator)

        def invalidValue = 0
        def validValue = -1

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "negativeOrZero validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def negativeOrZeroValidator = negativeOrZeroValidatorObjectProvider.getObject()
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Long))
        textField.addValidator(negativeOrZeroValidator)

        def invalidValue = 1
        def validValue = 0

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "min validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def minValidator = minValidatorObjectProvider.getObject(100)
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Integer))
        textField.addValidator(minValidator)

        def invalidValue = 99
        def validValue = 100

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "max validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def maxValidator = maxValidatorObjectProvider.getObject(100)
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Integer))
        textField.addValidator(maxValidator)

        def invalidValue = 101
        def validValue = 100

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()
    }

    // CAUTION test depends on time duration, so if you try to debug test can be failed
    def "future validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def futureValidator = futureValidatorObjectProvider.getObject()
        futureValidator.setCheckSeconds(true)

        def dateField = (DateField) validatorTestScreen.getWindow().getComponent("dateField")
        dateField.setDatatype(datatypeRegistry.find(OffsetDateTime))
        dateField.addValidator(futureValidator)

        when: "invalid value"
        dateField.setValue(timeSource.now().toOffsetDateTime())
        dateField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        def currentValue = timeSource.now().toOffsetDateTime()
        dateField.setValue(currentValue.plusSeconds(5))
        dateField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        dateField.setValue(null)
        dateField.validate()

        then:
        noExceptionThrown()
    }

    // CAUTION test depends on time duration, so if you try to debug test can be failed
    def "futureOrPresent validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def futureOrPresentValidator = futureOrPresentValidatorObjectProvider.getObject()
        futureOrPresentValidator.setCheckSeconds(true)

        def timeField = (TimeField) validatorTestScreen.getWindow().getComponent("timeField")
        timeField.setDatatype(datatypeRegistry.find(OffsetTime))
        timeField.addValidator(futureOrPresentValidator)

        when: "invalid value"
        def invalidValue = timeSource.now().toOffsetDateTime().toOffsetTime()
        timeField.setValue(invalidValue.minusSeconds(5))
        timeField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        def validValue = timeSource.now().toOffsetDateTime().toOffsetTime()
        timeField.setValue(validValue.plusSeconds(5))
        timeField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        timeField.setValue(null)
        timeField.validate()

        then:
        noExceptionThrown()
    }

    def "digits validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def digitsValidator = digitsValidatorObjectProvider.getObject(2, 2)

        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(BigDecimal))
        textField.addValidator(digitsValidator)

        when: "invalid bigDecimal value"
        textField.setValue(new BigDecimal("123.12"))
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid bigDecimal value"
        textField.setValue(new BigDecimal("12.34"))
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()

        textField.setDatatype(datatypeRegistry.find(String))
        when: "invalid string value"
        textField.setValue("absd")
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid string value"
        textField.setValue("12.34")
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "decimal min validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def decimalMinValidator = decimalMinValidatorObjectProvider.getObject(new BigDecimal(10))
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Integer))
        textField.addValidator(decimalMinValidator)

        def invalidInclusive = 9
        def validInclusive = 10

        when: "invalid value"
        textField.setValue(invalidInclusive)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validInclusive)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()

        decimalMinValidator.setMin(new BigDecimal(5), false)
        def invalidValue = 5
        def validValue = 6

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "decimal max validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def decimalMaxValidator = decimalMaxValidatorObjectProvider.getObject(new BigDecimal(10))
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Integer))
        textField.addValidator(decimalMaxValidator)

        def invalidInclusive = 11
        def validInclusive = 10

        when: "invalid value"
        textField.setValue(invalidInclusive)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validInclusive)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()

        decimalMaxValidator.setMax(new BigDecimal(5), false)
        def invalidValue = 5
        def validValue = 4

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "double min validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def doubleMinValidator = doubleMinValidatorObjectProvider.getObject(Double.valueOf(10.2))
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Double))
        textField.addValidator(doubleMinValidator)

        def invalidInclusive = Double.valueOf(10.1)
        def validInclusive = Double.valueOf(10.2)

        when: "invalid value"
        textField.setValue(invalidInclusive)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validInclusive)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()

        doubleMinValidator.setMin(new Double(5), false)
        def invalidValue = Double.valueOf(5)
        def validValue = Double.valueOf(6)

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()
    }

    def "double max validator test"() {
        showTestMainScreen()

        def validatorTestScreen = screens.create(ValidatorTestScreen)
        validatorTestScreen.show()

        def doubleMaxValidator = doubleMaxValidatorObjectProvider.getObject(Double.valueOf(10.2))
        def textField = (TextField) validatorTestScreen.getWindow().getComponent("numberField")
        textField.setDatatype(datatypeRegistry.find(Double))
        textField.addValidator(doubleMaxValidator)

        def invalidInclusive = Double.valueOf(10.3)
        def validInclusive = Double.valueOf(10)

        when: "invalid value"
        textField.setValue(invalidInclusive)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validInclusive)
        textField.validate()

        then:
        noExceptionThrown()

        when: "null value"
        textField.setValue(null)
        textField.validate()

        then:
        noExceptionThrown()

        doubleMaxValidator.setMax(Double.valueOf(5), false)
        def invalidValue = Double.valueOf(5)
        def validValue = Double.valueOf(4)

        when: "invalid value"
        textField.setValue(invalidValue)
        textField.validate()

        then:
        thrown(ValidationException)

        when: "valid value"
        textField.setValue(validValue)
        textField.validate()

        then:
        noExceptionThrown()
    }
}
