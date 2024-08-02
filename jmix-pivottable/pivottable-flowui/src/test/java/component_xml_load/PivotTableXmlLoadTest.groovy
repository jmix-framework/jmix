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


import component_xml_load.view.PivotTableOptionsTestView
import io.jmix.core.DataManager
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration
import io.jmix.flowui.testassist.UiTest
import io.jmix.flowui.testassist.UiTestUtils
import io.jmix.flowui.view.View
import io.jmix.flowui.view.navigation.ViewNavigationSupport
import io.jmix.pivottableflowui.component.PivotTable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.PivotTableFlowuiTestConfiguration

@UiTest(viewBasePackages = ["component_xml_load.view"])
@SpringBootTest(classes = [PivotTableFlowuiTestConfiguration, FlowuiTestAssistConfiguration])
class PivotTableXmlLoadTest {

    @Autowired
    protected ViewNavigationSupport navigationSupport;

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    protected <T extends View<?>> T navigateTo(Class<T> view) {
        navigationSupport.navigate(view);
        return UiTestUtils.getCurrentView();
    }

    void setup() {
        /*registerViewBasePackages("component_xml_load.screen")

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
        dataManager.save(saveContext)*/
    }

    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ZOO_ANIMAL_LINK")
        jdbcTemplate.execute("delete from TEST_ZOO")
        jdbcTemplate.execute("delete from TEST_ANIMAL")
    }

    @Test
    void "Load pivotTable component from XML"() {
        when: "Open the ComponentView"
        def pivotTableView = navigateToView(PivotTableOptionsTestView.class)

        then: "pivotTable component will be loaded"
        verifyAll(pivotTableView.pivotTable as PivotTable) {
            id.get() == "pivotTable"

        }
    }
}
