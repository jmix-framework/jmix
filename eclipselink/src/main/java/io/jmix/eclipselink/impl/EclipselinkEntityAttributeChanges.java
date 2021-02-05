/*
 * Copyright 2020 Haulmont.
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

package io.jmix.eclipselink.impl;

import io.jmix.data.impl.EntityAttributeChanges;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.descriptors.changetracking.AttributeChangeListener;
import org.eclipse.persistence.sessions.changesets.AggregateChangeRecord;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.ObjectChangeSet;

public class EclipselinkEntityAttributeChanges extends EntityAttributeChanges {
    /**
     * Accumulates changes for the entity. Stores changed attribute names and old values.
     */
    public void addChanges(Object entity) {
        if (!(entity instanceof ChangeTracker))
            return;

        AttributeChangeListener changeListener =
                (AttributeChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener();

        if (changeListener == null)
            return;

        addChanges(changeListener.getObjectChangeSet());
    }

    /**
     * INTERNAL
     */
    public void addChanges(ObjectChangeSet changeSet) {
        if (changeSet == null)
            return;

        for (ChangeRecord changeRecord : changeSet.getChanges()) {
            addChange(changeRecord.getAttribute(), changeRecord.getOldValue());
            if (changeRecord instanceof AggregateChangeRecord) {
                embeddedChanges.computeIfAbsent(changeRecord.getAttribute(), s -> {
                    EclipselinkEntityAttributeChanges embeddedChanges = new EclipselinkEntityAttributeChanges();
                    embeddedChanges.addChanges(((AggregateChangeRecord) changeRecord).getChangedObject());
                    return embeddedChanges;
                });
            }
        }
    }
}
