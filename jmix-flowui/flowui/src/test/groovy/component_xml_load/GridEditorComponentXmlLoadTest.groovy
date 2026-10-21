/*
 * Copyright 2026 Haulmont.
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

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import component_xml_load.grideditor.TestValueSourceFragment
import component_xml_load.screen.BrokenGridEditorChildDataContainerView
import component_xml_load.screen.BrokenGridEditorChildPropertyView
import component_xml_load.screen.BrokenGridEditorEditableFalseView
import component_xml_load.screen.BrokenGridEditorEmptyView
import component_xml_load.screen.BrokenGridEditorNoPropertyView
import component_xml_load.screen.BrokenGridEditorNotValueSourceView
import component_xml_load.screen.BrokenGridEditorTwoChildrenView
import component_xml_load.screen.GridEditorComponentView
import component_xml_load.screen.GridEditorFragmentHostView
import io.jmix.core.Metadata
import io.jmix.flowui.component.combobox.EntityComboBox
import io.jmix.flowui.component.combobox.JmixComboBox
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.component.grid.TreeDataGrid
import io.jmix.flowui.component.select.JmixSelect
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.data.SupportsValueSource
import io.jmix.flowui.data.value.BufferedContainerValueSource
import io.jmix.flowui.exception.GuiDevelopmentException
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.City
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.Status
import test_support.entity.sec.Group
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class GridEditorComponentXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata

    Customer ann
    Customer bob
    Order order
    Group root
    City city

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Declared editor component is built and bound to the column property"() {
        when: "Edit a row of a non-buffered grid"
        def view = open()
        def built = capture(view.customersDataGrid)
        view.customersDataGrid.editor.editItem(ann)

        then: "The declared combo box shows the row value and keeps it in the row"
        def status = built.status as JmixComboBox
        status.value == Status.OK
        status.required
        ann.status == Status.OK

        and: "It is bound to the edited row and the column property"
        def valueSource = status.valueSource as BufferedContainerValueSource
        valueSource.container.item.is(ann)
        valueSource.metaPropertyPath.toPathString() == "status"

        and: "The declared text field keeps its XML attributes after binding"
        verifyAll(built.name as TypedTextField) {
            id.get() == "nameEditor"
            maxLength == 5
            readOnly
            value == "Ann"
        }

        and: "The editable column without editorComponent gets the default component"
        built.email != null
    }

    def "Declared editor component does not wipe the row value in buffered mode"() {
        when: "Edit a row of a buffered grid and save it"
        def view = open()
        def built = capture(view.bufferedCustomersDataGrid)
        view.bufferedCustomersDataGrid.editor.editItem(ann)
        def saved = view.bufferedCustomersDataGrid.editor.save()

        then: "The row value is kept"
        built.status instanceof JmixComboBox
        saved
        ann.status == Status.OK
    }

    def "Select editor keeps the row value"() {
        when: "Edit a row of the grid with a select editor"
        def view = open()
        def built = capture(view.customersSelectDataGrid)
        view.customersSelectDataGrid.editor.editItem(ann)

        then: "The select shows the row value and keeps it in the row"
        built.status instanceof JmixSelect
        built.status.value == Status.OK
        ann.status == Status.OK
    }

    def "Entity combo box editor keeps the reference and resolves its items container"() {
        when: "Edit an order"
        def view = open()
        def built = capture(view.ordersDataGrid)
        view.ordersDataGrid.editor.editItem(order)

        then: "The entity combo box shows the reference and keeps it in the row"
        def customer = built.customer as EntityComboBox
        customer.value.is(ann)
        order.customer.is(ann)
        customer.getMetaClass().name == "test_Customer"
        customer.genericDataView.items.toList() == [ann, bob]
    }

    def "XML required takes precedence over a mandatory property"() {
        when: "Edit a row whose mandatory property has an editor declared as not required"
        def view = open()
        def built = capture(view.citiesDataGrid)
        view.citiesDataGrid.editor.editItem(city)

        then: "The declared text field is not required"
        built.name instanceof TypedTextField
        !(built.name as TypedTextField).required
    }

    def "Width is full unless declared"() {
        when: "Edit rows of grids with and without a declared width"
        def view = open()
        def customerBuilt = capture(view.customersDataGrid)
        def orderBuilt = capture(view.ordersDataGrid)
        view.customersDataGrid.editor.editItem(ann)
        view.ordersDataGrid.editor.editItem(order)

        then: "The width is full unless the element declares it"
        customerBuilt.status.width == "100%"
        orderBuilt.number.width == "10em"
    }

    def "Declared editor makes the column editable without the editable attribute"() {
        when: "Edit a row"
        def view = open()
        def built = capture(view.customersDataGrid)
        view.customersDataGrid.editor.editItem(ann)

        then: "The status column without 'editable' built its editor component"
        view.customersDataGrid.getColumnByKey("status") != null
        built.status != null
    }

    def "Declared editor component replaces the default one of an editable column"() {
        when: "Edit a row of a grid whose column has both editable and editorComponent"
        def view = open()
        def built = capture(view.editableCustomersDataGrid)
        view.editableCustomersDataGrid.editor.editItem(ann)

        then: "The declared text field is built instead of the default component"
        def name = built.name as TypedTextField
        name.id.get() == "editableNameEditor"
        name.value == "Ann"
    }

    def "Rebuild keeps the value"() {
        when: "Edit a row and refresh the editor"
        def view = open()
        def built = capture(view.customersDataGrid)
        view.customersDataGrid.editor.editItem(ann)
        def first = built.status
        view.customersDataGrid.editor.refresh()

        then: "A new component is built with the same value"
        !built.status.is(first)
        built.status.value == Status.OK
        ann.status == Status.OK
    }

    def "Buffered save writes only on save"() {
        when: "Change the value in a buffered editor"
        def view = open()
        def built = capture(view.bufferedCustomersDataGrid)
        view.bufferedCustomersDataGrid.editor.editItem(ann)
        (built.status as JmixComboBox).value = Status.NOT_OK

        then: "The row is unchanged before save"
        ann.status == Status.OK

        when: "Save the editor"
        def saved = view.bufferedCustomersDataGrid.editor.save()

        then: "The row gets the new value"
        saved
        ann.status == Status.NOT_OK
    }

    def "Buffered cancel discards"() {
        when: "Change the value in a buffered editor and cancel"
        def view = open()
        def built = capture(view.bufferedCustomersDataGrid)
        view.bufferedCustomersDataGrid.editor.editItem(ann)
        (built.status as JmixComboBox).value = Status.NOT_OK
        view.bufferedCustomersDataGrid.editor.cancel()

        then: "The row is unchanged"
        ann.status == Status.OK
    }

    def "Buffered save validates the declared component"() {
        when: "Empty a required declared field and save"
        def view = open()
        def built = capture(view.bufferedCustomersDataGrid)
        view.bufferedCustomersDataGrid.editor.editItem(ann)
        (built.status as JmixComboBox).value = null
        def saved = view.bufferedCustomersDataGrid.editor.save()

        then: "Save is refused and the row is unchanged"
        !saved
        ann.status == Status.OK
    }

    def "Buffered editor builds a new component for every row"() {
        when: "Edit a row, cancel and edit another row"
        def view = open()
        def built = capture(view.bufferedCustomersDataGrid)
        view.bufferedCustomersDataGrid.editor.editItem(ann)
        def first = built.status
        view.bufferedCustomersDataGrid.editor.cancel()
        view.bufferedCustomersDataGrid.editor.editItem(bob)

        then: "A new component is built for the other row"
        !built.status.is(first)
        built.status.value == Status.NOT_OK
        bob.status == Status.NOT_OK
    }

    def "includeAll override keeps the declared editor"() {
        when: "Edit a row of the grid with an overridden includeAll column"
        def view = open()
        def built = capture(view.includeAllCustomersDataGrid)
        view.includeAllCustomersDataGrid.editor.editItem(ann)

        then: "The declared combo box gets the enum items from the binding"
        def status = built.status as JmixComboBox
        status.value == Status.OK
        status.genericDataView.items.toList() == [Status.OK, Status.NOT_OK]
        ann.status == Status.OK
    }

    def "Tree data grid inherits the declared editor"() {
        when: "Edit a root row of the tree data grid"
        def view = open()
        def built = capture(view.groupsTreeDataGrid)
        view.groupsTreeDataGrid.editor.editItem(root)

        then: "The declared text field shows the row value"
        built.name instanceof TypedTextField
        built.name.value == "Root"
    }

    def "Fragment editor component is autowired"() {
        when: "Edit a row of a view's grid whose editor is a fragment"
        def view = openFragmentHostView()
        def built = capture(view.customersDataGrid)
        view.customersDataGrid.editor.editItem(ann)

        then: "The fragment is autowired, ready and bound"
        def fragment = built.name as TestValueSourceFragment
        fragment.innerField != null
        fragment.ready
        (fragment.valueSource as BufferedContainerValueSource).container.item.is(ann)
    }

    def "Fragment editor component is autowired in a grid hosted by a fragment"() {
        when: "Edit a row of a fragment's grid whose editor is a fragment"
        def view = openFragmentHostView()
        def grid = view.gridFragment.customersDataGrid
        def built = capture(grid)
        grid.editor.editItem(ann)

        then: "The fragment is autowired, ready and bound"
        def fragment = built.name as TestValueSourceFragment
        fragment.innerField != null
        fragment.ready
        (fragment.valueSource as BufferedContainerValueSource).container.item.is(ann)
    }

    def "Throw an exception for a broken editorComponent declaration: #description"() {
        when: "Open a view with a broken editorComponent declaration"
        navigateToView(viewClass)

        then: "Throw GuiDevelopmentException"
        def e = thrown GuiDevelopmentException
        e.message == message

        where:
        description                      | viewClass                              | message
        "editable is false"              | BrokenGridEditorEditableFalseView      | "'editable=\"false\"' cannot be combined with 'editorComponent' in the 'status' column"
        "column without property"        | BrokenGridEditorNoPropertyView         | "'property' attribute is required for the 'custom' column with 'editorComponent'"
        "no component element"           | BrokenGridEditorEmptyView              | "'editorComponent' of the 'name' column must contain exactly one component element"
        "two component elements"         | BrokenGridEditorTwoChildrenView        | "'editorComponent' of the 'name' column must contain exactly one component element"
        "property on the component"      | BrokenGridEditorChildPropertyView      | "Component in 'editorComponent' of the 'name' column cannot define 'property': it is bound to the column property"
        "dataContainer on the component" | BrokenGridEditorChildDataContainerView | "Component in 'editorComponent' of the 'name' column cannot define 'dataContainer': it is bound to the column property"
        "not a value source component"   | BrokenGridEditorNotValueSourceView     | "Component in 'editorComponent' of the 'name' column must implement ${SupportsValueSource.simpleName}, but ${JmixButton.simpleName} does not"
    }

    protected GridEditorComponentView open() {
        def view = navigateToView(GridEditorComponentView)

        createCustomers()
        view.customersDc.setItems([ann, bob])

        order = metadata.create(Order)
        order.number = "N-1"
        order.customer = ann
        view.ordersDc.setItems([order])

        root = metadata.create(Group)
        root.name = "Root"
        view.groupsDc.setItems([root])

        city = metadata.create(City)
        city.name = "Paris"
        view.citiesDc.setItems([city])

        UI.current.internals.stateTree.runExecutionsBeforeClientResponse()

        return view
    }

    protected GridEditorFragmentHostView openFragmentHostView() {
        def view = navigateToView(GridEditorFragmentHostView)

        createCustomers()
        view.customersDc.setItems([ann, bob])

        UI.current.internals.stateTree.runExecutionsBeforeClientResponse()

        return view
    }

    protected void createCustomers() {
        ann = metadata.create(Customer)
        ann.name = "Ann"
        ann.status = Status.OK
        ann.email = "ann@example.com"

        bob = metadata.create(Customer)
        bob.name = "Bob"
        bob.status = Status.NOT_OK
        bob.email = "bob@example.com"
    }

    protected static Map<String, Component> capture(DataGrid<?> grid) {
        Map<String, Component> built = [:]
        grid.editor.setEditComponentConfigurer { ctx -> built[ctx.property()] = ctx.editComponent() }
        return built
    }

    protected static Map<String, Component> capture(TreeDataGrid<?> grid) {
        Map<String, Component> built = [:]
        grid.editor.setEditComponentConfigurer { ctx -> built[ctx.property()] = ctx.editComponent() }
        return built
    }
}
