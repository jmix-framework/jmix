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

package com.haulmont.cuba.core.global;

import com.haulmont.cuba.core.entity.HasUuid;
import com.haulmont.cuba.core.entity.SoftDelete;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class CubaMetadataTools extends MetadataTools {

    @Override
    @Nullable
    public String findDeletedDateProperty(Class<?> clazz) {
        if (SoftDelete.class.isAssignableFrom(clazz)) {
            return "deleteTs";
        }

        return super.findDeletedDateProperty(clazz);
    }

    @Override
    @Nullable
    public String findDeletedByProperty(Class<?> clazz) {
        if (SoftDelete.class.isAssignableFrom(clazz)) {
            return "deletedBy";
        }

        return super.findDeletedByProperty(clazz);
    }

    @Override
    public List<String> getSoftDeleteProperties(Class<?> clazz) {
        if (SoftDelete.class.isAssignableFrom(clazz)) {
            return Arrays.asList("deleteTs", "deletedBy");
        }

        return super.getSoftDeleteProperties(clazz);
    }

    @Override
    public boolean isSoftDeletable(Class<?> entityClass) {
        return SoftDelete.class.isAssignableFrom(entityClass) || super.isSoftDeletable(entityClass);
    }

    @Override
    public boolean hasUuid(MetaClass metaClass) {
        if (HasUuid.class.isAssignableFrom(metaClass.getJavaClass())) {
            return true;
        }
        return super.hasUuid(metaClass);
    }

    @Override
    public String getUuidPropertyName(Class<?> clazz) {
        if (HasUuid.class.isAssignableFrom(clazz)) {
            return "uuid";
        }
        return super.getUuidPropertyName(clazz);
    }
}
