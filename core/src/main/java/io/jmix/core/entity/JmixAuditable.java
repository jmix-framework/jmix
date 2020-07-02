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

import javax.annotation.Nullable;

public interface JmixAuditable {

    void setCreatedDate(Object date);

    void setCreatedBy(Object createdBy);

    void setLastModifiedDate(Object date);

    void setLastModifiedBy(Object updatedBy);

    @Nullable
    default Class<?> getCreatedDateClass() {
        return null;
    }

    @Nullable
    default Class<?> getCreatedByClass() {
        return null;
    }

    @Nullable
    default Class<?> getLastModifiedDateClass() {
        return null;
    }

    @Nullable
    default Class<?> getLastModifiedByClass() {
        return null;
    }

}
