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

package io.jmix.reports.llm;

import io.jmix.core.annotation.Internal;

import java.util.regex.Pattern;

/**
 * Names under which an LLM data query may reference the values around it.
 * <p>
 * The rules live here because two sides must agree on them: the report designer tells query generation which
 * names exist, and the loader binds values under the very same names at run time. A name invented on one side
 * and not recognised on the other would fail the report instead of filtering a band.
 */
@Internal
public final class LlmQueryParameterNames {

    /**
     * A JPQL parameter name is an identifier, so a report parameter or a band field whose name is not one cannot
     * be referenced by a query at all.
     */
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private LlmQueryParameterNames() {
    }

    /**
     * Returns the name a field of a parent band row is referenced by. SQL and JPQL data sets use
     * {@code ${Band.field}}, but a JPQL parameter name cannot contain a dot, so the name is flattened.
     *
     * @param bandName  name of the band the field belongs to
     * @param fieldName name of the field within that band's row
     * @return the parameter name, which still has to be checked with {@link #isValid(String)}
     */
    public static String ofBandField(String bandName, String fieldName) {
        return bandName + "_" + fieldName;
    }

    /**
     * @param name candidate parameter name
     * @return {@code true} if a generated query can reference this name
     */
    public static boolean isValid(String name) {
        return VALID_NAME.matcher(name).matches();
    }
}
