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

import io.jmix.core.DevelopmentException
import io.jmix.core.querycondition.JpqlCondition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.PersistenceHints
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

import java.text.SimpleDateFormat
import java.util.stream.Collectors

import static io.jmix.core.repository.JmixDataRepositoryContext.of

class ScalarQueriesTest extends DataSpec {
    @Autowired
    CustomerRepository customerRepository

    @Autowired
    EmployeeRepository employeeRepository

    private Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse("2005-02-03")
    private Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse("1995-01-02")
    private Date date3 = new SimpleDateFormat("yyyy-MM-dd").parse("1985-05-07")


    void setup() {
        Customer customer = customerRepository.create()
        customer.name = "first"
        customer.address = new Address()
        customer.address.street = "Shadows"
        customer.address.city = "Ant-Meerin"
        customer.grade = CustomerGrade.BRONZE
        customerRepository.save(customer)

        customer = customerRepository.create()
        customer.name = "second"
        customer.address = new Address()
        customer.address.street = "undefined"
        customer.address.city = "Ubarweld"
        customer.grade = CustomerGrade.GOLD
        customerRepository.save(customer)

        Customer customerToRemove = customerRepository.create()
        customerToRemove.name = "third"
        customerToRemove.address = new Address()
        customerToRemove.address.street = "3rd"
        customerToRemove.address.city = "City17"
        customerToRemove.grade = CustomerGrade.PLATINUM
        customerRepository.save(customerToRemove)
        customerRepository.deleteById(customerToRemove.id)

        Employee e1 = employeeRepository.create()
        e1.name = "First"
        e1.secondName = "SN2"
        e1.homeAddress.city = "City2"
        e1.homeAddress.street = "Street1"
        e1.workAddress.city = "City2"
        e1.workAddress.street = "Street2"
        e1.registrationAddress.city = "City2"
        e1.registrationAddress.street = "Street3"
        e1.age = 20
        e1.workingHours = 40
        e1.birthDate = date1


        Employee e2 = employeeRepository.create()
        e2.name = "Second"
        e2.secondName = "SN1"
        e2.homeAddress.city = "City1"
        e2.homeAddress.street = "Street4"
        e2.workAddress.city = "City1"
        e2.workAddress.street = "Street5"
        e2.registrationAddress.city = "City1"
        e2.registrationAddress.street = "Street6"
        e2.age = null
        e2.workingHours = 30
        e2.birthDate = date2

        Employee e3 = employeeRepository.create()
        e3.name = "Third"
        e3.secondName = "SN3"
        e3.homeAddress.city = "City1"
        e3.homeAddress.street = "Street7"
        e3.workAddress.city = "City1"
        e3.workAddress.street = "Street8"
        e3.registrationAddress.city = "City1"
        e3.registrationAddress.street = "Street9"
        e3.age = 40
        e3.workingHours = 40
        e3.birthDate = date3


        employeeRepository.saveAll([e1, e2, e3])
    }

    void "check conditions"() {
        when:
        def filteredByEntityProperty = employeeRepository.queryEmployeeSecondNamesByContext(
                of(PropertyCondition.createWithValue("e.name", PropertyCondition.Operation.CONTAINS, "ir")))

        def filteredByReturnColumn = employeeRepository.queryEmployeeSecondNamesByContext(
                of(PropertyCondition.createWithValue("secondNameReturnColumn",
                        PropertyCondition.Operation.CONTAINS, "1"))
        )

        def filteredByComplexCondition = employeeRepository.queryEmployeeSecondNamesByContext(
                of(LogicalCondition.and(
                        JpqlCondition.createWithParameters("e.registrationAddress.city=:city", null, Map.of("city", "City1")),
                        PropertyCondition.createWithValue("e.name", PropertyCondition.Operation.CONTAINS, "ir"))
                )
        )

        then:
        filteredByEntityProperty == ["SN2", "SN3"]
        filteredByReturnColumn == ["SN1"]
        filteredByComplexCondition == ["SN3"]
    }

    void "check query hints"() {
        when:
        def grades = customerRepository.getAllGrades()
        def gradesIncludingDeleted = customerRepository.queryGradesByContext()

        def gradesByContextNoSoftDeletion = customerRepository.queryGradesByContext(
                of(Map.of(PersistenceHints.SOFT_DELETION, false)))

        def gradesByContextWithSoftDeletion = customerRepository.queryGradesByContext(
                of(Map.of(PersistenceHints.SOFT_DELETION, true)))


        then:
        grades.size() == 2
        gradesIncludingDeleted == [CustomerGrade.PLATINUM, CustomerGrade.GOLD, CustomerGrade.BRONZE]
        gradesByContextNoSoftDeletion == [CustomerGrade.PLATINUM, CustomerGrade.GOLD, CustomerGrade.BRONZE]
        gradesByContextWithSoftDeletion == [CustomerGrade.GOLD, CustomerGrade.BRONZE]
    }


