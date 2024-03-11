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

package io.jmix.core.impl.repository.query.utils;

import io.jmix.core.FluentLoader;
import io.jmix.core.LoadContext;
import io.jmix.core.Sort;
import org.springframework.data.domain.Pageable;

import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Utility class contains common methods to apply and process Spring Data repositories special query parameters and annotations.
 */
public class LoaderHelper {

    @SuppressWarnings("DataFlowIssue")
    public static void applyPageableForLoadContext(LoadContext<?> loadContext, Pageable pageable) {
        if (pageable.isPaged()) {
            loadContext.getQuery().setFirstResult((int) pageable.getOffset());
            loadContext.getQuery().setMaxResults(pageable.getPageSize());
        }
    }

    public static <T> FluentLoader.ByCondition<T> applyPageableForConditionLoader(FluentLoader.ByCondition<T> loader, Pageable pageable) {
        if (pageable.isPaged()) {
            loader.firstResult((int) pageable.getOffset())
                    .maxResults(pageable.getPageSize());
        }
        return loader;
    }

    public static <T> FluentLoader.ByQuery<T> applyPageableForQueryLoader(FluentLoader.ByQuery<T> loader, Pageable pageable) {
        if (pageable.isPaged()) {
            loader.firstResult((int) pageable.getOffset())
                    .maxResults(pageable.getPageSize())
                    .sort(springToJmixSort(pageable.getSort()));
        }
        return loader;
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

    public static org.springframework.data.domain.Sort jmixToSpringSort(@Nullable Sort sort) {
        if (sort != null && !sort.getOrders().isEmpty()) {
            List<org.springframework.data.domain.Sort.Order> orders = new LinkedList<>();
            for (Sort.Order order : sort.getOrders()) {
                orders.add(order.getDirection() == Sort.Direction.DESC
                        ? org.springframework.data.domain.Sort.Order.desc(order.getProperty())
                        : org.springframework.data.domain.Sort.Order.asc(order.getProperty()));
            }
            return org.springframework.data.domain.Sort.by(orders);
        }

        return org.springframework.data.domain.Sort.unsorted();
    }

    /**
     * Parses string value to boolean for known jmix hints.
     * @return boolean representation or the same value if hint is unknown
     */
    public static Serializable parseHint(String name, Serializable value){
        if (value instanceof String stringValue) {
            return switch (name) {
                case "jmix.softDeletion", "jmix.dynattr", "jmix.cacheable" -> Boolean.parseBoolean(stringValue);
                default -> stringValue;
            };
        }

        return value;
    }
}
