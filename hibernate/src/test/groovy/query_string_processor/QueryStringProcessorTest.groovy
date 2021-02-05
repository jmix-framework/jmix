/*
 * Copyright 2019 Haulmont.
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

package query_string_processor

import test_support.entity.TestAppEntity
import io.jmix.core.QueryStringProcessor
import test_support.DataSpec

import org.springframework.beans.factory.annotation.Autowired

class QueryStringProcessorTest extends DataSpec {

    @Autowired
    QueryStringProcessor processor

    def "test from clause in query"() {
        def query

        when:
        query = processor.process('from test_TestAppEntity o left join o.items i where i.name = :name', TestAppEntity)
        then:
        query == 'select o from test_TestAppEntity o left join o.items i where i.name = :name'

        when:
        query = processor.process('from test_TestAppEntity o, test_TestSecondAppEntity c where c.appEntity = o', TestAppEntity)
        then:
        query == 'select o from test_TestAppEntity o, test_TestSecondAppEntity c where c.appEntity = o'

        when:
        processor.process('from test_TestAppEntityItem l join l.appEntity o', TestAppEntity)
        then: "not supported, use full query syntax"
        thrown(RuntimeException)
    }

    def "test where clause in query"() {
        def query

        when:
        query = processor.process('where e.number = :num', TestAppEntity)
        then:
        query == 'select e from test_TestAppEntity e where e.number = :num'
    }

    def "test order by in query"() {
        def query

        when:
        query = processor.process('order by e.number', TestAppEntity)
        then:
        query == 'select e from test_TestAppEntity e order by e.number'
    }

    def "test property conditions in query"() {
        def query

        when:
        query = processor.process('e.number = :num and e.name = :name', TestAppEntity)
        then:
        query == 'select e from test_TestAppEntity e where e.number = :num and e.name = :name'
    }

    def "test upper case"() {
        def query

        when:
        query = processor.process('FROM test_TestAppEntity o LEFT JOIN o.items i WHERE i.name = :name', TestAppEntity)
        then:
        query == 'select o FROM test_TestAppEntity o LEFT JOIN o.items i WHERE i.name = :name'

        when:
        query = processor.process('FROM test_TestAppEntity o, test_TestSecondAppEntity c WHERE c.appEntity = o', TestAppEntity)
        then:
        query == 'select o FROM test_TestAppEntity o, test_TestSecondAppEntity c WHERE c.appEntity = o'
    }
}
