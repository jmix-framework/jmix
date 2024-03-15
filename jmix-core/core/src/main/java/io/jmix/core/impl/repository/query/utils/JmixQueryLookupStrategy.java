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

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.QueryStringProcessor;
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
import java.util.List;

/**
 * Determines query type and creates {@link RepositoryQuery RepositoryQueries} for Jmix data repositories
 */
public class JmixQueryLookupStrategy implements QueryLookupStrategy {

    private static final Logger log = LoggerFactory.getLogger(QueryLookupStrategy.class);

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
        JmixAbstractQuery resolvedQuery;
        if (query != null) {
            String qryString = query.value();
            resolvedQuery = new JmixCustomLoadQuery(dataManager, jmixMetadata, fetchPlanRepository, processors, method, repositoryMetadata, factory, qryString);
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
            log.debug(String.format("Query for %s resolved: %s", method, resolvedQuery.toString()));
        }

        return resolvedQuery;

    }
}
