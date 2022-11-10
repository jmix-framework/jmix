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
import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.List;
/**
 * {@link Parameters} extension required to support {@link io.jmix.core.FetchPlan} special parameter.
 */
public class JmixParameters extends Parameters<JmixParameters, JmixParameter> {

    private final int fetchPlanIndex;

    public JmixParameters(Method method) {
        super(method);
        fetchPlanIndex = findFetchPlanParameterIndex();
    }

    protected int findFetchPlanParameterIndex() {
        int fpIndex = -1;
        for (JmixParameter candidate : this) {
            if (FetchPlan.class.isAssignableFrom(candidate.getType())) {
                fpIndex = candidate.getIndex();
                break;
            }
        }
        return fpIndex;
    }

    private JmixParameters(List<JmixParameter> parameters) {
        super(parameters);
        fetchPlanIndex = findFetchPlanParameterIndex();
    }

    @Override
    protected JmixParameter createParameter(MethodParameter parameter) {
        return new JmixParameter(parameter);
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
}
