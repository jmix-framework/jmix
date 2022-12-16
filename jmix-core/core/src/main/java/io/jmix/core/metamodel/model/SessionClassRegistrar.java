/*
 * Copyright 2022 Haulmont.
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

package io.jmix.core.metamodel.model;

/**
 * Implementations of this interface are responsible for registering a {@link MetaClass} in the {@link Session}.
 *
 * The proper implementation is selected by the {@link SessionClassRegistrars}
 */
public interface SessionClassRegistrar {

    boolean supports(Class<? extends Session> sessionClass);

    void registerMetaClass(Session session, MetaClass metaClass);

    void registerMetaClass(Session session, String name, Class javaClass, MetaClass metaClass);

}
