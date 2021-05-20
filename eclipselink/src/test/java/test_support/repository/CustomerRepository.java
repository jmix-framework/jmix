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


import io.jmix.core.repository.FetchPlan;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import org.springframework.data.repository.query.Param;
import test_support.entity.repository.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JmixDataRepository<Customer, UUID> {
    @FetchPlan("repository_Customer.full")
    List<Customer> findByName(String name);

    List<Customer> findByAddressCity(String city);

    List<Customer> findByNameIsIn(List<String> names);

    long countCustomersByAddressCity(String city);

    List<Customer> findByAddressCityIn(List<String> cities);

    boolean existsByName(String name);

    void removeByName(String name);

    @FetchPlan("_instance_name")
    @Query("select c from repository$Customer c where c.name like concat(:name, '%')")
    List<Customer> findByNameStartingWith(@Param("name") String name);

    @Query("select c from repository$Customer c where c.name like concat(?1, '%')")
    List<Customer> findByQueryWithPositionParameter(String name);

    @Query("select c from repository$Customer c where c.name like ?2 and c.address.city like ?1")
    List<Customer> findByQueryWithReversedPositionalParametersOrder(String city, String name);

    @Query("select c from repository$Customer c where c.name like :name and c.address.city like :city")
    List<Customer> findByQueryWithReversedNamedParametersOrder(@Param("city") String city, @Param("name") String name);
}
