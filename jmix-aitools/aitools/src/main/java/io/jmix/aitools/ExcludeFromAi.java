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

package io.jmix.aitools;

import io.jmix.core.entity.annotation.MetaAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Marks an entity or attribute as off-limits to the AI: its data must never reach the model, and a
 * generated query may not read it. On a type the whole entity is excluded; on a field or getter only
 * that attribute is, and the exclusion is inherited by entity subclasses.
 * <p>
 * Unlike the {@code jmix.aitools.dataload.*} include/exclude properties, this is a code-level boundary
 * that cannot be overridden at deployment time &mdash; use it for data whose exposure decision must
 * live in code, such as PII. Prefer it over {@code @Secret} (which hides an attribute everywhere in
 * the framework) and {@code @SystemLevel} (an overridable, discovery-only flag) when the intent is
 * precisely &quot;usable in the application, closed to the AI&quot;.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, METHOD})
@MetaAnnotation
public @interface ExcludeFromAi {
}
