package spec.haulmont.cuba.core.query_sort

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.testsupport.TestJpqlSortExpressionProvider
import io.jmix.core.Metadata
import io.jmix.core.Sort
import io.jmix.data.impl.JpqlQueryBuilder
import io.jmix.data.persistence.JpqlSortExpressionProvider
import spec.haulmont.cuba.core.CoreTestSpecification

class QuerySortTest extends CoreTestSpecification {

    def "sort"() {

        JpqlQueryBuilder queryBuilder

        when: "by single property"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from test$User u')
                .setSort(Sort.by('name'))
                .setEntityName('test$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from test$User u order by u.name, u.id'

        when: "by two properties"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from test$User u')
                .setSort(Sort.by('login', 'name'))
                .setEntityName('test$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from test$User u order by u.login, u.name, u.id'

        when: "by two properties desc"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from test$User u')
                .setSort(Sort.by(Sort.Direction.DESC, 'login', 'name'))
                .setEntityName('test$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from test$User u order by u.login desc, u.name desc, u.id desc'

        when: "by reference property"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from test$User u')
                .setSort(Sort.by('group.name'))
                .setEntityName('test$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from test$User u left join u.group u_group order by u_group.name, u.id'

        when: "by reference property desc"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from test$User u')
                .setSort(Sort.by(Sort.Direction.DESC, 'group.name'))
                .setEntityName('test$User')

        then:

        queryBuilder.getResultQueryString() == 'select u from test$User u left join u.group u_group order by u_group.name desc, u.id desc'
    }

    def "sort by unique id property"() {

        JpqlQueryBuilder queryBuilder

        when:

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test$QueryResult e')
                .setSort(Sort.by('queryKey'))
                .setEntityName('test$QueryResult')

        then:

        queryBuilder.getResultQueryString() == 'select e from test$QueryResult e order by e.queryKey, e.id'
    }

    def "sort by single property with order function and nulls first"() {

        JpqlQueryBuilder queryBuilder
        TestJpqlSortExpressionProvider sortExpressionProvider

        setup:
        sortExpressionProvider = AppBeans.get(JpqlSortExpressionProvider)
        Metadata metadata = AppBeans.get(Metadata)
        sortExpressionProvider.addToUpperPath(metadata.getClass('test$Order').getPropertyPath('number'))

        when:

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test$Order e')
                .setSort(Sort.by('number'))
                .setEntityName('test$Order')

        then:

        queryBuilder.getResultQueryString() == 'select e from test$Order e order by upper( e.number) asc nulls first, e.id'

        cleanup:
        sortExpressionProvider.resetToUpperPaths()
    }

    def "sort by multiple properties in different directions is not supported"() {

        JpqlQueryBuilder queryBuilder

        when:

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select u from test$User u')
                .setSort(Sort.by(Sort.Order.asc('login'), Sort.Order.desc('name')))
                .setEntityName('test$User').getResultQueryString()

        then:

        thrown(UnsupportedOperationException)
    }

    def "sort by non-persistent property"() {

        JpqlQueryBuilder queryBuilder

        when: "by single non-persistent property"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test$EntitySnapshot e')
                .setSort(Sort.by('changeDate'))
                .setEntityName('test$EntitySnapshot')

        then:

        queryBuilder.getResultQueryString() == 'select e from test$EntitySnapshot e order by e.snapshotDate, e.id'

        when: "by persistent and non-persistent property"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test$EntitySnapshot e')
                .setSort(Sort.by('createTs', 'changeDate'))
                .setEntityName('test$EntitySnapshot')

        then:

        queryBuilder.getResultQueryString() == 'select e from test$EntitySnapshot e order by e.createTs, e.snapshotDate, e.id'

        when: "by single non-persistent property desc"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test$EntitySnapshot e')
                .setSort(Sort.by(Sort.Direction.DESC, 'changeDate'))
                .setEntityName('test$EntitySnapshot')

        then:

        queryBuilder.getResultQueryString() == 'select e from test$EntitySnapshot e order by e.snapshotDate desc, e.id desc'

        when: "by non-persistent property related to two other properties"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test$EntitySnapshot e')
                .setSort(Sort.by('label'))
                .setEntityName('test$EntitySnapshot')

        then:

        queryBuilder.getResultQueryString() == 'select e from test$EntitySnapshot e left join e.author e_author order by e.snapshotDate, e_author.login, e_author.name, e.id'

        when: "by non-persistent property related to two other properties desc"

        queryBuilder = AppBeans.get(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test$EntitySnapshot e')
                .setSort(Sort.by(Sort.Direction.DESC, 'label'))
                .setEntityName('test$EntitySnapshot')

        then:

        queryBuilder.getResultQueryString() == 'select e from test$EntitySnapshot e left join e.author e_author order by e.snapshotDate desc, e_author.login desc, e_author.name desc, e.id desc'
    }
}
