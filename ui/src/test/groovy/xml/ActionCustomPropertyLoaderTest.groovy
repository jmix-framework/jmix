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

package xml

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.action.ListAction
import io.jmix.ui.xml.layout.loader.ActionCustomPropertyLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ActionCustomPropertyLoaderTest extends Specification {

    @Autowired
    ActionCustomPropertyLoader propertyLoader

    TestAction action

    void setup() {
        action = new TestAction("test")
    }

    @Unroll
    def 'Boolean property "#value" is applied to action'(String value) {

        when: 'ActionCustomPropertyLoader has loaded the boolean property'
        propertyLoader.load(action, 'booleanProp', value)

        then: 'Boolean property is applied to action'
        action.booleanProp == Boolean.parseBoolean(value)

        where:
        value << [
                "true",
                "false",
                "abc"
        ]
    }

    @Unroll
    def 'Enum property "#value" is applied to action'(String value) {

        when: 'ActionCustomPropertyLoader has loaded the enum property'
        propertyLoader.load(action, 'enumProp', value)

        then: 'Enum property is applied to action'
        action.enumProp == TestEnum.valueOf(value)

        where:
        value << [
                "ONE",
                "TWO"
        ]
    }

    def 'Non-existent enum property cannot be applied to action'() {

        def value = 'NONEXISTENT'

        when: 'ActionCustomPropertyLoader has loaded the enum property'
        propertyLoader.load(action, 'enumProp', value)

        then: 'Enum property is null'
        action.enumProp == null
    }

    def 'Class property is applied to action'() {

        def value = 'xml.ActionCustomPropertyLoaderTest'

        when: 'ActionCustomPropertyLoader has loaded the value property'
        propertyLoader.load(action, 'classProp', value)

        then: 'String list property is applied to action'
        action.classProp == ActionCustomPropertyLoaderTest
    }

    def 'Integer list property is applied to action'() {

        def value = '1,2,3'

        when: 'ActionCustomPropertyLoader has loaded the value property'
        propertyLoader.load(action, 'integerListProp', value)

        then: 'Integer list property is applied to action'
        action.integerListProp == Arrays.asList(1, 2, 3)
    }

    def 'String list property is applied to action'() {

        def value = 'aaa,bbb,ccc'

        when: 'ActionCustomPropertyLoader has loaded the value property'
        propertyLoader.load(action, 'stringListProp', value)

        then: 'String list property is applied to action'
        action.stringListProp == Arrays.asList('aaa', 'bbb', 'ccc')
    }

    enum TestEnum {
        ONE,
        TWO
    }

    static class TestAction extends ListAction {

        private Boolean booleanProp;
        private TestEnum enumProp;
        private Class classProp;
        private List<String> stringListProp;
        private List<Integer> integerListProp;

        TestAction(String id) {
            super(id)
        }

        Boolean getBooleanProp() {
            return booleanProp
        }

        void setBooleanProp(Boolean booleanProp) {
            this.booleanProp = booleanProp
        }

        TestEnum getEnumProp() {
            return enumProp
        }

        void setEnumProp(TestEnum enumProp) {
            this.enumProp = enumProp
        }

        Class getClassProp() {
            return classProp
        }

        void setClassProp(Class classProp) {
            this.classProp = classProp
        }

        List<String> getStringListProp() {
            return stringListProp
        }

        void setStringListProp(List<String> stringListProp) {
            this.stringListProp = stringListProp
        }

        List<Integer> getIntegerListProp() {
            return integerListProp
        }

        void setIntegerListProp(List<Integer> integerListProp) {
            this.integerListProp = integerListProp
        }
    }
}
