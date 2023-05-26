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

package io.jmix.data.impl;

import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.data.impl.converters.AuditConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.Date;

@Component("data_EntityAuditValues")
public class EntityAuditValues {

    private final Logger logger = LoggerFactory.getLogger(EntityAuditValues.class);

    @Autowired
    protected AuditConversionService auditConversionService;

    public void setCreateInfo(Object entity, Date currentDate, @Nullable Object currentUser) {
        Class<?> createdDateClass = EntitySystemAccess.getCreatedDateClass(entity);
        if (createdDateClass != null) {
            if (auditConversionService.canConvert(currentDate.getClass(), createdDateClass)) {
                EntityValues.setCreatedDate(entity, auditConversionService.convert(currentDate, createdDateClass));
            } else {
                logger.warn("Cannot set @CreatedDate for {}. Unsupported field type '{}': no converter found.",
                        entity.getClass().getName(), createdDateClass.getName());
            }
        }

        Class<?> createdByClass = EntitySystemAccess.getCreatedByClass(entity);
        if (createdByClass != null) {
            if (currentUser != null) {
                if (auditConversionService.canConvert(currentUser.getClass(), createdByClass)) {
                    EntityValues.setCreatedBy(entity, auditConversionService.convert(currentUser, createdByClass));
                } else {
                    logger.warn("Cannot set @CreatedBy for {}. Unsupported field type '{}': no converter found.",
                            entity.getClass().getName(), createdByClass.getName());
                }
            } else {
                EntityValues.setCreatedBy(entity, null);
            }
        }
    }

    public void setUpdateInfo(Object entity, Date currentDate, @Nullable Object user, boolean dateOnly) {
        Class<?> lastModifiedDateClass = EntitySystemAccess.getLastModifiedDateClass(entity);
        if (lastModifiedDateClass != null) {
            if (auditConversionService.canConvert(currentDate.getClass(), lastModifiedDateClass)) {
                EntityValues.setLastModifiedDate(entity, auditConversionService.convert(currentDate, lastModifiedDateClass));
            } else {
                logger.warn("Cannot set @LastModifiedDate for {}. Unsupported field type '{}': no converter found.",
                        entity.getClass().getName(), lastModifiedDateClass.getName());
            }
        }

        Class<?> lastModifiedByClass = EntitySystemAccess.getLastModifiedByClass(entity);
        if (lastModifiedByClass != null && !dateOnly) {
            if (user != null) {
                if (auditConversionService.canConvert(user.getClass(), lastModifiedByClass)) {
                    EntityValues.setLastModifiedBy(entity, auditConversionService.convert(user, lastModifiedByClass));
                } else {
                    logger.warn("Cannot set @LastModifiedBy for {}. Unsupported field type '{}': no converter found.",
                            entity.getClass().getName(), lastModifiedByClass);
                }
            } else {
                EntityValues.setLastModifiedBy(entity, null);
            }
        }
    }
}
