/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.data.impl;

import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.event.AttributeChanges;
import io.jmix.data.event.EntityChangedEvent;

public class EntityChangedEventInfo {
    private final Object source;
    private final JmixEntity entity;
    private EntityChangedEvent.Type type;
    private final AttributeChanges changes;
    private MetaClass originalMetaClass;

    public EntityChangedEventInfo(Object source,
                                  JmixEntity entity,
                                  EntityChangedEvent.Type type,
                                  AttributeChanges changes,
                                  MetaClass originalMetaClass) {
        this.source = source;
        this.entity = entity;
        this.type = type;
        this.changes = changes;
        this.originalMetaClass = originalMetaClass;
    }

    public Object getSource() {
        return source;
    }

    public JmixEntity getEntity() {
        return entity;
    }

    public MetaClass getOriginalMetaClass() {
        return originalMetaClass;
    }

    public EntityChangedEvent.Type getType() {
        return type;
    }

    public AttributeChanges getChanges() {
        return changes;
    }
}
