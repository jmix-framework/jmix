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

package data_binding_text_component

import com.vaadin.flow.component.badge.Badge
import data_binding_text_component.view.DataBindingTextComponentView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.flowui.data.binding.TextComponentReadonlyDataBinding
import io.jmix.flowui.data.value.ContainerValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.City
import test_support.entity.Zoo
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class TextComponentReadonlyDataBindingTest extends FlowuiTestSpecification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    DataManager dataManager

    @Autowired
    TextComponentReadonlyDataBinding textComponentReadonlyDataBinding

    @Override
    void setup() {
        registerViewBasePackages("data_binding_text_component.view")

        def city = dataManager.create(City)
        city.name = "City"

        def zoo = dataManager.create(Zoo)
        zoo.name = "Zoo"
        zoo.address = "Street 1"
        zoo.city = city

        def saveContext = new SaveContext()
        saveContext.saving(city)
        saveContext.saving(zoo)
        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ZOO")
        jdbcTemplate.execute("delete from TEST_CITY")
    }

    def "Bind data container in loader"() {
        when: "Open DataBindingTextComponentView"
        def view = navigateToView(DataBindingTextComponentView.class)

        then: "Badges with data binding should have text value equal to zoo properties values"
        view.nameBadge.text == "Zoo"
        view.cityBadge.text == "City"

        and: "Badge without data binding should keep its own text"
        view.textBadge.text == "Text"
    }

    def "Bind badge in formLayout"() {
        when: "Open DataBindingTextComponentView"
        def view = navigateToView(DataBindingTextComponentView.class)

        then: "Badge in formLayout should take the data container from the form layout"
        view.formBadge.text == "Street 1"
    }

    def "Manual binding"() {
        def view = navigateToView(DataBindingTextComponentView.class)

        when: "Create badge and bind with zooDc using property"
        Badge zooName = new Badge()
        textComponentReadonlyDataBinding.bind(zooName, view.zooDc, "name")

        then: "Badge should have text value equal to zoo property value"
        zooName.text == "Zoo"

        when: "Change name property"
        view.zooDc.getItem().name = "Zoo Zoo"

        then: "Badge text should change its value after changing entity property"
        zooName.text == "Zoo Zoo"
    }

    def "Manual value source binding"() {
        def view = navigateToView(DataBindingTextComponentView.class)

        when: "Create badge and bind with value source"
        Badge zooName = new Badge()
        def zooNameValueSource = new ContainerValueSource<>(view.zooDc, "name")
        zooNameValueSource.setApplicationContext(applicationContext)
        def registration = textComponentReadonlyDataBinding.bind(zooName, zooNameValueSource)

        then: "Badge text should be equal to value source value"
        zooName.text == "Zoo"

        when: "Change value source value"
        zooNameValueSource.value = "Zoo Zoo"

        then: "Badge should change its text to the new value"
        zooName.text == "Zoo Zoo"

        when: "Unbind value source and set a new value"
        registration.remove()
        zooNameValueSource.value = "Zoo Zoo Zoo"

        then: "Badge should have old value"
        zooName.text == "Zoo Zoo"
    }
}
