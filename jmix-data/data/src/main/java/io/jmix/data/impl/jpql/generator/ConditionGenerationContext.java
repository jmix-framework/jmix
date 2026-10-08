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

import org.jspecify.annotations.Nullable;

import java.util.Collections;
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
    protected String collectionPath;
    protected String collectionAlias;
    protected String collectionFrom;
    protected List<String> valueProperties;
    protected List<String> selectedExpressions;
    protected boolean elementCollection;

    protected Map<Condition, ConditionGenerationContext> childContexts;

    protected IndexCounter counter;

    public ConditionGenerationContext(@Nullable Condition condition) {
        this.condition = condition;
        this.counter = new IndexCounter();
        if (condition instanceof LogicalCondition) {
            this.childContexts = Collections.unmodifiableMap(createChildContexts((LogicalCondition) condition, this));
        } else {
            this.childContexts = Collections.emptyMap();
        }

    }

    private ConditionGenerationContext(@Nullable Condition condition, IndexCounter counter) {
        this.condition = condition;
        this.counter = counter;
        if (condition instanceof LogicalCondition) {
            this.childContexts = Collections.unmodifiableMap(createChildContexts((LogicalCondition) condition, this));
        } else {
            this.childContexts = Collections.emptyMap();
        }
    }

    private Map<Condition, ConditionGenerationContext> createChildContexts(LogicalCondition logicalCondition, ConditionGenerationContext context) {
        Map<Condition, ConditionGenerationContext> createdContexts = new HashMap<>();
        for (Condition childCondition : logicalCondition.getConditions()) {
            ConditionGenerationContext childContext = new ConditionGenerationContext(childCondition, context.counter);
            childContext.copy(context);
            createdContexts.put(childCondition, childContext);
        }
        return createdContexts;
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

    public void setJoinMetaClass(@Nullable MetaClass joinMetaClass) {
        this.joinMetaClass = joinMetaClass;
    }

    /**
     * @return path to the to-many collection property whose condition is generated as an 'exists' subquery
     * (e.g. {@code e.tags}), or null if the condition is not generated as a subquery
     */
    @Nullable
    public String getCollectionPath() {
        return collectionPath;
    }

    public void setCollectionPath(@Nullable String collectionPath) {
        this.collectionPath = collectionPath;
    }

    /**
     * @return identification variable of the 'exists' subquery correlated with the collection property path
     * by a 'member of' condition, or null if the condition is not generated as a subquery
     */
    @Nullable
    public String getCollectionAlias() {
        return collectionAlias;
    }

    public void setCollectionAlias(@Nullable String collectionAlias) {
        this.collectionAlias = collectionAlias;
    }

    /**
     * @return content of the 'from' clause of the 'exists' subquery generated for a condition on a to-many
     * collection property path (e.g. {@code test_Tag cje_0 left join cje_0.category cje_1}), or null if
     * the condition is not generated as a subquery
     */
    @Nullable
    public String getCollectionFrom() {
        return collectionFrom;
    }

    public void setCollectionFrom(@Nullable String collectionFrom) {
        this.collectionFrom = collectionFrom;
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

    public boolean isElementCollection() {
        return elementCollection;
    }

    public void setElementCollection(boolean elementCollection) {
        this.elementCollection = elementCollection;
    }

    /**
     * @return unmodifiable map of child contexts
     */
    public Map<Condition, ConditionGenerationContext> getChildContexts() {
        return childContexts;
    }

    public int generateNextJoinIndex() {
        return counter.getAndIncrement();
    }

    public void copy(ConditionGenerationContext context) {
        setEntityName(context.getEntityName());
        setEntityAlias(context.getEntityAlias());
        setSelectedExpressions(context.getSelectedExpressions());
        setValueProperties(context.getValueProperties());
    }

    public static class IndexCounter {
        private int nextIndex = 0;

        public int getAndIncrement() {
            return nextIndex++;
        }
    }
}
