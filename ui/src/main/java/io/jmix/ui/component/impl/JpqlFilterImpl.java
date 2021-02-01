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

package io.jmix.ui.component.impl;

import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.datatype.impl.EnumerationImpl;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.JpqlFilter;
import io.jmix.ui.component.jpqlfilter.JpqlFilterSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class JpqlFilterImpl<V> extends AbstractSingleFilterComponent<V> implements JpqlFilter<V> {

    protected static final String JPQL_FILTER_STYLENAME = "jmix-jpql-filter";

    protected JpqlFilterSupport jpqlFilterSupport;

    protected String parameterName;
    protected Class parameterClass;
    protected String where;
    protected String join;

    @Autowired
    public void setJpqlFilterSupport(JpqlFilterSupport jpqlFilterSupport) {
        this.jpqlFilterSupport = jpqlFilterSupport;
    }

    @Override
    protected void initRootComponent(HBoxLayout root) {
        super.initRootComponent(root);

        root.unwrap(com.vaadin.ui.Component.class)
                .setPrimaryStyleName(JPQL_FILTER_STYLENAME);
    }

    @Override
    protected JpqlCondition createQueryCondition() {
        return new JpqlCondition();
    }

    @Override
    public String getInnerComponentPrefix() {
        return jpqlFilterSupport.getJpqlFilterPrefix(getId());
    }

    @Override
    public JpqlCondition getQueryCondition() {
        return (JpqlCondition) queryCondition;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void updateQueryCondition(@Nullable V newValue) {
        if (parameterName == null) {
            return;
        }

        Object parameterValue = null;
        if (newValue != null) {
            if (EntityValues.isEntity(newValue)) {
                parameterValue = EntityValues.getIdOrEntity(newValue);
            } else if (Enumeration.class.isAssignableFrom(parameterClass)) {
                Enumeration<?> enumeration = new EnumerationImpl<>(parameterClass);
                parameterValue = enumeration.format(newValue);
            } else {
                parameterValue = newValue;
            }
        }

        getQueryCondition().setParameterValuesMap(Collections.singletonMap(parameterName, parameterValue));
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

    @Override
    public Class getParameterClass() {
        return parameterClass;
    }

    @Override
    public void setParameterClass(Class parameterClass) {
        checkState(this.parameterClass == null, "Parameter class has already been initialized");
        checkNotNullArgument(parameterClass);

        this.parameterClass = parameterClass;
    }

    @Override
    public String getWhere() {
        return where;
    }

    @Nullable
    @Override
    public String getJoin() {
        return join;
    }

    @Override
    public void setCondition(String where, @Nullable String join) {
        checkNotNullArgument(where);

        if (StringUtils.isNotEmpty(parameterName)) {
            getQueryCondition().setWhere(where.replace("?", ":" + parameterName));
        }

        this.where = where;
        this.join = join;
    }
}
