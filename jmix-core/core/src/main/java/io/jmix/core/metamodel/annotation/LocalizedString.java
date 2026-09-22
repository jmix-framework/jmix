/*
 * Copyright 2026 Haulmont.
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

package io.jmix.core.metamodel.annotation;

import io.jmix.core.entity.annotation.MetaAnnotation;
import io.jmix.core.metamodel.datatype.impl.LocalizedStringDatatype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code String} entity attribute as a localized string: a default value followed by
 * {@code locale=value} lines, or a {@code msg://} reference. The attribute receives the
 * {@code localizedString} datatype, which shows the text of the current user's locale wherever the
 * framework formats attribute values.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MetaAnnotation
@PropertyDatatype(LocalizedStringDatatype.ID)
public @interface LocalizedString {
}
