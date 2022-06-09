/*
 * Copyright 2022 Haulmont.
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

import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.BigDecimalField
import component_xml_load.screen.ContainerView
import io.jmix.core.DataManager
import io.jmix.flowui.component.textfield.TypedTextField
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FormLayoutXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")

        def order = dataManager.create(Order)

        order.number = "numberString"
        order.amount = BigDecimal.valueOf(50.05)

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load formLayout container from XML"() {
        given: "An entity with some properties"
        def order = dataManager.load(Order).all().one()

        when: "Open the ContainerView and load data"
        def containerView = openScreen(ContainerView.class)
        containerView.loadData()

        then: "FormLayout attributes will be loaded with the value of the properties"
        def formLayout = containerView.formLayoutId
        verifyAll(formLayout) {
            id.get() == "formLayoutId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            enabled
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            visible
            width == "100px"
        }

        def formItems = formLayout.children.toArray()
                .findAll { it instanceof FormLayout.FormItem }

        verifyAll(formItems[0] as FormLayout.FormItem) {
            enabled
            visible
            (children.findAny().get() as TypedTextField<?>).value == order.number
            formLayout.getColspan(it) == 3
        }

        verifyAll(formItems[1] as FormLayout.FormItem) {
            enabled
            visible
            (children.findAny().get() as BigDecimalField).value == order.amount
            formLayout.getColspan(it) == 2
        }
    }
}
