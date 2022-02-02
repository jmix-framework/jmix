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

package io.jmix.data.impl.jpql.generator;

import com.google.common.collect.Iterables;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("data_SortJpqlGenerator")
public class SortJpqlGenerator {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;
    @Autowired
    protected JpqlSortExpressionProvider jpqlSortExpressionProvider;
    @Autowired
    protected DbmsSpecifics dbmsSpecifics;

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SortJpqlGenerator.class);

    public String processQuery(String entityName, List<String> valueProperties, String queryString, Sort sort) {
        List<Sort.Order> orders = sort.getOrders();
        if (orders.isEmpty()) {
            return queryString;
        }

        Sort.Direction defaultSort = Sort.Direction.ASC;

        Map<String,Sort.Direction> sortExpressions = new LinkedHashMap<>();

        if (entityName != null) {
            MetaClass metaClass = metadata.getClass(entityName);
            for (Sort.Order order : sort.getOrders()) {
                MetaPropertyPath metaPropertyPath = metaClass.getPropertyPath(order.getProperty());
                checkNotNullArgument(metaPropertyPath, "Could not resolve property path '%s' in '%s'", order.getProperty(), metaClass);

                sortExpressions.putAll(getPropertySortExpressions(metaPropertyPath, order.getDirection()));
            }
            if (!sortExpressions.isEmpty()) {
                sortExpressions.putAll(getUniqueSortExpression(sortExpressions, metaClass, defaultSort));
            }
        } else if (valueProperties != null) {
            List<String> selectedExpressions = queryTransformerFactory.parser(queryString).getSelectedExpressionsList();
            for (Sort.Order order : sort.getOrders()) {
                sortExpressions.putAll(getValuePropertySortExpression(order.getProperty(), valueProperties, selectedExpressions, order.getDirection()));
            }
        }

        return transformQuery(queryString, sortExpressions, defaultSort);
    }

    protected Map<String,Sort.Direction> getUniqueSortExpression(Map<String,Sort.Direction> sortExpressions, MetaClass metaClass, Sort.Direction direction) {
        String pkName = metadataTools.getPrimaryKeyName(metaClass);
        if (pkName != null) {
            MetaProperty idProperty = metaClass.getProperty(pkName);
            if (metadataTools.hasCompositePrimaryKey(metaClass)) {
                Map<String,Sort.Direction> uniqueSortExpressions = new LinkedHashMap<>();
                MetaClass pkMetaClass = idProperty.getRange().asClass();
                for (MetaProperty metaProperty : pkMetaClass.getProperties()) {
                    if (metadataTools.isJpa(metaProperty)) {
                        MetaPropertyPath idPropertyPath = metaClass.getPropertyPath(String.format("%s.%s", pkName, metaProperty.getName()));
                        Map<String,Sort.Direction> currentSortExpressions = getPropertySortExpressions(Objects.requireNonNull(idPropertyPath), direction);
                        if (currentSortExpressions.keySet().stream().noneMatch(sortExpressions::containsKey)) {
                            uniqueSortExpressions.putAll(currentSortExpressions);
                        }
                    }
                }
                return uniqueSortExpressions;
            } else {
                MetaPropertyPath idPropertyPath = metaClass.getPropertyPath(pkName);
                Map<String,Sort.Direction> uniqueSortExpressions = getPropertySortExpressions(Objects.requireNonNull(idPropertyPath), direction);
                if (uniqueSortExpressions.keySet().stream().noneMatch(sortExpressions::containsKey)) {
                    return uniqueSortExpressions;
                }
            }
        }
        return Collections.emptyMap();
    }

    protected String transformQuery(String queryString, Map<String,Sort.Direction> sortExpressions, Sort.Direction direction) {
        if (!sortExpressions.isEmpty()) {
            QueryTransformer transformer = queryTransformerFactory.transformer(queryString);
            transformer.replaceOrderByExpressions(sortExpressions);
            return transformer.getResult();
        } else {
            return queryString;
        }
    }

    protected Map<String,Sort.Direction> getPropertySortExpressions(MetaPropertyPath metaPropertyPath, Sort.Direction sortDirection) {
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

        if (metadataTools.isJpa(metaPropertyPath)) {
            if (!metaProperty.getRange().isClass()) {

                String sortExpression = metadataTools.isLob(metaProperty) ?
                        getLobPropertySortExpression(metaPropertyPath, sortDirection) :
                        getDatatypePropertySortExpression(metaPropertyPath, sortDirection);
                return sortExpression == null ? Collections.emptyMap() : Collections.singletonMap(sortExpression,sortDirection);

            } else if (!metaProperty.getRange().getCardinality().isMany()) {
                return getEntityPropertySortExpression(metaPropertyPath, sortDirection);
            }
        } else {
            return getNotPersistentPropertySortExpression(metaPropertyPath, sortDirection);
        }

        return Collections.emptyMap();
    }

    protected String getDatatypePropertySortExpression(MetaPropertyPath metaPropertyPath, Sort.Direction sortDirection) {
        return jpqlSortExpressionProvider.getDatatypeSortExpression(metaPropertyPath, sortDirection);
    }

    @Nullable
    protected String getLobPropertySortExpression(MetaPropertyPath metaPropertyPath, Sort.Direction sortDirection) {
        return supportsLobSorting(metaPropertyPath) ? jpqlSortExpressionProvider.getLobSortExpression(metaPropertyPath, sortDirection) : null;
    }

    protected Map<String,Sort.Direction> getEntityPropertySortExpression(MetaPropertyPath metaPropertyPath, Sort.Direction sortDirection) {
        Collection<MetaProperty> properties = metadataTools.getInstanceNameRelatedProperties(
                metaPropertyPath.getMetaProperty().getRange().asClass());

        if (!properties.isEmpty()) {
            Map<String,Sort.Direction> sortExpressions = new LinkedHashMap<>(properties.size());
            for (MetaProperty metaProperty : properties) {
                if (metadataTools.isJpa(metaProperty)) {
                    MetaPropertyPath childPropertyPath = new MetaPropertyPath(metaPropertyPath, metaProperty);
                    sortExpressions.putAll(getPropertySortExpressions(childPropertyPath, sortDirection));
                }
            }
            return sortExpressions;
        } else {
            return Collections.singletonMap(String.format("{E}.%s", metaPropertyPath), sortDirection);
        }
    }

    protected Map<String,Sort.Direction> getNotPersistentPropertySortExpression(MetaPropertyPath metaPropertyPath, Sort.Direction sortDirection) {
        List<String> related = metadataTools.getDependsOnProperties(metaPropertyPath.getMetaProperty());
        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);

        if (!related.isEmpty()) {
            Map<String,Sort.Direction> sortExpressions = new LinkedHashMap<>(related.size());
            for (String item : related) {
                MetaProperty metaProperty = propertyMetaClass.getProperty(item);
                if (metadataTools.isJpa(metaProperty)) {
                    List<MetaProperty> metaProperties = Arrays.asList(metaPropertyPath.getMetaProperties());
                    metaProperties.set(metaProperties.size() - 1, metaProperty);
                    MetaPropertyPath childPropertyPath = new MetaPropertyPath(metaPropertyPath.getMetaClass(),
                            Iterables.toArray(metaProperties, MetaProperty.class));
                    sortExpressions.putAll(getPropertySortExpressions(childPropertyPath, sortDirection));
                }
            }
            return sortExpressions;
        }

        return Collections.emptyMap();
    }

    protected Map<String,Sort.Direction> getValuePropertySortExpression(String property, List<String> valueProperties, List<String> selectedExpressions,
                                                          Sort.Direction sortDirection) {
        int index = valueProperties.indexOf(property);
        if (index >= 0 && index < selectedExpressions.size()) {
            return Collections.singletonMap(selectedExpressions.get(index),sortDirection);
        }

        if (property != null) {
            String[] properties = property.split("\\.");
            if (properties.length > 2) {
                log.debug("The length of {} property path is greater than 2. Only direct property sorting is allowed.",
                        property);
                return Collections.emptyMap();
            }
            for (String selectedExpression : selectedExpressions) {
                //Checking equality between the JPQL query entity alias and the root of property path (one-level depth).
                if (properties[0].equals(selectedExpression)) {
                    return Collections.singletonMap(property,sortDirection);
                }
            }
            log.debug("The root of the {} value property path does not match any of the selected expressions.",
                    property);
        }

        return Collections.emptyMap();
    }

    protected boolean supportsLobSorting(MetaPropertyPath metaPropertyPath) {
        MetaClass metaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
        String storeName = metaClass.getStore().getName();

        return storeName == null || dbmsSpecifics.getDbmsFeatures(storeName).supportsLobSortingAndFiltering();
    }
}
