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

package validator

import io.jmix.core.TimeSource
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.flowui.component.validation.*
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import validator.screen.ValidatorTestView

import java.time.LocalTime

@SpringBootTest
class ValidatorsTest extends FlowuiTestSpecification {

    @Autowired
    DatatypeRegistry datatypeRegistry

    @Autowired
    TimeSource timeSource

    @Autowired
    ObjectProvider<DateTimeRangeValidator> dateTimeRangeValidatorObjectProvider
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
    @Autowired
    ObjectProvider<EmailValidator> emailValidatorObjectProvider

    @Override
    void setup() {
        registerScreenBasePackages("validator")
    }

    def "Load validators from screen"() {
        when: "Open the screen"
        openScreen(ValidatorTestView.class)

        then: "The screen will be open without exceptions"
        noExceptionThrown()
    }

    def "DateTimeRange validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def dateTimeRangeValidator = dateTimeRangeValidatorObjectProvider.getObject()
        dateTimeRangeValidator.message = "errorMessage"

        Calendar date = Calendar.getInstance()
        date.time = new Date()

        date.add(Calendar.MONTH, -1)
        dateTimeRangeValidator.min = date.time

        date.add(Calendar.MONTH, 2)
        dateTimeRangeValidator.max = date.time

        date.add(Calendar.MONTH, 1)
        def invalidValue = date.time
        def validValue = new Date()

        def dateField = validatorTestView.datePicker
        dateField.addValidator(dateTimeRangeValidator)
        dateField.datatype = datatypeRegistry.find(Date)

        when: "Set invalid value value"
        dateField.typedValue = invalidValue

        then: "Component is not valid"
        dateField.invalid
        dateField.errorMessage == "errorMessage"

        when: "Set valida value"
        dateField.typedValue = validValue

        then: "Component is valid"
        !dateField.invalid

        when: "Set null value"
        dateField.typedValue = null

        then: "Component is valid"
        !dateField.invalid
    }

    def "Size validator string test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def sizeValidator = sizeValidatorObjectProvider.getObject()
        sizeValidator.setSize(2, 4)

        def textField = validatorTestView.stringField
        textField.addValidator(sizeValidator)

        when: "Set invalid value"
        def invalidValue = "invalidValue"
        textField.value = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        def validValue = "vali"
        textField.value = validValue

        then: "Component is valid"
        !textField.invalid

        when: "A validator with custom message"
        def customMessage = 'min = ${min} max = ${max}'
        sizeValidator.message = customMessage
        textField.value = invalidValue

        then: "The message will be displayed"
        textField.invalid
        textField.errorMessage == 'min = 2 max = 4'
    }

