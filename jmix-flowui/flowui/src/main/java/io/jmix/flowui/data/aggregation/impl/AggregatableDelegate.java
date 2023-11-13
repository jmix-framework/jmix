/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.data.aggregation.impl;

import com.google.common.base.Preconditions;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.data.aggregation.Aggregation;
import io.jmix.flowui.data.aggregation.AggregationStrategy;
import io.jmix.flowui.data.aggregation.Aggregations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component("flowui_AggregatableDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AggregatableDelegate<K> {

    protected Aggregations aggregations;
    protected CurrentAuthentication currentAuthentication;
    protected DatatypeRegistry datatypeRegistry;

    protected Function<K, Object> itemProvider;
    protected BiFunction<K, MetaPropertyPath, Object> itemValueProvider;

    @Autowired
    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    public void setItemProvider(Function<K, Object> itemProvider) {
        this.itemProvider = itemProvider;
    }

    public void setItemValueProvider(BiFunction<K, MetaPropertyPath, Object> itemValueProvider) {
        this.itemValueProvider = itemValueProvider;
    }

    public Map<AggregationInfo, String> aggregate(@Nullable AggregationInfo[] aggregationInfos,
                                                  Collection<K> itemsIds) {
        Preconditions.checkNotNull(aggregationInfos, "AggregationInfo can not be null");
        Preconditions.checkState(aggregationInfos.length != 0,
                "Aggregation must be executed at least by one field");

        Preconditions.checkNotNull(itemProvider, "ItemProvider can not be null");
        Preconditions.checkNotNull(itemValueProvider, "ItemValueProvider can not be null");

        return doAggregation(aggregationInfos, itemsIds);
    }

    public Map<AggregationInfo, Object> aggregateValues(@Nullable AggregationInfo[] aggregationInfos,
                                                        Collection<K> itemIds) {
        Preconditions.checkNotNull(aggregationInfos, "AggregationInfos can not be null");
        Preconditions.checkState(aggregationInfos.length != 0,
                "Aggregation must be executed at least by one field");

        Preconditions.checkNotNull(itemProvider, "ItemProvider can not be null");
        Preconditions.checkNotNull(itemValueProvider, "ItemValueProvider can not be null");

        Map<AggregationInfo, Object> aggregationResults = new HashMap<>();

        for (AggregationInfo aggregationInfo : aggregationInfos) {
            Object value = doPropertyAggregation(aggregationInfo, itemIds);
            aggregationResults.put(aggregationInfo, value);
        }

        return aggregationResults;
    }

    protected Map<AggregationInfo, String> doAggregation(AggregationInfo[] aggregationInfos, Collection<K> itemIds) {
        Map<AggregationInfo, String> aggregationResults = new HashMap<>();

        for (AggregationInfo aggregationInfo : aggregationInfos) {
            final Object value = doPropertyAggregation(aggregationInfo, itemIds);

            String formattedValue;
            if (aggregationInfo.getFormatter() != null) {
                formattedValue = aggregationInfo.getFormatter()
                        .apply(value);
            } else {
                // propertyPath could be null in case of custom aggregation
                MetaPropertyPath propertyPath = aggregationInfo.getPropertyPath();

                Range range = propertyPath != null
                        ? propertyPath.getRange()
                        : null;
                if (range != null && range.isDatatype()) {
                    if (aggregationInfo.getType() != AggregationInfo.Type.COUNT) {
                        Class<?> resultClass;

                        if (aggregationInfo.getStrategy() == null) {
                            Class<?> rangeJavaClass = propertyPath.getRangeJavaClass();
                            Aggregation<?> aggregation = aggregations.get(rangeJavaClass);
                            resultClass = Objects.requireNonNull(aggregation).getResultClass();
                        } else {
                            resultClass = aggregationInfo.getStrategy().getResultClass();
                        }

                        Locale locale = currentAuthentication.getLocale();
                        formattedValue = datatypeRegistry.get(resultClass).format(value, locale);
                    } else {
                        formattedValue = Objects.requireNonNull(value).toString();
                    }
                } else {
                    if (aggregationInfo.getStrategy() != null) {
                        Class<?> resultClass = aggregationInfo.getStrategy().getResultClass();

                        Locale locale = currentAuthentication.getLocale();
                        formattedValue = datatypeRegistry.get(resultClass).format(value, locale);
                    } else {
                        formattedValue = Objects.requireNonNull(value).toString();
                    }
                }
            }

            aggregationResults.put(aggregationInfo, formattedValue);
        }

        return aggregationResults;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Object doPropertyAggregation(AggregationInfo aggregationInfo, Collection<K> itemIds) {
        //noinspection rawtypes
        Collection items;

        if (aggregationInfo.getType() == AggregationInfo.Type.CUSTOM
                && aggregationInfo.getPropertyPath() == null) {
            // Use items in this case
            items = itemIds.stream()
                    .map(itemProvider)
                    .toList();
        } else {
            //noinspection DataFlowIssue
            items = valuesByProperty(aggregationInfo.getPropertyPath(), itemIds);
        }

        if (aggregationInfo.getStrategy() == null) {
            Class<?> javaClass = aggregationInfo.getPropertyPath().getRangeJavaClass();
            Aggregation<?> aggregation = aggregations.get(javaClass);

            Preconditions.checkNotNull(aggregationInfo.getType(), "Type of aggregation can not be null");
            Preconditions.checkNotNull(aggregation, "Aggregation for %s class does not exist",
                    javaClass.getSimpleName());

            return switch (aggregationInfo.getType()) {
                case SUM -> aggregation.sum(items);
                case AVG -> aggregation.avg(items);
                case MIN -> aggregation.min(items);
                case MAX -> aggregation.max(items);
                case COUNT -> aggregation.count(items);
                default -> throw new IllegalArgumentException(String.format("Unknown aggregation type: %s",
                        aggregationInfo.getType()));
            };
        }

        AggregationStrategy<?, ?> strategy = aggregationInfo.getStrategy();
        return strategy.aggregate(items);
    }

    protected List<?> valuesByProperty(MetaPropertyPath propertyPath, Collection<K> itemIds) {
        return itemIds.stream()
                .map(itemId -> itemValueProvider.apply(itemId, propertyPath))
                .toList();
    }
}
