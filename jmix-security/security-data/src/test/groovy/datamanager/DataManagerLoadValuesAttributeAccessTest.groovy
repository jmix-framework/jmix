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

package datamanager

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SecurityContextHelper
import io.jmix.security.role.RoleGrantedAuthorityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Unroll
import test_support.SecurityDataSpecification
import test_support.entity.ManyToOneEntity
import test_support.entity.OneToManyEntity
import test_support.entity.TestOrder
import test_support.role.TestLoadValuesAttributeRole

class DataManagerLoadValuesAttributeAccessTest extends SecurityDataSpecification {

    static final String PASSWORD = "123"

    @Autowired
    DataManager dataManager

    @Autowired
    UnconstrainedDataManager unconstrainedDataManager

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    @Autowired
    Metadata metadata

    @Autowired
    RoleGrantedAuthorityUtils roleGrantedAuthorityUtils

    UserDetails user
    TestOrder order
    ManyToOneEntity manyToOneEntity
    Authentication systemAuthentication

    def setup() {
        user = User.builder()
                .username("user")
                .password("{noop}$PASSWORD")
                .authorities(roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(TestLoadValuesAttributeRole.NAME))
                .build()
        userRepository.addUser(user)

        order = metadata.create(TestOrder)
        order.number = 'n1'
        def oneToManyEntity = metadata.create(OneToManyEntity)
        oneToManyEntity.name = 'p1'
        manyToOneEntity = metadata.create(ManyToOneEntity)
        manyToOneEntity.name = 'm1'
        manyToOneEntity.oneToManyEntity = oneToManyEntity
        unconstrainedDataManager.save(order, oneToManyEntity, manyToOneEntity)

        systemAuthentication = SecurityContextHelper.getAuthentication()
    }

    def cleanup() {
        SecurityContextHelper.setAuthentication(systemAuthentication)
        userRepository.removeUser(user)
        jdbcTemplate.execute('delete from TEST_ORDER')
        jdbcTemplate.execute('delete from TEST_MANY_TO_ONE_ENTITY')
        jdbcTemplate.execute('delete from TEST_ONE_TO_MANY_ENTITY')
    }

    def "loadValues hides a denied column selected by a single attribute path"() {
        given:
        authenticate()

        when:
        def result = dataManager.loadValues('select o.id, o.number from test_Order o')
                .properties('orderId', 'number')
                .list()

        then:
        result.size() == 1
        result[0].getValue('orderId') == order.id
        result[0].getValue('number') == null
    }

    @Unroll
    def "loadValues hides only the denied column when the first column is #description"() {
        given:
        authenticate()

        when:
        def loader = dataManager.loadValues("select $firstColumn, o.id, o.number from test_Order o")
                .properties('first', 'orderId', 'number')
        if (firstColumn.contains(':p')) {
            loader.parameter('p', 'x')
        }
        def result = loader.list()

        then:
        result.size() == 1
        (result[0].getValue('first') != null) == firstColumnVisible
        result[0].getValue('orderId') == order.id
        result[0].getValue('number') == null

        where:
        description                                     | firstColumn                                                         | firstColumnVisible
        'a literal'                                     | "'marker'"                                                          | true
        'an entity'                                     | 'o'                                                                 | true
        'a parameter'                                   | 'concat(:p, \'\')'                                                  | true
        'an expression over allowed attributes'         | "case when o.id is null or o.id = o.id then 'a' else 'b' end"       | true
        'an expression over several attributes'         | "case when o.id is null then o.number else o.number end"            | false
        'a scalar subquery'                             | '(select count(x) from test_Order x where x.id = o.id)'             | true
        'a scalar subquery over a denied attribute'     | "(select count(x) from test_Order x where x.number = o.number)"     | false
    }

    def "loadValues hides the denied column placed before an expression"() {
        given:
        authenticate()

        when:
        def result = dataManager.loadValues(
                "select o.number, case when o.id is null then 'a' else 'b' end, o.id from test_Order o")
                .properties('number', 'marker', 'orderId')
                .list()

        then:
        result.size() == 1
        result[0].getValue('number') == null
        result[0].getValue('marker') == 'b'
        result[0].getValue('orderId') == order.id
    }

    def "loadValues hides a denied attribute of a joined entity when a literal column comes first"() {
        given:
        authenticate()

        when:
        def result = dataManager.loadValues(
                "select 'x', m.name, p.name from test_ManyToOneEntity m join m.oneToManyEntity p")
                .properties('marker', 'manyName', 'oneName')
                .list()

        then:
        result.size() == 1
        result[0].getValue('marker') == 'x'
        result[0].getValue('manyName') == 'm1'
        result[0].getValue('oneName') == null
    }

    def "loadValues hides every column that selects the same denied attribute"() {
        given:
        authenticate()

        when:
        def result = dataManager.loadValues('select o.number, o.id, o.number from test_Order o')
                .properties('number1', 'orderId', 'number2')
                .list()

        then:
        result.size() == 1
        result[0].getValue('number1') == null
        result[0].getValue('orderId') == order.id
        result[0].getValue('number2') == null
    }

    protected void authenticate() {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.username, PASSWORD))
        SecurityContextHelper.setAuthentication(authentication)
    }
}
