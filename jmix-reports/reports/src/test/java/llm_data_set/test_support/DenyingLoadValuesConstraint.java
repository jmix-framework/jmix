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

package llm_data_set.test_support;

import io.jmix.core.constraint.EntityOperationConstraint;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.accesscontext.LoadValuesAccessContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Denies a chosen selected attribute, or the whole entity, of a {@code loadValues} query. Registered with the
 * real {@code AccessManager}, it drives denial through the genuine constraint mechanism, so a test sees what a
 * user with narrower permissions would see rather than what a mock was told to answer.
 */
public class DenyingLoadValuesConstraint implements EntityOperationConstraint<LoadValuesAccessContext> {

    protected final Set<String> deniedSelectedPaths = new HashSet<>();
    protected final Set<String> deniedFilterPaths = new HashSet<>();

    /**
     * Denies reading the selected attribute with this property path, as it appears in the select clause.
     */
    public void denySelectedPath(String propertyPath) {
        deniedSelectedPaths.add(propertyPath);
    }

    /**
     * Denies reading an attribute the query uses outside its select clause. The platform constraint refuses
     * such a query outright — the row set cannot be narrowed to hide it — and this does the same.
     */
    public void denyFilterPath(String propertyPath) {
        deniedFilterPaths.add(propertyPath);
    }

    public void reset() {
        deniedSelectedPaths.clear();
        deniedFilterPaths.clear();
    }

    @Override
    public Class<LoadValuesAccessContext> getContextType() {
        return LoadValuesAccessContext.class;
    }

    @Override
    public void applyTo(LoadValuesAccessContext context) {
        for (MetaPropertyPath propertyPath : context.getAllPropertyPaths()) {
            if (propertyPath != null && deniedFilterPaths.contains(propertyPath.toPathString())) {
                throw new AccessDeniedException("attribute",
                        propertyPath.getMetaClass() + "." + propertyPath.toPathString());
            }
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
