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

import component_xml_load.screen.MultiSelectComboBoxView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Customer
import test_support.entity.sales.Status
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class MultiSelectComboBoxXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")

        def customer = dataManager.create(Customer.class)

        dataManager.save(customer)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_CUSTOMER")
    }

    def "Load multiSelectComboBox component from XML"() {
        when: "Open the MultiSelectComboBoxView"
        def multiSelectComboBoxView = openScreen(MultiSelectComboBoxView)
        def multiSelectComboBox = multiSelectComboBoxView.multiSelectComboBoxId
        multiSelectComboBox.setValue(Status.NOT_OK, Status.OK)

        then: "MultiSelectComboBox attributes will be loaded"
        verifyAll(multiSelectComboBox) {
            id.get() == "multiSelectComboBoxId"
            allowCustomValue
            allowedCharPattern == "testPattern"
            autofocus
            autoOpen
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            clearButtonVisible
            enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            !invalid
            label == "labelString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            opened
            pageSize == 20
            placeholder == "placeholderString"
            readOnly
            required
            requiredIndicatorVisible
            themeNames.containsAll(["small", "align-center"])
            getTitle() == "titleString"
            getTypedValue().containsAll([Status.OK, Status.NOT_OK])
            getValue().containsAll([Status.OK, Status.NOT_OK])
            tooltip.getText() == "tooltipTextString"
            visible
            width == "100px"
        }
    }
}
