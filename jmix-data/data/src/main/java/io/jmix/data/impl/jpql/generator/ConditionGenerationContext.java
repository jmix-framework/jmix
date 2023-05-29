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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;

import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionGenerationContext {

    protected final Condition condition;
    protected String entityName;
    protected String entityAlias;
    protected String joinAlias;
    protected String joinProperty;
    protected MetaClass joinMetaClass;
    protected List<String> valueProperties;
    protected List<String> selectedExpressions;

    protected Map<Condition, ConditionGenerationContext> childContexts = new HashMap<>();

    public ConditionGenerationContext(@Nullable Condition condition) {
        this.condition = condition;
        if (condition instanceof LogicalCondition) {
            createChildContexts((LogicalCondition) condition, this);
        }
    }

    private void createChildContexts(LogicalCondition logicalCondition, ConditionGenerationContext context) {
        for (Condition childCondition : logicalCondition.getConditions()) {
            ConditionGenerationContext childContext = new ConditionGenerationContext(childCondition);
            childContext.copy(context);
            getChildContexts().put(childCondition, childContext);
            if (childCondition instanceof LogicalCondition){
                createChildContexts((LogicalCondition) childCondition,childContext);
            }
        }
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
    public String getJoinAlias() {
        return joinAlias;
    }

    public void setJoinAlias(String joinAlias) {
        this.joinAlias = joinAlias;
    }

    @Nullable
    public String getJoinProperty() {
        return joinProperty;
    }

    public void setJoinProperty(String joinProperty) {
        this.joinProperty = joinProperty;
    }

    @Nullable
    public MetaClass getJoinMetaClass() {
        return joinMetaClass;
    }

    public void setJoinMetaClass(MetaClass joinMetaClass) {
        this.joinMetaClass = joinMetaClass;
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

    public Map<Condition, ConditionGenerationContext> getChildContexts() {
        return childContexts;
    }

    public void copy(ConditionGenerationContext context) {
        setEntityName(context.getEntityName());
        setEntityAlias(context.getEntityAlias());
        setSelectedExpressions(context.getSelectedExpressions());
        setValueProperties(context.getValueProperties());
    }
}
