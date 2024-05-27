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
import io.jmix.core.impl.repository.query.utils.JmixQueryLookupStrategy;
import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import io.jmix.core.impl.repository.support.method_metadata.MethodMetadataHelper;
import io.jmix.core.repository.ApplyConstraints;
import io.jmix.core.repository.FetchPlan;
import io.jmix.core.repository.JmixDataRepositoryContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;


import static io.jmix.core.Sort.*;

/**
 * Query implementation for Jmix. If you need different types of queries, you can either extend this class or implement parent interface.
 *
 * @see JmixQueryLookupStrategy is responsible for generating Query implementations based on interface method names that will be executed by the Jmix.
 * @see RepositoryQuery
 */
public abstract class JmixAbstractQuery implements RepositoryQuery {

    protected final Method method;
    protected final RepositoryMetadata metadata;
    protected final ProjectionFactory factory;
    protected final JmixQueryMethod queryMethod;

    /**
     * {@link UnconstrainedDataManager} or {@link DataManager} will be chosen depending on {@link ApplyConstraints} annotation on method/repository or ancestor method/repository
     */
    protected UnconstrainedDataManager dataManager;
    protected List<QueryStringProcessor> queryStringProcessors;
    protected FetchPlanRepository fetchPlanRepository;

    protected Metadata jmixMetadata;

    protected final Map<String, Integer> namedParametersBindings = new HashMap<>();


    protected int sortIndex;
    protected int pageableIndex;
    protected int fetchPlanIndex;
    protected int jmixContextIndex;

    protected final Map<String, Serializable> queryHints;
    protected final String fetchPlanByAnnotation;

