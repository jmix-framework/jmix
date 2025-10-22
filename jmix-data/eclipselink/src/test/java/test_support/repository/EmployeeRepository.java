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

package test_support.repository;

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.JmixDataRepositoryContext;
import io.jmix.core.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import test_support.entity.repository.Employee;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

public interface EmployeeRepository extends JmixDataRepository<Employee, UUID> {
    List<Employee> findEmployeesByHomeAddressStreetOrWorkAddressStreetOrRegistrationAddressStreet(String homeStreet, String workStreet, String registrationStreet);

    Page<Employee> findEmployeesByNameNotNullOrderByNameDesc(Pageable pageable);

    @Query("select e from repository$Employee e where(e.name is not null) order by e.name desc")
    Page<Employee> queryEmployeesByNameNotNullOrderByNameDesc(Pageable pageable);

    List<Employee> findEmployeesByNameNotNullOrderByNameAsc(Sort sort);

    @Query("select e from repository$Employee e where(e.name like ?1 or e.secondName like ?1 or e.lastName like ?2)")
    List<Employee> findEmployeesByNames(String nameOrSecondName, String thirdName);


    //different return types

    Optional<Employee> findTopByOrderByNameDesc();

    Set<Employee> findFirstByOrderByNameDesc();

    Stream<Employee> findTop1ByOrderByNameDesc();

    Iterator<Employee> findFirst1ByOrderByNameDesc();

    Employee getByNameContains(String namePart);

    Optional<Employee> findByNameContains(String namePart);


    // Scalar queries
    @Query("select e.age from repository$Employee e order by e.name desc")
    Page<Integer> queryEmployeeAgesPageOrderByNameDesc(Pageable pageable);

    @Query("select e.age from repository$Employee e order by e.name desc")
    List<Integer> queryEmployeeAgesListOrderByNameDesc();

    @Query("select e.age from repository$Employee e order by e.name desc")
    LinkedHashSet<Integer> queryEmployeeAgesLHSOrderByNameDesc();

    @Query("select e.age from repository$Employee e order by e.name desc")
    Iterable<Integer> queryEmployeeAgesIterableOrderByNameDesc();

    @Query("select e.birthDate from repository$Employee e where(e.age is not null)  order by e.age")
    Stream<Date> queryEmployeeAgesStreamSortByAgeNotNull();


    @Query("select e.age, e.name from repository$Employee e order by e.name desc")
    List incorrectReturnTypeQuery();


    @Query(value = "select e.age, e.secondName from repository$Employee e", properties = {"age", "secondNameForSort"})
    Slice<KeyValueEntity> queryEmployeeAges(Pageable pageable);

    @Query(value = "select e.age, e.secondName from repository$Employee e", properties = {"age", "secondNameForSort"})
    List<KeyValueEntity> queryEmployeeAgesWithSortParam(Sort sort);

    @Query(value = "select e.secondName from repository$Employee e", properties = "secondNameReturnColumn")
    List<String> queryEmployeeSecondNamesByContext(JmixDataRepositoryContext context);

    @Query("select e.secondName from repository$Employee e where e.name = :name")
    Optional<String> queryEmployeeSecondNameByFirstName(@Param("name") String name);

    @Query("select e.age from repository$Employee e where e.name = :name")
    Optional<BigDecimal> queryEmployeeAgeBigDecimalByName(@Param("name") String name);


    @Query(value = "select e.age, e.name, e.secondName from repository$Employee e where e.name = :name",
            properties = {"age", "name", "secondName"})
    Optional<KeyValueEntity> queryEmployeeValuesOptionalNamed(@Param("name") String name);

    @Query(value = "select e.age, e.name, e.secondName from repository$Employee e order by e.name",
            properties = {"age", "name", "secondName"})
    List<KeyValueEntity> queryEmployeeValuesList();

    @Query(value = "select e.age, e.name, e.secondName from repository$Employee e order by e.name",
            properties = {"firstColumn", "secondColumn", "thirdColumn"})
    LinkedHashSet<KeyValueEntity> queryEmployeeValuesSet();


    @Query(value = "select e.name, e.secondName from repository$Employee e order by e.secondName",
            properties = {"name", "secondName"})
    Stream<KeyValueEntity> queryKeyValueEntitiesStream();

    @Query(value = "select e.workAddress.city, sum(e.workingHours) from repository$Employee e group by e.workAddress.city " +
            "order by sum(e.workingHours) desc",
            properties = {"city", "personHours"})
    List<KeyValueEntity> personHoursByCitiesOrderByHoursDesc();
}
