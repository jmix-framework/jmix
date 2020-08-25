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

package io.jmix.core.entity;

import io.jmix.core.JmixEntity;

public class EntitySystemValues {
    /**
     * @return true if entity has been soft deleted, false - otherwise or if entity doesn't support soft deletion
     */
    public static boolean isSoftDeleted(JmixEntity entity) {
        return entity.__getEntityEntry() instanceof EntityEntrySoftDelete
                && ((EntityEntrySoftDelete) entity.__getEntityEntry()).isDeleted();
    }

    public static boolean isVersionedSupported(JmixEntity entity) {
        return entity.__getEntityEntry() instanceof EntityEntryVersioned;
    }

    public static Object getVersion(JmixEntity entity) {
        if (isVersionedSupported(entity)) {
            ((EntityEntryVersioned) entity.__getEntityEntry()).getVersion();
        }
        return null;
    }

    public static void setVersion(JmixEntity entity, Object version) {
        if (isVersionedSupported(entity)) {
            ((EntityEntryVersioned) entity.__getEntityEntry()).setVersion(version);
        }
    }


}
