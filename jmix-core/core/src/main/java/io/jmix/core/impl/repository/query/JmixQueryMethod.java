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

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersSource;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

/**
 *
 * {@link QueryMethod} extension required to support {@link io.jmix.core.FetchPlan} special parameter.
 */
public class JmixQueryMethod extends QueryMethod {

    public JmixQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
    }

    @Override//todo taimanov: do not remove until it is required by single available constructor of QueryMethod
    protected JmixParameters createParameters(Method method) {
        return new JmixParameters(ParametersSource.of(method));
    }

    @Override
    protected Parameters<?, ?> createParameters(ParametersSource parametersSource) {
        return new JmixParameters(parametersSource);
    }
}
