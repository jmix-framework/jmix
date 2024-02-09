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
import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.repository.JmixDataRepositoryContext;
import io.jmix.core.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.core.impl.repository.query.utils.LoaderHelper.springToJmixSort;

/**
 * {@link RepositoryQuery} for query methods annotated with {@link Query @Query}.
 */
public class JmixCustomLoadQuery extends JmixAbstractQuery {

    protected static final String PARAMETER_TEMPLATE = "([:?][a-zA-Z0-9_$]+)";
    protected static final String PARAMETER_PREFIX = "p_";

    protected String query;

    public JmixCustomLoadQuery(DataManager dataManager, Metadata jmixMetadata, Method method, RepositoryMetadata metadata, ProjectionFactory factory, String query) {
        super(dataManager, jmixMetadata, method, metadata, factory);
        this.query = query;
        Matcher m = Pattern.compile(PARAMETER_TEMPLATE).matcher(query);
        Set<String> parameterNames = new HashSet<>();
        Boolean positionParametersFound = null;
        while (m.find()) {
            String occurrence = m.group();
            String paramName;
            if (occurrence.startsWith("?")) {
                if (!Boolean.FALSE.equals(positionParametersFound)) {
                    positionParametersFound = true;
                    paramName = occurrence.replace("?", PARAMETER_PREFIX);
                    this.query = this.query.replace(occurrence, ":" + paramName);
                } else {
                    throw new DevelopmentException(String.format("There are mixed parameter types in query '%s' for %s",
                            query,
                            formatMethod(method)));
                }
            } else if (occurrence.startsWith(":")) {
                if (!Boolean.TRUE.equals(positionParametersFound)) {
                    positionParametersFound = false;
                    paramName = occurrence.replace(":", "");
                } else {
                    throw new DevelopmentException(String.format("There are mixed parameter types in query '%s' for %s",
                            query,
                            formatMethod(method)));
                }
            } else {
                throw new RuntimeException("Cannot happen");
            }
            parameterNames.add(paramName);
        }

        if (parameterNames.size() > 0)
            matchQueryParameters(parameterNames, queryMethod.getParameters().getBindableParameters(), positionParametersFound);
    }

    @Override
    public Object execute(Object[] parameters) {
        UnconstrainedDataManager dataManager = getDataManager();
        //todo taimanov: rework through LoadContext
        FluentLoader.ByQuery<?> query = dataManager
                .load(metadata.getDomainType())
                .query(this.query)
                .parameters(buildNamedParametersMap(parameters))
                .hints(queryHints);

        JmixDataRepositoryContext jmixDataRepositoryContext = jmixContextIndex != -1 ? (JmixDataRepositoryContext) parameters[jmixContextIndex] : null;

        if (fetchPlanIndex != -1) {
            query.fetchPlan((FetchPlan) parameters[fetchPlanIndex]);
        } else if (jmixDataRepositoryContext != null && jmixDataRepositoryContext.fetchPlan() != null) {
            query.fetchPlan(jmixDataRepositoryContext.fetchPlan());
        } else {
            query.fetchPlan(fetchPlan);
        }

        if (sortIndex != -1) {
            query.sort(LoaderHelper.springToJmixSort((Sort) parameters[sortIndex]));
        }

        FluentLoader.ByCondition<?> conditionQuery = null;

        if (jmixDataRepositoryContext != null) {
            query.hints(jmixDataRepositoryContext.hints());

            if (jmixDataRepositoryContext.condition() != null) {
                 conditionQuery = query.condition(jmixDataRepositoryContext.condition());
            }
        }

        if(conditionQuery==null){
            conditionQuery = query.condition(LogicalCondition.and());
        }

        return considerPagingAndProcess(conditionQuery, parameters);
    }

    protected Object considerPagingAndProcess(FluentLoader.ByCondition<?> loader, Object[] parameters) {
        Class<?> returnType = method.getReturnType();
        if (Slice.class.isAssignableFrom(returnType)) {
            if (pageableIndex == -1) {
                throw new DevelopmentException(String.format("Pageable parameter should be provided for method returns instance of Slice: %s", formatMethod(method)));
            }

            Pageable pageable = (Pageable) parameters[pageableIndex];

            LoaderHelper.applyPageableForConditionLoader(loader, pageable);
            loader.sort(springToJmixSort(pageable.getSort()));

            if (Page.class.isAssignableFrom(returnType)) {
                LoadContext<?> context = new LoadContext<>(jmixMetadata.getClass(metadata.getDomainType()))
                        .setQuery(new LoadContext.Query(query)
                                .setParameters(buildNamedParametersMap(parameters)))
                        .setHints(queryHints);
                long count = dataManager.getCount(context);
                return new PageImpl(loader.list(), pageable, count);
            } else {
                if (pageable.isPaged())
                    loader.maxResults(pageable.getPageSize() + 1);

                List<?> results = loader.list();
                boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();

                return new SliceImpl(hasNext ? results.subList(0, pageable.getPageSize()) : results, pageable, hasNext);
            }
        }
        return loader.list();
    }

    protected void matchQueryParameters(Set<String> parameterNames,
                                        Parameters<? extends Parameters, ? extends Parameter> bindableParameters,
                                        boolean fromPositionParameters) {

        if (fromPositionParameters) {
            for (String parameterName : parameterNames) {
                int position = Integer.parseInt(parameterName.substring(PARAMETER_PREFIX.length())) - 1;
                namedParametersBindings.put(parameterName, bindableParameters.getParameter(position).getIndex());
            }
        } else {
            for (Parameter bindableParameter : bindableParameters.toList()) {
                //noinspection OptionalGetWithoutIsPresent
                String name = bindableParameter.getName().get();//existence checked by spring
                if (parameterNames.contains(name)) {
                    namedParametersBindings.put(name, bindableParameter.getIndex());
                } else {
                    throw new DevelopmentException(String.format("Parameter %s of method %s does not included to query \"%s\"",
                            name,
                            formatMethod(method),
                            query
                    ));
                }
            }
        }
    }

    @Override
    protected String getQueryDescription() {
        return String.format("%s; query:%s", super.getQueryDescription(), query);
    }
}