//    def "size validator collection test"() {
//        def validatorTestView = openScreen(ValidatorTestView.class)
//
//        def customMessage = "custom message"
//        def sizeValidator = sizeValidatorObjectProvider.getObject(customMessage)
//        sizeValidator.setMin(2)
//        sizeValidator.setMax(4)
//
//        def twinColumn = (TwinColumn<String>) validatorTestView.getWindow().getComponent("twinColumn")
//        twinColumn.addValidator(sizeValidator)
//        twinColumn.setOptionsList(Arrays.asList("one", "two", "three"))
//
//        when: "invalid value"
//        twinColumn.setValue(Arrays.asList("one"))
//        twinColumn.validate()
//
//        then:
//        def e = thrown(ValidationException)
//        e.getDetailsMessage() == customMessage
//
//        when: "valid value"
//        twinColumn.setValue(Arrays.asList("one", "two"))
//        twinColumn.validate()
//
//        then:
//        noExceptionThrown()
//
//        when: "null value"
//        twinColumn.setValue(null)
//        twinColumn.validate()
//
//        then:
//        noExceptionThrown()
//    }
//
    def "Regexp validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        RegexpValidator regexpValidator = regexpValidatorObjectProvider.getObject('^\\w*$')

        def textField = validatorTestView.stringField
        textField.addValidator(regexpValidator)

        when: "Set invalid value"
        textField.typedValue = "^%"

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = "abcdefg123"

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }

    def "Positive validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def validValue = 10
        def invalidValue = 0 // and less

        def positiveValidator = positiveValidatorObjectProvider.getObject()

        def textField = validatorTestView.numberField
        textField.addValidator(positiveValidator)
        textField.datatype = datatypeRegistry.find(Integer)

        when: "Set invalid value"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }

    def "PositiveOrZero validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def validValue = BigDecimal.valueOf(0)
        def invalidValue = BigDecimal.valueOf(-1)
        def positiveOrZeroValidator = positiveOrZeroValidatorObjectProvider.getObject()

        def textField = validatorTestView.numberField
        textField.addValidator(positiveOrZeroValidator)
        textField.datatype = datatypeRegistry.find(BigDecimal)

        when: "Set invalid value"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }

    def "Past validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def pastValidator = pastValidatorObjectProvider.getObject()
        pastValidator.message = "errorMessage"
        pastValidator.checkSeconds = false

        def dateField = validatorTestView.datePicker

        def date = Calendar.getInstance()
        date.time = new Date()

        date.add(Calendar.MONTH, 1)
        def invalidValue = date.time

        date.add(Calendar.MONTH, -2)
        def validValue = date.time

        dateField.datatype = datatypeRegistry.find(Date)
        dateField.addValidator(pastValidator)

        when: "Set invalid value"
        dateField.typedValue = invalidValue

        then: "Component is not valid"
        dateField.invalid
        dateField.errorMessage == "errorMessage"

        when: "Set valid value"
        dateField.typedValue = validValue

        then: "Component is valid"
        !dateField.invalid

        when: "Set null value"
        dateField.typedValue = null

        then: "Component is valid"
        !dateField.invalid
    }

    def "PastOrPresent validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def pastOrPresentValidator = pastOrPresentValidatorObjectProvider.getObject()
        pastOrPresentValidator.checkSeconds = true
        pastOrPresentValidator.message = "errorMessage"

        def timeField = validatorTestView.timePicker

        timeField.addValidator(pastOrPresentValidator)
        timeField.datatype = datatypeRegistry.find(LocalTime)

        def currentTime = (LocalTime) timeSource.now().toLocalTime()


        when: "Set invalid value"
        timeField.typedValue = currentTime.plusSeconds(5)

        then: "Component is not valid"
        timeField.invalid
        timeField.errorMessage == "errorMessage"

        when: "Set valid value"
        timeField.typedValue = timeSource.now().toLocalTime()

        then: "Component is valid"
        !timeField.invalid

        when: "Set null value"
        timeField.typedValue = null

        then: "Component is valid"
        !timeField.invalid
    }

    def "NotNull validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def notNullValidator = notNullValidatorObjectProvider.getObject()
        def dateField = validatorTestView.datePicker
        dateField.addValidator(notNullValidator)
        dateField.datatype = datatypeRegistry.find(Date)

        when: "Set null value"
        dateField.typedValue = null

        then: "Component is not valid"
        dateField.invalid

        when: "Set not null value"
        dateField.typedValue = new Date()

        then: "Component is valid"
        !dateField.invalid
    }

    def "NotEmpty validator string test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def notEmptyValidator = notEmptyValidatorObjectProvider.getObject()
        def textField = validatorTestView.stringField
        textField.addValidator(notEmptyValidator)

        when: "Set empty value"
        textField.typedValue = ""

        then: "Component is not valid"
        textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is not valid"
        textField.invalid

        when: "Set not empty value"
        textField.typedValue = "not empty value"

        then: "Component is valid"
        !textField.invalid
    }

