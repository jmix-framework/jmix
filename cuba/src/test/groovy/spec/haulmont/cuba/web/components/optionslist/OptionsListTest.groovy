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
import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.optionslist.screens.OptionsListTestWindow
import spock.lang.Ignore

class OptionsListTest extends UiScreenSpec {

    @SuppressWarnings(['GroovyAssignabilityCheck', 'GroovyAccessibility'])
    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.optionslist.screens', 'com.haulmont.cuba.web.app.main.'])
    }

    def 'List value is propagated to ValueSource from multiselect OptionsList'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('main', OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = createLegacyScreen()
        screen.show()

        def optionsList = screen.optionsList as OptionsList<List<OrderLine>, OrderLine>
        def orderLine = screen.allOrderLinesDs.items.iterator().next()
        def orderLinesDs = screen.orderLinesDs

        when: 'List value is set to OptionsList'
        optionsList.setValue([orderLine])

        then: 'ValueSource is updated'
        orderLinesDs.items.size() == 1 && orderLinesDs.items.contains(orderLine)
    }

    /*
     * Most likely nested collection datasource cannot correctly notify component about collection changed
     */
    @Ignore
    def 'List value is propagated to multiselect OptionsList from ValueSource'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('main', OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = createLegacyScreen()
        screen.show()

        def optionsList = screen.optionsList as OptionsList<List<OrderLine>, OrderLine>
        def orderLine = screen.allOrderLinesDs.getItems().iterator().next()

        when: 'List value is set to ValueSource'
        screen.orderLinesDs.addItem(orderLine)

        then: 'OptionsList is updated'
        optionsList.value.size() == 1 && optionsList.value.contains(orderLine)
    }

    def 'Set value is propagated to ValueSource from multiselect OptionsList'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('main', OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = createLegacyScreen()
        screen.show()

        def optionsList = screen.setOptionsList as OptionsList<Set<Product>, Product>
        def product = screen.allProductsDs.items.iterator().next()
        def catalog = screen.catalogDs.item

        when: 'Set value is set to OptionsList'
        optionsList.setValue(Collections.singleton(product))

        then: 'ValueSource is updated'
        catalog.products.size() == 1 && catalog.products.contains(product)
    }

    def 'Value is propagated to ValueSource from single select OptionsList'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('main', OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = createLegacyScreen()
        screen.show()

        def optionsList = screen.singleOptionsList
        def product = screen.allProductsDs.items.iterator().next()

        when: 'A value is set to single select OptionsList'
        optionsList.setValue(product)

        then: 'Property container is updated'
        screen.productDs.item == product
    }

    def 'Value is propagated to single select OptionsList from ValueSource'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('main', OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = createLegacyScreen()
        screen.show()

        def singleOptionsList = screen.singleOptionsList
        def product = screen.allProductsDs.items.iterator().next()

        when: 'A value is set to property container'
        screen.orderLineDs.item.product = product

        then: 'Single select OptionsList is updated'
        singleOptionsList.value == product
    }

    def testNew() {
        when:
        Component component = uiComponents.create(OptionsList)

        then:
        component != null
        component instanceof OptionsList
    }

    def testGetSetValue() {
        when:
        def component = uiComponents.create(OptionsList)

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
        def component = uiComponents.create(OptionsList)

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
        def component = uiComponents.create(OptionsList)

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
        def component = uiComponents.create(OptionsList)

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
        def component = uiComponents.create(OptionsList)

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

    protected OptionsListTestWindow createLegacyScreen() {
        return screens.create("optionslist-test-screen", OpenMode.NEW_TAB)
    }
}
