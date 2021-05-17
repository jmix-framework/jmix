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

package io.jmix.data.repositories.query;

import io.jmix.core.*;
import io.jmix.data.repositories.query.utils.LoaderHelper;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmixJpqlQuery extends JmixAbstractQuery {

    protected static final String PARAMETER_TEMPLATE = "([:?][a-zA-Z0-9]+)";

    protected String jpql;

    protected Boolean usePositionParameters = null;
    protected List<Integer> positionParametersOrder = null;

    public JmixJpqlQuery(DataManager dataManager, Metadata jmixMetadata, Method method, RepositoryMetadata metadata, ProjectionFactory factory, String query) {
        super(dataManager, jmixMetadata, method, metadata, factory);
        this.jpql = query;
        Matcher m = Pattern.compile(PARAMETER_TEMPLATE).matcher(query);
        List<String> parameterNames = new ArrayList<>();
        while (m.find()) {
            //extracting name from com.haulmont.addons.cuba.jpa.repositories.query removing colon
            String occurrence = query.substring(m.start(), m.end());
            if (occurrence.contains("?")) {
                if (!Boolean.FALSE.equals(usePositionParameters)) {
                    usePositionParameters = true;
                    occurrence = occurrence.replace("?", "");
                } else
                    throwMixedParameterTypesException();
            }
            if (occurrence.contains(":")) {
                if (!Boolean.TRUE.equals(usePositionParameters)) {
                    usePositionParameters = false;
                    occurrence = occurrence.replace(":", "");
                } else
                    throwMixedParameterTypesException();
            }
            parameterNames.add(occurrence);
        }

        matchQueryParameters(parameterNames, queryMethod.getParameters().getBindableParameters());
    }

    protected void throwMixedParameterTypesException() {
        throw new DevelopmentException(String.format("There are mixed parameter types in query '%s' for %s",
                jpql,
                formatMethod(method)));
    }

    protected Object[] collectPositionParameterValues(Object[] methodParameters) {
        return positionParametersOrder.stream().map(index -> methodParameters[index]).toArray();
    }

    @Override
    public Object execute(Object[] parameters) {
        DataManager dataManager = getDataManager();

        FluentLoader.ByQuery<?> query;
        if (usePositionParameters) {
            query = dataManager
                    .load(metadata.getDomainType())
                    .query(jpql, collectPositionParameterValues(parameters));
        } else {
            query = dataManager
                    .load(metadata.getDomainType())
                    .query(jpql)
                    .parameters(buildNamedParametersMap(parameters));
        }

        query.fetchPlan(fetchPlan);

        if (sortIndex != -1) {
            query.sort(LoaderHelper.springToJmixSort((Sort) parameters[sortIndex]));
        }

        return considerPagingAndProcess(query, parameters);
    }

    protected Object considerPagingAndProcess(FluentLoader.ByQuery<?> loader, Object[] parameters) {
        Class<?> returnType = method.getReturnType();
        if (Slice.class.isAssignableFrom(returnType)) {
            if (pageableIndex == -1) {
                throw new DevelopmentException(String.format("Pageable parameter should be provided for method returns instance of Slice: %s", formatMethod(method)));
            }

            Pageable pageable = (Pageable) parameters[pageableIndex];

            LoaderHelper.applyPageableForJpqlQuery(loader, pageable);

            if (Page.class.isAssignableFrom(returnType)) {
                if (usePositionParameters) {
                    throw new DevelopmentException("Paging is not implemented yet for jpql query methods with positional parameters.");
                } else {
                    LoadContext<?> context = new LoadContext<>(jmixMetadata.getClass(metadata.getDomainType())).setQuery(
                            new LoadContext.Query(jpql)
                                    .setParameters(buildNamedParametersMap(parameters))
                    );
                    long count = dataManager.getCount(context);
                    return new PageImpl(loader.list(), pageable, count);
                }
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

    protected void matchQueryParameters(List<String> parameterNames, Parameters<? extends Parameters, ? extends Parameter> bindableParameters) {

        if (usePositionParameters) {
            positionParametersOrder = new ArrayList<>();
            for (int i = 0; i < bindableParameters.toList().size(); i++) {
                positionParametersOrder.add(bindableParameters.getParameter(i).getIndex());
            }
        } else {
            namedParametersBindings = new HashMap<>();
            for (Parameter bindableParameter : bindableParameters.toList()) {
                //noinspection OptionalGetWithoutIsPresent
                String name = bindableParameter.getName().get();//existence checked by spring
                if (parameterNames.contains(name)) {
                    namedParametersBindings.put(name, bindableParameter.getIndex());
                } else {
                    throw new DevelopmentException(String.format("Parameter %s of method %s does not included to query \"%s\"",
                            name,
                            formatMethod(method),
                            jpql
                    ));
                }
            }
        }
    }

    @Override
    protected String getQueryDescription() {
        return String.format("%s; jpql:%s", super.getQueryDescription(), jpql);
    }
}
