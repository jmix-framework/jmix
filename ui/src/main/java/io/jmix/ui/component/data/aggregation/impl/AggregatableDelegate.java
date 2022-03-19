/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component.data.aggregation.impl;

import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.AggregationInfo;
import io.jmix.ui.component.data.aggregation.Aggregation;
import io.jmix.ui.component.data.aggregation.AggregationStrategy;
import io.jmix.ui.component.data.aggregation.Aggregations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("ui_AggregatableDelegate")
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

    public Map<AggregationInfo, String> aggregate(@Nullable AggregationInfo[] aggregationInfos, Collection<K> itemIds) {
        if (aggregationInfos == null || aggregationInfos.length == 0) {
            throw new NullPointerException("Aggregation must be executed at least by one field");
        }

        if (itemProvider == null || itemValueProvider == null) {
            throw new NullPointerException("ItemProvider and ItemValueProvider must be non-nulls");
        }

        return doAggregation(itemIds, aggregationInfos);
    }

    protected Map<AggregationInfo, String> doAggregation(Collection<K> itemIds, AggregationInfo[] aggregationInfos) {
        Map<AggregationInfo, String> aggregationResults = new HashMap<>();
        for (AggregationInfo aggregationInfo : aggregationInfos) {
            final Object value = doPropertyAggregation(aggregationInfo, itemIds);

            String formattedValue;
            if (aggregationInfo.getFormatter() != null) {
                formattedValue = aggregationInfo.getFormatter().apply(value);
            } else {
                // propertyPath could be null in case of custom aggregation
                MetaPropertyPath propertyPath = aggregationInfo.getPropertyPath();

                Range range = propertyPath != null ? propertyPath.getRange() : null;
                if (range != null && range.isDatatype()) {
                    if (aggregationInfo.getType() != AggregationInfo.Type.COUNT) {
                        Class resultClass;
                        if (aggregationInfo.getStrategy() == null) {
                            Class rangeJavaClass = propertyPath.getRangeJavaClass();
                            Aggregation aggregation = aggregations.get(rangeJavaClass);
                            resultClass = aggregation.getResultClass();
                        } else {
                            resultClass = aggregationInfo.getStrategy().getResultClass();
                        }

                        Locale locale = currentAuthentication.getLocale();
                        formattedValue = datatypeRegistry.get(resultClass).format(value, locale);
                    } else {
                        formattedValue = value.toString();
                    }
                } else {
                    if (aggregationInfo.getStrategy() != null) {
                        Class resultClass = aggregationInfo.getStrategy().getResultClass();

                        Locale locale = currentAuthentication.getLocale();
                        formattedValue = datatypeRegistry.get(resultClass).format(value, locale);
                    } else {
                        formattedValue = value.toString();
                    }
                }
            }

            aggregationResults.put(aggregationInfo, formattedValue);
        }
        return aggregationResults;
    }

    public Map<AggregationInfo, Object> aggregateValues(@Nullable AggregationInfo[] aggregationInfos, Collection<K> itemIds) {
        if (aggregationInfos == null || aggregationInfos.length == 0) {
            throw new NullPointerException("Aggregation must be executed at least by one field");
        }

        if (itemProvider == null || itemValueProvider == null) {
            throw new NullPointerException("ItemProvider and ItemValueProvider must be non-nulls");
        }

        Map<AggregationInfo, Object> aggregationResults = new HashMap<>();

        for (AggregationInfo aggregationInfo : aggregationInfos) {
            Object value = doPropertyAggregation(aggregationInfo, itemIds);
            aggregationResults.put(aggregationInfo, value);
        }

        return aggregationResults;
    }

    @SuppressWarnings("unchecked")
    protected Object doPropertyAggregation(AggregationInfo aggregationInfo, Collection<K> itemIds) {
        List items;

        if (aggregationInfo.getType() == AggregationInfo.Type.CUSTOM
                && aggregationInfo.getPropertyPath() == null) {
            // use items in this case;
            items = itemIds.stream()
                    .map(itemProvider::apply)
                    .collect(Collectors.toList());
        } else {
            items = valuesByProperty(aggregationInfo.getPropertyPath(), itemIds);
        }

        if (aggregationInfo.getStrategy() == null) {
            Class rangeJavaClass = aggregationInfo.getPropertyPath().getRangeJavaClass();
            Aggregation aggregation = aggregations.get(rangeJavaClass);

            switch (aggregationInfo.getType()) {
                case COUNT:
                    return aggregation.count(items);
                case AVG:
                    return aggregation.avg(items);
                case MAX:
                    return aggregation.max(items);
                case MIN:
                    return aggregation.min(items);
                case SUM:
                    return aggregation.sum(items);
                default:
                    throw new IllegalArgumentException(String.format("Unknown aggregation type: %s",
                            aggregationInfo.getType()));
            }
        } else {
            AggregationStrategy strategy = aggregationInfo.getStrategy();
            return strategy.aggregate(items);
        }
    }

    protected List valuesByProperty(MetaPropertyPath propertyPath, Collection<K> itemIds) {
        final List<Object> values = new ArrayList<>(itemIds.size());
        for (final K itemId : itemIds) {
            final Object value = itemValueProvider.apply(itemId, propertyPath);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }
}
