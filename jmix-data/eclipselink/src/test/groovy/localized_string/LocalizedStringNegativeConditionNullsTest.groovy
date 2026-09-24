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

package localized_string

import io.jmix.core.DataManager
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.impl.JpqlQueryBuilder
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import test_support.DataSpec
import test_support.entity.TestLocalizedNameEntity

@TestPropertySource(properties = ["jmix.data.include-null-clause-in-not-conditions=true"])
class LocalizedStringNegativeConditionNullsTest extends DataSpec {

    static final Locale DE = Locale.GERMAN

    @Autowired
    DataManager dataManager
    @Autowired
    BeanFactory beanFactory

    void setup() {
        dataManager.save(item('1', null), item('2', 'Plain'), item('3', 'Default\nde=Bericht'))
    }

    TestLocalizedNameEntity item(String code, String name) {
        def e = dataManager.create(TestLocalizedNameEntity)
        e.code = code
        e.name = name
        return e
    }

    List<String> codes(Condition condition) {
        dataManager.load(TestLocalizedNameEntity).condition(condition).list()*.code.sort()
    }

    String jpqlOf(Condition condition) {
        def queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_LocalizedNameEntity e')
                .setEntityName('test_LocalizedNameEntity')
                .setCondition(condition)
                .setQueryParameters([:])
        return queryBuilder.getResultQueryString()
    }

    def "a negative comparison keeps the rows whose column is null"() {
        when:
        authenticateWithLocale(DE)

        then:
        codes(PropertyCondition.notEqual('name', 'Bericht')) == ['1', '2']
        codes(PropertyCondition.createWithValue('name', PropertyCondition.Operation.NOT_CONTAINS, 'bericht')) == ['1', '2']

        and: "the resolved text is null only when the column is, so the clause asks the column, which the " +
                "parser reads at once, instead of the resolved expression in its slow function form"
        def jpql = jpqlOf(PropertyCondition.notEqual('name', 'Bericht'))
        jpql.contains(' or e.name is null)')
        !jpql.contains("function('coalesce'")
    }
}
