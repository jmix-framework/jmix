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

package io.jmix.ui.component;

import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;

import javax.annotation.Nullable;

/**
 * JpqlFilter is a UI component used for filtering entities returned by the {@link DataLoader}.
 * The component contains JPQL expressions that will be added to the 'from' and 'where' data
 * loader query sections. The component can automatically render proper layout for setting a
 * condition value. In general case a JpqlFilter layout contains a label with caption and
 * a field for editing a condition value.
 *
 * @param <V> value type
 */
@StudioElement(
        caption = "JPQLFilter",
        xmlElement = "jpqlFilter",
        defaultProperty = "parameterClass",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/filter-components.html#jpql-filter",
        unsupportedProperties = {"dataLoader", "captionWidth", "autoApply", "captionPosition", "captionVisible"}
)
public interface JpqlFilter<V> extends SingleFilterComponent<V> {

    String NAME = "jpqlFilter";

    /**
     * @return a {@link JpqlCondition} related to the current JpqlFilter
     */
    @Override
    JpqlCondition getQueryCondition();

    /**
     * @return a Java class of the associated query parameter
     */
    Class getParameterClass();

    /**
     * Sets a Java class of the associated query parameter.
     *
     * @param parameterClass a Java class of the associated query parameter
     */
    @StudioProperty(type = PropertyType.JAVA_CLASS_NAME, required = true, typeParameter = "V")
    void setParameterClass(Class parameterClass);

    /**
     * Returns a a JPQL expression which will be added to the 'where' data loader query section.
     * <p>
     * The <code>{E}</code> placeholder should be used in the expression instead of the alias
     * of the entity being selected. The condition can only have one parameter denoted by
     * <code>?</code> if used.
     * <p>
     * Example of selecting Car entities by an attribute of the joined Repair collection:
     * <pre>
     * r.description like ?
     * </pre>
     *
     * @return a JPQL expression which will be added to the 'where' data loader query section
     * @see #setCondition(String, String)
     */
    String getWhere();

    /**
     * Returns a JPQL expression which will be added to the 'from' data loader query section.
     * <p>
     * This can be required to create a complex condition based on an attribute of a related
     * collection. The expression should be started with <code>join</code> or
     * <code>left join</code> statements.
     * <p>
     * The <code>{E}</code> placeholder should be used in the expression instead of the alias
     * of the entity being selected.
     * <p>
     * Example of joining the Repair collection when selecting Car entities:
     * <pre>
     * join {E}.repairs r
     * </pre>
     *
     * @return a JPQL expression which will be added to the 'from' data loader query section
     * @see #setCondition(String, String)
     */
    @Nullable
    String getJoin();

    /**
     * Sets JPQL expressions which will be added to the data loader query 'from' and 'where'
     * sections.
     *
     * @param where a JPQL expression which will be added to the 'where' data loader query section
     * @param join  a JPQL expression which will be added to the 'from' data loader query section
     * @see #getJoin()
     * @see #getWhere()
     */
    void setCondition(String where, @Nullable String join);

    /**
     * Sets whether the query condition has an IN expression and the value is a collection.
     *
     * @param hasInExpression whether the query condition has an IN expression
     */
    @StudioProperty(defaultValue = "false")
    void setHasInExpression(boolean hasInExpression);

    /**
     * @return whether the query condition has an IN expression and the value is a collection
     */
    boolean hasInExpression();
}
