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
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.impl.repository.query.utils.JmixQueryLookupStrategy;
import io.jmix.core.impl.repository.support.method_metadata.MethodMetadataHelper;
import io.jmix.core.repository.ApplyConstraints;
import io.jmix.core.repository.FetchPlan;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.RepositoryQuery;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    protected Metadata jmixMetadata;

    protected final Map<String, Integer> namedParametersBindings = new HashMap<>();


    protected int sortIndex;
    protected int pageableIndex;
    protected int fetchPlanIndex;
    protected int jmixContextIndex;

    protected final Map<String, Serializable> queryHints;
    protected final String fetchPlanByAnnotation;

    public JmixAbstractQuery(DataManager dataManager, Metadata jmixMetadata, Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        this.method = method;
        this.metadata = metadata;
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
