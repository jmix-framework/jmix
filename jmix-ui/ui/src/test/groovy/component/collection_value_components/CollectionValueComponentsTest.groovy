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

package component.collection_value_components

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.*
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import spock.lang.Unroll
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class CollectionValueComponentsTest extends ScreenSpecification {

    @Unroll
    def "both null and empty collection must be considered as empty value by collection value components"() {
        Field<Collection<?>> field = uiComponents.create(name)

        when:
        field.setValue(null)

        then:
        field.isEmpty()

        when:
        field.setValue(Collections.emptyList())

        then:
        field.isEmpty()

        when:
        field.setValue(Collections.singletonList("value"))

        then:
        !field.isEmpty()

        where:
        name << [
                ValuesPicker.NAME,
                TagPicker.NAME,
                TagField.NAME,
                CheckBoxGroup.NAME,
                MultiSelectList.NAME,
                TwinColumn.NAME
        ]
    }
}
