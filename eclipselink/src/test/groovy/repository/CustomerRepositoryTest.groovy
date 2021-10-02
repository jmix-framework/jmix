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

import com.google.common.collect.Lists
import io.jmix.core.DataManager
import io.jmix.core.EntityStates
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.User
import test_support.DataSpec
import test_support.entity.repository.Address
import test_support.entity.repository.Customer
import test_support.repository.CustomerRepository

class CustomerRepositoryTest extends DataSpec {

    @Autowired
    private CustomerRepository customerRepository
    @Autowired
    private EntityStates entityStates
    @Autowired
    private DataManager dataManager
    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    protected SystemAuthenticator authenticator
    @Autowired
    protected InMemoryUserRepository userRepository

    private Customer customer1, customer2, customer3

    private User admin

    void setup() {
        admin = User.builder()
                .username('admin')
                .password('{noop}admin123')
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(admin)
        customer1 = customerRepository.create()
        customer1.setName("cust1")
        customer1.setAddress(new Address())
        customer1.getAddress().setCity("Samara")

        customer2 = customerRepository.create()
        customer2.setName("some cust 2")
        customer2.setAddress(new Address())
        customer2.getAddress().setCity("Springfield")

        customer3 = customerRepository.create()
        customer3.setName("another cust 3")
        customer3.setAddress(new Address())
        customer3.getAddress().setCity("Springfield")


        dataManager.save(
                customer1,
                customer2,
                customer3
        )
    }


    void cleanup() {
        jdbcTemplate.execute("delete from REPOSITORY_SALES_ORDER")
        jdbcTemplate.execute("delete from REPOSITORY_CUSTOMER")
    }


    void testSave() {
        setup:
        authenticator.begin()
        when:
        Customer customer = customerRepository.create()
        customer.setName("customer")


        customerRepository.save(customer)

        Map<String, Object> row = jdbcTemplate.queryForMap("select * from REPOSITORY_CUSTOMER where ID = '" + customer.getId() + "'")
        then:
        row != null
        row.get("CREATE_TS") != null
        row.get("CREATED_BY") != null

        cleanup:
        authenticator.end()
    }

    void testBasicCount() {
        when:
        long count = customerRepository.count()
        then:
        count == 3
    }


    void testFindAll() {
        when:
        List<Customer> customers = Lists.newArrayList(customerRepository.findAll())
        then:
        customers.size() == 3
        customers.contains(customer1)
        customers.contains(customer2)
        customers.contains(customer3)
    }

    void testFindAllById() {
        when:
        List<Customer> customers = Lists.newArrayList(customerRepository.findAllById(Arrays.asList(customer1.getId(), customer2.getId())))
        then:
        customers.size() == 2
        customers.contains(customer1)
        customers.contains(customer2)
    }

    void testCountCustomersByCity() {
        when:
        long first = customerRepository.countCustomersByAddressCity("Samara")
        then:
        first == 1

        when:
        long second = customerRepository.countCustomersByAddressCity("Springfield")
        then:
        second == 2

        when:
        long third = customerRepository.countCustomersByAddressCity("London")
        then:
        third == 0
    }


    void testFindCustomersByCityInList() {
        when:
        List<Customer> customers = customerRepository.findByAddressCityIn(Arrays.asList("Samara", "Springfield"))
        then:
        customers.contains(customer1)
        customers.contains(customer2)
        customers.contains(customer3)
        customers.size() == 3
    }


    void testSoftDeleteCustomer() {
        when:
        Customer customer = customerRepository.findById(customer3.getId()).get()
        customerRepository.delete(customer)

        Customer deleted = dataManager.load(Customer).id(customer.getId()).optional().orElse(null)

        then:
        deleted == null
    }

/*
    @Test//todo make separae test(s?) for softDelition when considering of it will be implemented
    public void testDeleteCustomer() throws SQLException {
        try (Transaction tx = persistence.getTransaction()) {
            persistence.setSoftDeletion(false)
            Customer customer = customerRepository.findOne(customer3.getId())
            customerRepository.delete(customer)
            tx.commit()
        }

        QueryRunner runner = new QueryRunner(persistence.getDataSource())
        Map<String, Object> row = runner.query("select * from REPOSITORY_CUSTOMER where ID = '" + customer3.getId() + "'",
                new MapHandler())
        assertNull(row)
    }
*/


    void testSoftDeleteCustomerById() {
        when:
        customerRepository.deleteById(customer3.getId())

        Customer deleted = dataManager.load(Customer).id(customer3.getId()).optional().orElse(null)

        then:
        deleted == null
    }


    void testFindCustomerById() {
        when:
        Customer customer = customerRepository.findById(customer1.getId()).get()
        then:
        customer == customer1
        entityStates.isDetached(customer)
    }


    void testFindCustomerByName() {
        when:
        List<Customer> customers = customerRepository.findByName(customer1.getName())
        then:
        customers.size() == 1
        customers.get(0) == customer1
    }


    void testFindCustomerByAddressCity() {
        when:
        List<Customer> customers = customerRepository.findByAddressCity(customer1.getAddress().getCity())
        then:
        customers.size() == 1
        customers.get(0) == customer1
    }

    void testQueryWithAdvancedLike() {
        when:
        List<Customer> customers = customerRepository.findByNameStartingWith("cust")
        then:
        customers.size() == 1
        customers.get(0) == customer1
    }

    void testQueryWithPositionalParam() {
        when:
        List<Customer> customers = customerRepository.findByQueryWithPositionParameter("cust")
        then:
        customers.size() == 1
        customers.get(0) == customer1
    }

    void testQueryWithInClause() {
        when:
        List<Customer> customers = customerRepository.findByNameIsIn(Arrays.asList(customer1.getName(), customer2.getName()))
        then:
        customers.size() == 2
        customers.contains(customer1)
        customers.contains(customer2)

        when:
        customers = customerRepository.findByNameIsIn(Collections.singletonList("Fake Name Should not be found"))
        then:
        customers.size() == 0
    }

    void testExistsQuery() {
        expect:
        customerRepository.existsByName("cust1")
        !customerRepository.existsByName("He-Who-Must-Not-Be-Named")

    }

    void "test parameters order"() {
        when:
        List<Customer> customers = customerRepository.findByQueryWithReversedPositionalParametersOrder("Samara", "cust1")
        then:
        customers.size() == 1
        customers[0] == customer1

        when:
        customers = customerRepository.findByQueryWithReversedNamedParametersOrder("Springfield", "some cust 2")
        then:
        customers.size() == 1
        customers[0] == customer2

    }
}
