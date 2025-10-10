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

import io.jmix.core.*;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.repository.query.*;
import io.jmix.core.repository.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Stream;

/**
 * Determines query type and creates {@link RepositoryQuery RepositoryQueries} for Jmix data repositories
 */
public class JmixQueryLookupStrategy implements QueryLookupStrategy {

    private static final Logger log = LoggerFactory.getLogger(QueryLookupStrategy.class);

    public static final String PROPERTY_PREFIX = "property_";

    private DataManager dataManager;
    private Metadata jmixMetadata;
    private FetchPlanRepository fetchPlanRepository;
    private List<QueryStringProcessor> processors;

    public JmixQueryLookupStrategy(DataManager dataManager,
                                   Metadata jmixMetadata,
                                   FetchPlanRepository fetchPlanRepository,
                                   List<QueryStringProcessor> processors) {
        this.dataManager = dataManager;
        this.jmixMetadata = jmixMetadata;
        this.fetchPlanRepository = fetchPlanRepository;
        this.processors = processors;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata repositoryMetadata, ProjectionFactory factory, NamedQueries namedQueries) {
        Query query = method.getDeclaredAnnotation(Query.class);
        JmixAbstractQuery<?> resolvedQuery;
        if (query != null) {
            if (isEntityReturnType(method)) {
                String qryString = query.value();
                resolvedQuery = new JmixCustomLoadQuery(dataManager, jmixMetadata, fetchPlanRepository, processors, method, repositoryMetadata, factory, qryString);
            } else {
                String scalarQueryString = query.value();
                List<String> propertyNames;
                if (query.properties().length == 0) {
                    propertyNames = generatePropertyNames(method, scalarQueryString);
                } else {
                    propertyNames = Arrays.asList(query.properties());
                }
                resolvedQuery = new JmixScalarQuery(dataManager, jmixMetadata, method, repositoryMetadata, factory, scalarQueryString, propertyNames);
            }
        } else {
            PartTree qryTree = new PartTree(method.getName(), repositoryMetadata.getDomainType());
            if (qryTree.isDelete()) {
                resolvedQuery = new JmixDeleteQuery(dataManager, jmixMetadata, fetchPlanRepository, processors, method, repositoryMetadata, factory, qryTree);
            } else if (qryTree.isCountProjection()) {
                resolvedQuery = new JmixCountQuery(dataManager, jmixMetadata, fetchPlanRepository, processors, method, repositoryMetadata, factory, qryTree);
            } else if (qryTree.isExistsProjection()) {
                resolvedQuery = new JmixExistsQuery(dataManager, jmixMetadata, fetchPlanRepository, processors, method, repositoryMetadata, factory, qryTree);
            } else {
                resolvedQuery = new JmixListQuery(dataManager, jmixMetadata, fetchPlanRepository, processors, method, repositoryMetadata, factory, qryTree);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Query for %s resolved: %s", method, resolvedQuery));
        }

        return resolvedQuery;
    }

    protected boolean isEntityReturnType(Method method) {
        Class<?> methodReturnType = method.getReturnType();

        if (Entity.class.isAssignableFrom(methodReturnType) && !KeyValueEntity.class.isAssignableFrom(methodReturnType))
            return true;

        if (methodReturnType.getName().equals("void") || Void.class.isAssignableFrom(methodReturnType)) {
            return true;//preserve old behavior for void type
        }

        if (method.getGenericReturnType() instanceof ParameterizedType parameterizedType
                && parameterizedType.getActualTypeArguments().length == 1
                && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> clazz
                && Entity.class.isAssignableFrom(clazz)
                && !KeyValueEntity.class.isAssignableFrom(clazz)) {
            return true;
        }

        return false;
    }

    protected List<String> generatePropertyNames(Method method, String query) {
        query = query.toLowerCase().trim();

        int fromClausePosition = query.indexOf(" from ");
        if (!query.startsWith("select ") || fromClausePosition < 0)
            throw new DevelopmentException("Malformed scalar value query: '" + query + "' for method "
                    + JmixAbstractQuery.formatMethod(method));

        String selectExpressions = query.substring(0, fromClausePosition);
        String[] expressoins = selectExpressions.split(",");

        if ((Collection.class.isAssignableFrom(method.getReturnType())
                || Stream.class.isAssignableFrom(method.getReturnType())
                || Iterable.class.isAssignableFrom(method.getReturnType())
                || Optional.class.isAssignableFrom(method.getReturnType()))
                && method.getGenericReturnType() instanceof ParameterizedType parameterizedType
                && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> clazz
                && !KeyValueEntity.class.isAssignableFrom(clazz)
                && expressoins.length > 1) {
            throw new DevelopmentException("Multiple values per row is supported for KeyValueEntity generic types only. Query: '"
                    + query + "' for method " + JmixAbstractQuery.formatMethod(method));
        }

        List<String> propertyNames = new ArrayList<>(expressoins.length);
        for (int i = 0; i < expressoins.length; i++) {
            propertyNames.add(PROPERTY_PREFIX + i);
        }
        return propertyNames;
    }
}
