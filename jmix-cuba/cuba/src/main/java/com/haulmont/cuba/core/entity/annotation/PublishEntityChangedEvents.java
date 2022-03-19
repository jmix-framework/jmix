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

package com.haulmont.cuba.core.entity.annotation;

import io.jmix.core.entity.annotation.MetaAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated this annotation is required only for sending legacy {@link com.haulmont.cuba.core.app.events.EntityChangedEvent}.
 * The new {@link io.jmix.core.event.EntityChangedEvent} is sent for any entity without marking it with an annotation.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@MetaAnnotation
public @interface PublishEntityChangedEvents {

    /**
     * Publish event when the entity is created. True by default.
     */
    boolean created() default true;

    /**
     * Publish event when the entity is updated. True by default.
     */
    boolean updated() default true;

    /**
     * Publish event when the entity is deleted. True by default.
     */
    boolean deleted() default true;
}
