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

package io.jmix.hibernate.impl.lazyloading;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PersistentCollectionSupport {

    public static boolean fillInternalCollection(PersistentCollection original, PersistentCollection loaded) {
        if (loaded instanceof Collection) {
            Collection loadedCollection = (Collection) loaded;
            Collection innerCollection = null;
            if (original instanceof PersistentBag) {
                innerCollection = (List<?>) getField(original, "bag");
            } else if (original instanceof PersistentList) {
                innerCollection = (List<?>) getField(original, "list");
            } else if (original instanceof PersistentSet) {
                innerCollection = (Set<?>) getField(original, "set");
            }
            if (innerCollection != null) {
                innerCollection.addAll(loadedCollection);
                return true;
            }
        }
        return false;
    }

    protected static Object getField(Object collection, String fieldName) {
        Object internal;
        try {
            Field field = ReflectionUtils.findField(collection.getClass(), fieldName);
            if (field == null) {
                throw new RuntimeException(String.format("Unable to access field for collection: %s on PersistentCollection %s",
                        fieldName, collection.getClass().getName()));
            }
            ReflectionUtils.makeAccessible(field);
            internal = field.get(collection);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access");
        }

        return internal;
    }
}
