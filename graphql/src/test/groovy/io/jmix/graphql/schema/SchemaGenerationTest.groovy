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

package io.jmix.graphql.schema

import graphql.schema.idl.SchemaPrinter
import io.jmix.graphql.AbstractGraphQLTest
import io.jmix.graphql.spqr.SpqrSchemaGenerator
import org.springframework.beans.factory.annotation.Autowired

class SchemaGenerationTest extends AbstractGraphQLTest {

    @Autowired
    SpqrSchemaGenerator spqrSchemaGenerator

    def "graphql schema created"() {
        when:
        def schema = spqrSchemaGenerator.generate()
        println new SchemaPrinter().print(schema)

        then:
        schema.getQueryType().getFieldDefinition('scr_CarList') != null
        schema.getMutationType().getFieldDefinition('upsert_scr_Car') != null
        schema.getMutationType().getFieldDefinition('unknown') == null
    }
}
