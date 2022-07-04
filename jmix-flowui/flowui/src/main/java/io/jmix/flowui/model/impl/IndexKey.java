/*
 * Copyright 2019 Haulmont.
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

package io.jmix.flowui.model.impl;

import io.jmix.core.entity.EntityValues;

public class IndexKey {

    private Object key;

    private IndexKey() {
    }

    /**
     * Creates IndexKey by entity instance or its id, if the id is not null.
     */
    public static IndexKey ofEntity(Object entity) {
        IndexKey indexKey = new IndexKey();
        indexKey.key = EntityValues.getId(entity) != null ? EntityValues.getId(entity) : entity;
        return indexKey;
    }

    /**
     * Creates IndexKey by object instance (it can be an entity or its id).
     */
    public static IndexKey of(Object id) {
        IndexKey indexKey = new IndexKey();
        indexKey.key = id;
        return indexKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKey indexKey = (IndexKey) o;
        return key.equals(indexKey.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "IndexKey{" +
                "key=" + key +
                '}';
    }
}
