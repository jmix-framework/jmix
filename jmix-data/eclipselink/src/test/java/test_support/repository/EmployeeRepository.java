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

import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import test_support.entity.repository.Employee;

import java.util.*;
import java.util.stream.Stream;

public interface EmployeeRepository extends JmixDataRepository<Employee, UUID> {
    List<Employee> findEmployeesByHomeAddressStreetOrWorkAddressStreetOrRegistrationAddressStreet(String homeStreet, String workStreet, String registrationStreet);

    Page<Employee> findEmployeesByNameNotNullOrderByNameDesc(Pageable pageable);

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
}
