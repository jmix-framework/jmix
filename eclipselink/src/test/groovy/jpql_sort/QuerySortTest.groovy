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

package jpql_sort

import io.jmix.core.Metadata
import io.jmix.core.Sort
import io.jmix.data.impl.JpqlQueryBuilder
import io.jmix.data.persistence.JpqlSortExpressionProvider
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import test_support.DataSpec
import test_support.TestJpqlSortExpressionProvider

class QuerySortTest extends DataSpec {

    @Autowired
    Metadata metadata

    @Autowired
    JpqlSortExpressionProvider sortExpressionProvider

    @Autowired
    BeanFactory beanFactory

    def "sort"() {

        JpqlQueryBuilder queryBuilder

        when: "by single property"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from sec$User u')
                .setSort(Sort.by('name'))
                .setEntityName('sec$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from sec$User u order by u.name asc, u.id asc'

        when: "by two properties"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from sec$User u')
                .setSort(Sort.by('login', 'name'))
                .setEntityName('sec$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from sec$User u order by u.login asc, u.name asc, u.id asc'

        when: "by two properties desc"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from sec$User u')
                .setSort(Sort.by(Sort.Direction.DESC, 'login', 'name'))
                .setEntityName('sec$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from sec$User u order by u.login desc, u.name desc, u.id asc'

        when: "by reference property"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from sec$User u')
                .setSort(Sort.by('group.name'))
                .setEntityName('sec$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from sec$User u left join u.group u_group order by u_group.name asc, u.id asc'

        when: "by reference property desc"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from sec$User u')
                .setSort(Sort.by(Sort.Direction.DESC, 'group.name'))
                .setEntityName('sec$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from sec$User u left join u.group u_group order by u_group.name desc, u.id asc'
    }

    def "sort by unique id property"() {

        JpqlQueryBuilder queryBuilder

        when:

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from sec$User u')
                .setSort(Sort.by('login'))
                .setEntityName('sec$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from sec$User u order by u.login asc, u.id asc'
    }

    def "sort by single property with order function and nulls first"() {

        JpqlQueryBuilder queryBuilder

        setup:
        ((TestJpqlSortExpressionProvider) sortExpressionProvider).addToUpperPath(metadata.getClass('sales_Order').getPropertyPath('number'))

        when:

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from sales_Order e')
                .setSort(Sort.by('number'))
                .setEntityName('sales_Order')

        then:

        queryBuilder.getResultQueryString() == 'select e from sales_Order e order by upper( e.number) asc nulls first, e.id asc'

        cleanup:
        ((TestJpqlSortExpressionProvider) sortExpressionProvider).resetToUpperPaths()
    }

    def "sort by multiple properties in different directions"() {

        JpqlQueryBuilder queryBuilder

        when:

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from sec$User u')
                .setSort(Sort.by(Sort.Order.asc('login'), Sort.Order.desc('name')))
                .setEntityName('sec$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from sec$User u order by u.login asc, u.name desc, u.id asc'

        cleanup:
        ((TestJpqlSortExpressionProvider) sortExpressionProvider).resetToUpperPaths()
    }

    def "sort by non-persistent property"() {

        JpqlQueryBuilder queryBuilder

        when: "by single non-persistent property"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_TestAppEntity e')
                .setSort(Sort.by('changeDate'))
                .setEntityName('test_TestAppEntity')

        then:

        queryBuilder.getResultQueryString() == 'select e from test_TestAppEntity e order by e.appDate asc, e.id asc'

        when: "by persistent and non-persistent property"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_TestAppEntity e')
                .setSort(Sort.by('createTs', 'changeDate'))
                .setEntityName('test_TestAppEntity')

        then:

        queryBuilder.getResultQueryString() == 'select e from test_TestAppEntity e order by e.createTs asc, e.appDate asc, e.id asc'

        when: "by single non-persistent property desc"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_TestAppEntity e')
                .setSort(Sort.by(Sort.Direction.DESC, 'changeDate'))
                .setEntityName('test_TestAppEntity')

        then:

        queryBuilder.getResultQueryString() == 'select e from test_TestAppEntity e order by e.appDate desc, e.id asc'

        when: "by non-persistent property related to two other properties"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_TestAppEntity e')
                .setSort(Sort.by('label'))
                .setEntityName('test_TestAppEntity')

        then:

        queryBuilder.getResultQueryString() == 'select e from test_TestAppEntity e left join e.author e_author order by e_author.login asc, e_author.name asc, e.number asc, e.id asc'

        when: "by non-persistent property related to two other properties desc"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_TestAppEntity e')
                .setSort(Sort.by(Sort.Direction.DESC, 'label'))
                .setEntityName('test_TestAppEntity')

        then:

        queryBuilder.getResultQueryString() == 'select e from test_TestAppEntity e left join e.author e_author order by e_author.login desc, e_author.name desc, e.number desc, e.id asc'
    }

    def "sort key-value entity"() {

        JpqlQueryBuilder queryBuilder

        when: "by single persistent property"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e.name from test_TestAppEntity e')
                .setSort(Sort.by('name'))
                .setValueProperties(['name'])

        then:

        queryBuilder.getResultQueryString() == 'select e.name from test_TestAppEntity e order by e.name asc'

        when: "by aggregated single persistent property"

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e.id, min(e.name) from test_TestAppEntity e group by e.id')
                .setSort(Sort.by('min'))
                .setValueProperties(['id', 'min'])

        then:

        queryBuilder.getResultQueryString() == 'select e.id, min(e.name) from test_TestAppEntity e group by e.id order by min(e.name) asc'
    }

    def "sort by column of composite primary key"() {

        JpqlQueryBuilder queryBuilder

        when:

        queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_TestCompositeKeyEntity e')
                .setSort(Sort.by(Sort.Direction.DESC, 'id.tenant'))
                .setEntityName("test_TestCompositeKeyEntity")

        then:

        queryBuilder.getResultQueryString() == 'select e from test_TestCompositeKeyEntity e order by e.id.tenant desc, e.id.entityId asc'
    }
}
