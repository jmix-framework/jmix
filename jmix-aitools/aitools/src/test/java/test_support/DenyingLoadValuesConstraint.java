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

package test_support;

import io.jmix.core.constraint.EntityOperationConstraint;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.accesscontext.LoadValuesAccessContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Test {@link EntityOperationConstraint} that denies access to chosen selected attributes (or the
 * whole entity) of a {@code loadValues} query. Registered with the real {@code AccessManager}, it lets
 * tests drive attribute-level denial through the genuine constraint mechanism instead of mocking it.
 * <p>
 * Configure the target denial via {@link #denySelectedPath} / {@link #denyEntity} before running the
 * query, and call {@link #reset} between tests.
 */
public class DenyingLoadValuesConstraint implements EntityOperationConstraint<LoadValuesAccessContext> {

    private final Set<String> deniedSelectedPaths = new HashSet<>();
    private boolean entityDenied;

    /**
     * Denies read access to the selected attribute with the given property path (e.g. {@code "name"}).
     *
     * @param propertyPath property path of the attribute to deny, as it appears in the select clause
     */
    public void denySelectedPath(String propertyPath) {
        deniedSelectedPaths.add(propertyPath);
    }

    /**
     * Denies read access to the queried entity as a whole.
     */
    public void denyEntity() {
        entityDenied = true;
    }

    /**
     * Clears all configured denials.
     */
    public void reset() {
        deniedSelectedPaths.clear();
        entityDenied = false;
    }

    @Override
    public Class<LoadValuesAccessContext> getContextType() {
        return LoadValuesAccessContext.class;
    }

    @Override
    public void applyTo(LoadValuesAccessContext context) {
        if (entityDenied) {
            context.setDenied();
            return;
        }

        for (MetaPropertyPath propertyPath : context.getSelectedPropertyPaths()) {
            if (propertyPath != null && deniedSelectedPaths.contains(propertyPath.toPathString())) {
                for (Integer index : context.getSelectedIndexes(propertyPath)) {
                    context.addDeniedSelectedIndex(index);
                }
            }
        }
    }
}
