/*
 * Copyright 2021 Haulmont.
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

package value_provider

import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.configuration.mapping.ReferenceImportPolicy
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping
import io.jmix.dataimport.extractor.data.ImportedDataItem
import io.jmix.dataimport.extractor.data.ImportedObject
import io.jmix.dataimport.property.populator.EntityPropertiesPopulator
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Order

class EmbeddableEntityCreationTest extends DataImportSpec {

    @Autowired
    protected EntityPropertiesPopulator propertiesPopulator

    def 'test embeddable entity creation using data from imported object'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("deliveryDetails", ReferenceImportPolicy.CREATE)
                        .withDataFieldName("deliveryDetails")
                        .addSimplePropertyMapping("deliveryDate", "deliveryDate")
                        .addSimplePropertyMapping("fullAddress", "fullAddress")
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        def deliveryDetailsImportedObject = new ImportedObject()
        deliveryDetailsImportedObject.addRawValue("deliveryDate", '25/06/2021 17:00')
        deliveryDetailsImportedObject.addRawValue("fullAddress", null)

        importedDataItem.addRawValue('deliveryDetails', deliveryDetailsImportedObject)

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = propertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 0
        checkDeliveryDetails(order.deliveryDetails, '25/06/2021 17:00', null)
    }

    def 'test embeddable entity creation using data from imported data item'() {
        given:
        def configuration = ImportConfiguration.builder(Order, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("deliveryDetails", ReferenceImportPolicy.CREATE)
                        .addSimplePropertyMapping("deliveryDate", "deliveryDate")
                        .addSimplePropertyMapping("fullAddress", "fullAddress")
                        .build())
                .withDateFormat("dd/MM/yyyy hh:mm")
                .build()

        def importedDataItem = new ImportedDataItem()
        importedDataItem.addRawValue("deliveryDate", '25/06/2021 17:00')
        importedDataItem.addRawValue("fullAddress", null)

        when: 'entity properties populated'
        def order = dataManager.create(Order)
        def entityInfo = propertiesPopulator.populateProperties(order, configuration, importedDataItem)

        then:
        entityInfo.entity == order
        entityInfo.createdReferences.size() == 0
        checkDeliveryDetails(order.deliveryDetails, '25/06/2021 17:00', null)
    }
}
