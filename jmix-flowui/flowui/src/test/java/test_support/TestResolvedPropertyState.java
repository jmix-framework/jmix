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

import io.jmix.core.EntityEntry;
import io.jmix.core.EntityEntryExtraState;
import io.jmix.core.EntityValuesProvider;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.impl.EntityInternals;
import io.jmix.core.event.AttributeChanges;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Keeps the values of a property that is not in the entity's own metadata, the way the dynamic attributes
 * add-on keeps the values of its {@code +code} properties.
 */
public class TestResolvedPropertyState implements EntityEntryExtraState, EntityValuesProvider {

    public static final String PROPERTY = "+note";

    protected EntityEntry entityEntry;
    protected Map<String, Object> values = new HashMap<>();

    public TestResolvedPropertyState(EntityEntry entityEntry) {
        this.entityEntry = entityEntry;
    }

    public static void addTo(Object entity) {
        EntitySystemAccess.addExtraState(entity,
                new TestResolvedPropertyState(EntitySystemAccess.getEntityEntry(entity)));
    }

    @Override
    public EntityEntry getEntityEntry() {
        return entityEntry;
    }

    @Override
    public void copy(EntityEntryExtraState extraState) {
        if (extraState instanceof TestResolvedPropertyState state) {
            values = new HashMap<>(state.values);
        }
    }

    @Override
    public boolean supportAttribute(String name) {
        return PROPERTY.equals(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttributeValue(String name) {
        return (T) values.get(name);
    }

    @Override
    public void setAttributeValue(String name, @Nullable Object value, boolean checkEquals) {
        Object oldValue = values.get(name);
        if (!checkEquals || !Objects.equals(oldValue, value)) {
            values.put(name, value);
            EntityInternals.fireListeners(entityEntry.getSource(), name, oldValue, value);
        }
    }

    @Override
    public Set<String> getAttributes() {
        return values.keySet();
    }

    @Override
    public Set<AttributeChanges.Change> getChanges() {
        return Set.of();
    }
}
