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
import io.jmix.core.EntityStates
import io.jmix.core.FetchPlans
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.jdbc.core.JdbcTemplate
import test_support.DataSpec
import test_support.entity.repository.Address
import test_support.entity.repository.Customer
import test_support.entity.repository.SalesOrder
import test_support.repository.OrderRepository

import java.time.LocalDate
import java.time.ZoneOffset

class OrderRepositoryTest extends DataSpec {
    @Autowired
    private Metadata metadata;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private JdbcTemplate jdbcTemplate
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private FetchPlans fetchPlans;


    private Customer customer1, customer2, customer3;
    private SalesOrder order1, order2, order3, order4, order5;

    void setup() {

        customer1 = metadata.create(Customer.class);
        customer1.setName("cust1");
        customer1.setAddress(new Address());
        customer1.getAddress().setCity("Samara");

        customer2 = metadata.create(Customer.class);
        customer2.setName("some cust 2");
        customer2.setAddress(new Address());
        customer2.getAddress().setCity("Springfield");

        customer3 = metadata.create(Customer.class);
        customer3.setName("another cust 3");
        customer3.setAddress(new Address());
        customer3.getAddress().setCity("Springfield");


        order1 = metadata.create(SalesOrder.class);
        order1.setCustomer(customer1);
        order1.setNumber("111");
        order1.setDate(Date.from(LocalDate.parse("2010-01-01").atStartOfDay().toInstant(ZoneOffset.UTC)));

        order2 = metadata.create(SalesOrder.class);
        order2.setCustomer(customer1);
        order2.setNumber("112");
        order2.setDate(Date.from(LocalDate.parse("2010-03-01").atStartOfDay().toInstant(ZoneOffset.UTC)));

        order3 = metadata.create(SalesOrder.class);
        order3.setCustomer(customer2);
        order3.setNumber("113");
        order3.setDate(Date.from(LocalDate.parse("2004-02-29").atStartOfDay().toInstant(ZoneOffset.UTC)));

        order4 = metadata.create(SalesOrder.class);
        order4.setCustomer(customer2);
        order4.setNumber("114");
        order4.setDate(Date.from(LocalDate.parse("2018-08-19").atStartOfDay().toInstant(ZoneOffset.UTC)));

        order5 = metadata.create(SalesOrder.class);
        order5.setCustomer(customer3);
        order5.setNumber(null);
        order5.setDate(null)


        dataManager.save(
                customer1,
                customer2,
                customer3,
                order1,
                order2,
                order3,
                order4,
                order5
        );
    }

    void cleanup() {
        jdbcTemplate.execute("delete from REPOSITORY_SALES_ORDER")
        jdbcTemplate.execute("delete from REPOSITORY_CUSTOMER")
    }

    void testCountOrdersByCustomer() {
        when:
        long first = orderRepository.countSalesOrdersByCustomer(customer1);
        then:
        first == 2

        when:
        long second = orderRepository.countSalesOrdersByCustomer(customer2);
        then:
        second == 2
        when:
        long third = orderRepository.countSalesOrdersByCustomer(customer3);
        then:
        third == 1
    }

    void testCountOrdersByCustomerCity() {
        when:
        long first = orderRepository.countSalesOrdersByCustomerAddressCity("Samara");
        then:
        first == 2

        when:
        long second = orderRepository.countSalesOrdersByCustomerAddressCity("Springfield");
        then:
        second == 3

        when:
        long third = orderRepository.countSalesOrdersByCustomerAddressCity("London");
        then:
        third == 0

    }

    void testRemovingSalesOrdersByCustomerAddressCity() {
        expect:
        orderRepository.existsSalesOrdersByCustomerAddressCity("Samara")
        !orderRepository.existsSalesOrdersByCustomerAddressCity("London")

        when:
        orderRepository.removeSalesOrdersByCustomerAddressCity("Samara")
        orderRepository.removeSalesOrdersByCustomerAddressCity("London")

        then:
        !orderRepository.existsSalesOrdersByCustomerAddressCity("Samara")
        !orderRepository.existsSalesOrdersByCustomerAddressCity("London")
    }


    void testQueryOrderByCustomerWithAndClause() {
        when:
        List<SalesOrder> orders = orderRepository.findByCustomerNameAndCustomerAddressCity("some cust 2", "Springfield");
        then:
        orders.size() == 2
        orders.contains(order3)
    }


    void testFindByAssociationProperty() {
        when:
        List<SalesOrder> orders = orderRepository.findByCustomer(customer1);
        then:
        orders.size() == 2
        orders.contains(order1)
        orders.contains(order2)
    }

    void testFindByDateAfter() {
        when:
        List<SalesOrder> orders = orderRepository.findSalesOrderByDateAfter(Date.from(LocalDate.parse("2010-05-05").atStartOfDay().toInstant(ZoneOffset.UTC)));
        then:
        orders.size() == 1
        orders.contains(order4)
    }

    void testFindByDateBefore() {
        when:
        List<SalesOrder> orders = orderRepository.findSalesOrderByDateBefore(Date.from(LocalDate.parse("2010-05-05").atStartOfDay().toInstant(ZoneOffset.UTC)));
        then:
        orders.size() == 3
        orders.contains(order1)
        orders.contains(order2)
        orders.contains(order3)
    }

