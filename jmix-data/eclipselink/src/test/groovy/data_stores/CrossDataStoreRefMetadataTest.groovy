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

package data_stores

import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.multidb.MainReport

class CrossDataStoreRefMetadataTest extends DataSpec {

    @Autowired
    Metadata metadata

    def "test CDS reference property meta-annotations"() {
        def metaClass = metadata.getClass(MainReport)

        when:
        def cdsrProperty = metaClass.getProperty('db1Order')
        def cdsrIdProperty = metaClass.getProperty('db1OrderId')

        then:
        cdsrProperty.getAnnotations().get("jmix.crossDataStoreRefId") == cdsrIdProperty
        cdsrIdProperty.getAnnotations().get("jmix.crossDataStoreRef") == cdsrProperty
    }

    def "test DTO reference property is not a CDS reference"() {
        def metaClass = metadata.getClass(MainReport)

        when:
        def dtoProperty = metaClass.getProperty('testUuidDto')

        then:
        dtoProperty.getAnnotations().get("jmix.crossDataStoreRefId") == null
    }

    def "test method-based DTO reference property is not a CDS reference"() {
        def metaClass = metadata.getClass(MainReport)

        when:
        def dtoProperty = metaClass.getProperty('methodBasedTestUuidDto')

        then:
        dtoProperty.getAnnotations().get("jmix.crossDataStoreRefId") == null
    }

    def "test method-based DTO reference property with DependsOn is not a CDS reference"() {
        def metaClass = metadata.getClass(MainReport)

        when:
        def dtoProperty = metaClass.getProperty('methodBasedTestUuidDtoWithDependsOn')

        then:
        dtoProperty.getAnnotations().get("jmix.crossDataStoreRefId") == null
    }
}
