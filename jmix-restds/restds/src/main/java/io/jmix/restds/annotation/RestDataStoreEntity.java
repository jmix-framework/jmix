/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.annotation;

import io.jmix.core.entity.annotation.MetaAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the entity is stored using {@link io.jmix.restds.impl.RestDataStore} and defines relevant parameters.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MetaAnnotation
public @interface RestDataStoreEntity {

    /**
     * Defines the name of the remote entity. Required if the remote entity name is different from the local one.
     * <p>
     * For example, the local entity is {@code CustomerDto} while the remote entity is {@code Customer}.
     */
    String remoteName();
}
