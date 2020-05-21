/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.metamodel.model.impl;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.StoreDescriptor;

public class StoreImpl implements Store {

    private String name;
    private StoreDescriptor descriptor;

    public StoreImpl(String name, StoreDescriptor descriptor) {
        Preconditions.checkNotNullArgument(name, "name is null");
        Preconditions.checkNotNullArgument(descriptor, "descriptor is null");
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StoreDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "StoreImpl{" +
                "name='" + name + '\'' +
                ", descriptor=" + descriptor.getClass().getName() + ":" + descriptor.getBeanName() +
                '}';
    }
}
