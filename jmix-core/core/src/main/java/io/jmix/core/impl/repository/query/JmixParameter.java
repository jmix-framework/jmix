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
import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;

/**
 * {@link Parameter} extension required to support {@link io.jmix.core.FetchPlan} special parameter.
 */
public class JmixParameter extends Parameter {

    protected JmixParameter(MethodParameter parameter) {
        super(parameter);
    }

    @Override
    public boolean isSpecialParameter() {
        return super.isSpecialParameter()
                || FetchPlan.class.isAssignableFrom(getType())
                || JmixDataRepositoryContext.class.isAssignableFrom(getType());
    }
}
