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

package com.haulmont.chile.core.model;

import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;

/**
 * Legacy meta-model entry point.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.metamodel.model.Session}.
 */
@Deprecated
public interface Session extends io.jmix.core.metamodel.model.Session {

    @Nullable
    @Override
    MetaClass getClass(String name);

    MetaClass getClassNN(String name);

    @Nullable
    @Override
    MetaClass getClass(Class<?> javaClass);

    MetaClass getClassNN(Class<?> javaClass);
}
