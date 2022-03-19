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
import io.jmix.core.repository.ApplyConstraints;
import io.jmix.core.repository.JmixDataRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import test_support.entity.TestOrder;

import java.util.List;
import java.util.UUID;

public interface FirstRepository extends JmixDataRepository<TestOrder, UUID> {

    @Override
    Iterable<TestOrder> findAll(Sort sort, @Nullable FetchPlan fetchPlan);

    @Override
    @ApplyConstraints(false)
    Iterable<TestOrder> findAll(FetchPlan fetchPlan);

    @ApplyConstraints(false)
    List<TestOrder> findByIdNotNull();

    List<TestOrder> searchByNumberNotNull();

    @io.jmix.core.repository.FetchPlan("_instance_name")
    List<TestOrder> searchById(UUID id);
}
