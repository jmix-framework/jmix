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

package component.menu

import component.menu.screen.MenuPropertiesInjectionTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.LoadContext
import io.jmix.core.common.util.Dom4j
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.menu.MenuConfig
import io.jmix.ui.menu.MenuItem
import io.jmix.ui.menu.MenuItemCommands
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.bean.TestWebBean
import test_support.entity.sales.Order
import test_support.entity.sales.screen.OrderEdit

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class MenuItemCommandsTest extends ScreenSpecification {

    @Autowired
    MenuItemCommands menuCommands

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    MenuConfig menuConfig

    def orderId = UUID.fromString('60885987-1b61-4247-94c7-dff348347f93')

    @Override
    void setup() {
        exportScreensPackages(['component.menu.screen', 'test_support.entity.sales.screen'])

        TestMenuItemConsumer.launched.set(false)
        TestMenuItemConsumer.applicationContextIsSet.set(false)

        TestMenuItemRunnable.launched.set(false)
        TestMenuItemRunnable.applicationContextIsSet.set(false)

        TestRunnable.launched.set(false)
        TestRunnable.applicationContextIsSet.set(false)

        def order = dataManager.create(Order)
        order.setId(orderId)
        dataManager.save(order)
    }

    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    @SuppressWarnings(['GroovyAccessibility'])
    def 'Create and run Screen command'() {
        def mainScreen = showTestMainScreen()

        when: 'Screen menu item command with params and properties is running'
        def menuItem = createScreenMenuItem()
        def screenCmd = menuCommands.create(mainScreen, menuItem) as MenuItemCommands.ScreenCommand

        screenCmd.run()

        then: 'All params are loaded, all properties are injected into UI Controller'
        screenCmd.getDescription() == 'Opening window: "test_MenuPropertiesInjectionTestScreen"'

        menuItem.properties.find { it.name == 'testIntProperty' && it.value == '42' }
        menuItem.properties.find { it.name == 'testStringProperty' && it.value == 'Hello World!' }
        menuItem.properties.find { it.name == 'entityToEdit' }

        MenuPropertiesInjectionTestScreen testScreen = screens.getOpenedScreens().getActiveScreens().stream()
                .filter({ it instanceof MenuPropertiesInjectionTestScreen })
                .findFirst()
                .orElseThrow({
                    throw new IllegalStateException('MenuPropertiesInjectionTestScreen should be in opened screens')
                }) as MenuPropertiesInjectionTestScreen

        def order = dataManager.load(Id.of(orderId, Order.class)).one()

        testScreen.testIntProperty == 42
        testScreen.testStringProperty == 'Hello World!'
        testScreen.entityToEdit == order
    }

    @SuppressWarnings('GroovyAccessibility')
    def 'Create and run Editor Screen command'() {
        def mainScreen = showTestMainScreen()

        when: 'Editor screen command is running'
        menuCommands.create(mainScreen, createEditorMenuItem())
                .run()

        then: 'The "entityToEdit" property is injected'
        def userEditor = screens.getOpenedScreens()
                .getActiveScreens()
                .stream()
                .filter({ it instanceof OrderEdit })
                .findFirst()
                .orElseThrow({
                    throw new IllegalStateException('OrderEdit should be in active screens')
                }) as OrderEdit

        def order = dataManager.load(Id.of(orderId, Order.class)).one()

        userEditor.getEditedEntity() == order
    }

    @SuppressWarnings('GroovyAccessibility')
    def 'Create and run Bean command'() {
        def mainScreen = showTestMainScreen()

        when: 'Bean command menu item is running'
        def beanCommand = menuCommands.create(mainScreen, createBeanMenuItem())
        beanCommand.run()

        then: 'Corresponding bean method is invoked'
        beanCommand.getDescription() == 'Calling bean method: test_WebBean#testMethod'

        applicationContext.getBean(TestWebBean)
                .testMethodInvoked
                .get()
    }

    @SuppressWarnings('GroovyAccessibility')
    def 'Create and run Bean command with params'() {
        def mainScreen = showTestMainScreen()

        when: 'Bean command menu item is running'
        def beanCommand = menuCommands.create(mainScreen, createBeanMenuItemWithParams())
        beanCommand.run()

        then: 'Corresponding bean method is invoked'
        beanCommand.getDescription() == 'Calling bean method: test_WebBean#testMethodWithParams'

        applicationContext.getBean(TestWebBean)
                .paramsExist
                .get()
    }


    @SuppressWarnings('GroovyAccessibility')
    def 'Create and run Runnable command'() {
        when: 'Runnable class menu item is running'
        def runnableCmd = menuCommands
                .create(null, createRunnableMenuItem())
        runnableCmd.run()

        then: 'Corresponding Runnable instance is launched'
        runnableCmd.getDescription() == 'Running "component.menu.TestRunnable"'

        TestRunnable.launched.get()
        TestRunnable.applicationContextIsSet.get()
    }

    def 'Create and run MenuItemRunnable command'() {
        when: 'MenuItemRunnable menu item is running'
        def menuItemRunnableCmd = menuCommands
                .create(null, createMenuItemRunnable())
        menuItemRunnableCmd.run()

        then: 'Corresponding instance is launched'
        menuItemRunnableCmd.getDescription() == 'Running "component.menu.TestMenuItemRunnable"'

        TestMenuItemRunnable.launched.get()
        TestMenuItemRunnable.applicationContextIsSet.get()
    }

    def 'Create and run Consumer command'() {
        when: 'Menu item params consumer is running'
        def consumerCmd = menuCommands
                .create(null, createConsumerMenuItem())
        consumerCmd.run()

        then: 'Corresponding instance is launched'
        consumerCmd.getDescription() == 'Running "component.menu.TestMenuItemConsumer"'

        TestMenuItemConsumer.launched.get()
        TestMenuItemConsumer.applicationContextIsSet.get()
    }

    MenuItem createScreenMenuItem() {
        def itemDescriptor = Dom4j.readDocument('''
<item screen="test_MenuPropertiesInjectionTestScreen"  openType="NEW_TAB" resizable="true">
    <properties>
        <property name="testIntProperty" value="42"/>
        <property name="testStringProperty" value="Hello World!"/>
        <property name="entityToEdit" entityClass="test_support.entity.sales.Order" 
                entityId="60885987-1b61-4247-94c7-dff348347f93" entityFetchPlan="_local"/> 
    </properties>
</item>
''').rootElement
        def menuItem = menuConfig.createMenuItem(itemDescriptor, null);
        menuItem.setDescriptor(itemDescriptor)
        menuItem
    }

    MenuItem createEditorMenuItem() {
        def itemDescriptor = Dom4j.readDocument('''
<item screen="test_Order.edit">
    <properties>
        <property name="entityToEdit"
                  entityClass="test_support.entity.sales.Order"
                  entityId="60885987-1b61-4247-94c7-dff348347f93"/>
    </properties>
</item>
''').rootElement
        def menuItem = menuConfig.createMenuItem(itemDescriptor, null);
        menuItem.setDescriptor(itemDescriptor)
        menuItem
    }

    MenuItem createBeanMenuItem() {
        def itemDescriptor = Dom4j.readDocument(
                '''
                <item id="testBeanItem" bean="test_WebBean" beanMethod="testMethod"></item>
                ''').rootElement
        def menuItem = menuConfig.createMenuItem(itemDescriptor, null)
        menuItem.setDescriptor(itemDescriptor)
        menuItem
    }

    MenuItem createBeanMenuItemWithParams() {
        def itemDescriptor = Dom4j.readDocument(
                '''
                <item id="testBeanItem" bean="test_WebBean" beanMethod="testMethodWithParams">
                    <properties>
                        <property name="param1" value="value1"/>
                    </properties>
                </item>
                ''').rootElement
        def menuItem = menuConfig.createMenuItem(itemDescriptor, null)
        menuItem.setDescriptor(itemDescriptor)
        menuItem
    }

    MenuItem createRunnableMenuItem() {
        def itemDescriptor = Dom4j.readDocument(
                '''
                <item id="testClassItem" class="component.menu.TestRunnable"></item>
                ''').rootElement
        def menuItem = menuConfig.createMenuItem(itemDescriptor, null)
        menuItem.setDescriptor(itemDescriptor)
        menuItem
    }

    MenuItem createMenuItemRunnable() {
        def itemDescriptor = Dom4j.readDocument(
                '''
                <item id="testClassItem" class="component.menu.TestMenuItemRunnable"></item>
                ''').rootElement
        def menuItem = menuConfig.createMenuItem(itemDescriptor, null)
        menuItem.setDescriptor(itemDescriptor)
        menuItem
    }

    MenuItem createConsumerMenuItem() {
        def itemDescriptor = Dom4j.readDocument(
                '''
                <item id="testConsumerItem" class="component.menu.TestMenuItemConsumer"></item>
                ''').rootElement
        def menuItem = menuConfig.createMenuItem(itemDescriptor, null);
        menuItem.setDescriptor(itemDescriptor)
        menuItem
    }
}
