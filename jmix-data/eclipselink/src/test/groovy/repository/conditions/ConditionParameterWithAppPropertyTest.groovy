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

package repository.conditions

import io.jmix.core.CoreProperties
import io.jmix.core.DataManager
import io.jmix.core.querycondition.JpqlCondition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.test.context.TestPropertySource
import test_support.DataSpec
import test_support.entity.repository.Customer
import test_support.repository.CustomerRepository

import static io.jmix.core.querycondition.PropertyCondition.Operation.EQUAL
import static io.jmix.core.querycondition.PropertyCondition.Operation.EQUAL
import static io.jmix.core.repository.JmixDataRepositoryContext.of
import static io.jmix.core.repository.JmixDataRepositoryContext.of
import static io.jmix.core.repository.JmixDataRepositoryContext.of
import static io.jmix.core.repository.JmixDataRepositoryContext.of

@TestPropertySource(properties = ["jmix.core.skip-null-or-empty-conditions-by-default=true"])
class ConditionParameterWithAppPropertyTest extends DataSpec {

    @Autowired
    protected CustomerRepository customerRepository
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected DataManager dataManager;

    void setup() {
        Customer fullNamedCustomer = customerRepository.create();
        fullNamedCustomer.name = "fullNamedCustomer"
        fullNamedCustomer.note = "one"
        customerRepository.save(fullNamedCustomer)

        Customer emptyNamedCustomer = customerRepository.create();
        emptyNamedCustomer.name = ""
        emptyNamedCustomer.note = "two"
        customerRepository.save(emptyNamedCustomer)

        Customer nullNamedCustomer = customerRepository.create();
        nullNamedCustomer.note = "one"
        customerRepository.save(nullNamedCustomer)
    }

    void cleanup() {
        customerRepository.deleteAll()
    }

    void "Simple conditions always skipping if skip-null-or-empty-conditions-by-default is true"() {
        expect:
        List<Customer> customers = customerRepository.findAll(Sort.by("name")).toList()
        customers.size() == 3
        customers[0].name == null
        customers[1].name == ""
        customers[2].name == "fullNamedCustomer"

        when:
        def result1 = customerRepository.findAll(of(PropertyCondition
                .createWithValue("name", EQUAL, null)))
        then:
        result1.size() == 3

        when:
        def result2 = customerRepository.findAll(of(PropertyCondition
                .createWithValue("name", EQUAL, null)
                .skipNullOrEmpty()))
        then:
        result2.size() == 3

        when:
        def result3 = customerRepository.findAll(of(JpqlCondition
                .createWithParameters('e.name=:name', null, Collections.singletonMap('name', null))))
        then:
        result3.size() == 3

        when:
        def result4 = customerRepository.findAll(of(JpqlCondition
                .createWithParameters('e.name=:name', null, Collections.singletonMap('name', null))
                .skipNullOrEmpty()))
        then:
        result4.size() == 3
    }

    void "Complex conditions always skipping if skip-null-or-empty-conditions-by-default is true"() {
        when:
        def result1 = customerRepository.findAll(of(
                LogicalCondition.and(
                        PropertyCondition.createWithValue("name", EQUAL, null),
                        PropertyCondition.createWithValue("note", EQUAL, "one"))
        ))

        then:
        result1.size() == 2

        when:
        def result2 = customerRepository.findAll(of(LogicalCondition.and(
                PropertyCondition.createWithValue("name", EQUAL, null).skipNullOrEmpty(),
                PropertyCondition.createWithValue("note", EQUAL, "one"))
        ))
        then:
        result2.size() == 2

        when:
        coreProperties.skipNullOrEmptyConditionsByDefault
        def result3 = customerRepository.findAll(of(
                LogicalCondition.and(
                        JpqlCondition.createWithParameters('e.name=:name', null, Collections.singletonMap('name', null)),
                        PropertyCondition.createWithValue("note", EQUAL, "one"))))

        then:
        result3.size() == 2

        when:
        coreProperties.skipNullOrEmptyConditionsByDefault
        def result4 = customerRepository.findAll(of(LogicalCondition.and(
                JpqlCondition.createWithParameters('e.name=:name', null, Collections.singletonMap('name', null))
                        .skipNullOrEmpty(),
                PropertyCondition.createWithValue("note", EQUAL, "one"))))
        then:
        result4.size() == 2
    }
}
