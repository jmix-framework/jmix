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

package io.jmix.core.impl.repository.query;

import io.jmix.core.*;
import io.jmix.core.impl.repository.query.utils.QueryParameterUtils;
import io.jmix.core.repository.JmixDataRepositoryContext;
import io.jmix.core.repository.Query;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RepositoryQuery} for query methods annotated with {@link Query @Query}.
 */
public class JmixCustomLoadQuery extends JmixAbstractEntityQuery {
    protected String query;

    public JmixCustomLoadQuery(DataManager dataManager,
                               Metadata jmixMetadata,
                               FetchPlanRepository fetchPlanRepository,
                               List<QueryStringProcessor> queryStringProcessors,
                               Method method,
                               RepositoryMetadata metadata,
                               ProjectionFactory factory,
                               String query) {
        super(dataManager, jmixMetadata, fetchPlanRepository, queryStringProcessors, method, metadata, factory);
        this.query = QueryParameterUtils.replaceQueryParameters(queryMethod, method, query, namedParametersBindings);
    }

    /**
     * Builds {@link LoadContext} based on
     * <ul>
     *     <li>{@link Query}</li>
     *     <li>{@link JmixDataRepositoryContext#condition()},</li>
     *     <li>{@link io.jmix.core.repository.QueryHints},</li>
     *     <li>{@link JmixDataRepositoryContext#hints()}.</li>
     * </ul>
     * <p>
     * Suitable as is for count query.
     *
     * @param parameters query method parameters
     * @return {@link LoadContext} with {@link LoadContext#getQuery()} not null
     */
    @Override
    protected LoadContext<?> prepareQueryContext(Object[] parameters) {
        String queryString = QueryUtils.applyQueryStringProcessors(queryStringProcessors, this.query, metadata.getDomainType());
        LoadContext.Query lcQuery = new LoadContext.Query(queryString);

        if (jmixContextIndex != -1 && parameters[jmixContextIndex] != null) {
            JmixDataRepositoryContext jmixDataRepositoryContext = (JmixDataRepositoryContext) parameters[jmixContextIndex];
            if (jmixDataRepositoryContext.condition() != null) {
                lcQuery.setCondition(jmixDataRepositoryContext.condition());
            }
        }

        lcQuery.setParameters(buildNamedParametersMap(parameters));

        return new LoadContext<>(jmixMetadata.getClass(metadata.getDomainType()))
                .setQuery(lcQuery)
                .setHints(collectHints(parameters));
    }

    @Override
    protected String getQueryDescription() {
        return String.format("%s; query:%s", super.getQueryDescription(), query);
    }
}
