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

package io.jmix.flowui.component.jpqlfilter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.flowui.component.filer.SingleFilterComponentBase;
import io.jmix.flowui.model.DataLoader;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * JpqlFilter is a UI component used for filtering entities returned by the {@link DataLoader}.
 * The component contains JPQL expressions that will be added to the 'from' and 'where' data
 * loader query sections. The component can automatically render proper layout for setting a
 * condition value. In general case a JpqlFilter layout contains a label and a field for
 * editing a condition value.
 *
 * @param <V> value type
 */
public class JpqlFilter<V> extends SingleFilterComponentBase<V> {

    protected static final String JPQL_FILTER_CLASS_NAME = "jmix-jpql-filter";

    protected JpqlFilterSupport jpqlFilterSupport;

    protected String parameterName;
    protected Class<?> parameterClass;
    protected String where;
    protected String join;
    protected boolean hasInExpression;

    @Override
    protected void autowireDependencies() {
        super.autowireDependencies();

        jpqlFilterSupport = applicationContext.getBean(JpqlFilterSupport.class);
    }

    @Override
    protected void initRootComponent(HorizontalLayout root) {
        super.initRootComponent(root);
        root.setClassName(JPQL_FILTER_CLASS_NAME);
    }

    @Override
    protected JpqlCondition createQueryCondition() {
        return new JpqlCondition();
    }

    @Override
    public String getInnerComponentPrefix() {
        return jpqlFilterSupport.getJpqlFilterPrefix(getId());
    }

    /**
     * @return a {@link JpqlCondition} related to the current JpqlFilter
     */
    @Override
    public JpqlCondition getQueryCondition() {
        return (JpqlCondition) queryCondition;
    }

    @Override
    protected void updateQueryCondition(@Nullable V newValue) {
        if (parameterName == null) {
            return;
        }

        if (parameterClass == Void.class) {
            if (Boolean.TRUE.equals(newValue)) {
                getQueryCondition().setWhere(where);
                getQueryCondition().setJoin(join);
            } else {
                resetQueryCondition();
            }

            if (!getQueryCondition().getParameterValuesMap().isEmpty()) {
                getQueryCondition().setParameterValuesMap(Collections.emptyMap());
            }
        } else {
            Object parameterValue = null;
            if (newValue != null) {
                if (EntityValues.isEntity(newValue)) {
                    parameterValue = EntityValues.getIdOrEntity(newValue);
                } else {
                    parameterValue = newValue;
                }
            }

            getQueryCondition().setParameterValuesMap(Collections.singletonMap(parameterName, parameterValue));
        }
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public void setParameterName(String parameterName) {
        checkState(this.parameterName == null, "Parameter name has already been initialized");
        checkNotNullArgument(parameterName);

        if (StringUtils.isNotEmpty(where)) {
            getQueryCondition().setWhere(where.replace("?", ":" + parameterName));
        }

        this.parameterName = parameterName;
    }

    /**
     * @return a Java class of the associated query parameter
     */
    public Class<?> getParameterClass() {
        return parameterClass;
    }

    /**
     * Sets a Java class of the associated query parameter.
     *
     * @param parameterClass a Java class of the associated query parameter
     */
    public void setParameterClass(Class<?> parameterClass) {
        checkState(this.parameterClass == null, "Parameter class has already been initialized");
        checkNotNullArgument(parameterClass);

        if (parameterClass == Void.class) {
            resetQueryCondition();
        }

        this.parameterClass = parameterClass;
    }

    /**
     * Returns a JPQL expression which will be added to the 'where' data loader query section.
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
    public String getWhere() {
        return Strings.nullToEmpty(where);
    }

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
    public String getJoin() {
        return join;
    }

    /**
     * Sets JPQL expressions which will be added to the data loader query 'from' and 'where'
     * sections.
     *
     * @param where a JPQL expression which will be added to the 'where' data loader query section
     * @param join  a JPQL expression which will be added to the 'from' data loader query section
     * @see #getJoin()
     * @see #getWhere()
     */
    public void setCondition(String where, @Nullable String join) {
        checkNotNullArgument(where);

        if (parameterClass != Void.class) {
            if (StringUtils.isNotEmpty(parameterName)) {
                getQueryCondition().setWhere(where.replace("?", ":" + parameterName));
            }

            getQueryCondition().setJoin(join);
        }

        this.where = where;
        this.join = join;
    }

    /**
     * @return whether the query condition has an IN expression and the value is a collection
     */
    public boolean hasInExpression() {
        return hasInExpression;
    }

    /**
     * Sets whether the query condition has an IN expression and the value is a collection.
     *
     * @param hasInExpression whether the query condition has an IN expression
     */
    public void setHasInExpression(boolean hasInExpression) {
        this.hasInExpression = hasInExpression;
    }

    protected void resetQueryCondition() {
        getQueryCondition().setWhere("");
        getQueryCondition().setJoin("");
    }
}
