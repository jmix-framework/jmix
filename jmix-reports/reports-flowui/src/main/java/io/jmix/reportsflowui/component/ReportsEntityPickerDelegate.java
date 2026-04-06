/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.component;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.component.delegate.AbstractFieldDelegate;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * INTERNAL! Will be removed in next releases.
 *
 * @param <C> type of component
 * @param <E> type of entity
 */
@Component("flowui_MultiEntityPickerDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportsEntityPickerDelegate<C extends ReportsMultiEntityPicker<E>, E>
        extends AbstractFieldDelegate<C, Collection<E>, Collection<E>> {

    protected MetaClass metaClass;

    public ReportsEntityPickerDelegate(C component) {
        super(component);
    }

    @Nullable
    public MetaClass getMetaClass() {
        ValueSource<Collection<E>> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource<?, ?>) valueSource).getMetaPropertyPath().getMetaProperty();
            return metaProperty.getRange().asClass();
        } else {
            return metaClass;
        }
    }

    public void setMetaClass(@Nullable MetaClass metaClass) {
        ValueSource<Collection<E>> valueSource = getValueSource();
        if (valueSource != null) {
            throw new IllegalStateException(ValueSource.class.getSimpleName() + " is not null");
        }
        this.metaClass = metaClass;
    }

    public void checkValueType(@Nullable E value) {
        if (value == null) {
            return;
        }

        MetaClass metaClass = getMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Neither metaClass nor valueSource is set for " +
                    component.getClass().getSimpleName());
        }

        Class<?> fieldClass = metaClass.getJavaClass();
        Class<?> valueClass = value.getClass();
        if (!fieldClass.isAssignableFrom(valueClass)) {
            throw new IllegalArgumentException(
                    String.format("Could not set value with class %s to field with class %s",
                            valueClass.getCanonicalName(),
                            fieldClass.getCanonicalName())
            );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<Collection<E>> createValueBinding(ValueSource<Collection<E>> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }

    public boolean equalCollections(@Nullable Collection<E> a, @Nullable Collection<E> b) {
        if (CollectionUtils.isEmpty(a) && CollectionUtils.isEmpty(b)) {
            return true;
        }

        if ((CollectionUtils.isEmpty(a) && CollectionUtils.isNotEmpty(b))
                || (CollectionUtils.isNotEmpty(a) && CollectionUtils.isEmpty(b))) {
            return false;
        }

        return CollectionUtils.isEqualCollection(a, b);
    }
}
