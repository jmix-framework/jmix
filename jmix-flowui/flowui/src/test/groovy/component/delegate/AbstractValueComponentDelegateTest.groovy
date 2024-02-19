/*
 * Copyright 2024 Haulmont.
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

package component.delegate

import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.shared.Registration
import component_container.view.ComponentContainerView
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.delegate.IntegerFieldDelegate
import io.jmix.flowui.component.textfield.JmixIntegerField
import io.jmix.flowui.data.BindingState
import io.jmix.flowui.data.ValueSource
import io.jmix.flowui.data.binding.impl.AbstractValueBinding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import javax.annotation.Nullable
import java.util.function.Consumer

@SpringBootTest
class AbstractValueComponentDelegateTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    @Override
    void setup() {
        registerViewBasePackages("component.delegate")
    }

    def "AbstractValueComponentDelegate.setValueSource accepts not only EntityValueSource"() {
        when: "Set custom value source"
        def integerField = uiComponents.create(JmixIntegerField.class)
        def customValueSource = new CustomValueSource<Integer>()
        def valueBinding = new AbstractValueBinding(customValueSource, integerField) {
            @Override
            protected Object getComponentValue() {
                return null
            }

            @Override
            protected void setComponentValue(@Nullable Object value) {

            }
        }
        def integerFieldDelegate = new IntegerFieldDelegate(integerField) {
            @Override
            protected AbstractValueBinding<Integer> createValueBinding(ValueSource<Integer> valueSource) {
                return valueBinding
            }
        }

        integerFieldDelegate.setValueSource(customValueSource)

        then:
        noExceptionThrown()
    }

    class CustomValueSource<V> implements ValueSource<V> {
        @Override
        BindingState getState() {
            return null
        }

        @Override
        Registration addStateChangeListener(Consumer<StateChangeEvent> listener) {
            return null
        }

        @Override
        Class getType() {
            return null
        }

        @Override
        Object getValue() {
            return null
        }

        @Override
        void setValue(@Nullable Object value) {

        }

        @Override
        boolean isReadOnly() {
            return false
        }

        @Override
        Registration addValueChangeListener(Consumer listener) {
            return null
        }
    }
}
