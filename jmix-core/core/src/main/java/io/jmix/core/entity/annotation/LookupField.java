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

package io.jmix.core.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines which UI component is used for selecting references to an entity.
 * <p>
 * On an entity class, applies whenever a reference to this entity is selected in generated UI
 * (form fields, property filters, etc.). Propagates to subclasses. On a reference attribute,
 * applies to this attribute only and takes precedence over both the class-level annotation on
 * the referenced entity and the {@code jmix.ui.component.entity-field-fqn} /
 * {@code entity-field-actions} application properties. The application properties in turn take
 * precedence over the class-level annotation.
 * <p>
 * With {@link LookupType#DROPDOWN} and no {@link #itemsQuery()}, all instances are loaded
 * eagerly — suitable for small dictionary entities only. For larger tables configure lazy
 * loading via {@link #itemsQuery()}. Items are loaded through {@code DataManager}, so
 * row-level security constraints apply.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@MetaAnnotation
public @interface LookupField {

    LookupType type();

    /**
     * Optional list of picker action ids registered in the UI module's actions registry,
     * for example:
     * <pre>@LookupField(type = LookupType.DROPDOWN, actions = {"entity_lookup", "entity_open"})</pre>
     */
    String[] actions() default {};

    /**
     * Optional query configuration for lazy loading of {@link LookupType#DROPDOWN} items.
     * Ignored (with a warning in the log) for {@link LookupType#VIEW}.
     */
    LookupItemsQuery itemsQuery() default @LookupItemsQuery;
}
