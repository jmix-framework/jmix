/*
 * Copyright 2020 Haulmont.
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

package io.jmix.searchui.entity;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.ui.entity.AbstractSingleFilterCondition;

@JmixEntity(name = "search_FullTextFilterCondition")
@SystemLevel
public class FullTextFilterCondition extends AbstractSingleFilterCondition {

    private static final long serialVersionUID = 6530536119809636937L;

    @JmixProperty
    protected String parameterName;

    @JmixProperty
    protected String searchStrategyName;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getSearchStrategyName() {
        return searchStrategyName;
    }

    public void setSearchStrategyName(String searchStrategyName) {
        this.searchStrategyName = searchStrategyName;
    }
}
