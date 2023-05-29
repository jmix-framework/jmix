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

package spec.haulmont.cuba.web.components.pickerfield

import com.haulmont.cuba.core.model.common.Role
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.gui.components.PickerField
import spec.haulmont.cuba.web.UiScreenSpec

@SuppressWarnings("GroovyAssignabilityCheck")
class PickerFieldIncorrectValueTest extends UiScreenSpec {

    def "PickerField throws exception if metaClass is not set"() {
        def pickerField = cubaUiComponents.create(PickerField.class)

        when:
        pickerField.setValue(metadata.create(User))

        then:
        thrown(IllegalStateException)
        pickerField.getValue() == null
    }

    def "PickerField throws exception if metaClass does not match value type"() {
        def pickerField = cubaUiComponents.create(PickerField.class)

        when:
        pickerField.setMetaClass(metadata.getClass(Role))
        pickerField.setValue(metadata.create(User))

        then:
        thrown(IllegalArgumentException)
        pickerField.getValue() == null
    }
}