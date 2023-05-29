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

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

/**
 * Interface to be implemented by entities that contain information about who updated them and when.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link LastModifiedBy} and {@link LastModifiedDate} on fields.
 */
@Deprecated
public interface Updatable {

    Date getUpdateTs();

    void setUpdateTs(Date updateTs);

    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);
}