    void "check scalar query return types for multiple values"() {
        when:
        def page0 = employeeRepository.queryEmployeeAgesPageOrderByNameDesc(PageRequest.of(0, 2))
        def page1 = employeeRepository.queryEmployeeAgesPageOrderByNameDesc(PageRequest.of(1, 2))

        def sliceBySecondNameAsc = employeeRepository.queryEmployeeAges(PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "secondNameForSort")))

        def listBySecondNameAscUsingSortParam = employeeRepository.queryEmployeeAgesWithSortParam(
                Sort.by(Sort.Direction.ASC, "secondNameForSort"))

        def listRes = employeeRepository.queryEmployeeAgesListOrderByNameDesc()

        def linkedHashSetRes = employeeRepository.queryEmployeeAgesLHSOrderByNameDesc()

        def iterableResult = employeeRepository.queryEmployeeAgesIterableOrderByNameDesc()

        def streamResult = employeeRepository.queryEmployeeAgesStreamSortByAgeNotNull()

        def allGrades = customerRepository.getAllGrades()

        def kveStreamResult = employeeRepository.queryKeyValueEntitiesStream()


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

        listBySecondNameAscUsingSortParam.size() == 3
        listBySecondNameAscUsingSortParam.get(0).getValue("age") == null
        listBySecondNameAscUsingSortParam.get(0).getValue("secondNameForSort") == "SN1"
        listBySecondNameAscUsingSortParam.get(1).getValue("age") == 20
        listBySecondNameAscUsingSortParam.get(1).getValue("secondNameForSort") == "SN2"

        listRes == [40, null, 20]

        linkedHashSetRes.size() == 3
        linkedHashSetRes =~ [40, null, 20]

        iterableResult == [40, null, 20]

        streamResult.collect(Collectors.toList()) == [date1, date3]

        allGrades == [CustomerGrade.BRONZE, CustomerGrade.GOLD]

        kveStreamResult
                .map { it -> it.getValue("secondName") + " " + it.getValue("name") }
                .collect(Collectors.toList()) == ["SN1 Second", "SN2 First", "SN3 Third"]
    }

    void "aggregate queries test"() {
        when:
        def personHoursByCities = employeeRepository.personHoursByCitiesOrderByHoursDesc()

        then:
        personHoursByCities.size() == 2
        personHoursByCities[0].getValue("city") == "City1"
        personHoursByCities[0].getValue("personHours") == 70

        personHoursByCities[1].getValue("city") == "City2"
        personHoursByCities[1].getValue("personHours") == 40
    }

    void "check KeyValueEntity collections"() {
        when:
        def kveList = employeeRepository.queryEmployeeValuesList()
        def kveSet = employeeRepository.queryEmployeeValuesSet()

        then:
        kveList[0].getValue("age") == 20
        kveList[0].getValue("name") == "First"
        kveList[0].getValue("secondName") == "SN2"

        kveList[1].getValue("age") == null
        kveList[1].getValue("name") == "Second"
        kveList[1].getValue("secondName") == "SN1"

        kveList[2].getValue("age") == 40
        kveList[2].getValue("name") == "Third"
        kveList[2].getValue("secondName") == "SN3"


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

        def resultForFirst = employeeRepository.queryEmployeeSecondNameByFirstName("First")
        def resultForNotExisted = employeeRepository.queryEmployeeSecondNameByFirstName("NotExisted")
        def bigDecimalAgeForFirstEmployee = employeeRepository.queryEmployeeAgeBigDecimalByName("First")
        def bigDecimalAgeForNotExisted = employeeRepository.queryEmployeeAgeBigDecimalByName("NotExisted")
        def kVENamedValuesForFirstEmployee = employeeRepository.queryEmployeeValuesOptionalNamed("First")
        def kVEAgeForNotExisted = employeeRepository.queryEmployeeValuesOptionalNamed("NotExisted")


        then:
        resultForFirst.isPresent()
        resultForFirst.get() == "SN2"
        resultForNotExisted.isEmpty()

        bigDecimalAgeForFirstEmployee.isPresent()
        bigDecimalAgeForFirstEmployee.get() instanceof BigDecimal
        bigDecimalAgeForNotExisted.isEmpty()

        kVENamedValuesForFirstEmployee.isPresent()
        kVENamedValuesForFirstEmployee.get().getValue("age") == 20
        kVENamedValuesForFirstEmployee.get().getValue("name") == "First"
        kVENamedValuesForFirstEmployee.get().getValue("secondName") == "SN2"

        kVEAgeForNotExisted.isEmpty()
    }

    void 'raw return type considered as entity query'() {
        when:
        def res = employeeRepository.incorrectReturnTypeQuery()

        then:
        thrown(DevelopmentException)
    }

    void cleanup() {
        jdbc.update("delete from REPOSITORY_EMPLOYEE")
        jdbc.update("delete from REPOSITORY_CUSTOMER")
    }
}
