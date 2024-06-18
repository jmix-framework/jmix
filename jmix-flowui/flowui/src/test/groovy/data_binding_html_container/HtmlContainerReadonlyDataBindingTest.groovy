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

package data_binding_html_container

import com.vaadin.flow.component.html.Div
import data_binding_html_container.view.DataBindingHtmlContainerView
import io.jmix.core.DataManager
import io.jmix.core.MetadataTools
import io.jmix.core.SaveContext
import io.jmix.flowui.data.binding.HtmlContainerReadonlyDataBinding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.Animal
import test_support.entity.Zoo
import test_support.spec.FlowuiTestSpecification

import java.util.stream.Collectors

@SpringBootTest
class HtmlContainerReadonlyDataBindingTest extends FlowuiTestSpecification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    DataManager dataManager

    @Autowired
    MetadataTools metadataTools

    @Autowired
    HtmlContainerReadonlyDataBinding htmlContainerReadonlyDataBinding

    @Override
    void setup() {
        registerViewBasePackages("data_binding_html_container.view")

        def saveContext = new SaveContext()

        def zoo = dataManager.create(Zoo)
        zoo.name = "Zoo"
        zoo.city = "City"
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

    def "Bind data containers in loader"() {
        when: "Open DataBindingHtmlContainerView"
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)
        dataBindingHtmlContainerView.loadData()

        def zooAnimals = dataBindingHtmlContainerView.zooDc.getItem().animals
        def zooAnimalsAsString = zooAnimals.stream()
                .map(metadataTools::format)
                .collect(Collectors.joining(", "))

        then:
        dataBindingHtmlContainerView.name.getText() == "Zoo"
        dataBindingHtmlContainerView.city.getText() == "City"
        dataBindingHtmlContainerView.zooAnimals.getText() == zooAnimalsAsString
    }

    def "Manual binding"() {
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)
        dataBindingHtmlContainerView.loadData()

        when: "Create div and bind with zooDc"
        Div zooName = new Div()
        htmlContainerReadonlyDataBinding.bind(zooName, dataBindingHtmlContainerView.zooDc, "name")
        Div zooCity = new Div()
        htmlContainerReadonlyDataBinding.bind(zooCity, dataBindingHtmlContainerView.zooDc, "city")

        then:
        zooName.getText() == "Zoo"
        zooCity.getText() == "City"

        when: "Change name and city properties"
        dataBindingHtmlContainerView.zooDc.getItem().name = "Zoo Zoo"
        dataBindingHtmlContainerView.zooDc.getItem().city = "City City"

        then:
        zooName.getText() == "Zoo Zoo"
        zooCity.getText() == "City City"

        when: "Create div and bind with animalsDc"
        Div animals = new Div()
        htmlContainerReadonlyDataBinding.bind(animals, dataBindingHtmlContainerView.animalsDc)

        def animalEntities = dataBindingHtmlContainerView.animalsDc.getItems()
        def animalsAsString = animalEntities.stream()
                .map(metadataTools::format)
                .collect(Collectors.joining(", "))

        then:
        animals.getText() == animalsAsString
    }

    def "Manual value source binding"() {
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)
        dataBindingHtmlContainerView.loadData()

        when: "Create div and bind with value source"
        Div zooName = new Div()
        htmlContainerReadonlyDataBinding.bind(zooName, dataBindingHtmlContainerView.zooName.getValueSource())

        then:
        zooName.getText() == "Zoo"
        dataBindingHtmlContainerView.zooName.getValue() == "Zoo"

        when: "Change text field value"
        dataBindingHtmlContainerView.zooName.setValue("Zoo Zoo")

        then:
        zooName.getText() == dataBindingHtmlContainerView.zooName.getValue()
    }

    def "Unbind test"() {
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)
        dataBindingHtmlContainerView.loadData()

        when: "Create div and bind with value source"
        Div zooName = new Div()
        htmlContainerReadonlyDataBinding.bind(zooName, dataBindingHtmlContainerView.zooName.getValueSource())
        dataBindingHtmlContainerView.zooName.setValue("Zoo Zoo")

        then:
        zooName.getText() == "Zoo Zoo"

        when: "Unbind value source"
        htmlContainerReadonlyDataBinding.unbind(zooName)
        dataBindingHtmlContainerView.zooName.setValue("Zoo Zoo Zoo")

        then:
        zooName.getText() == "Zoo Zoo"
    }
}
