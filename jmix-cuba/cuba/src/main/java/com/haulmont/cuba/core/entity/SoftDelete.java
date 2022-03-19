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
package com.haulmont.cuba.core.entity;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.annotation.Internal;

import java.util.Date;

/**
 * Interface to be implemented by entities that support soft deletion.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link DeletedDate} and {@link DeletedBy} annotations
 */
@Deprecated
public interface SoftDelete {

    /**
     * Returns true if the entity is deleted.
     */
    Boolean isDeleted();

    /**
     * Returns deletion timestamp or null if not deleted.
     */
    Date getDeleteTs();

    /**
     * Returns login name of the user who deleted the entity
     * or null if not deleted.
     */
    String getDeletedBy();

    /**
     * INTERNAL. Sets soft deletion timestamp.
     */
    @Internal
    void setDeleteTs(Date deleteTs);

    /**
     * INTERNAL. Sets login name of the user who deleted the entity.
     */
    @Internal
    void setDeletedBy(String deletedBy);
}