    void testSorting() {
        when:
        List<SalesOrder> orders = orderRepository.findSalesOrderByDateBeforeOrderByDateAsc(Date.from(LocalDate.parse("2010-05-05").atStartOfDay().toInstant(ZoneOffset.UTC)));
        then:
        orders.size() == 3
        orders[0] == order3
        orders[1] == order1
        orders[2] == order2


        when:
        orders = orderRepository.findSalesOrderByDateBeforeOrderByDateDesc(Date.from(LocalDate.parse("2010-05-05").atStartOfDay().toInstant(ZoneOffset.UTC)));
        then:
        orders.size() == 3
        orders[0] == order2
        orders[1] == order1
        orders[2] == order3

    }

    void 'dynamic sorting works'() {
        when:
        List<SalesOrder> orders = orderRepository.findSalesByDateAfterAndNumberIn(
                Date.from(LocalDate.parse("2005-01-01").atStartOfDay().toInstant(ZoneOffset.UTC)),
                Sort.by(Sort.Direction.DESC, "number"),
                ["111", "114", "113", "112"])
        then:
        orders != null
        orders[0] == order4
        orders[1] == order2
        orders[2] == order1

        when:
        orders = orderRepository.findSalesByDateAfterAndNumberIn(
                new Date(0),
                Sort.by(Sort.Direction.ASC, "number"),
                ["111", "114", "113", "112"])
        then:
        orders != null
        orders[0] == order1
        orders[1] == order2
        orders[2] == order3
        orders[3] == order4

        when:
        orders = orderRepository.findSalesByQuery(
                new Date(0),
                Sort.by(Sort.Direction.ASC, "number"),
                ["111", "114", "113", "112"])
        then:
        orders != null
        orders[0] == order1
        orders[1] == order2
        orders[2] == order3
        orders[3] == order4

        when:
        orders = orderRepository.findSalesByCustomerNotNull(Sort.by("number"))
        then:
        orders != null
        orders[0] == order5
        orders[1] == order1
        orders[2] == order2
        orders[3] == order3
        orders[4] == order4

    }

