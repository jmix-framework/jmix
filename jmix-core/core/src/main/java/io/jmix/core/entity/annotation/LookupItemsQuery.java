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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures lazy loading of drop-down items for {@link LookupField} with
 * {@link LookupType#DROPDOWN}. Usable only as the {@link LookupField#itemsQuery()} member.
 * <p>
 * Either set an explicit {@link #query()}, or set {@link #byInstanceName()} to match items by
 * query conditions built from the instance-name metadata of the referenced entity. If neither
 * is set, items are loaded eagerly.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface LookupItemsQuery {

    /**
     * If true, drop-down items are matched by query conditions
     * ({@link io.jmix.core.querycondition.Condition}) built from the instance-name metadata of
     * the referenced entity: string-typed instance-name-related attributes are matched with a
     * case-insensitive {@code contains} condition, combined with {@code or} if there are
     * several. Works with any data store capable of loading instances, including
     * non-persistent DTO entities in custom stores. Mutually exclusive with an explicit
     * {@link #query()} — if both are set, the explicit query wins and a warning is logged.
     * {@link #searchStringFormat()} and {@link #escapeValueForLike()} do not apply in this mode:
     * matching is always a case-insensitive substring search with the value escaped for the
     * underlying {@code like}.
     */
    boolean byInstanceName() default false;

    /**
     * JPQL query with a mandatory {@code :searchString} parameter, for example:
     * <pre>select e from Customer e where e.name like :searchString escape '\\' order by e.name</pre>
     * Empty string means "not set". Applies only when {@link #byInstanceName()} is false.
     */
    String query() default "";

    /**
     * Format of the {@code :searchString} parameter value with the {@code ${inputString}}
     * placeholder for the user input, for example {@code "(?i)%${inputString}%"}.
     * Empty string means "not set" (raw user input is used). Applies to the explicit
     * {@link #query()} only; if set together with {@link #byInstanceName()}, it is ignored and
     * a warning is logged.
     */
    String searchStringFormat() default "";

    /**
     * Whether to escape the user input for use in a {@code like} clause. The query must then
     * contain the matching {@code escape '\\'} clause. Applies to the explicit {@link #query()}
     * only; ignored (silently) when {@link #byInstanceName()} is true, where escaping is always
     * on.
     */
    boolean escapeValueForLike() default false;

    /**
     * Name of a fetch plan of the referenced entity to load items with.
     * Empty string means "not set" (default is the loader's default fetch plan with an
     * explicit query; {@code _instance_name} with {@link #byInstanceName()}).
     */
    String fetchPlan() default "";
}
