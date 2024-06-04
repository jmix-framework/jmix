/*
 * Copyright 2022 Haulmont.
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

import io.jmix.core.FetchPlan;
import io.jmix.core.repository.JmixDataRepositoryContext;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersSource;

import java.lang.reflect.Method;
import java.util.List;

/**
 * {@link Parameters} extension required to support {@link io.jmix.core.FetchPlan} and {@link JmixDataRepositoryContext}
 * special parameters.
 */
public class JmixParameters extends Parameters<JmixParameters, JmixParameter> {

    private final int fetchPlanIndex;
    private final int jmixContextIndex;

    public JmixParameters(ParametersSource parametersSource) {
        super(parametersSource, JmixParameter::new);
        fetchPlanIndex = findParameterIndexByType(FetchPlan.class);
        jmixContextIndex = findParameterIndexByType(JmixDataRepositoryContext.class);
    }

    protected int findParameterIndexByType(Class<?> clazz) {
        int fpIndex = -1;
        for (JmixParameter candidate : this) {
            if (clazz.isAssignableFrom(candidate.getType())) {
                fpIndex = candidate.getIndex();
                break;
            }
        }
        return fpIndex;
    }

    private JmixParameters(List<JmixParameter> parameters) {
        super(parameters);
        fetchPlanIndex = findParameterIndexByType(FetchPlan.class);
        jmixContextIndex = findParameterIndexByType(JmixDataRepositoryContext.class);
    }

    @Override
    protected JmixParameters createFrom(List<JmixParameter> parameters) {
        return new JmixParameters(parameters);
    }

    /**
     * @return whether {@link FetchPlan} argument is present in the {@link Method}'s parameter list
     */
    public boolean hasFetchPlanParameter() {
        return fetchPlanIndex != -1;
    }

    /**
     * @return the index of the {@link FetchPlan} {@link Method} parameter if available. Will return {@literal -1} if there is
     * no {@link FetchPlan} argument in the {@link Method}'s parameter list.
     */
    public int getFetchPlanIndex() {
        return fetchPlanIndex;
    }

    /**
     * @return whether {@link JmixDataRepositoryContext} argument is present in the {@link Method}'s parameter list
     */
    public boolean hasJmixContextIndex() {
        return jmixContextIndex != -1;
    }

    /**
     * @return the index of the {@link JmixDataRepositoryContext} {@link Method} parameter if available. Will return {@literal -1} if there is
     * no {@link JmixDataRepositoryContext} argument in the {@link Method}'s parameter list.
     */
    public int getJmixContextIndex() {
        return jmixContextIndex;
    }
}
