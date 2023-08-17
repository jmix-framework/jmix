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

import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.shared.Tooltip
import component_xml_load.screen.MultiSelectComboBoxView
import io.jmix.core.DataManager
import io.jmix.flowui.component.SupportsMetaClass
import io.jmix.flowui.kit.component.HasTitle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Product
import test_support.entity.sales.ProductTag
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class MultiSelectComboBoxXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        ProductTag productTag1 = dataManager.create(ProductTag.class)
                .with(true) { it.name = "Product tag 1" } as ProductTag
        ProductTag productTag2 = dataManager.create(ProductTag.class)
                .with(true) { it.name = "Product tag 2" } as ProductTag
        ProductTag productTag3 = dataManager.create(ProductTag.class)
                .with(true) { it.name = "Product tag 3" } as ProductTag
        ProductTag productTag4 = dataManager.create(ProductTag.class)
                .with(true) { it.name = "Product tag 4" } as ProductTag

        Product product = dataManager.create(Product.class)
                .with(true) {
                    it.setName("Product")
                    it.setTags(List.of(productTag1, productTag2))
                }

        dataManager.save(productTag1, productTag2, productTag3, productTag4, product)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_PRODUCT_TAG_LINK")
        jdbcTemplate.execute("delete from TEST_PRODUCT_TAG")
        jdbcTemplate.execute("delete from TEST_PRODUCT")
    }

    def "Load multiSelectComboBox component from XML"() {
        when: "Open the MultiSelectComboBoxView"
        def multiSelectComboBoxView = navigateToView(MultiSelectComboBoxView)
        def productTagsDc = multiSelectComboBoxView.productTagsDc
        def multiSelectComboBox = multiSelectComboBoxView."${multiSelectComboBoxComponent}Id" as MultiSelectComboBox

        then: "MultiSelectComboBox attributes will be loaded"
        verifyAll(multiSelectComboBox) {
            id.get() == "${multiSelectComboBoxComponent}Id"
            allowCustomValue
            allowedCharPattern == "testPattern"
            autofocus
            autoOpen
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
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
            tabIndex == 3
            themeNames.containsAll(["small", "align-center"])
            (it as HasTitle).getTitle() == "titleString"
            visible
            width == "100px"

            getSelectedItems().size() == getValue().size()
            productTagsDc.getItems().containsAll(multiSelectComboBox.getGenericDataView().getItems().toArray())

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }

        where:
        multiSelectComboBoxComponent << ["multiSelectComboBox", "multiSelectComboBoxPicker"]
    }

    def "Load multiSelectComboBoxPicker actions from XML"() {
        when: "Open the MultiSelectComboBoxView"
        def multiSelectComboBoxView = navigateToView(MultiSelectComboBoxView)
        def multiSelectComboBoxPicker = multiSelectComboBoxView.multiSelectComboBoxPickerId

        then: "MultiSelectComboBoxPicker actions will be loaded"
        verifyAll(multiSelectComboBoxPicker) {
            multiSelectComboBoxPicker.getAction("lookup") != null
            multiSelectComboBoxPicker.getAction("clear") != null
        }
    }

    def "Load multiSelectComboBox component with metaClass attribute from XML"() {
        when: "Open the MultiSelectComboBoxView"
        def multiSelectComboBoxView = navigateToView(MultiSelectComboBoxView)
        def multiSelectComboBoxComponent = multiSelectComboBoxView."${multiSelectComboBox}MetaClassId" as MultiSelectComboBox

        then: "MultiSelectComboBox attributes will be loaded"
        verifyAll(multiSelectComboBoxComponent) {
            id.get() == "${multiSelectComboBox}MetaClassId"
            (it as SupportsMetaClass).metaClass.getJavaClass() == ProductTag.class
        }

        where:
        multiSelectComboBox << ["multiSelectComboBox", "multiSelectComboBoxPicker"]
    }
}
