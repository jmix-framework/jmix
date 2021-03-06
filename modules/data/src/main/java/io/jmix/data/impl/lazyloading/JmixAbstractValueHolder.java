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

package io.jmix.data.impl.lazyloading;

import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.indirection.WeavedAttributeValueHolderInterface;

import java.io.Serializable;

public abstract class JmixAbstractValueHolder implements ValueHolderInterface, WeavedAttributeValueHolderInterface,
        Cloneable, Serializable {
    protected volatile boolean isInstantiated;
    protected volatile Object value;

    @Override
    public boolean isCoordinatedWithProperty() {
        return false;
    }

    @Override
    public void setIsCoordinatedWithProperty(boolean coordinated) {

    }

    @Override
    public boolean isNewlyWeavedValueHolder() {
        return false;
    }

    @Override
    public void setIsNewlyWeavedValueHolder(boolean isNew) {

    }

    @Override
    public boolean shouldAllowInstantiationDeferral() {
        return false;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public boolean isInstantiated() {
        return isInstantiated;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }
}
