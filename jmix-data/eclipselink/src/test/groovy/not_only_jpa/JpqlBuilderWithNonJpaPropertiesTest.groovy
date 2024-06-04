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

package not_only_jpa

import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.impl.JpqlQueryBuilder
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.Customer

class JpqlBuilderWithNonJpaPropertiesTest extends DataSpec {

    @Autowired
    BeanFactory beanFactory

    def "test only non-jpa properties in conditions"(Condition condition) {
        JpqlQueryBuilder queryBuilder

        when:
        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
                .setEntityName('data_TestEntityWithNonPersistentRef')
                .setQueryString('select e from data_TestEntityWithNonPersistentRef e')
                .setCondition(condition)
                .setQueryParameters([:])

        then:
        queryBuilder.getResultQueryString() == 'select e from data_TestEntityWithNonPersistentRef e'
        queryBuilder.getResultParameters().size() == 0

        where:
        condition << [
                LogicalCondition.and()
                              .add(PropertyCondition.equal('customer', new Customer())),

                LogicalCondition.and()
                        .add(PropertyCondition.equal('customer.name', 'abc')),

                LogicalCondition.and()
                        .add(PropertyCondition.equal('customer.name', 'abc'))
                        .add(PropertyCondition.equal('customer.id', UUID.randomUUID())),

                LogicalCondition.and()
                        .add(PropertyCondition.equal('customer.name', 'abc'))
                        .add(LogicalCondition.or(
                                PropertyCondition.equal('customer.id', UUID.randomUUID())
                        ))
        ]
    }

    def "test jpa and non-jpa properties in conditions"() {
        JpqlQueryBuilder queryBuilder

        when:
        def condition = LogicalCondition.and()
                .add(PropertyCondition.createWithParameterName('name', PropertyCondition.Operation.EQUAL, 'name'))
                .add(PropertyCondition.equal('customer.name', 'abc'))
                .add(LogicalCondition.or(
                        PropertyCondition.equal('customer.id', UUID.randomUUID())
                ))

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
                .setEntityName('data_TestEntityWithNonPersistentRef')
                .setQueryString('select e from data_TestEntityWithNonPersistentRef e')
                .setCondition(condition)
                .setQueryParameters(['name': 'abc'])

        then:
        queryBuilder.getResultQueryString() == 'select e from data_TestEntityWithNonPersistentRef e where (e.name = :name)'
        queryBuilder.getResultParameters().size() == 1
    }
}
