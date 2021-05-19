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

package data_repositories

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import test_support.DataSpec
import test_support.entity.data_repositories.Employee
import test_support.entity.sales.Customer
import test_support.entity.sales.Status
import test_support.repository.EmployeeRepository
import test_support.repository.JCustomerRepository

class MiscDataRepositoriesTest extends DataSpec {
    @Autowired
    JCustomerRepository customerRepository

    @Autowired
    EmployeeRepository employeeRepository

    @Autowired
    Metadata metadata

    @Autowired
    DataManager dataManager

    void setup() {
        Customer customer = new Customer();
        customer.name = "first"
        customer.status = Status.OK
        customerRepository.save(customer)
        customer = new Customer();
        customer.name = "second"
        customer.status = Status.NOT_OK
        customerRepository.save(customer)
    }

    void cleanup() {
        customerRepository.deleteAll()
    }


    void "smoke test"() {
        expect:
        customerRepository != null
    }

    void "test get customers"() {
        expect:
        customerRepository.findAll().size() == 2
        customerRepository.findCustomersByName("first").size() == 1
        customerRepository.findCustomersByName("first")[0].status == Status.OK
        customerRepository.findCustomersByName("second").size() == 1
        customerRepository.findCustomersByName("second")[0].status == Status.NOT_OK
        customerRepository.findCustomersByName("third").size() == 0
    }

    void "test remove method"() {
        expect:
        customerRepository.findAll().size() == 2

        when:
        customerRepository.removeCustomerByName("first")
        then:
        customerRepository.findAll().size() == 1
        !customerRepository.existsCustomerByName("first")
        customerRepository.existsCustomerByName("second")

        when:
        customerRepository.removeCustomerByName("second")
        then:
        customerRepository.findAll().size() == 0
        !customerRepository.existsCustomerByName("first")
        !customerRepository.existsCustomerByName("second")
    }

    void "test parameter names do not clash"() {
        setup:
        Employee e1 = metadata.create(Employee);
        e1.name = "First"
        e1.homeAddress.city = "Moscow"
        e1.homeAddress.street = "Arbat"
        e1.workAddress.city = "Moscow"
        e1.workAddress.street = "Pokrovka"
        e1.registrationAddress.city = "Moscow"
        e1.registrationAddress.street = "Sharikopodshipnikovskaya"

        Employee e2 = metadata.create(Employee);
        e2.name = "Second"
        e2.homeAddress.city = "St. Petersburg"
        e2.homeAddress.street = "Nevskiy pr."
        e2.workAddress.city = "St. Petersburg"
        e2.workAddress.street = "Sadovaja"
        e2.registrationAddress.city = "St. Petersburg"
        e2.registrationAddress.street = "Rabochaya"

        Employee e3 = metadata.create(Employee);
        e3.name = "Third"
        e3.homeAddress.city = "Samara"
        e3.homeAddress.street = "Molodogvardeyskaya"
        e3.workAddress.city = "Samara"
        e3.workAddress.street = "Moscowskoye shosse"
        e3.registrationAddress.city = "Samara"
        e3.registrationAddress.street = "Samarskaya"


        dataManager.save(e1, e2, e3)

        when:
        List<Employee> employees = employeeRepository.findEmployeesByHomeAddressStreetOrWorkAddressStreetOrRegistrationAddressStreet("Arbat", "Sadovaja", "Samarskaya")

        then:
        employees.size() == 3

        cleanup:
        dataManager.remove(e1, e2, e3)
    }

    void 'check sort priority'() {
        setup:
        Employee e1 = metadata.create(Employee);
        e1.name = "1"
        e1.secondName = "1"
        e1.lastName = "2"

        Employee e2 = metadata.create(Employee);
        e2.name = "1"
        e2.secondName = "2"
        e2.lastName = "1"

        Employee e3 = metadata.create(Employee);
        e3.name = "2"
        e3.secondName = "1"
        e3.lastName = "2"

        Employee e4 = metadata.create(Employee);
        e4.name = "2"
        e4.secondName = "2"
        e4.lastName = "1"

        Employee e5 = metadata.create(Employee)
        e5.name = "3"
        e5.secondName = "1"
        e5.lastName = "2"

        Employee e6 = metadata.create(Employee);
        e6.name = "3"
        e6.secondName = "2"
        e6.lastName = "1"

        dataManager.save(e1, e2, e3, e4, e5, e6)

        when: "mixed sort 1"
        List<Employee> results = employeeRepository.findEmployeesByNameNotNullOrderByNameAsc(
                Sort.by("lastName"))
        then:
        results.size() == 6
        results[0] == e2
        results[1] == e1
        results[2] == e4
        results[3] == e3
        results[4] == e6
        results[5] == e5

        when: "mixed sort 2"
        results = employeeRepository.findEmployeesByNameNotNullOrderByNameAsc(
                Sort.by("secondName"))

        then:
        results.size() == 6
        results[0] == e1
        results[1] == e2
        results[2] == e3
        results[3] == e4
        results[4] == e5
        results[5] == e6

        when: "mixed sort by Pageable 1"
        Page<Employee> page1 = employeeRepository.findEmployeesByNameNotNullOrderByNameDesc(
                PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "secondName"))
        )

        then:
        page1.numberOfElements == 3
        page1.toList()[0] == e3
        page1.toList()[1] == e2
        page1.toList()[2] == e1

        when: "mixed sort by Pageable 2"
        Page<Employee> page0 = employeeRepository.findEmployeesByNameNotNullOrderByNameDesc(
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "lastName"))
        )

        then:
        page0.numberOfElements == 3
        page0.toList()[0] == e5
        page0.toList()[1] == e6
        page0.toList()[2] == e3

        cleanup:
        dataManager.remove(e1, e2, e3, e4, e5, e6)

    }

    void "check repeated parameters in query"() {
        setup:
        Employee e1 = metadata.create(Employee);
        e1.name = "R."
        e1.secondName = "B."
        e1.lastName = "F."

        Employee e2 = metadata.create(Employee);
        e2.name = "John"
        e2.secondName = "R."
        e2.lastName = "Smith"

        Employee e3 = metadata.create(Employee);
        e3.name = "Helen"
        e3.secondName = "N."
        e3.lastName = "Phillips"

        Employee e4 = metadata.create(Employee);
        e4.name = "Howard"
        e4.secondName = "Phillips"
        e4.lastName = "Lovecraft"

        dataManager.save(e1, e2, e3)

        when:
        List<Employee> employees = employeeRepository.findEmployeesByNames("R.", "Phillips")

        then:
        employees.size() == 3
        employees.contains(e1)
        employees.contains(e2)
        employees.contains(e3)

        cleanup:
        dataManager.remove(e1, e2, e3)

    }
}
