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

package io.jmix.data.repositories.query.utils;

import io.jmix.core.FluentLoader;
import io.jmix.core.Sort;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class LoaderHelper {
    public static <T> FluentLoader.ByCondition<T> applyPageableForConditionQuery(FluentLoader.ByCondition<T> loader, Pageable pageable) {
        if (pageable.isPaged()) {
            loader.firstResult((int) pageable.getOffset())
                    .maxResults(pageable.getPageSize());
        }
        return loader;
    }

    public static <T> FluentLoader.ByQuery<T> applyPageableForJpqlQuery(FluentLoader.ByQuery<T> loader, Pageable pageable) {
        return loader.firstResult(pageable.getPageNumber() * pageable.getPageSize())
                .maxResults(pageable.getPageSize())
                .sort(springToJmixSort(pageable.getSort()));
    }

    public static Sort springToJmixSort(@Nullable org.springframework.data.domain.Sort sort) {
        if (sort != null && sort.isSorted()) {
            List<Sort.Order> orders = new LinkedList<>();
            for (org.springframework.data.domain.Sort.Order order : sort) {
                orders.add(order.getDirection() == org.springframework.data.domain.Sort.Direction.DESC
                        ? Sort.Order.desc(order.getProperty())
                        : Sort.Order.asc(order.getProperty()));
            }
            return Sort.by(orders);
        }
        return Sort.UNSORTED;
    }


}
