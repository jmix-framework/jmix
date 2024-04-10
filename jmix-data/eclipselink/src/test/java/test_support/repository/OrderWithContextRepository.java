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

package test_support.repository;

import io.jmix.core.repository.*;
import io.jmix.data.PersistenceHints;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import test_support.entity.repository.SalesOrder;

import java.util.List;
import java.util.UUID;


public interface OrderWithContextRepository extends JmixDataRepository<SalesOrder, UUID> {

    @FetchPlan("SalesOrder.full")
    List<SalesOrder> findByCountGreaterThan(int minCount,
                                            JmixDataRepositoryContext context,
                                            io.jmix.core.FetchPlan fetchPlan);


    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    List<SalesOrder> findByNumberLike(String numberPattern, JmixDataRepositoryContext context);

    List<SalesOrder> findTop3ByCountGreaterThanOrderByCount(int minCount);

    @Query("select o from repository$SalesOrder o where o.count >= ?1 order by o.number desc")
    List<SalesOrder> findByQueryAndJDRC(int count, JmixDataRepositoryContext context);


    @FetchPlan("SalesOrder.full")
    @Query("select o from repository$SalesOrder o where o.count >= ?1")
    List<SalesOrder> loadByCustomQuery(int minCount,
                                       JmixDataRepositoryContext context,
                                       io.jmix.core.FetchPlan fetchPlan);

    @QueryHints({@QueryHint(name = PersistenceHints.SOFT_DELETION, value = "false")})
    @Query("select o from repository$SalesOrder o where o.number like concat('%',?1)")
    List<SalesOrder> loadByQueryWithHints(String numberPattern, JmixDataRepositoryContext context);

    @Query("select r from repository$SalesOrder r where upper(r.number) like upper(:number)")
    Page<SalesOrder> pageByNumberLikeIgnoreCase(JmixDataRepositoryContext context,
                                                @Param("number") String number,
                                                Pageable pageable);

    Page<SalesOrder> findByNumberLikeIgnoreCase(String number,
                                                JmixDataRepositoryContext context,
                                                Pageable pageable);

    void removeByNumberNotNull(JmixDataRepositoryContext context);
}
