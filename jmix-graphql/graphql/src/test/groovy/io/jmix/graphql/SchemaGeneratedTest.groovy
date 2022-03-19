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

package io.jmix.graphql

import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaPrinter
import org.springframework.beans.factory.annotation.Autowired

import static graphql.schema.idl.SchemaPrinter.Options.defaultOptions

class SchemaGeneratedTest extends AbstractGraphQLTest {

    @Autowired
    GraphQLSchema graphQLSchema

    //  update schema script
    //  new File("src/test/resources/graphql/io/jmix/graphql/schema.graphql")
    //  .write(new SchemaPrinter(defaultOptions().includeDirectives(false)).print(graphQLSchema))
    def "graphql schema generated"() {
        when:
        def opts = defaultOptions().includeDirectives(false)
        def schemaLines = new SchemaPrinter(opts).print(graphQLSchema).split('\n')
        def expectSchemaLines = new File("src/test/resources/graphql/io/jmix/graphql/schema.graphql").readLines()

        then:
        for (int i = 0; i < schemaLines.length; i++) {
            assert expectSchemaLines[i].trim() == schemaLines[i].trim()
        }
    }
}
