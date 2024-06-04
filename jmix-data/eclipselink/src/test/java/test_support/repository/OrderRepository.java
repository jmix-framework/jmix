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


import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.repository.FetchPlan;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import test_support.entity.repository.Customer;
import test_support.entity.repository.SalesOrder;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JmixDataRepository<SalesOrder, UUID> {

    @FetchPlan("_instance_name")
    List<SalesOrder> findByCustomer(Customer customer);

    List<SalesOrder> findByCustomerNameAndCustomerAddressCity(String name, String city);

    long countSalesOrdersByCustomer(Customer customer);

    long countSalesOrdersByCustomerAddressCity(String city);

    boolean existsSalesOrdersByCustomerAddressCity(String city);

    void removeSalesOrdersByCustomerAddressCity(String city);

    List<SalesOrder> findSalesOrderByDateAfter(Date date);

    List<SalesOrder> findSalesOrderByDateBefore(Date date);

    List<SalesOrder> findSalesOrderByDateBeforeOrderByDateAsc(Date date);

    List<SalesOrder> findSalesOrderByDateBeforeOrderByDateDesc(Date date);

    List<SalesOrder> findSalesByCustomerNotNull(Sort sort);

    List<SalesOrder> findSalesByDateAfterAndNumberIn(Date date, Sort sort, List<String> numbers);

    Page<SalesOrder> findSalesByDateAfterAndNumberIn(Date date, Pageable pageable, List<String> numbers);

    Page<SalesOrder> findByDateAfterAndNumberIn(Date date, Pageable pageable, io.jmix.core.FetchPlan plan, List<String> numbers);

    Slice<SalesOrder> findSalesByCustomerNameIn(List<String> customerNames, Pageable pageable);

    @Query("select o from repository$SalesOrder o where (o.date> ?1 and o.number in ?2)")
    List<SalesOrder> findSalesByQuery(Date date, Sort sort, List<String> numbers);

    @Query("select o from repository$SalesOrder o where (o.date> :date and o.number in :numbers)")
    Page<SalesOrder> findSalesByQueryWithPaging(@Param("date") Date date,
                                                Pageable pageable,
                                                @Param("numbers") List<String> numbers);

    @Query("select o from repository$SalesOrder o where (o.date> ?2 and o.number in ?1)")
    Page<SalesOrder> findSalesByQueryWithPagingAndPositionalParameters(List<String> names,
                                                                       Pageable pageable,
                                                                       Date date);


    long countByNumberInOrDateIsNull(List<String> numbers);

    @FetchPlan("SalesOrder.full")
    List<SalesOrder> findByCustomerNotNullOrderByCustomerAddressCityAscDateAsc();

    @FetchPlan("SalesOrder.full")
    List<SalesOrder> findByCustomerNotNullOrderByCustomerAddressCityDescDateDesc();

    default SalesOrder getByExtractedNumber(String searchSource){
        String conditionString  = searchSource.replaceAll("[A-Za-z ]","");
        return getDataManager().load(SalesOrder.class)
                .condition(PropertyCondition.equal("number",conditionString))
                .one();
    }

}
