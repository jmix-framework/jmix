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

package com.haulmont.chile.core.model.impl;

import com.haulmont.chile.core.model.Session;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.SessionImplementation;

import javax.annotation.Nullable;
import java.util.Collection;

public class CubaSession implements Session, SessionImplementation {

    private io.jmix.core.metamodel.model.SessionImplementation delegate;

    public CubaSession(io.jmix.core.metamodel.model.Session delegate) {
        if (!(delegate instanceof SessionImplementation)) {
            throw new IllegalStateException("The session delegate of CubaSession must implement the SessionImplementation interface");
        }
        this.delegate = (SessionImplementation) delegate;
    }

    @Nullable
    @Override
    public MetaClass findClass(String name) {
        return delegate.findClass(name);
    }

    @Nullable
    @Override
    public MetaClass getClass(String name) {
        return delegate.findClass(name);
    }

    @Override
    public MetaClass getClassNN(String name) {
        return delegate.getClass(name);
    }

    @Nullable
    @Override
    public MetaClass findClass(Class<?> javaClass) {
        return delegate.findClass(javaClass);
    }

    @Override
    public MetaClass getClass(Class<?> javaClass) {
        return delegate.getClass(javaClass);
    }

    @Override
    public MetaClass getClassNN(Class<?> javaClass) {
        return delegate.getClass(javaClass);
    }

    @Override
    public Collection<MetaClass> getClasses() {
        return delegate.getClasses();
    }

    @Override
    public void registerClass(MetaClass metaClass) {
        delegate.registerClass(metaClass);
    }

    @Override
    public void registerClass(String name, Class javaClass, MetaClass metaClass) {
        delegate.registerClass(name, javaClass, metaClass);
    }
}