    void 'paging works'() {
        when: "first page"
        Page<SalesOrder> first = orderRepository.findSalesByDateAfterAndNumberIn(
                new Date(0),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "number")),
                ["111", "114", "113", "112"])
        then:
        first.numberOfElements == 2
        first.toList()[0] == order1
        first.toList()[1] == order2
        first.hasNext()
        !first.hasPrevious()
        !entityStates.isLoaded(first.toList()[0], "customer")

        when: "second page"
        Page<SalesOrder> second = orderRepository.findSalesByDateAfterAndNumberIn(
                new Date(0),
                PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "number")),
                ["111", "114", "113", "112"])
        then:
        second.numberOfElements == 2
        second.toList()[0] == order3
        second.toList()[1] == order4
        !second.hasNext()
        second.hasPrevious()

        when: "less items than page size"
        Page<SalesOrder> third = orderRepository.findSalesByDateAfterAndNumberIn(
                new Date(0),
                PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "number")),
                ["111", "114", "113", "112"])
        then:
        third.numberOfElements == 1
        third.toList()[0] == order1
        third.totalElements == 4
        third.totalPages == 2

        when: "PageRequest.unpaged() passed"
        Page<SalesOrder> unpagedPage = orderRepository.findSalesByDateAfterAndNumberIn(
                new Date(0),
                Pageable.unpaged(),
                ["111", "114", "113", "112"])
        then:
        unpagedPage.numberOfElements == 4
        unpagedPage.totalElements == 4
        unpagedPage.totalPages == 1


        when: "jpql query request paged 1"
        Page<SalesOrder> pageByQueryOne = orderRepository.findSalesByQueryWithPaging(new Date(0),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "number")),
                ["111", "114", "113", "112"])
        then:
        pageByQueryOne.numberOfElements == 2
        pageByQueryOne.toList()[0] == order1
        pageByQueryOne.toList()[1] == order2
        pageByQueryOne.hasNext()
        !pageByQueryOne.hasPrevious()

        when: "jpql query request paged 2"
        Page<SalesOrder> pageByQueryTwo = orderRepository.findSalesByQueryWithPaging(new Date(0),
                PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "number")),
                ["111", "114", "113", "112"])
        then:
        pageByQueryTwo.numberOfElements == 1
        pageByQueryTwo.toList()[0] == order1
        !pageByQueryTwo.hasNext()
        pageByQueryTwo.hasPrevious()

        when: "jpql query request paged 3 (with positional parameters)"
        Page<SalesOrder> pageByQueryThree = orderRepository.findSalesByQueryWithPagingAndPositionalParameters(
                ["111", "114", "113", "112"],
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "number")),
                new Date(0))
        then:
        pageByQueryThree.numberOfElements == 3
        pageByQueryThree.toList()[0] == order4
        pageByQueryThree.toList()[1] == order3
        pageByQueryThree.toList()[2] == order2
        pageByQueryThree.hasNext()
        !pageByQueryThree.hasPrevious()

    }

    void 'fetch plan parameter works with paging'() {
        when:
        Page<SalesOrder> page = orderRepository.findByDateAfterAndNumberIn(
                new Date(0),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "number")),
                fetchPlans.builder(SalesOrder).addAll("customer").build(),
                ["111", "114", "113", "112"]
        )
        then:
        entityStates.isLoaded(page.toList()[0], "customer")
        !entityStates.isLoaded(page.toList()[0].customer, "address")

        when:
        Page<SalesOrder> page2 = orderRepository.findByDateAfterAndNumberIn(
                new Date(0),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "number")),
                fetchPlans.builder(SalesOrder).addAll("customer", "customer.address").build(),
                ["111", "114", "113", "112"]
        )
        then:
        entityStates.isLoaded(page2.toList()[0], "customer")
        entityStates.isLoaded(page2.toList()[0].customer, "address")
    }

    void 'slicing works'() {
        when:
        Slice<SalesOrder> first = orderRepository.findSalesByCustomerNameIn(
                ["cust1", "some cust 2", "another cust 3"],
                PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "customer.address.city", "date")))
        then:
        first != null
        !(first instanceof Page)
        first.numberOfElements == 3
        first.hasNext()
        !first.hasPrevious()
        first.toList()[0] == order1
        first.toList()[1] == order2
        first.toList()[2] == order5


        when:
        Slice<SalesOrder> second = orderRepository.findSalesByCustomerNameIn(
                ["cust1", "some cust 2", "another cust 3"],
                first.nextPageable())
        then:
        second != null
        second.numberOfElements == 2
        !second.hasNext()
        second.hasPrevious()
        second.toList()[0] == order3
        second.toList()[1] == order4

        when:
        Slice<SalesOrder> unpagedSlice = orderRepository.findSalesByCustomerNameIn(
                ["cust1", "some cust 2", "another cust 3"],
                Pageable.unpaged())
        then:
        unpagedSlice != null
        unpagedSlice.numberOfElements == 5
        !unpagedSlice.hasNext()
        !unpagedSlice.hasPrevious()
    }


    void 'sorting by two properties works'() {
        when:
        List<SalesOrder> ordersAsc = orderRepository.findByCustomerNotNullOrderByCustomerAddressCityAscDateAsc()
        then:
        ordersAsc != null
        ordersAsc[0] == order1 && ordersAsc[0].customer.address.city == "Samara"//[Samara - Fri Jan 01 00:00:00 SAMT 2010]
        ordersAsc[1] == order2 && ordersAsc[1].customer.address.city == "Samara"//[Samara - Mon Mar 01 00:00:00 SAMT 2010]
        ordersAsc[2] == order5 && ordersAsc[2].customer.address.city == "Springfield"//[Springfield - null]
        ordersAsc[3] == order3 && ordersAsc[3].customer.address.city == "Springfield"//[Springfield - Sun Feb 29 00:00:00 SAMT 2004]
        ordersAsc[4] == order4 && ordersAsc[4].customer.address.city == "Springfield"//[Springfield - Sun Aug 19 00:00:00 SAMT 2018]

        when:
        List<SalesOrder> ordersDesc = orderRepository.findByCustomerNotNullOrderByCustomerAddressCityDescDateDesc()

        then:
        ordersDesc[0] == order5 && ordersDesc[0].customer.address.city == "Springfield"//[Springfield - null]
        ordersDesc[1] == order4 && ordersDesc[1].customer.address.city == "Springfield"//[Springfield - Sun Aug 19 00:00:00 SAMT 2018]
        ordersDesc[2] == order3 && ordersDesc[2].customer.address.city == "Springfield"//[Springfield - Sun Feb 29 00:00:00 SAMT 2004]
        ordersDesc[3] == order2 && ordersDesc[3].customer.address.city == "Samara"//[Samara - Mon Mar 01 00:00:00 SAMT 2010]
        ordersDesc[4] == order1 && ordersDesc[4].customer.address.city == "Samara"//[Samara - Fri Jan 01 00:00:00 SAMT 2010]

        when:
        Iterable<SalesOrder> ordersAll = orderRepository.findAll(Sort.by(Sort.Direction.ASC, "customer.address.city", "date"))
        then:
        ordersAll[0] == order1
        ordersAll[1] == order2
        ordersAll[2] == order5
        ordersAll[3] == order3
        ordersAll[4] == order4
    }

    void 'findAll with paging test'() {
        when:
        Page<SalesOrder> secondOfThree = orderRepository.findAll(
                PageRequest.of(1, 3, Sort.Direction.ASC, "customer.address.city", "date"))
        then: "findAll method works"
        secondOfThree.getNumberOfElements() == 2
        secondOfThree.getContent()[0] == order3
        secondOfThree.getContent()[1] == order4
        secondOfThree.totalPages == 2
        secondOfThree.totalElements == 5


    }

    void testCountByIsNull() {
        when:
        long count = orderRepository.countByNumberInOrDateIsNull(["111", "-2"]);

        then:
        count == 2 //numbers: 111,null
    }

}
