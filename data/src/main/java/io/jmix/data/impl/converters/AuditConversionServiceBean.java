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

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.stereotype.Component;

/**
 * Default implementation to support common types.
 * Instantiate this bean and add custom converters to support different date/time and user types.
 */
@Component(AuditConversionService.NAME)
public class AuditConversionServiceBean implements AuditConversionService {

    private GenericConversionService conversionService;

    public AuditConversionServiceBean() {
        conversionService = new GenericConversionService();

        conversionService.addConverter(Jsr310Converters.DateToLocalDateTimeConverter.INSTANCE);
        conversionService.addConverter(Jsr310Converters.LocalDateTimeToDateConverter.INSTANCE);

        conversionService.addConverter(AuditConverters.DateToLongConverter.INSTANCE);
        conversionService.addConverter(AuditConverters.LongToDateConverter.INSTANCE);

        conversionService.addConverter(AuditConverters.UserToStringConverter.INSTANCE);
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }

    @Override
    public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        return conversionService.convert(source, targetType);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return conversionService.convert(source, sourceType, targetType);
    }

    public void addConverter(Converter<?, ?> converter) {
        conversionService.addConverter(converter);
    }

    public GenericConversionService getConversionService() {
        return conversionService;
    }

}
