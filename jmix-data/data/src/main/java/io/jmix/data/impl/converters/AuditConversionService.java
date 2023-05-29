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

package io.jmix.data.impl.converters;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.annotation.Nullable;
import java.util.Date;

/**
 * Used for current time conversion from {@link Date} to {@link CreatedDate}, {@link LastModifiedDate} and {@link DeletedDate} field types.<br>
 * <p>
 * Also converts current user to {@link CreatedBy}, {@link LastModifiedBy} and {@link DeletedBy} field types
 */
public interface AuditConversionService {

    /**
     * @see org.springframework.core.convert.ConversionService#canConvert(Class, Class)
     */
    boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);

    /**
     * @see org.springframework.core.convert.ConversionService#convert(Object, Class)
     */
    @Nullable
    <T> T convert(@Nullable Object source, Class<T> targetType);
}
