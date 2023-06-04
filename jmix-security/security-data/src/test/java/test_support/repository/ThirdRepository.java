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

import io.jmix.core.FetchPlan;
import org.springframework.lang.Nullable;
import org.springframework.data.domain.Sort;
import test_support.entity.TestOrder;

import java.util.List;
import java.util.UUID;

public interface ThirdRepository extends SecondRepository {

    @Override
    Iterable<TestOrder> findAll(Sort sort, @Nullable FetchPlan fetchPlan);

    List<TestOrder> findByNumberNotNull();

    @Override
    List<TestOrder> findByIdNotNull();

    @Override
    List<TestOrder> searchByIdNotNull();

    @Override
    @io.jmix.core.repository.FetchPlan("_base")
    List<TestOrder> searchById(UUID id);
}
