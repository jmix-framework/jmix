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
import io.jmix.core.Stores
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.impl.JpqlQueryBuilder
import io.jmix.data.persistence.DbmsFeatures
import io.jmix.data.persistence.DbmsSpecifics
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import test_support.DataSpec
import test_support.entity.TestLocalizedNameEntity
import test_support.localized_string.BinaryCharHsqlDbmsFeatures

/**
 * The expression on a database whose char function returns a binary string, as MySQL's does: a case-insensitive
 * comparison lower-cases the column inside the expression, and with it the key of every entry marker. Only HSQLDB
 * is reachable from the suite, so HSQLDB reports a binary char function here; lower-casing inside the expression
 * is as valid there. Whether the char function of MySQL really returns a binary string is not tested.
 */
@ContextConfiguration(classes = [LocalizedStringBinaryCharFunctionTest.TestConfiguration], inheritLocations = true)
class LocalizedStringBinaryCharFunctionTest extends DataSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    DbmsSpecifics dbmsSpecifics
    @Autowired
    BeanFactory beanFactory

    void setup() {
        dataManager.save(
                item('1', 'Report\npt_BR=Relatório mensal'),
                item('2', 'pt_BR=Relatório anual'))
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

    def "the database reports a binary char function"() {
        expect: "otherwise the features below would run the branch the rest of the suite runs"
        dbmsSpecifics.getDbmsFeatures(Stores.MAIN).charFunctionBinary
    }

    def "a case-insensitive comparison lower-cases the column inside the expression"() {
        expect: "the query builder spaces the arguments of a function out"
        jpqlOf(PropertyCondition.contains('name', 'report')).replace(' ', '').contains('lower(e.name)')
    }

    def "a case-insensitive search finds an entry whose key carries a region"() {
        when:
        authenticateWithLocale(Locale.forLanguageTag('pt-BR'))

        then: "the key is looked for in the lower-cased column, so it is lower-cased too"
        codes(PropertyCondition.contains('name', 'RELATÓRIO MENSAL')) == ['1']
    }

    def "an entry on the first line of the lower-cased column is not read as the default value"() {
        when: "an English user reads the default value of both items"
        authenticateWithLocale(Locale.ENGLISH)

        then: "the second item has no default value, so the text of its pt_BR entry is not found"
        codes(PropertyCondition.contains('name', 'relat')) == []
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        DbmsFeatures test_BinaryCharHsqlDbmsFeatures() {
            return new BinaryCharHsqlDbmsFeatures()
        }
    }
}
