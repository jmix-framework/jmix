/*
 * Copyright 2025 Haulmont.
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

package io.jmix.eclipselink.impl.lazyloading;

import io.jmix.core.LoadContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;
import java.util.Objects;

public class ElementCollectionValueHolder extends AbstractValueHolder {

    public ElementCollectionValueHolder(BeanFactory beanFactory, ValueHolderInterface<?> originalValueHolder,
                                        Object owner, MetaProperty metaProperty) {
        super(beanFactory, originalValueHolder, owner, metaProperty);
    }

    @Override
    protected Object loadValue() {
        MetaClass metaClass = getMetadata().getClass(getOwner());

        LoadContext<?> loadContext = createLoadContextByOwner(metaClass);

        Object reloadedOwner = getDataManager().load(loadContext);

        return EntityValues.<Collection<Object>>getValue(reloadedOwner, getPropertyInfo().getName());
    }

    protected LoadContext<?> createLoadContextByOwner(MetaClass metaClass) {
        return new LoadContext<>(metaClass)
                .setId(Objects.requireNonNull(EntityValues.getId(getOwner())))
                .setFetchPlan(
                        getFetchPlans().builder(metaClass.getJavaClass())
                                .add(getPropertyInfo().getName())
                                .build())
                .setAccessConstraints(getLoadOptions().getAccessConstraints())
                .setHints(getLoadOptions().getHintsCopy());
    }

    @Override
    protected void afterLoadValue(Object value) {

    }
}
