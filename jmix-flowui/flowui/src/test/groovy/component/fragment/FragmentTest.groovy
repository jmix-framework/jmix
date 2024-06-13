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

package component.fragment

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.provider.Query
import component.fragment.component.*
import component.fragment.view.TestFragmentHostView
import component.fragment.view.TestParametersFragmentHostView
import component.standarddetailview.view.BlankTestView
import io.jmix.core.DataManager
import io.jmix.flowui.Fragments
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.data.grid.ContainerDataGridItems
import io.jmix.flowui.data.grid.EmptyDataGridItems
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.exception.GuiDevelopmentException
import io.jmix.flowui.fragment.FragmentUtils
import io.jmix.flowui.impl.UiComponentsImpl
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.lang.Nullable
import test_support.entity.petclinic.Address
import test_support.entity.petclinic.Country
import test_support.entity.sales.Product
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FragmentTest extends FlowuiTestSpecification {

    @Autowired
    Fragments fragments
    @Autowired
    DataComponents dataComponents
    @Autowired
    DataManager dataManager
    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    ViewNavigators viewNavigators
    @Autowired
    UiComponentsImpl uiComponents

    private Country country1
    private Country country2

    @Override
    void setup() {
        registerViewBasePackages("component.standarddetailview.view", "component.fragment.view")

        country1 = dataManager.create(Country)
        country1.name = "Country 1"
        country2 = dataManager.create(Country)
        country1.name = "Country 2"

        def product1 = dataManager.create(Product)
        product1.name = "Product 1"
        def product2 = dataManager.create(Product)
        product2.name = "Product 2"

        dataManager.save(country1, country2, product1, product2)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from PC_COUNTRY")
        jdbcTemplate.execute("delete from TEST_PRODUCT")
    }

    def "Fragment containing a DataGrid with MetaClass and relative path to descriptor"() {
        when:
        def parent = navigateToView(BlankTestView)
        def dataGridFragment = fragments.create(parent, TestDataGridFragment)

        then:
        dataGridFragment.clicks == 0
        dataGridFragment.testBtn.text == "Test"
        getIconAttribute(dataGridFragment.testBtn.icon) == getIconAttribute(VaadinIcon.PLUS.create())
        dataGridFragment.items instanceof EmptyDataGridItems

        when:
        dataGridFragment.click()

        then:
        dataGridFragment.getClicks() == 1

        when:
        def container = dataComponents.createCollectionContainer(Product)
        container.setItems(List.of(
                dataManager.create(Product),
                dataManager.create(Product)
        ))
        dataGridFragment.setDataContainer(container)

        then:
        dataGridFragment.items instanceof ContainerDataGridItems
        dataGridFragment.items.items.size() == 2
    }

    def "Fragment containing a DataGrid and provided data components"() {
        def parent = navigateToView(BlankTestView)
        viewNavigators.detailView(parent, Address)
                .withViewClass(TestFragmentHostView)
                .navigate()
        TestFragmentHostView hostView = UiTestUtils.getCurrentView()

        when:
        def dataGridFragmentProvided = hostView.dataGridFragmentProvided

        then:
        dataGridFragmentProvided.clicks == 0
        dataGridFragmentProvided.testBtn.text == "Test"
        getIconAttribute(dataGridFragmentProvided.testBtn.icon) == getIconAttribute(VaadinIcon.PLUS.create())

        and:
        dataGridFragmentProvided.items instanceof ContainerDataGridItems
        dataGridFragmentProvided.items.items.size() == 2

        when:
        dataGridFragmentProvided.click()

        then:
        dataGridFragmentProvided.getClicks() == 1

        when:
        def dataGridFragment = hostView.dataGridFragment

        then:
        dataGridFragment.clicks == 0
        dataGridFragment.testBtn.text == "Test"
        getIconAttribute(dataGridFragment.testBtn.icon) == getIconAttribute(VaadinIcon.PLUS.create())

        and:
        dataGridFragment.items instanceof ContainerDataGridItems
        dataGridFragment.items.items.size() == 2

        when:
        dataGridFragment.click()

        then:
        dataGridFragment.getClicks() == 1
    }

    def "Fragment with FormLayout and data components"() {
        def parent = navigateToView(BlankTestView)

        when:
        def addressFragment = fragments.create(parent, TestAddressFragment)

        then:
        FragmentUtils.getFragmentData(addressFragment).containerIds.size() == 2

        when:
        def countryField = addressFragment.countryField

        then:
        countryField.label == "Address.country"
        countryField.valueSource instanceof ContainerValueSource
        countryField.dataProvider.size(new Query<Country, ?>()) == 2

        when:
        def cityField = addressFragment.cityField

        then:
        cityField.label == "Address.city"
        cityField.valueSource instanceof ContainerValueSource

        when:
        def postcodeField = addressFragment.postcodeField

        then:
        postcodeField.label == "Address.postcode"
        postcodeField.valueSource instanceof ContainerValueSource
    }

    def "Fragment with FormLayout and provided data components"() {
        def parent = navigateToView(BlankTestView)
        viewNavigators.detailView(parent, Address)
                .withViewClass(TestFragmentHostView)
                .navigate()
        TestFragmentHostView hostView = UiTestUtils.getCurrentView()
        def entity = hostView.editedEntity
        entity.country = country1
        entity.postcode = "123456"

        def addressFragment = hostView.addressFragment

        when:
        def fragmentData = FragmentUtils.getFragmentData(addressFragment)
        def countriesDl = fragmentData.getLoader("countriesDl")

        then: "countriesDl has query from host view"
        countriesDl.query == "select e from pc_Country e"

        when:
        def countryField = addressFragment.countryField

        then:
        countryField.label == "Address.country"
        countryField.valueSource instanceof ContainerValueSource
        countryField.dataProvider.size(new Query<Country, ?>()) == 2
        countryField.value == country1

        when:
        def postcodeField = addressFragment.postcodeField

        then:
        postcodeField.label == "Address.postcode"
        postcodeField.valueSource instanceof ContainerValueSource
        postcodeField.typedValue == "123456"

        when:
        countryField.value = country2

        then:
        entity.country == country2
    }

    def "Fragment as field and full path to descriptor"() {
        when:
        def parent = navigateToView(BlankTestView)
        def stepperField = fragments.create(parent, TestStepperField)

        then:
        stepperField.value == 0

        when:
        stepperField.clickUp()

        then:
        stepperField.value == 1

        when:
        stepperField.clickDown()

        then:
        stepperField.value == 0
    }

    def "Fragment with programmatic creation of nested components"() {
        when:
        def parent = navigateToView(BlankTestView)
        def textField = fragments.create(parent, TestTypedTextField)

        then:
        textField.getContent().isClearButtonVisible()
        textField.readyListenerFired
    }

    def "Fragment with several root components in XML"() {
        when:
        def parent = navigateToView(BlankTestView)
        def component = fragments.create(parent, TestIncorrectStepperField)

        then:
        thrown(GuiDevelopmentException)
    }

    def "Fragment with incorrect root component type in XML"() {
        when:
        def parent = navigateToView(BlankTestView)
        def component = fragments.create(parent, TestIncorrectTypedTextField)

        then:
        thrown(GuiDevelopmentException)
    }

    def "Extended Fragment inherits a full path XML descriptor"() {
        when:
        def parent = navigateToView(BlankTestView)
        def stepperField = fragments.create(parent, TestStepperFieldExt)

        then:
        stepperField.value == 0

        when:
        stepperField.clickUp()

        then:
        stepperField.value == 1

        when:
        stepperField.clickDown()

        then:
        stepperField.value == 0
    }

    def "Fragment with different components"() {
        when:
        def parent = navigateToView(BlankTestView)
        def component = fragments.create(parent, TestFragment)
        def tabSheet = component.tabSheet

        then: "TabSheet correctly loads tabs"
        tabSheet.getTabAt(0).getId().orElse("") == "tab1"
        tabSheet.getTabAt(0).getLabel() == "Tab 1"

        tabSheet.getTabAt(1).getId().orElse("") == "tab2"
        tabSheet.getTabAt(1).getLabel() == "Tab 2"

        when:
        def tabs = component.tabs

        then: "Tabs correctly loads tabs"
        tabs.getTabAt(0).getId().orElse("") == "tab1"
        tabs.getTabAt(0).getLabel() == "Tab 1"

        tabs.getTabAt(1).getId().orElse("") == "tab2"
        tabs.getTabAt(1).getLabel() == "Tab 2"

        when:
        def accordionPanel1 = component.accordionPanel1
        def accordionPanel2 = component.accordionPanel2

        then: "Accordion panels are found"
        accordionPanel1.summaryText == "Panel 1"
        accordionPanel2.summaryText == "Panel 2"

        when:
        def dropdownButton = component.dropdownButton
        def dropdownButtonItem1 = dropdownButton.getItem("componentItem")
        def dropdownButtonSubpart1 = dropdownButton.getSubPart("componentItem")
        def dropdownButtonItem2 = dropdownButton.getItem("textItem")
        def dropdownButtonSubpart2 = dropdownButton.getSubPart("textItem")

        then: "DropdownButton items can be found by id"
        dropdownButtonItem1 != null
        dropdownButtonSubpart1 != null
        dropdownButtonItem1 == dropdownButtonSubpart1

        dropdownButtonItem2 != null
        dropdownButtonSubpart2 != null
        dropdownButtonItem2 == dropdownButtonSubpart2
    }

    def "Fragment gets provided data components through host fragment"() {
        def parent = navigateToView(BlankTestView)
        viewNavigators.detailView(parent, Address)
                .withViewClass(TestFragmentHostView)
                .navigate()
        TestFragmentHostView hostView = UiTestUtils.getCurrentView()

        when:
        def hostFragment = hostView.hostFragment
        def dataGridFragment = hostFragment.dataGridFragment

        then:
        dataGridFragment.items instanceof ContainerDataGridItems
        dataGridFragment.items.items.size() == 2
    }

    def "Fragment can be replaced by inheritor"() {
        def parent = navigateToView(BlankTestView)

        when:
        def fragment1 = fragments.create(parent, TestOriginFragment)

        then:
        fragment1.message == "Origin"

        when: "register replacement"
        uiComponents.register(TestOriginReplacementFragment, TestOriginFragment)

        and: "create fragment by parent class"
        def fragment2 = fragments.create(parent, TestOriginFragment)

        then: "inheritor is created"
        fragment2 instanceof TestOriginReplacementFragment
        fragment2.message == "Replacement"
    }

    def "Parameters are set after UI components are injected"() {
        def parent = navigateToView(BlankTestView)

        when:
        viewNavigators.view(parent, TestParametersFragmentHostView)
                .navigate()

        then:
        noExceptionThrown()
    }

    @Nullable
    private static String getIconAttribute(Component icon) {
        return icon != null ? icon.element.getAttribute("icon") : null
    }
}
