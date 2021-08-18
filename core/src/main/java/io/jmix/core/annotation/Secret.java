/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.annotation;

import io.jmix.core.entity.annotation.MetaAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Annotation that indicates that annotated element is secret.
 * For example, if the User entity attribute is annotated with @Secret then this attribute won't be returned by the REST API
 * /userInfo endpoint.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
@MetaAnnotation
public @interface Secret {
}
