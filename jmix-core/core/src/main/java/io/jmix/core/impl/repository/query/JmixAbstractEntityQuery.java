/*
 * Copyright 2025 Haulmont.
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
import io.jmix.core.repository.JmixDataRepositoryContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.List;

import static io.jmix.core.Sort.by;

public abstract class JmixAbstractEntityQuery extends JmixAbstractQuery<LoadContext<?>> {

    protected final String fetchPlanByAnnotation;
    protected FetchPlanRepository fetchPlanRepository;
    protected int fetchPlanIndex;
    protected List<QueryStringProcessor> queryStringProcessors;

    public JmixAbstractEntityQuery(DataManager dataManager,
                                   Metadata jmixMetadata,
                                   FetchPlanRepository fetchPlanRepository,
                                   List<QueryStringProcessor> queryStringProcessors,
                                   Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(dataManager, jmixMetadata, method, metadata, factory);

        this.fetchPlanRepository = fetchPlanRepository;
        io.jmix.core.repository.FetchPlan fetchPlanAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, io.jmix.core.repository.FetchPlan.class);
        fetchPlanByAnnotation = fetchPlanAnnotation != null ? fetchPlanAnnotation.value() : io.jmix.core.FetchPlan.BASE;

        this.queryStringProcessors = queryStringProcessors;
    }

    protected void setupFetchPlan(LoadContext<?> loadContext, Object[] parameters) {
        JmixDataRepositoryContext jmixDataRepositoryContext = jmixContextIndex != -1 ? (JmixDataRepositoryContext) parameters[jmixContextIndex] : null;
        if (fetchPlanIndex != -1 && parameters[fetchPlanIndex] != null) {
            loadContext.setFetchPlan((FetchPlan) parameters[fetchPlanIndex]);
        } else if (jmixDataRepositoryContext != null && jmixDataRepositoryContext.fetchPlan() != null) {
            loadContext.setFetchPlan(jmixDataRepositoryContext.fetchPlan());
        } else {
            loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(metadata.getDomainType(), fetchPlanByAnnotation));
        }
    }

    @Override
    protected void processSpecialParameters() {
        super.processSpecialParameters();
        JmixParameters parameters = (JmixParameters) queryMethod.getParameters();
        fetchPlanIndex = parameters.getFetchPlanIndex();
    }

    @Override
    @Nullable
    public Object execute(Object[] parameters) {
        LoadContext<?> loadContext = prepareQueryContext(parameters);
        setupFetchPlan(loadContext, parameters);
        loadContext.getQuery().setSort(by(getSortFromParams(parameters)));

        return processAccordingToReturnType(loadContext, parameters);
    }

    @Override
    protected String getQueryDescription() {
        return String.format("fetchPlan:'%s'; fetchPlanIndex:'%s'; ", fetchPlanByAnnotation, fetchPlanIndex)
                + super.getQueryDescription();
    }
}
