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

package component_xml_load

import component_xml_load.screen.ValidatorView
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ValidatorsXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DatatypeRegistry datatypeRegistry

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")
    }

    def "Load decimalValidators from XML"() {
        given: "A component with decimalValidators"
        def validatorView = openScreen(ValidatorView.class)
        def decimalMaxField = validatorView.decimalField

        when: "Set invalid value for max"
        decimalMaxField.value = 100.02

        then: "Component is not valid"
        decimalMaxField.invalid
        decimalMaxField.errorMessage == "errorMessageStringForMax"

        when: "Set invalid value for min"
        decimalMaxField.value = 99.98
        decimalMaxField.invalid
        decimalMaxField.errorMessage == "errorMessageStringForMin"

        then: "Component is not valid"

        when: "Set valid value"
        decimalMaxField.value = 100.01

        then: "Component is valid"
        !decimalMaxField.invalid

        when: "Set null value"
        decimalMaxField.value = null

        then: "Component is valid"
        !decimalMaxField.invalid
    }

    def "Load digitsValidator from XML"() {
        given: "A component with digitsValidator"
        def validatorView = openScreen(ValidatorView.class)

        def digitsField = validatorView.digitsField

        when: "Set invalid bigDecimal value"
        digitsField.value = BigDecimal.valueOf(123.12)

        then: "Component is not valid"
        digitsField.invalid
        digitsField.errorMessage == "errorMessageString"

        when: "Set valid bigDecimal value"
        digitsField.value = BigDecimal.valueOf(12.34)

        then: "Component is valid"
        !digitsField.invalid

        when: "Set null value"
        digitsField.value = null

        then: "Component is valid"
        !digitsField.invalid
    }

    def "Load doubleValidator from XML"() {
        given: "A component with a doubleValidator"
        def validatorView = openScreen(ValidatorView.class)

        def doubleField = validatorView.doubleField


        when: "Set invalid value for max"
        doubleField.typedValue = 100.03d

        then: "Component is not valid"
        doubleField.invalid
        doubleField.errorMessage == "errorMessageStringForMax"

        when: "Set invalid value for min"
        doubleField.typedValue = 99.98d

        then: "Component is not valid"
        doubleField.invalid
        doubleField.errorMessage == "errorMessageStringForMin"


        when: "Set valid value"
        doubleField.typedValue = 100.01d

        then: "Component is valid"
        !doubleField.invalid

        when: "Set null value"
        doubleField.typedValue = null

        then: "Component is valid"
        !doubleField.invalid
    }

    def "Load emailValidator from XML"() {
        given: "A component with a emailValidator"
        def validatorView = openScreen(ValidatorView.class)

        def emailField = validatorView.emailField

        when: "Set invalid value"
        def invalidValue = "notEmail"
        emailField.typedValue = invalidValue

        then: "Component is not valid"
        emailField.invalid
        emailField.errorMessage == "errorMessageStringForEmail"

        when: "Set valid value"
        def validValue = "this@email.real"
        emailField.typedValue = validValue

        then: "Component is valid"
        !emailField.invalid

        when: "Set null value"
        emailField.typedValue = null

        then: "Component is valid"
        !emailField.invalid
    }

    def "Load futureOrPresentValidator from XML"() {
        given: "A component with a futureOrPresentValidator"
        def validatorView = openScreen(ValidatorView.class)

        def futureOrPresentField = validatorView.futureOrPresentField

        def date = Calendar.getInstance()
        date.time = new Date()

        date.add(Calendar.MONTH, -1)
        def invalidValue = date.time

        date.add(Calendar.MONTH, 2)
        def validValue = date.time

        when: "Set invalid value"
        futureOrPresentField.typedValue = invalidValue

        then: "Component is not valid"
        futureOrPresentField.invalid
        futureOrPresentField.errorMessage == "errorMessageString"

        when: "Set valid value"
        futureOrPresentField.typedValue = validValue

        then: "Component is valid"
        !futureOrPresentField.invalid

        when: "Set null value"
        futureOrPresentField.typedValue = null

        then: "Component is valid"
        !futureOrPresentField.invalid
    }

    def "Load futureValidator from XML"() {
        given: "A component with a futureValidator"
        def validatorView = openScreen(ValidatorView.class)

        def futureField = validatorView.futureField

        def date = Calendar.getInstance()
        date.time = new Date()

        date.add(Calendar.MONTH, -1)
        def invalidValue = date.time

        date.add(Calendar.MONTH, 2)
        def validValue = date.time

        when: "Set invalid value"
        futureField.typedValue = invalidValue

        then: "Component is not valid"
        futureField.invalid
        futureField.errorMessage == "errorMessageString"

        when: "Set valid value"
        futureField.typedValue = validValue

        then: "Component is valid"
        !futureField.invalid

        when: "Set null value"
        futureField.typedValue = null

        then: "Component is valid"
        !futureField.invalid
    }

    def "Load integerValidator from XML"() {
        given: "A component with a integerValidator"
        def validatorView = openScreen(ValidatorView.class)

        def integerField = validatorView.integerField

        when: "Set invalid value for max"
        integerField.typedValue = 102

        then: "Component is not valid"
        integerField.invalid
        integerField.errorMessage == "errorMessageStringForMax"

        when: "Set invalid value for min"
        integerField.typedValue = 98

        then: "Component is not valid"
        integerField.invalid
        integerField.errorMessage == "errorMessageStringForMin"

        when: "Set valid value"
        integerField.typedValue = 100

        then: "Component is valid"
        !integerField.invalid

        when: "Set null value"
        integerField.typedValue = null

        then: "Component is valid"
        !integerField.invalid
    }

    def "Load negativeOrZeroValidator from XML"() {
        given: "A component with a negativeOrZeroValidator"
        def validatorView = openScreen(ValidatorView.class)

        def negativeOrZeroField = validatorView.negativeOrZeroField

        when: "Set invalid value"
        negativeOrZeroField.typedValue = 1

        then: "Component is not valid"
        negativeOrZeroField.invalid
        negativeOrZeroField.errorMessage == "errorMessageString"

        when: "Set valid value"
        negativeOrZeroField.typedValue = 0

        then: "Component is valid"
        !negativeOrZeroField.invalid

        when: "Set null value"
        negativeOrZeroField.typedValue = null

        then: "Component is valid"
        !negativeOrZeroField.invalid
    }

    def "Load negativeValidator from XML"() {
        given: "A component with a negativeValidator"
        def validatorView = openScreen(ValidatorView.class)

        def negativeField = validatorView.negativeField

        when: "Set invalid value"
        negativeField.typedValue = 0

        then: "Component is not valid"
        negativeField.invalid
        negativeField.errorMessage == "errorMessageString"

        when: "Set valid value"
        negativeField.typedValue = -1

        then: "Component is valid"
        !negativeField.invalid

        when: "Set null value"
        negativeField.typedValue = null

        then: "Component is valid"
        !negativeField.invalid
    }

    def "Load notBlankValidator from XML"() {
        given: "A component with a notBlankValidator"
        def validatorView = openScreen(ValidatorView.class)

        def notBlankField = validatorView.notBlankField

        def invalidValue = "   \t   "
        def validValue = "   \t   t"

        when: "Set invalid value"
        notBlankField.typedValue = invalidValue

        then: "Component is not valid"
        notBlankField.invalid
        notBlankField.errorMessage == "errorMessageString"

        when: "Set valid value"
        notBlankField.typedValue = validValue

        then: "Component is valid"
        !notBlankField.invalid

        when: "Set null value"
        notBlankField.typedValue = null

        then: "Component is not valid"
        notBlankField.invalid
    }

    def "Load notEmptyValidator from XML"() {
        given: "A component with a notEmpty"
        def validatorView = openScreen(ValidatorView.class)

        def notEmptyField = validatorView.notEmptyField

        when: "Set empty value"
        notEmptyField.typedValue = ""

        then: "Component is not valid"
        notEmptyField.invalid
        notEmptyField.errorMessage == "errorMessageString"

        when: "Set null value"
        notEmptyField.typedValue = null

        then: "Component is not valid"
        notEmptyField.invalid

        when: "Set not empty value"
        notEmptyField.typedValue = "not empty value"

        then: "Component is valid"
        !notEmptyField.invalid
    }

    def "Load notNullValidator from XML"() {
        given: "A component with a notNullValidator"
        def validatorView = openScreen(ValidatorView.class)

        def notNullField = validatorView.notNullField

        when: "Set null value"
        notNullField.typedValue = null

        then: "Component is not valid"
        notNullField.invalid
        notNullField.errorMessage == "errorMessageString"

        when: "Set not null value"
        notNullField.typedValue = new Date()

        then: "Component is valid"
        !notNullField.invalid
    }

    def "Load pastOrPresentValidator from XML"() {
        given: "A component with a pastOrPresentValidator"
        def validatorView = openScreen(ValidatorView.class)

        def pastOrPresentField = validatorView.pastOrPresentField

        def date = Calendar.getInstance()
        date.time = new Date()

        date.add(Calendar.MONTH, 1)
        def invalidValue = date.time

        date.add(Calendar.MONTH, -2)
        def validValue = date.time

        when: "Set invalid value"
        pastOrPresentField.typedValue = invalidValue

        then: "Component is not valid"
        pastOrPresentField.invalid
        pastOrPresentField.errorMessage == "errorMessageString"

        when: "Set valid value"
        pastOrPresentField.typedValue = validValue

        then: "Component is valid"
        !pastOrPresentField.invalid

        when: "Set null value"
        pastOrPresentField.typedValue = null

        then: "Component is valid"
        !pastOrPresentField.invalid
    }

    def "Load pastValidator from XML"() {
        given: "A component with a pastValidator"
        def validatorView = openScreen(ValidatorView.class)

        def pastField = validatorView.pastField

        def date = Calendar.getInstance()
        date.time = new Date()

        date.add(Calendar.MONTH, 1)
        def invalidValue = date.time

        date.add(Calendar.MONTH, -2)
        def validValue = date.time

        when: "Set invalid value"
        pastField.typedValue = invalidValue

        then: "Component is not valid"
        pastField.invalid
        pastField.errorMessage == "errorMessageString"

        when: "Set valid value"
        pastField.typedValue = validValue

        then: "Component is valid"
        !pastField.invalid

        when: "Set null value"
        pastField.typedValue = null

        then: "Component is valid"
        !pastField.invalid
    }

    def "Load positiveOrZeroValidator from XML"() {
        given: "A component with a positiveOrZeroValidator"
        def validatorView = openScreen(ValidatorView.class)

        def positiveOrZeroField = validatorView.positiveOrZeroField

        when: "Set invalid value for max"
        positiveOrZeroField.typedValue = -1

        then: "Component is not valid"
        positiveOrZeroField.invalid
        positiveOrZeroField.errorMessage == "errorMessageString"

        when: "Set valid value"
        positiveOrZeroField.typedValue = 0

        then: "Component is valid"
        !positiveOrZeroField.invalid

        when: "Set null value"
        positiveOrZeroField.typedValue = null

        then: "Component is valid"
        !positiveOrZeroField.invalid
    }

    def "Load positiveValidator from XML"() {
        given: "A component with a positiveValidator"
        def validatorView = openScreen(ValidatorView.class)

        def positiveField = validatorView.positiveField

        when: "Set invalid value"
        positiveField.typedValue = 0

        then: "Component is not valid"
        positiveField.invalid
        positiveField.errorMessage == "errorMessageString"

        when: "Set valid value"
        positiveField.typedValue = 1

        then: "Component is valid"
        !positiveField.invalid

        when: "Set null value"
        positiveField.typedValue = null

        then: "Component is valid"
        !positiveField.invalid
    }

    def "Load regexpValidator from XML"() {
        given: "A component with a regexpValidator"
        def validatorView = openScreen(ValidatorView.class)

        def regexpField = validatorView.regexpField

        when: "Set invalid value"
        regexpField.typedValue = "^%"

        then: "Component is not valid"
        regexpField.invalid
        regexpField.errorMessage == "errorMessageString"

        when: "Set valid value"
        regexpField.typedValue = "abcdefg123"

        then: "Component is valid"
        !regexpField.invalid

        when: "Set null value"
        regexpField.typedValue = null

        then: "Component is valid"
        !regexpField.invalid
    }

    def "Size validator string test"() {
        given: "A component with a validator"
        def validatorView = openScreen(ValidatorView.class)

        def sizeField = validatorView.sizeField

        when: "Set invalid value"
        sizeField.value = "invalidValue"

        then: "Component is not valid"
        sizeField.invalid
        sizeField.errorMessage == "errorMessageString"

        when: "Set valid value"
        sizeField.value = "vali"

        then: "Component is valid"
        !sizeField.invalid
    }
}
