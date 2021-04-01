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

package io.jmix.data.impl.jpql.generator;

import io.jmix.core.querycondition.Condition;

import javax.annotation.Nullable;
import java.util.List;

public class ConditionGenerationContext {

    protected final Condition condition;
    protected String entityName;
    protected String entityAlias;
    protected List<String> valueProperties;
    protected List<String> selectedExpressions;

    public ConditionGenerationContext(@Nullable Condition condition) {
        this.condition = condition;
    }

    @Nullable
    public Condition getCondition() {
        return condition;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityAlias() {
        return entityAlias;
    }

    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    @Nullable
    public List<String> getValueProperties() {
        return valueProperties;
    }

    public void setValueProperties(@Nullable List<String> valueProperties) {
        this.valueProperties = valueProperties;
    }

    @Nullable
    public List<String> getSelectedExpressions() {
        return selectedExpressions;
    }

    public void setSelectedExpressions(@Nullable List<String> selectedExpressions) {
        this.selectedExpressions = selectedExpressions;
    }

    public void copy(ConditionGenerationContext context) {
        setEntityName(context.getEntityName());
        setEntityAlias(context.getEntityAlias());
        setSelectedExpressions(context.getSelectedExpressions());
        setValueProperties(context.getValueProperties());
    }
}