    public JmixAbstractQuery(DataManager dataManager,
                             Metadata jmixMetadata,
                             FetchPlanRepository fetchPlanRepository,
                             List<QueryStringProcessor> queryStringProcessors,
                             Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        this.method = method;
        this.metadata = metadata;
        this.fetchPlanRepository = fetchPlanRepository;
        this.queryStringProcessors = queryStringProcessors;
        this.factory = factory;
        this.queryMethod = getQueryMethod();
        this.jmixMetadata = jmixMetadata;

        ApplyConstraints applyConstraintsAnnotation = MethodMetadataHelper.determineApplyConstraints(method, metadata.getRepositoryInterface());
        this.dataManager = applyConstraintsAnnotation.value() ? dataManager : dataManager.unconstrained();
        this.queryHints = Collections.unmodifiableMap(MethodMetadataHelper.determineQueryHints(method));

        processSpecialParameters();

        FetchPlan fetchPlanAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, FetchPlan.class);
        fetchPlanByAnnotation = fetchPlanAnnotation != null ? fetchPlanAnnotation.value() : io.jmix.core.FetchPlan.BASE;
    }

    @Override
    public JmixQueryMethod getQueryMethod() {
        return new JmixQueryMethod(method, metadata, factory);
    }

    public UnconstrainedDataManager getDataManager() {
        return dataManager;
    }

    protected Map<String, Object> buildNamedParametersMap(Object[] values) {
        Map<String, Object> paramsMap = new HashMap<>();
        for (Map.Entry<String, Integer> parameterBinding : namedParametersBindings.entrySet()) {
            paramsMap.put(parameterBinding.getKey(), values[parameterBinding.getValue()]);
        }
        return paramsMap;
    }

    protected void processSpecialParameters() {
        JmixParameters parameters = (JmixParameters) queryMethod.getParameters();

        pageableIndex = parameters.getPageableIndex();
        sortIndex = parameters.getSortIndex();
        fetchPlanIndex = parameters.getFetchPlanIndex();
        jmixContextIndex = parameters.getJmixContextIndex();
    }

    protected void setupFetchPlan(LoadContext<?> loadContext, Object[] parameters) {
        JmixDataRepositoryContext jmixDataRepositoryContext = jmixContextIndex != -1 ? (JmixDataRepositoryContext) parameters[jmixContextIndex] : null;
        if (fetchPlanIndex != -1 && parameters[fetchPlanIndex] != null) {
            loadContext.setFetchPlan((io.jmix.core.FetchPlan) parameters[fetchPlanIndex]);
        } else if (jmixDataRepositoryContext != null && jmixDataRepositoryContext.fetchPlan() != null) {
            loadContext.setFetchPlan(jmixDataRepositoryContext.fetchPlan());
        } else {
            loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(metadata.getDomainType(), fetchPlanByAnnotation));
        }
    }

    protected List<Order> getSortFromParams(Object[] parameters) {
        List<Order> orders = new LinkedList<>();

        if (sortIndex != -1) {
            orders.addAll(LoaderHelper.springToJmixSort((org.springframework.data.domain.Sort) parameters[sortIndex])
                    .getOrders());
        }
        if (pageableIndex != -1) {
            orders.addAll(LoaderHelper.springToJmixSort(((Pageable) parameters[pageableIndex]).getSort()).getOrders());
        }
        return orders;
    }


    protected Map<String, Serializable> collectHints(Object[] parameters) {
        Map<String, Serializable> hints = new HashMap<>(queryHints);

        if (jmixContextIndex != -1 && parameters[jmixContextIndex] != null) {
            ((JmixDataRepositoryContext) parameters[jmixContextIndex]).hints().forEach((name, value) ->
                    hints.put(name, LoaderHelper.parseHint(name, value)));
        }
        return hints;
    }

    @Override
    public Object execute(Object[] parameters) {
        LoadContext<?> loadContext = prepareQueryContext(parameters);
        setupFetchPlan(loadContext, parameters);
        loadContext.getQuery().setSort(by(getSortFromParams(parameters)));

        return processAccordingToReturnType(loadContext, parameters);
    }

    protected abstract LoadContext<?> prepareQueryContext(Object[] parameters);

    @Nullable
    protected Object processAccordingToReturnType(LoadContext<?> loadContext, Object[] parameters) {
        Class<?> returnType = method.getReturnType();
        if (Slice.class.isAssignableFrom(returnType)) {
            if (pageableIndex == -1) {
                throw new DevelopmentException(String.format("Pageable parameter should be provided for method returns instance of Slice: %s", formatMethod(method)));
            }

            Pageable pageable = (Pageable) parameters[pageableIndex];

            LoaderHelper.applyPageableForLoadContext(loadContext, pageable);

            if (Page.class.isAssignableFrom(returnType)) {
                long count = dataManager.getCount(loadContext.copy());
                return new PageImpl<>(dataManager.loadList(loadContext), pageable, count);
            } else {

                if (pageable.isPaged())
                    loadContext.getQuery().setMaxResults(pageable.getPageSize() + 1);// have to load additional one to know whether next results present

                List<?> results = dataManager.loadList(loadContext);
                boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();

                return new SliceImpl(hasNext ? results.subList(0, pageable.getPageSize()) : results, pageable, hasNext);
            }
        }

        List<?> result = dataManager.loadList(loadContext);

        if (returnType.isAssignableFrom(metadata.getDomainType())
                || Optional.class.isAssignableFrom(returnType)) {
            if (result.size() > 1) {
                throw new IncorrectResultSizeDataAccessException(1, result.size());
            }
            return result.size() == 1 ? result.iterator().next() : null;
        }

        if (Iterator.class.isAssignableFrom(returnType)) {
            return result.iterator();
        }

        return result;
    }

    protected static String formatMethod(Method method) {
        return method.getDeclaringClass().getName() + '#' + method.getName();
    }

    @Override
    public String toString() {
        return String.format("%s:{%s}", this.getClass().getSimpleName(), getQueryDescription());
    }

    protected String getQueryDescription() {
        return String.format("fetchPlan:'%s'; fetchPlanIndex:'%s'; jmixArgsIndex:'%s'; sortIndex:'%s'; pageableIndex:'%s'", fetchPlanByAnnotation, fetchPlanIndex, jmixContextIndex, sortIndex, pageableIndex);
    }
}
