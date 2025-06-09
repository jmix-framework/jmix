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

import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.springframework.beans.factory.BeanFactory;

public class NonLoadingValueHolder extends AbstractValueHolder {
    public NonLoadingValueHolder(BeanFactory beanFactory,
                                 ValueHolderInterface originalValueHolder,
                                 Object owner, MetaProperty metaProperty) {
        //todo [jmix-framework/jmix#3936] whether to preserve parameters?
        super(beanFactory, originalValueHolder, owner, metaProperty);
    }

    @Override
    public Object getValue() {
        throw new IllegalStateException(
                String.format("Attribute [%s] is not fetched for %s. Enable lazy loading or add this attribute to fetch plan.",
                        getPropertyInfo().getName(), getOwner().getClass().getName()));
    }

    @Override
    protected Object loadValue() {
        throw new RuntimeException("Should not be invoked: lazy loading is disabled.");
    }

    @Override
    protected void afterLoadValue(Object value) {
        throw new RuntimeException("Should not be invoked: lazy loading is disabled.");
    }
}
