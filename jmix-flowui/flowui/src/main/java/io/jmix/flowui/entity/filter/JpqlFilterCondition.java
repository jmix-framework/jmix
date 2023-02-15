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

package io.jmix.flowui.entity.filter;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity(name = "flowui_JpqlFilterCondition")
@SystemLevel
public class JpqlFilterCondition extends AbstractSingleFilterCondition {

    protected String parameterName;

    protected String parameterClass;

    protected String where;

    protected String join;

    protected Boolean hasInExpression = false;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterClass() {
        return parameterClass;
    }

    public void setParameterClass(String parameterClass) {
        this.parameterClass = parameterClass;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public Boolean getHasInExpression() {
        return hasInExpression;
    }

    public void setHasInExpression(Boolean hasInExpression) {
        this.hasInExpression = hasInExpression;
    }
}
