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

package spec.haulmont.cuba.web.components.optionslist

import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.model.sales.OrderLine
import com.haulmont.cuba.core.model.sales.Product
import com.haulmont.cuba.gui.components.OptionsList
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.data.DsBuilder
import com.haulmont.cuba.gui.data.impl.DatasourceImpl
import io.jmix.core.FetchPlan
import io.jmix.ui.component.Component
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.optionslist.screens.OptionsListTestScreen

import java.util.function.Consumer

class OptionsListTest extends UiScreenSpec {

    @SuppressWarnings(['GroovyAssignabilityCheck', 'GroovyAccessibility'])
    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.optionslist.screens', 'com.haulmont.cuba.web.app.main.'])
    }

    def 'List value is propagated to ValueSource from multiselect OptionsList'() {
        showMainScreen()

        def screen = screens.create(OptionsListTestScreen)
        screen.show()

        def optionsList = screen.optionsList as OptionsList<List<OrderLine>, OrderLine>
        def orderLine = screen.allOrderLinesDc.getItems().get(0)
        def orderLinesDc = screen.orderLinesDc

        when: 'List value is set to OptionsList'
        optionsList.setValue([orderLine])

        then: 'ValueSource is updated'
        orderLinesDc.items.size() == 1 && orderLinesDc.items.contains(orderLine)
    }

    def 'List value is propagated to multiselect OptionsList from ValueSource'() {
        showMainScreen()

        def screen = screens.create(OptionsListTestScreen)
        screen.show()

        def optionsList = screen.optionsList as OptionsList<List<OrderLine>, OrderLine>
        def orderLine = screen.allOrderLinesDc.getItems().get(0)

        when: 'List value is set to ValueSource'
        screen.orderLinesDc.mutableItems.add(orderLine)

        then: 'OptionsList is updated'
        optionsList.value.size() == 1 && optionsList.value.contains(orderLine)
    }

    def 'Set value is propagated to ValueSource from multiselect OptionsList'() {
        showMainScreen()

        def screen = screens.create(OptionsListTestScreen)
        screen.show()

        def optionsList = screen.setOptionsList as OptionsList<Set<Product>, Product>
        def product = screen.allProductsDc.items.get(0)
        def catalog = screen.catalogDc.item

        when: 'Set value is set to OptionsList'
        optionsList.setValue(Collections.singleton(product))

        then: 'ValueSource is updated'
        catalog.products.size() == 1 && catalog.products.contains(product)
    }

    def 'Value is propagated to ValueSource from single select OptionsList'() {
        showMainScreen()

        def screen = screens.create(OptionsListTestScreen)
        screen.show()

        def optionsList = screen.singleOptionsList
        def product = screen.allProductsDc.items.get(0)

        when: 'A value is set to single select OptionsList'
        optionsList.setValue(product)

        then: 'Property container is updated'
        screen.productDc.item == product
    }

    def 'Value is propagated to single select OptionsList from ValueSource'() {
        showMainScreen()

        def screen = screens.create(OptionsListTestScreen)
        screen.show()

        def singleOptionsList = screen.singleOptionsList
        def product = screen.allProductsDc.items.get(0)

        when: 'A value is set to property container'
        screen.orderLineDc.item.product = product

        then: 'Single select OptionsList is updated'
        singleOptionsList.value == product
    }

    def 'ValueChangeEvent is fired exactly once for OptionsList'() {
        showMainScreen()

        def screen = screens.create(OptionsListTestScreen)
        screen.show()

        def optionsList = screen.optionsList as OptionsList<List<OrderLine>, OrderLine>
        def requiredOptionsList = screen.requiredOptionsList as OptionsList<List<OrderLine>, OrderLine>
        def singleOptionsList = screen.singleOptionsList as OptionsList<Product, Product>

        def valueChangeListener = Mock(Consumer)
        def requiredValueChangeListener = Mock(Consumer)
        def singleValueChangeListener = Mock(Consumer)

        optionsList.addValueChangeListener(valueChangeListener)
        requiredOptionsList.addValueChangeListener(requiredValueChangeListener)
        singleOptionsList.addValueChangeListener(singleValueChangeListener)

        def order = screen.orderDc.item
        def orderLine = screen.orderLineDc.item

        def olOption = screen.allOrderLinesDc.items.get(0)
        def secondOlOption = screen.allOrderLinesDc.items.get(1)

        def productOption = screen.allProductsDc.items.get(0)

        when: 'A value is set to OptionsList'
        optionsList.setValue([olOption])
        singleOptionsList.setValue(productOption)

        then: 'ValueChangeEvent is fired once'
        1 * valueChangeListener.accept(_)
        1 * requiredValueChangeListener.accept(_)
        1 * singleValueChangeListener.accept(_)

        when: 'ValueSource is changed'
        screen.orderLinesDc.mutableItems.add(secondOlOption)

        then: 'ValueChangeEvent is fired once'
        1 * valueChangeListener.accept(_)
        1 * requiredValueChangeListener.accept(_)

        when: 'Entity property value is set to null'
        order.orderLines = null
        orderLine.product = null

        then: 'ValueChangeEvent is fired once'
        1 * valueChangeListener.accept(_)
        1 * requiredValueChangeListener.accept(_)
        1 * singleValueChangeListener.accept(_)
    }

    def testNew() {
        when:
        Component component = cubaUiComponents.create(OptionsList)

        then:
        component != null
        component instanceof OptionsList
    }

    def testGetSetValue() {
        when:
        def component = cubaUiComponents.create(OptionsList)

        then:
        component.value == null

        when:
        component.setOptionsList(["One", "Two", "Three"])
        component.setValue("One")

        then:
        component.value == 'One'
    }

    def testSetToReadonly() {
        when:
        def component = cubaUiComponents.create(OptionsList)

        component.setEditable(false)

        then:
        !component.editable

        when:

        component.setOptionsList(new ArrayList<>(Arrays.asList("One", "Two", "Three")))
        component.setValue("One")

        then:
        component.value == 'One'
        !component.editable
    }

    def testSetToReadonlyFromValueListener() {
        when:
        def component = cubaUiComponents.create(OptionsList)

        then:
        component.editable

        when:
        component.addValueChangeListener({ e -> component.setEditable(false) })

        component.setOptionsList(["One", "Two", "Three"])
        component.setValue("One")

        then:
        component.value == 'One'
        !component.editable
    }

    def testDatasource() {
        when:
        def component = cubaUiComponents.create(OptionsList)

        //noinspection unchecked
        def testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource() as Datasource<User>

        testDs.setItem(new User())
        ((DatasourceImpl) testDs).valid()

        then:
        component.value == null

        when:

        component.setDatasource(testDs, "group")

        then:
        component.datasource != null
    }


    def testOptionsDatasource() {
        when:
        def component = cubaUiComponents.create(OptionsList)

        //noinspection unchecked
        def testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource() as Datasource<User>

        testDs.setItem(new User())
        ((DatasourceImpl) testDs).valid()

        then:
        component.value == null

        when:
        Group g = new Group()
        g.setName("Group 0")
        testDs.getItem().setGroup(g)

        //noinspection unchecked
        CollectionDatasource<Group, UUID> groupsDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(Group.class)
                .setView(viewRepository.getFetchPlan(Group.class, FetchPlan.LOCAL))
                .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                .setAllowCommit(false)
                .buildCollectionDatasource()

        Group g1 = new Group()
        g1.setName("Group 1")
        groupsDs.includeItem(g1)
        Group g2 = new Group()
        g2.setName("Group 2")
        groupsDs.includeItem(g2)

        component.setOptionsDatasource(groupsDs)
        component.setValue(g2)

        then:
        g2 == component.value

        when:
        component.setDatasource(testDs, "group")
        component.setValue(g)

        then:
        g == testDs.item.group

        when:
        component.setValue(g1)

        then:
        g1 == testDs.item.group

        when:
        testDs.getItem().setGroup(g2)

        then:
        g2 == component.value
    }
}
