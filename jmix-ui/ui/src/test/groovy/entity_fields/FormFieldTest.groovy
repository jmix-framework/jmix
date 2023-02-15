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

package entity_fields

import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.ScreenBuilders
import io.jmix.ui.UiConfiguration
import io.jmix.ui.action.entitypicker.EntityClearAction
import io.jmix.ui.action.entitypicker.EntityLookupAction
import io.jmix.ui.component.EntityComboBox
import io.jmix.ui.component.EntityPicker
import io.jmix.ui.UiComponentProperties
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Address
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.screen.OrderEdit

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class FormFieldTest extends ScreenSpecification {
    @Autowired
    ScreenBuilders screenBuilders
    @Autowired
    DataManager dataManager
    @Autowired
    UiComponentProperties componentProperties

    private Customer customer1
    private Customer customer2
    private Order order
    private mainScreen

    @Override
    void setup() {
        exportScreensPackages(['test_support.entity.sales.screen'])

        customer1 = dataManager.save(new Customer(name: 'c1', address: new Address()))
        customer2 = dataManager.save(new Customer(name: 'c2', address: new Address()))
        order = dataManager.save(new Order(number: '1'))

        mainScreen = showTestMainScreen()
    }

    @Override
    void cleanup() {
        componentProperties.getEntityFieldType().remove('test_Customer')
        componentProperties.getEntityFieldActions().remove('test_Customer')

        dataManager.remove(order, customer1, customer2)
    }

    def "entityPicker is specified in properties"() {
        componentProperties.getEntityFieldType().put('test_Customer', EntityPicker.NAME)
        componentProperties.getEntityFieldActions().remove('test_Customer')

        when:
        def editor = showOrderEdit()

        then:
        fieldIsEntityPicker(editor.customerField, [EntityLookupAction.ID, EntityClearAction.ID])

        when: "specify actions in properties"
        def actionIds = ['entity_lookup', 'entity_open', 'entity_clear']
        componentProperties.getEntityFieldActions().put('test_Customer', actionIds)
        editor = showOrderEdit()

        then:
        fieldIsEntityPicker(editor.customerField, actionIds)
    }

    def "entityPicker is not specified in properties"() {
        componentProperties.getEntityFieldType().remove('test_Customer')
        componentProperties.getEntityFieldActions().remove('test_Customer')

        when:
        def editor = showOrderEdit()

        then:
        fieldIsEntityPicker(editor.customerField, [EntityLookupAction.ID, EntityClearAction.ID])

        when: "specify actions in properties"
        def actionIds = ['entity_lookup', 'entity_open', 'entity_clear']
        componentProperties.getEntityFieldActions().put('test_Customer', actionIds)
        editor = showOrderEdit()

        then:
        fieldIsEntityPicker(editor.customerField, actionIds)
    }

    def "entityComboBox is specified in properties"() {
        componentProperties.getEntityFieldType().put('test_Customer', EntityComboBox.NAME)

        when:
        def editor = showOrderEdit()

        then:
        fieldIsEntityComboBox(editor.customerField, [])

        when: "specify actions in properties"
        def actionIds = ['entity_lookup', 'entity_open', 'entity_clear']
        componentProperties.getEntityFieldActions().put('test_Customer', actionIds)
        editor = showOrderEdit()

        then:
        fieldIsEntityComboBox(editor.customerField, actionIds)
    }

    def "entityComboBox is not specified in properties, field with options"() {
        componentProperties.getEntityFieldType().remove('test_Customer')
        componentProperties.getEntityFieldActions().remove('test_Customer')

        when:
        def editor = showOrderEdit()

        then:
        fieldIsEntityComboBox(editor.customerFieldWithOptions, [])

        when: "specify actions in properties"
        def actionIds = ['entity_lookup', 'entity_open', 'entity_clear']
        componentProperties.getEntityFieldActions().put('test_Customer', actionIds)
        editor = showOrderEdit()

        then:
        fieldIsEntityComboBox(editor.customerFieldWithOptions, actionIds)
    }

    private OrderEdit showOrderEdit() {
        def editor = screenBuilders.editor(Order, mainScreen)
                .withScreenClass(OrderEdit)
                .editEntity(order)
                .withOpenMode(OpenMode.DIALOG)
                .build()
        editor.show()
        return editor
    }

    private void fieldIsEntityComboBox(def field, List actionIds) {
        assert field instanceof EntityComboBox
        EntityComboBox comboBox = (EntityComboBox) field
        assert comboBox.actions.collect { it.id } == actionIds
    }

    private void fieldIsEntityPicker(def field, List actionIds) {
        assert field instanceof EntityPicker
        assert !(field instanceof EntityComboBox)
        assert ((EntityPicker) field).actions.collect { it.id } == actionIds
    }
}
