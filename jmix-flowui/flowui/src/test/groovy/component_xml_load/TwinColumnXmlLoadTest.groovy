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

package component_xml_load

import component_xml_load.screen.TwinColumnView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.flowui.component.listbox.JmixMultiSelectListBox
import io.jmix.flowui.component.twincolumn.TwinColumn
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.Animal
import test_support.entity.Zoo
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class TwinColumnXmlLoadTest extends FlowuiTestSpecification {
    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def saveContext = new SaveContext()

        def zoo = dataManager.create(Zoo)
        zoo.name = "Zoo"
        zoo.animals = new LinkedList<>()
        List<Animal> animals = new LinkedList<>()
        for (int i = 1; i <= 10; i++) {
            def animal = dataManager.create(Animal)
            animal.name = "Animal" + i
            if (i > 5) {
                zoo.animals.add(animal)
            }
            animals.add(animal)
        }

        saveContext.saving(animals)
        saveContext.saving(zoo)
        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ZOO_ANIMAL_LINK")
        jdbcTemplate.execute("delete from TEST_ZOO")
        jdbcTemplate.execute("delete from TEST_ANIMAL")
    }

    def "Load #twinColumn component from XML"() {
        when: "Open the ComponentView"
        def twinColumnView = navigateToView(TwinColumnView.class)

        then: "#twinColumn component will be loaded"
        verifyAll(twinColumnView.twinColumn as TwinColumn) {
            id.get() == "twinColumn"

            ariaLabel.get() == "ariaLabelString"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "blue"
            enabled
            helperText == "helperTextString"
            height == "500px"
            label == "labelString"
            maxHeight == "1000px"
            maxWidth == "700px"
            minHeight == "100px"
            minWidth == "80px"
            optionsColumnLabel == "Options"
            readOnly
            reorderable
            required
            selectAllButtonsVisible
            selectedItemsColumnLabel == "Selected"
            themeNames.contains("rounded")
            !visible
            width == "200px"
        }
    }

    def "Load TwinColumn component with datasource from XML"() {
        given: "An entity with some property"
        def zoo = dataManager.load(Zoo).all().one()

        when: "Open the ComponentView and load data"
        def twinColumnView = navigateToView(TwinColumnView.class)
        twinColumnView.loadData()

        then: "TwinColumn component will be loaded with the value of the property"
        verifyAll(twinColumnView.twinColumn) {
            id.get() == "twinColumn"
            required
            value.size() == zoo.animals.size()
            value[0] == zoo.animals.get(0)
        }
    }

    def "Load twinColumn component with options container from XML"() {
        when:
        def animals = dataManager.load(Animal).all().list()
        def twinColumnView = navigateToView(TwinColumnView.class)
        def twinColumnWithItemsContainer = twinColumnView.twinColumnWithItemsContainer
        def options = (JmixMultiSelectListBox<Animal>) twinColumnWithItemsContainer.getSubPart("options")
        twinColumnView.loadData()

        then: "Set options listBox selected items"
        options.getListDataView().getItems().toList().size() == animals.size()

        when: "Change twinColumn value"
        twinColumnWithItemsContainer.value = new LinkedList<>(List.of(animals[0]))

        then:
        options.getListDataView().getItems().toList().size() == animals.size() - 1
    }

    def "Load twinColumn component and test items selection"() {
        given: "Open the TwinColumnView and load data"
        def animals = dataManager.load(Animal).all().list()
        def twinColumnView = navigateToView(TwinColumnView.class)
        def twinColumnWithItemsContainer = twinColumnView.twinColumnWithItemsContainer

        def field = TwinColumn.getDeclaredField("saveAndRestoreListBoxesScrollTopPosition")
        field.setAccessible(true)
        field.set(twinColumnWithItemsContainer, false)

        def options = (JmixMultiSelectListBox<Animal>) twinColumnWithItemsContainer.getSubPart("options")
        def selected = (JmixMultiSelectListBox<Animal>) twinColumnWithItemsContainer.getSubPart("selected")
        def selectItems = (JmixButton) twinColumnWithItemsContainer.getSubPart("selectItems")
        def selectAllItems = (JmixButton) twinColumnWithItemsContainer.getSubPart("selectAllItems")
        def deselectItems = (JmixButton) twinColumnWithItemsContainer.getSubPart("deselectItems")
        def deselectAllItems = (JmixButton) twinColumnWithItemsContainer.getSubPart("deselectAllItems")
        def clearListBoxesSelection = (JmixButton) twinColumnWithItemsContainer.getSubPart("clearListBoxesSelection")

        twinColumnView.loadData()
        //test clear listBox item selection
        when:
        options.setValue(Set.of(animals[0]))
        clearListBoxesSelection.click()
        then:
        options.getValue().isEmpty()

        //test select items
        when:
        options.setValue(Set.of(animals[0], animals[1], animals[2]))
        selectItems.click()
        then:
        options.getListDataView().getItemCount() == animals.size() - 3
        selected.getListDataView().getItemCount() == 3
        twinColumnWithItemsContainer.getValue().size() == 3

        //test deselect items
        when:
        deselectItems.click()
        then:
        options.getListDataView().getItemCount() == animals.size()
        selected.getListDataView().getItemCount() == 0
        twinColumnWithItemsContainer.getValue().size() == 0

        //test select all items
        when:
        selectAllItems.click()
        then:
        options.getListDataView().getItemCount() == 0
        selected.getListDataView().getItemCount() == animals.size()
        twinColumnWithItemsContainer.getValue().size() == animals.size()

        //test deselect all items
        when:
        deselectAllItems.click()
        then:
        options.getListDataView().getItemCount() == animals.size()
        selected.getListDataView().getItemCount() == 0
        twinColumnWithItemsContainer.getValue().size() == 0

        //test reorderable false
        when:
        twinColumnWithItemsContainer.setReorderable(false)
        options.setValue(Set.of(animals[1]))
        selectItems.click()
        options.setValue(Set.of(animals[0]))
        selectItems.click()
        then:
        selected.getListDataView().getItem(0) == animals[1]
        selected.getListDataView().getItem(1) == animals[0]

        //test reorderable true
        when:
        twinColumnWithItemsContainer.setReorderable(true)
        deselectAllItems.click()
        options.setValue(Set.of(animals[1]))
        selectItems.click()
        options.setValue(Set.of(animals[0]))
        selectItems.click()

        then:
        selected.getListDataView().getItem(0) == animals[0]
        selected.getListDataView().getItem(1) == animals[1]

        //test clear listBoxes selection button visible
        when:
        twinColumnWithItemsContainer.setClearColumnsSelectionButtonVisible(true)
        then:
        twinColumnWithItemsContainer.isClearColumnsSelectionButtonVisible()
        clearListBoxesSelection.isVisible()
        when:
        twinColumnWithItemsContainer.setClearColumnsSelectionButtonVisible(false)
        then:
        !clearListBoxesSelection.isVisible()

        //test show select all items button
        when:
        twinColumnWithItemsContainer.setSelectAllButtonsVisible(true)
        then:
        selectAllItems.isVisible()
        deselectAllItems.isVisible()

        when:
        twinColumnWithItemsContainer.setSelectAllButtonsVisible(false)
        then:
        !selectAllItems.isVisible()
        !deselectAllItems.isVisible()
    }
}