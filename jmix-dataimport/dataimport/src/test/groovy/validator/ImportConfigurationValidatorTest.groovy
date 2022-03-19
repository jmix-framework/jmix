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

package validator

import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.DuplicateEntityPolicy
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.configuration.mapping.ReferenceImportPolicy
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping
import io.jmix.dataimport.exception.ImportException
import io.jmix.dataimport.impl.ImportConfigurationValidator
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Order

class ImportConfigurationValidatorTest extends DataImportSpec {

    @Autowired
    ImportConfigurationValidator validator

    def 'test incorrect import policy for embedded property'() {
        given:
        def importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("deliveryDetails", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .addSimplePropertyMapping("deliveryDate", "Delivery Date")
                        .addSimplePropertyMapping("fullAddress", "Delivery Address")
                        .lookupByAllSimpleProperties()
                        .build())
                .build()

        when:
        validator.validate(importConfiguration)

        then:
        def ex = thrown(ImportException)
        ex.message == 'Incorrect policy [CREATE_IF_MISSING] for embedded reference [deliveryDetails]. Only CREATE policy supported.'

    }

    def 'test incorrect unique entity configuration if mapping for one-to-many property exists'() {
        given:
        def importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("lines", ReferenceImportPolicy.CREATE)
                        .addSimplePropertyMapping("quantity", "Quantity")
                        .lookupByAllSimpleProperties()
                        .build())
                .addUniqueEntityConfiguration(DuplicateEntityPolicy.UPDATE, "orderNumber", "customer.name")
                .build()

        when:
        validator.validate(importConfiguration)

        then:
        def ex = thrown(ImportException)
        ex.message == 'UPDATE policy for duplicates is not supported if there is a mapping for one-to-many property. Reference property: [lines]'
    }

    def 'test incorrect import policy for one-to-many property'() {
        given:
        def importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.XLSX)
                .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("lines", ReferenceImportPolicy.CREATE_IF_MISSING)
                        .addSimplePropertyMapping("quantity", "Quantity")
                        .lookupByAllSimpleProperties()
                        .build())
                .build()

        when:
        validator.validate(importConfiguration)

        then:
        def ex = thrown(ImportException)
        ex.message == 'Incorrect policy [CREATE_IF_MISSING] for one-to-many reference [lines]. Only CREATE policy supported.'
    }
}
