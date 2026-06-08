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

package data_importer

import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.configuration.ImportTransactionStrategy
import test_support.entity.Product

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import io.jmix.dataimport.DataImporter
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec

class DataImportXmlXxeTest extends DataImportSpec {

    @Autowired
    DataImporter dataImporter

    def 'DataImporter XML input does not expose content from an external entity'() {
        given:
        def leakedValue = 'xxe-' + System.nanoTime()
        def secretFile = Files.createTempFile('jmix-dataimport-xxe', '.txt')
        secretFile.toFile().deleteOnExit()
        Files.writeString(secretFile, leakedValue)

        def importConfig = ImportConfiguration.builder(Product, InputDataFormat.XML)
                .addSimplePropertyMapping('name', 'name')
                .addSimplePropertyMapping('price', 'price')
                .addSimplePropertyMapping('special', 'special')
                .withBooleanFormats('Yes', 'No')
                .withTransactionStrategy(ImportTransactionStrategy.TRANSACTION_PER_ENTITY)
                .build()

        def xmlContent = """<?xml version="1.0" encoding="UTF-8"?>
                                    <!DOCTYPE product [
                                      <!ENTITY xxe SYSTEM "${secretFile.toUri()}">
                                    ]>
                                    <product>
                                      <name>&xxe;</name>
                                      <special>No</special>
                                      <price>1.23</price>
                                    </product>
                                """.getBytes(StandardCharsets.UTF_8)

        when:
        def result = dataImporter.importData(importConfig, xmlContent)

        then:
        if (result.success) {
            assert result.importedEntityIds.size() == 1

            def imported = dataManager.load(Product).id(result.importedEntityIds[0]).one()
            assert !(imported.name ?: '').contains(leakedValue)
        } else {
            assert result.importedEntityIds.empty
            assert !((result.errorMessage ?: '').contains(leakedValue))
            assert result.failedEntities.every { !((it.errorMessage ?: '').contains(leakedValue)) }
        }
    }
}
