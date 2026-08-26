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

import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.constraint.EntityOperationConstraint;

/**
 * Denies entity READ the way a resource role without that permission does: through {@code CrudEntityContext}, the
 * context the platform decides entity operations by and the one the loader asks before executing a query.
 */
public class CrudEntityDenial implements EntityOperationConstraint<CrudEntityContext> {

    protected final DenyingLoadValuesConstraint deniedBy;

    public CrudEntityDenial(DenyingLoadValuesConstraint deniedBy) {
        this.deniedBy = deniedBy;
    }

    @Override
    public Class<CrudEntityContext> getContextType() {
        return CrudEntityContext.class;
    }

    @Override
    public void applyTo(CrudEntityContext context) {
        if (deniedBy.getDeniedEntities().contains(context.getEntityClass().getName())) {
            context.setReadDenied();
        }
    }
}
