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

package io.jmix.eclipselink.impl.lazyloading;

import com.google.common.collect.ForwardingSet;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.eclipse.persistence.indirection.IndirectSet;
import org.eclipse.persistence.indirection.ValueHolderInterface;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class IndirectSetWrapper<E> extends ForwardingSet<E> implements IndirectCollection, Serializable {
    private static final long serialVersionUID = -4117263695105315477L;

    protected final Set<E> delegate;
    protected final IndirectSet<E> indirectDelegate;

    public IndirectSetWrapper(Set<E> delegate, IndirectSet<E> indirectDelegate) {
        this.delegate = delegate;
        this.indirectDelegate = indirectDelegate;
    }

    @Override
    protected Set<E> delegate() {
        return delegate;
    }

    @Override
    public void clearDeferredChanges() {
        indirectDelegate.clearDeferredChanges();
    }

    @Override
    public boolean hasDeferredChanges() {
        return indirectDelegate.hasDeferredChanges();
    }

    @Override
    public Collection getAddedElements() {
        return indirectDelegate.getAddedElements();
    }

    @Override
    public Collection getRemovedElements() {
        return indirectDelegate.getRemovedElements();
    }

    @Override
    public Object getDelegateObject() {
        return indirectDelegate.getDelegateObject();
    }

    @Override
    public void setUseLazyInstantiation(boolean useLazyInstantiation) {
        indirectDelegate.setUseLazyInstantiation(useLazyInstantiation);
    }

    @Override
    public ValueHolderInterface getValueHolder() {
        return indirectDelegate.getValueHolder();
    }

    @Override
    public boolean isInstantiated() {
        return indirectDelegate.isInstantiated();
    }

    @Override
    public void setValueHolder(ValueHolderInterface valueHolder) {
        indirectDelegate.setValueHolder(valueHolder);
    }

    public static <E> IndirectSetWrapper<E> copyWithNewWrapper(IndirectSetWrapper<E> oldWrapper, Function<Collection<Object>, Collection<Object>> newCollectionWrapFunction) {
        //noinspection unchecked
        return new IndirectSetWrapper<>((Set<E>) newCollectionWrapFunction.apply((Collection<Object>) oldWrapper.indirectDelegate), oldWrapper.indirectDelegate);
    }
}
