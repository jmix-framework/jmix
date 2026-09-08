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
 * Declares that an entity or an attribute must never be exposed to the AI: its data may not reach the
 * model, and a generated query may not read it.
 * <p>
 * This is a code-level trust boundary. Unlike the {@code jmix.aitools.dataload.*} include/exclude
 * properties, it cannot be undone at deployment time: an annotated element stays hidden regardless of
 * property overrides, and no force-include rule can bring it back. Use it for data whose exposure
 * decision must survive code review rather than live in overridable configuration &mdash; typically
 * information that is a normal, usable attribute in the application (personally identifiable
 * information, for example) yet must stay out of the model.
 * <p>
 * On a <b>type</b> the entity disappears from domain-model discovery and, having left the introspected
 * index, becomes unqueryable. On a <b>field or getter</b> the single attribute is hidden the same way
 * while the entity itself stays available. Being a {@link MetaAnnotation}, it propagates to entity
 * subclasses and can be overridden through {@code metadata.xml}.
 * <p>
 * Enforced today by the data-load subsystem &mdash; the only AI feature that reads the domain model;
 * any future feature that reads the domain model is expected to honour it too. Prefer it over
 * {@code @Secret} (which hides an attribute everywhere in the framework, not just from the AI) and
 * over {@code @SystemLevel} (which only declutters discovery under an overridable flag) when the
 * intent is precisely &quot;usable in the application, closed to the AI&quot;.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, METHOD})
@MetaAnnotation
public @interface ExcludeFromAi {
}
