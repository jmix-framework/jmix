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

package repository

import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import io.jmix.core.Metadata
import io.jmix.eclipselink.impl.entitycache.QueryCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.repository.Address
import test_support.entity.repository.Customer
import test_support.entity.repository.SalesOrder
import test_support.repository.FeaturedOrderRepository
import test_support.repository.OrderRepository

import java.time.LocalDate
import java.time.ZoneOffset

class QueryHintsTest extends DataSpec {
    @Autowired
    private FeaturedOrderRepository featuredOrderRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private FetchPlanRepository fetchPlanRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate

    @Autowired
    private QueryCache queryCache


    private Customer customer1
    private SalesOrder order1, order2

    void setup() {

        customer1 = metadata.create(Customer.class);
        customer1.setName("cust1");
        customer1.setAddress(new Address());
        customer1.getAddress().setCity("Samara");

        order1 = metadata.create(SalesOrder.class);
        order1.setCustomer(customer1);
        order1.setNumber("111");
        order1.setDate(Date.from(LocalDate.parse("2010-01-01").atStartOfDay().toInstant(ZoneOffset.UTC)));

        order2 = metadata.create(SalesOrder.class);
        order2.setCustomer(customer1);
        order2.setNumber("112");
        order2.setDate(Date.from(LocalDate.parse("2010-03-01").atStartOfDay().toInstant(ZoneOffset.UTC)));


        dataManager.save(
                customer1,
                order1,
                order2,
        )
    }

    void cleanup() {
        jdbcTemplate.execute("delete from REPOSITORY_SALES_ORDER")
        jdbcTemplate.execute("delete from REPOSITORY_CUSTOMER")
        queryCache.invalidateAll()
    }

    void "hints work for base methods"() {
        setup:
        dataManager.remove(order2)
        FetchPlan plan = fetchPlanRepository.findFetchPlan(metadata.getClass(SalesOrder), "_instance_name")

        expect:
        featuredOrderRepository.findAll().size() == 2
        featuredOrderRepository.findById(order2.id, plan).isPresent()
        featuredOrderRepository.findAll(plan).size() == 2
        featuredOrderRepository.findAll(Sort.unsorted(), plan).size() == 2
        featuredOrderRepository.findAll(Pageable.ofSize(10), plan).totalElements == 2
        featuredOrderRepository.findAll(Pageable.ofSize(10), plan).numberOfElements == 2
        featuredOrderRepository.findAll([order2.id], plan).size() == 1
        featuredOrderRepository.findAll(Sort.unsorted()).size() == 2
        featuredOrderRepository.findAll(Pageable.ofSize(1)).totalElements == 2
        featuredOrderRepository.findAll(Pageable.ofSize(1)).totalPages == 2
        featuredOrderRepository.findById(order2.id).isPresent()
        featuredOrderRepository.existsById(order2.id)
        featuredOrderRepository.findAllById([order2.id]).size() == 1
        featuredOrderRepository.count() == 2

        orderRepository.findAll().size() == 1
        !orderRepository.findById(order2.id, plan).isPresent()
        orderRepository.findAll(plan).size() == 1
        orderRepository.findAll(Sort.unsorted(), plan).size() == 1
        orderRepository.findAll(Pageable.ofSize(10), plan).totalElements == 1
    }

    def "hints work for delete methods"() {
        when: "entity soft-deleted"
        featuredOrderRepository.delete(order2)
        then: "entity still exists for not soft-delete queries"
        featuredOrderRepository.findById(order2.id).isPresent()

        when: "soft-deletion disabled"
        featuredOrderRepository.deleteAllById([order2.id])
        then: "entity completely deleted"
        !featuredOrderRepository.findById(order2.id).isPresent()
        featuredOrderRepository.findAll().size() == 1

        when:
        featuredOrderRepository.deleteAll()
        then:
        featuredOrderRepository.findAll().size() == 0

    }

    def "hints work for query methods"() {
        setup:
        dataManager.remove(order2)

        expect:
        featuredOrderRepository.loadByQueryWithSoftDeletionFalse().size() == 2
        featuredOrderRepository.loadByQueryWithSoftDeletionTrue().size() == 1
        featuredOrderRepository.loadAllByQuery().size() == 1

        featuredOrderRepository.findSalesByCustomer(customer1, Pageable.ofSize(1)).totalElements == 2
        featuredOrderRepository.findSalesByCustomer(customer1, Pageable.ofSize(1)).totalPages == 2
        featuredOrderRepository.findSalesByCustomer(customer1, Pageable.ofSize(1)).numberOfElements == 1

        featuredOrderRepository.countCustomersByIdNotNull() == 2
        featuredOrderRepository.existsByNumber(order2.number)

        when:
        featuredOrderRepository.removeByNumber(order2.number)
        then:
        !featuredOrderRepository.existsByNumber(order2.number)
        featuredOrderRepository.count() == 1
    }

    def "test query cache hint"() {
        expect:
        queryCache.size() == 0
        when:
        featuredOrderRepository.loadAllByQuery()
        then:
        queryCache.size() == 0

        when:
        featuredOrderRepository.loadAllWithCache()
        then:
        queryCache.size() == 1

    }
}
