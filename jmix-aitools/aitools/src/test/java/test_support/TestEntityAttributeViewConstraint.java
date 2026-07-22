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

import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.constraint.AccessConstraint;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test constraint that denies viewing of explicitly registered entity attributes.
 * <p>
 * Does nothing until an attribute is registered via {@link #denyView(String, String)}, so it is safe
 * to keep in a shared test context. Tests must call {@link #clear()} after use.
 */
public class TestEntityAttributeViewConstraint implements AccessConstraint<EntityAttributeContext> {

    protected final Set<String> deniedViewAttributes = ConcurrentHashMap.newKeySet();

    /**
     * Denies viewing of the given entity attribute.
     *
     * @param entityName    Jmix entity name
     * @param attributeName attribute name
     */
    public void denyView(String entityName, String attributeName) {
        deniedViewAttributes.add(entityName + "." + attributeName);
    }

    /**
     * Removes all registered denials.
     */
    public void clear() {
        deniedViewAttributes.clear();
    }

    @Override
    public Class<EntityAttributeContext> getContextType() {
        return EntityAttributeContext.class;
    }

    @Override
    public void applyTo(EntityAttributeContext context) {
        String attribute = context.getPropertyPath().getMetaClass().getName() + "." + context.getPropertyPath();
        if (deniedViewAttributes.contains(attribute)) {
            context.setViewDenied();
        }
    }
}
