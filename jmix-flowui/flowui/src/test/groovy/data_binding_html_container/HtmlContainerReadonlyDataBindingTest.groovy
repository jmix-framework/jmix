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
import io.jmix.flowui.data.value.ContainerValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.Animal
import test_support.entity.City
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

        def city = dataManager.create(City)
        city.name = "City"

        def zoo = dataManager.create(Zoo)
        zoo.name = "Zoo"
        zoo.address = "Street 1"
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
        city.setZoos(new ArrayList<Zoo>(List.of(zoo)))

        saveContext.saving(animals)
        saveContext.saving(zoo)
        saveContext.saving(city)
        dataManager.save(saveContext)

        zoo.setCity(city)
        saveContext.saving(zoo)
        animals.forEach {animal -> animal.setZoo(zoo)}
        saveContext.saving(animals)
        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_CITY_ZOO_LINK")
        jdbcTemplate.execute("delete from TEST_ZOO_ANIMAL_LINK")
        jdbcTemplate.execute("delete from TEST_ANIMAL")
        jdbcTemplate.execute("delete from TEST_ZOO")
        jdbcTemplate.execute("delete from TEST_CITY")
    }

    def "Bind data containers in loader"() {
        when: "Open DataBindingHtmlContainerView"
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)

        def zooAnimals = dataBindingHtmlContainerView.zooDc.getItem().animals
        def zooAnimalsAsString = zooAnimals.stream()
                .map(metadataTools::format)
                .collect(Collectors.joining(", "))

        then: "Divs with data binding should have text value equal to zoo properties values"
        dataBindingHtmlContainerView.name.text == "Zoo"
        dataBindingHtmlContainerView.address.text == "Street 1"
        dataBindingHtmlContainerView.zooAnimals.text == zooAnimalsAsString
    }

    def "Manual binding"() {
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)

        when: "Create div and bind with zooDc using property"
        Div zooName = new Div()
        htmlContainerReadonlyDataBinding.bind(zooName, dataBindingHtmlContainerView.zooDc, "name")
        Div zooAddress = new Div()
        htmlContainerReadonlyDataBinding.bind(zooAddress, dataBindingHtmlContainerView.zooDc, "address")

        then: "Divs with data binding should have text value equal to zoo properties values"
        zooName.text == "Zoo"
        zooAddress.text == "Street 1"

        when: "Change name and city properties"
        dataBindingHtmlContainerView.zooDc.getItem().name = "Zoo Zoo"
        dataBindingHtmlContainerView.zooDc.getItem().address = "Street 2"

        then: "Divs text should change its value after changing entity properties"
        zooName.text == "Zoo Zoo"
        zooAddress.text == "Street 2"

        when: "Create div and bind with collection animalsDc"
        Div animals = new Div()

        def selectedItem = dataBindingHtmlContainerView.animalsDc.getItems().get(1)
        dataBindingHtmlContainerView.animalsDc.setItem(selectedItem)
        htmlContainerReadonlyDataBinding.bind(animals, dataBindingHtmlContainerView.animalsDc, "name")

        then: "Div text should be equal to collection with CollectionFormatter"
        animals.text == selectedItem.name
    }

    def "Manual value source binding"() {
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)

        when: "Create div and bind with value source"
        Div zooName = new Div()
        def zooNameValueSource = new ContainerValueSource<>(dataBindingHtmlContainerView.zooDc, "name")
        zooNameValueSource.setApplicationContext(applicationContext)
        htmlContainerReadonlyDataBinding.bind(zooName, zooNameValueSource)

        then: "Div text should be equal to textField value"
        zooName.text == "Zoo"
        zooNameValueSource.value == "Zoo"

        when: "Change text field value"
        zooNameValueSource.value = "Zoo Zoo"

        then: "Div should change its value to new value of textField"
        zooName.text == zooNameValueSource.value
    }

    def "Unbind test"() {
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)

        when: "Create div and bind with value source"
        Div zooName = new Div()

        def zooNameValueSource = new ContainerValueSource<>(dataBindingHtmlContainerView.zooDc, "name")
        zooNameValueSource.setApplicationContext(applicationContext)
        htmlContainerReadonlyDataBinding.bind(zooName, zooNameValueSource)
        zooNameValueSource.value = "Zoo Zoo"

        then: "Div text should be equal to textField value"
        zooName.text == "Zoo Zoo"

        when: "Unbind value source and set new value to textField"
        htmlContainerReadonlyDataBinding.unbind(zooName)
        zooNameValueSource.value = "Zoo Zoo Zoo"

        then: "Div should have old value"
        zooName.text == "Zoo Zoo"
    }

    def "Bind date for html container in layoutForm"() {
        when: "Open the view"
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)

        then: "Html container in formLayout should have the text value equal to zoo instance name value"
        dataBindingHtmlContainerView.formDiv.text == dataBindingHtmlContainerView.zooDc.item.name
    }

    def "Bind nested property"() {
        when: "Open the view"
        def dataBindingHtmlContainerView = navigateToView(DataBindingHtmlContainerView.class)

        then: "Html containers should have the text values from the nested properties"
        dataBindingHtmlContainerView.animalZoo.text == "Zoo"
        dataBindingHtmlContainerView.animalZooCity.text == "City"
    }
}
