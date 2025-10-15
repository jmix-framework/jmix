/*
 * Copyright 2025 Haulmont.
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

import io.jmix.core.entity.KeyValueEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import test_support.DataSpec
import test_support.entity.repository.Address
import test_support.entity.repository.Customer
import test_support.entity.repository.CustomerGrade
import test_support.entity.repository.Employee
import test_support.repository.CustomerRepository
import test_support.repository.EmployeeRepository

import java.util.stream.Collectors

import static io.jmix.core.impl.repository.query.utils.JmixQueryLookupStrategy.PROPERTY_PREFIX

class ScalarQueriesTest extends DataSpec {
    @Autowired
    CustomerRepository customerRepository

    @Autowired
    EmployeeRepository employeeRepository


    void setup() {
        Customer customer = customerRepository.create()
        customer.name = "first"
        customer.address = new Address()
        customer.address.street = "Shadows"
        customer.address.city = "Ant-Meerin"
        customer.grade = CustomerGrade.BRONZE
        customerRepository.save(customer)

        customer = new Customer();
        customer.name = "second"
        customer.address = new Address()
        customer.address.street = "undefined"
        customer.address.city = "Ubarweld"
        customer.grade = CustomerGrade.GOLD
        customerRepository.save(customer)

        Employee e1 = employeeRepository.create();
        e1.name = "First"
        e1.secondName = "SN2"
        e1.homeAddress.city = "City2"
        e1.homeAddress.street = "Street1"
        e1.workAddress.city = "City2"
        e1.workAddress.street = "Street2"
        e1.registrationAddress.city = "City2"
        e1.registrationAddress.street = "Street3"
        e1.age = 20

        Employee e2 = employeeRepository.create();
        e2.name = "Second"
        e2.secondName = "SN1"
        e2.homeAddress.city = "City1"
        e2.homeAddress.street = "Street4"
        e2.workAddress.city = "City1"
        e2.workAddress.street = "Street5"
        e2.registrationAddress.city = "City1"
        e2.registrationAddress.street = "Street6"
        e2.age = null

        Employee e3 = employeeRepository.create();
        e3.name = "Third"
        e3.secondName = "SN3"
        e3.homeAddress.city = "City3"
        e3.homeAddress.street = "Street7"
        e3.workAddress.city = "City3"
        e3.workAddress.street = "Street8"
        e3.registrationAddress.city = "City3"
        e3.registrationAddress.street = "Street9"
        e3.age = 40


        employeeRepository.saveAll([e1, e2, e3])
    }


    void "check scalar query return types for multiple values"() {
        when:
        def page0 = employeeRepository.queryEmployeeAgesPageByNameNotNullOrderByNameDesc(PageRequest.of(0, 2))
        def page1 = employeeRepository.queryEmployeeAgesPageByNameNotNullOrderByNameDesc(PageRequest.of(1, 2))

        def rawPage0 = employeeRepository.queryEmployeeAgesRawPageByNameNotNullOrderByNameDesc(PageRequest.of(0, 1))
        def rawPage1 = employeeRepository.queryEmployeeAgesRawPageByNameNotNullOrderByNameDesc(PageRequest.of(1, 1))
        def rawPage2 = employeeRepository.queryEmployeeAgesRawPageByNameNotNullOrderByNameDesc(PageRequest.of(2, 1))

        def sliceBySecondNameAsc = employeeRepository.queryEmployeeAges(PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "secondNameForSort")))

        def listRes = employeeRepository.queryEmployeeAgesListByNameNotNullOrderByNameDesc()
        def rawListRes = employeeRepository.queryEmployeeAgesRawListByNameNotNullOrderByNameDesc()

        def linkedHashSetRes = employeeRepository.queryEmployeeAgesLHSByNameNotNullOrderByNameDesc()
        def rawLinkedHashSetRes = employeeRepository.queryEmployeeAgesRawLHSByNameNotNullOrderByNameDesc()

        def iterableResult = employeeRepository.queryEmployeeAgesIterableByNameNotNullOrderByNameDesc()

        def streamResult = employeeRepository.queryEmployeeAgesStreamByNameNotNullOrderByNameDesc()

        def allGrades = customerRepository.getAllGrades()

        then:
        page0.size == 2
        page0.numberOfElements == 2
        page0[0] == 40
        page0[1] == null

        page1.size == 2
        page1.numberOfElements == 1
        page1[0] == 20

        sliceBySecondNameAsc.hasNext()
        sliceBySecondNameAsc.numberOfElements == 2
        sliceBySecondNameAsc.getContent().get(0).getValue("age") == null
        sliceBySecondNameAsc.getContent().get(0).getValue("secondNameForSort") == "SN1"
        sliceBySecondNameAsc.getContent().get(1).getValue("age") == 20
        sliceBySecondNameAsc.getContent().get(1).getValue("secondNameForSort") == "SN2"

        listRes == [40, null, 20]

        rawListRes.size() == 3
        rawListRes[0] instanceof KeyValueEntity
        ((KeyValueEntity) rawListRes[0]).getValue(PROPERTY_PREFIX + 0) == 40

        linkedHashSetRes.size() == 3
        linkedHashSetRes =~ [40, null, 20]

        rawLinkedHashSetRes.size() == 3
        rawLinkedHashSetRes[0] instanceof KeyValueEntity
        [40, null, 20].contains(((KeyValueEntity) rawLinkedHashSetRes[0]).getValue(PROPERTY_PREFIX + 0))


        rawPage0.size == 1
        rawPage0.numberOfElements == 1
        rawPage0.totalPages == 3
        rawPage0.totalElements == 3
        ((KeyValueEntity) rawPage0[0]).getValue(PROPERTY_PREFIX + 0) == 40
        ((KeyValueEntity) rawPage1[0]).getValue(PROPERTY_PREFIX + 0) == null
        ((KeyValueEntity) rawPage2[0]).getValue(PROPERTY_PREFIX + 0) == 20

        iterableResult == [40, null, 20]

        streamResult.collect(Collectors.toList()) == [40, null, 20]

        allGrades == [CustomerGrade.BRONZE, CustomerGrade.GOLD]
    }

    void "check KeyValueEntity collections"() {
        when:
        def kveList = employeeRepository.queryEmployeeValuesList()
        def kveSet = employeeRepository.queryEmployeeValuesSet()

        then:
        kveList[0].getValue(PROPERTY_PREFIX + 0) == 20
        kveList[0].getValue(PROPERTY_PREFIX + 1) == "First"
        kveList[0].getValue(PROPERTY_PREFIX + 2) == "SN2"

        kveList[1].getValue(PROPERTY_PREFIX + 0) == null
        kveList[1].getValue(PROPERTY_PREFIX + 1) == "Second"
        kveList[1].getValue(PROPERTY_PREFIX + 2) == "SN1"

        kveList[2].getValue(PROPERTY_PREFIX + 0) == 40
        kveList[2].getValue(PROPERTY_PREFIX + 1) == "Third"
        kveList[2].getValue(PROPERTY_PREFIX + 2) == "SN3"


        kveSet[0].getValue("firstColumn") == 20
        kveSet[0].getValue("secondColumn") == "First"
        kveSet[0].getValue("thirdColumn") == "SN2"

        kveSet[1].getValue("firstColumn") == null
        kveSet[1].getValue("secondColumn") == "Second"
        kveSet[1].getValue("thirdColumn") == "SN1"

        kveSet[2].getValue("firstColumn") == 40
        kveSet[2].getValue("secondColumn") == "Third"
        kveSet[2].getValue("thirdColumn") == "SN3"
    }


    void "check scalar query return types for single value"() {
        when:
        def rawKVEResult = customerRepository.countAllByScalarQueryAndReturnKeyValueEntity();
        def longResult = customerRepository.countAllByScalarQueryAndReturnLong();
        List<Long> longListResult = customerRepository.countAllByScalarQueryAndReturnLongList()
        Collection<Long> collectionResult = customerRepository.countAllByScalarQueryAndReturnCollection()
        LinkedHashSet<Long> linkedHashSetResult = customerRepository.countAllByScalarQueryAndReturnLinkedHashSet()
        Set<Long> setResult = customerRepository.countAllByScalarQueryAndReturnLongSet()
        def kveWithSeveralProperties = customerRepository.countAllByScalarQueryWithComplexResult()
        def countWithParam = customerRepository.countAllByScalarQueryWithParams("first")

        then:
        noExceptionThrown()
        rawKVEResult[0].getValue("count") == 2
        longResult == 2
        longListResult[0] == 2
        collectionResult[0] == 2
        linkedHashSetResult[0] == 2
        setResult.iterator().next() == 2

        kveWithSeveralProperties.getValue("count") == 2
        kveWithSeveralProperties.getValue("constant") == 12

        countWithParam == 1
    }

    void "check optional return type with JmixDataRepositoryQueryContext"() {
        when:
        //todo taimanov: fix Conditions for ValueLoadQuery in DataManager
//        def resultForFirstWirhCondition = employeeRepository.queryEmployeeSecondNameByContext(
//                of(createWithValue("name", EQUAL, "First")))

        def resultForFirst = employeeRepository.queryEmployeeSecondNameByFirstName("First")
        def resultForNotExisted = employeeRepository.queryEmployeeSecondNameByFirstName("NotExisted")
        def bigDecimalAgeForFirstEmployee = employeeRepository.queryEmployeeAgeBigDecimalByName("First")
        def bigDecimalAgeForNotExisted = employeeRepository.queryEmployeeAgeBigDecimalByName("NotExisted")
        def kVEValuesForFirstEmployee = employeeRepository.queryEmployeeValuesOptional("First")
        def kVENamedValuesForFirstEmployee = employeeRepository.queryEmployeeValuesOptionalNamed("First")
        def kVEAgeForNotExisted = employeeRepository.queryEmployeeValuesOptional("NotExisted")


        then:
        resultForFirst.isPresent()
        resultForFirst.get() == "SN2"
        resultForNotExisted.isEmpty()

        bigDecimalAgeForFirstEmployee.isPresent()
        bigDecimalAgeForFirstEmployee.get() instanceof BigDecimal
        bigDecimalAgeForNotExisted.isEmpty()

        kVEValuesForFirstEmployee.isPresent()
        kVEValuesForFirstEmployee.get().getValue(PROPERTY_PREFIX + 0) == 20
        kVEValuesForFirstEmployee.get().getValue(PROPERTY_PREFIX + 1) == "First"
        kVEValuesForFirstEmployee.get().getValue(PROPERTY_PREFIX + 2) == "SN2"

        kVENamedValuesForFirstEmployee.isPresent()
        kVENamedValuesForFirstEmployee.get().getValue("age") == 20
        kVENamedValuesForFirstEmployee.get().getValue("name") == "First"
        kVENamedValuesForFirstEmployee.get().getValue("secondName") == "SN2"

        kVEAgeForNotExisted.isEmpty()

    }

    void cleanup() {
        customerRepository.deleteAll()
        employeeRepository.deleteAll()
    }
}
