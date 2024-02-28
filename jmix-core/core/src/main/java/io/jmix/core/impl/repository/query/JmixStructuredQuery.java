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

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.impl.repository.query.utils.ConditionTransformer;
import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.repository.JmixDataRepositoryContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base {@link RepositoryQuery} class for repository queries constructed by method name.
 */
public abstract class JmixStructuredQuery extends JmixAbstractQuery {
    protected final Condition conditions;
    protected final boolean distinct;

    /**
     * Ignored if PageRequest parameter passed with its own max results.
     */
    protected final Integer maxResults;

    public JmixStructuredQuery(DataManager dataManager, Metadata jmixMetadata, Method method, RepositoryMetadata metadata, ProjectionFactory factory, PartTree qryTree) {
        super(dataManager, jmixMetadata, method, metadata, factory);
        List<String> parameterNames = new ArrayList<>();
        conditions = ConditionTransformer.fromPartTree(qryTree, parameterNames);
        distinct = qryTree.isDistinct();
        maxResults = qryTree.isLimiting() ? qryTree.getMaxResults() : null;

        Parameters<? extends Parameters, ? extends Parameter> bindableParameters = queryMethod.getParameters().getBindableParameters();
        for (int i = 0; i < parameterNames.size(); i++) {//bind parameters to names according to their order
            namedParametersBindings.put(parameterNames.get(i), bindableParameters.getParameter(i).getIndex());
        }
    }

    @Override
    protected String getQueryDescription() {
        return String.format("%s; conditions: '%s'; maxResults: '%s'", super.getQueryDescription(), conditions, maxResults);
    }


    /**
     * Builds {@link LoadContext} based on
     * <ul>
     *     <li>derived method name,</li>
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
    protected LoadContext<?> prepareStructuredQueryContext(Object[] parameters) {
        Map<String, Serializable> hints = new HashMap<>(queryHints);

        JmixDataRepositoryContext jmixDataRepositoryContext = jmixContextIndex != -1 ? (JmixDataRepositoryContext) parameters[jmixContextIndex] : null;
        Condition currentCallConditions = this.conditions;
        if (jmixDataRepositoryContext != null) {
            if (jmixDataRepositoryContext.condition() != null) {
                currentCallConditions = LogicalCondition.and(currentCallConditions, jmixDataRepositoryContext.condition());
            }

            jmixDataRepositoryContext.hints().forEach((name, value) ->
                    hints.put(name, LoaderHelper.parseHint(name, value)));
        }

        String entityName = jmixMetadata.getClass(metadata.getDomainType()).getName();

        String queryString = String.format("select %s e from %s e", distinct ? "distinct" : "", entityName);

        return new LoadContext<>(jmixMetadata.getClass(metadata.getDomainType()))
                .setQuery(new LoadContext.Query(queryString)
                        .setCondition(currentCallConditions)
                        .setParameters(buildNamedParametersMap(parameters)))
                .setHints(hints);
    }
}
