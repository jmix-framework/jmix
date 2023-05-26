/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.AbstractField;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.component.SupportsMetaClass;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("flowui_EntityCollectionFieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityCollectionFieldDelegate<C extends AbstractField<?, Set<T>> & SupportsMetaClass, T, V>
        extends AbstractFieldDelegate<C, Collection<V>, Set<T>> {

    protected MetaClass metaClass;

    public EntityCollectionFieldDelegate(C component) {
        super(component);
    }

    @Nullable
    public MetaClass getMetaClass() {
        ValueSource<Collection<V>> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource<?, ?>) valueSource).getMetaPropertyPath().getMetaProperty();
            return metaProperty.getRange().asClass();
        } else {
            return metaClass;
        }
    }

    public void setMetaClass(@Nullable MetaClass metaClass) {
        ValueSource<Collection<V>> valueSource = getValueSource();
        if (valueSource != null) {
            throw new IllegalStateException(ValueSource.class.getSimpleName() + " is not null");
        }
        this.metaClass = metaClass;
    }

    @Nullable
    public Collection<V> convertToModel(Set<V> presentationValue, @Nullable Stream<V> options) {
        Stream<V> items = options == null ? Stream.empty()
                : options.filter(presentationValue::contains);

        if (getValueSource() != null) {
            Class<Collection<V>> targetType = getValueSource().getType();

            if (List.class.isAssignableFrom(targetType)) {
                return items.collect(Collectors.toList());
            }

            if (Set.class.isAssignableFrom(targetType)) {
                return items.collect(Collectors.toCollection(LinkedHashSet::new));
            }
        }

        return items.collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<V> convertToPresentation(@Nullable Collection<V> modelValue) {
        if (modelValue instanceof List) {
            return new LinkedHashSet<>(modelValue);
        }

        return modelValue == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(modelValue);
    }

    public boolean equalCollections(@Nullable Collection<V> a, @Nullable Collection<V> b) {
        if (CollectionUtils.isEmpty(a) && CollectionUtils.isEmpty(b)) {
            return true;
        }

        if ((CollectionUtils.isEmpty(a) && CollectionUtils.isNotEmpty(b))
                || (CollectionUtils.isNotEmpty(a) && CollectionUtils.isEmpty(b))) {
            return false;
        }

        return CollectionUtils.isEqualCollection(a, b);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<Collection<V>> createValueBinding(ValueSource<Collection<V>> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }
}