//    def "notEmpty validator collection test"() {
//        showTestMainScreen()
//
//        def validatorTestView = screens.create(ValidatorTestView)
//        validatorTestView.show()
//
//        def notEmptyValidator = notEmptyValidatorObjectProvider.getObject()
//        def twinColumn = (TwinColumn<String>) validatorTestView.getWindow().getComponent("twinColumn")
//        twinColumn.addValidator(notEmptyValidator)
//        twinColumn.setOptionsList(Arrays.asList("one", "two", "three"))
//
//        when: "empty value"
//        twinColumn.setValue(Collections.emptyList())
//        twinColumn.validate()
//
//        then:
//        thrown(ValidationException)
//
//        when: "null value"
//        twinColumn.setValue(null)
//        twinColumn.validate()
//
//        then:
//        thrown(ValidationException)
//
//        when: "not empty value"
//        twinColumn.setValue(Arrays.asList("one"))
//        twinColumn.validate()
//
//        then:
//        noExceptionThrown()
//    }

    def "NotBlank validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def notBlankValidator = notBlankValidatorObjectProvider.getObject()
        def textField = validatorTestView.stringField
        textField.addValidator(notBlankValidator)

        def invalidValue = "   \t   "
        def validValue = "   \t   t"

        when: "Set invalid value"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is not valid"
        textField.invalid
    }

    def "Negative validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def negativeValidator = negativeValidatorObjectProvider.getObject()
        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(Long)
        textField.addValidator(negativeValidator)

        def invalidValue = 0
        def validValue = -1

        when: "Set invalid value"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }

    def "NegativeOrZero validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def negativeOrZeroValidator = negativeOrZeroValidatorObjectProvider.getObject()
        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(Long)
        textField.addValidator(negativeOrZeroValidator)

        def invalidValue = 1
        def validValue = 0

        when: "Set invalid value"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }

    def "Min validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def minValidator = minValidatorObjectProvider.getObject(100)
        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(Integer)
        textField.addValidator(minValidator)

        def invalidValue = 99
        def validValue = 100

        when: "Set invalid value"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }

    def "Max validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def maxValidator = maxValidatorObjectProvider.getObject(100)
        def textField = validatorTestView.numberField
        textField.setDatatype(datatypeRegistry.find(Integer))
        textField.addValidator(maxValidator)

        def invalidValue = 101
        def validValue = 100

        when: "Set invalid value"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }

    def "Future validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def futureValidator = futureValidatorObjectProvider.getObject()
        futureValidator.message = "errorMessage"
        futureValidator.checkSeconds = false

        def dateField = validatorTestView.datePicker

        def date = Calendar.getInstance()
        date.time = new Date()

        date.add(Calendar.MONTH, -1)
        def invalidValue = date.time

        date.add(Calendar.MONTH, 2)
        def validValue = date.time

        dateField.datatype = datatypeRegistry.find(Date)
        dateField.addValidator(futureValidator)

        when: "Set invalid value"
        dateField.typedValue = invalidValue

        then: "Component is not valid"
        dateField.invalid
        dateField.errorMessage == "errorMessage"

        when: "Set valid value"
        dateField.typedValue = validValue

        then: "Component is valid"
        !dateField.invalid

        when: "Set null value"
        dateField.typedValue = null

        then: "Component is valid"
        !dateField.invalid
    }

    def "FutureOrPresent validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def futureOrPresentValidator = futureOrPresentValidatorObjectProvider.getObject()
        futureOrPresentValidator.checkSeconds = true
        futureOrPresentValidator.message = "errorMessage"

        def timeField = validatorTestView.timePicker

        timeField.addValidator(futureOrPresentValidator)
        timeField.datatype = datatypeRegistry.find(LocalTime)

        def currentTime = (LocalTime) timeSource.now().toLocalTime()


        when: "Set invalid value"
        timeField.typedValue = currentTime.minusSeconds(5)

        then: "Component is not valid"
        timeField.invalid
        timeField.errorMessage == "errorMessage"

        when: "Set valid value"
        timeField.typedValue = timeSource.now().toLocalTime().plusSeconds(3)

        then: "Component is valid"
        !timeField.invalid

        when: "Set null value"
        timeField.typedValue = null

        then: "Component is valid"
        !timeField.invalid
    }

    def "Digits validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def digitsValidator = digitsValidatorObjectProvider.getObject(2, 2)

        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(BigDecimal)
        textField.addValidator(digitsValidator)

        when: "Set invalid bigDecimal value"
        textField.typedValue = BigDecimal.valueOf(123.12)

        then: "Component is not valid"
        textField.invalid

        when: "Set valid bigDecimal value"
        textField.typedValue = BigDecimal.valueOf(12.34)

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid

        when: "Set invalid string value"
        textField.datatype = datatypeRegistry.find(String)

        textField.typedValue = "absd"

        then: "Component is not valid"
        textField.invalid

        when: "Set valid string value"
        textField.typedValue = "12.34"

        then: "Component is valid"
        !textField.invalid
    }

    def "Decimal min validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def decimalMinValidator = decimalMinValidatorObjectProvider.getObject(new BigDecimal(10))
        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(Integer)
        textField.addValidator(decimalMinValidator)

        def invalidInclusive = 9
        def validInclusive = 10

        when: "Set invalid value"
        textField.typedValue = invalidInclusive

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validInclusive

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid

        when: "Set invalid value"
        decimalMinValidator.min = BigDecimal.valueOf(5)
        decimalMinValidator.inclusive = false

        def invalidValue = 5
        def validValue = 6

        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid
    }

    def "Decimal max validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def decimalMaxValidator = decimalMaxValidatorObjectProvider.getObject(new BigDecimal(10))
        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(Integer)
        textField.addValidator(decimalMaxValidator)

        def invalidInclusive = 11
        def validInclusive = 10

        when: "Set invalid value"
        textField.typedValue = invalidInclusive

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validInclusive

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid

        when: "Set invalid value"
        decimalMaxValidator.max = BigDecimal.valueOf(5)
        decimalMaxValidator.inclusive = false

        def invalidValue = 5
        def validValue = 4

        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid
    }

    def "Double min validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def doubleMinValidator = doubleMinValidatorObjectProvider.getObject(Double.valueOf(10.2))
        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(Double)
        textField.addValidator(doubleMinValidator)

        def invalidInclusive = Double.valueOf(10.1)
        def validInclusive = Double.valueOf(10.2)

        when: "Set invalid value"
        textField.typedValue = invalidInclusive

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validInclusive

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid

        when: "Set invalid value"
        doubleMinValidator.min = Double.valueOf(5)
        doubleMinValidator.inclusive = false

        def invalidValue = Double.valueOf(5)
        def validValue = Double.valueOf(6)
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid
    }

    def "Double max validator test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def doubleMaxValidator = doubleMaxValidatorObjectProvider.getObject(Double.valueOf(10.2))
        def textField = validatorTestView.numberField
        textField.datatype = datatypeRegistry.find(Double)
        textField.addValidator(doubleMaxValidator)

        def invalidInclusive = Double.valueOf(10.3)
        def validInclusive = Double.valueOf(10)

        when: "Set invalid value"
        textField.typedValue = invalidInclusive

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validInclusive

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid

        when: "Set invalid value"
        doubleMaxValidator.max = Double.valueOf(5)
        doubleMaxValidator.inclusive = false

        def invalidValue = Double.valueOf(5)
        def validValue = Double.valueOf(4)

        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid

        when: "Set valid value"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid
    }

    def "Email validator string test"() {
        given: "A component with a validator"
        def validatorTestView = openScreen(ValidatorTestView.class)

        def emailValidator = emailValidatorObjectProvider.getObject()
        emailValidator.message = "errorMessage"

        def textField = validatorTestView.stringField
        textField.addValidator(emailValidator)

        when: "Set invalid value"
        def invalidValue = "notEmail"
        textField.typedValue = invalidValue

        then: "Component is not valid"
        textField.invalid
        textField.errorMessage == emailValidator.message

        when: "Set valid value"
        def validValue = "this@email.real"
        textField.typedValue = validValue

        then: "Component is valid"
        !textField.invalid

        when: "Set null value"
        textField.typedValue = null

        then: "Component is valid"
        !textField.invalid
    }
}
