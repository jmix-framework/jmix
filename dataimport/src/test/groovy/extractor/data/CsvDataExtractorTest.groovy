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

package extractor.data

import io.jmix.core.Resources
import io.jmix.dataimport.InputDataFormat
import io.jmix.dataimport.configuration.ImportConfiguration
import io.jmix.dataimport.exception.ImportException
import io.jmix.dataimport.extractor.data.impl.CsvDataExtractor
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataImportSpec
import test_support.entity.Product

class CsvDataExtractorTest extends DataImportSpec {

    @Autowired
    protected CsvDataExtractor csvDataExtractor

    @Autowired
    protected Resources resources;

    def "test imported data from input stream"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/csv/products.csv")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.CSV);

        when: 'imported data extracted'
        def importedData = csvDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['Product Name', 'Special', 'Price']

        importedData.items.size() == 2
        def firstProduct = importedData.items[0]
        firstProduct.itemIndex == 1
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('Product Name') == 'Outback Power Nano-Carbon Battery 12V'
        firstProduct.getRawValue('Special') == 'Yes'
        firstProduct.getRawValue('Price') == '6.25'

        def secondProduct = importedData.items[1]
        secondProduct.itemIndex == 2
        secondProduct.rawValues.size() == 3
        secondProduct.getRawValue('Product Name') == 'Fullriver Sealed Battery 6V'
        secondProduct.getRawValue('Special') == 'No'
        secondProduct.getRawValue('Price') == '5.10'
    }

    def "test imported data from byte array"() {
        given:
        def inputBytes = IOUtils.toByteArray(resources.getResourceAsStream("test_support/input_data_files/csv/products.csv"))

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.CSV)

        when: 'imported data extracted'
        def importedData = csvDataExtractor.extract(importConfiguration, inputBytes)

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['Product Name', 'Special', 'Price']

        importedData.items.size() == 2
        def firstProduct = importedData.items[0]
        firstProduct.itemIndex == 1
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('Product Name') == 'Outback Power Nano-Carbon Battery 12V'
        firstProduct.getRawValue('Special') == 'Yes'
        firstProduct.getRawValue('Price') == '6.25'

        def secondProduct = importedData.items[1]
        secondProduct.itemIndex == 2
        secondProduct.rawValues.size() == 3
        secondProduct.getRawValue('Product Name') == 'Fullriver Sealed Battery 6V'
        secondProduct.getRawValue('Special') == 'No'
        secondProduct.getRawValue('Price') == '5.10'
    }

    def "test import from byte array"() {
        given:
        def csvString = resources.getResourceAsString("test_support/input_data_files/csv/products.csv")

        when: 'imported data extracted'
        def importedData = csvDataExtractor.extract(new ImportConfiguration(Product, InputDataFormat.CSV), csvString.getBytes())

        then:
        importedData.dataFieldNames.size() == 3
        importedData.dataFieldNames == ['Product Name', 'Special', 'Price']

        importedData.items.size() == 2
        def firstProduct = importedData.items[0]
        firstProduct.itemIndex == 1
        firstProduct.rawValues.size() == 3
        firstProduct.getRawValue('Product Name') == 'Outback Power Nano-Carbon Battery 12V'
        firstProduct.getRawValue('Special') == 'Yes'
        firstProduct.getRawValue('Price') == '6.25'

        def secondProduct = importedData.items[1]
        secondProduct.itemIndex == 2
        secondProduct.rawValues.size() == 3
        secondProduct.getRawValue('Product Name') == 'Fullriver Sealed Battery 6V'
        secondProduct.getRawValue('Special') == 'No'
        secondProduct.getRawValue('Price') == '5.10'
    }

    def "test windows-1251 encoding"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/csv/products_win_1251_encoding.csv")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.CSV);
        importConfiguration.setInputDataCharset("windows-1251")

        when: 'imported data extracted'
        def importedData = csvDataExtractor.extract(importConfiguration, inputStream)

        then:
        importedData.dataFieldNames.size() == 2
        importedData.dataFieldNames == ['Имя товара', 'Цена']

        importedData.items.size() == 1
        def firstProduct = importedData.items[0]
        firstProduct.itemIndex == 1
        firstProduct.rawValues.size() == 2
        firstProduct.getRawValue('Имя товара') == 'Аккумулятор Outback Power Nano-Carbon 12V'
        firstProduct.getRawValue('Цена') == '6,25'
    }

    def "test unsupported encoding"() {
        given:
        def inputStream = resources.getResourceAsStream("test_support/input_data_files/csv/products_win_1251_encoding.csv")

        ImportConfiguration importConfiguration = new ImportConfiguration(Product, InputDataFormat.CSV);
        importConfiguration.setInputDataCharset("win-1251")

        when: 'imported data extracted'
        csvDataExtractor.extract(importConfiguration, inputStream)

        then:
        thrown(ImportException)
    }
}
