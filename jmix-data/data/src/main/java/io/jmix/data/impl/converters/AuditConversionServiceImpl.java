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

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.util.Collection;

/**
 * Default implementation to support common types.
 * Instantiate this bean and add custom converters to support different date/time and user types.
 */
@Component("data_AuditConversionService")
public class AuditConversionServiceImpl implements AuditConversionService {

    private GenericConversionService conversionService;

    public AuditConversionServiceImpl() {
        conversionService = new GenericConversionService();

        addConverters(Jsr310Converters.getConvertersToRegister());

        addConverter(AuditConverters.DateToLongConverter.INSTANCE);
        addConverter(AuditConverters.LongToDateConverter.INSTANCE);

        addConverter(AuditConverters.DateToOffsetDateTimeConverter.INSTANCE);
        addConverter(AuditConverters.OffsetDateTimeToDateConverter.INSTANCE);

        addConverter(AuditConverters.LocalDateToDateConverter.INSTANCE);
        addConverter(AuditConverters.DateToLocalDateConverter.INSTANCE);

        addConverter(AuditConverters.LocalDateTimeToDateConverter.INSTANCE);
        addConverter(AuditConverters.DateToLocalDateTimeConverter.INSTANCE);

        addConverter(AuditConverters.LocalDateTimeToLocalDateConverter.INSTANCE);
        addConverter(AuditConverters.LocalDateToLocalDateTimeConverter.INSTANCE);

        addConverter(AuditConverters.LocalDateToOffsetDateTimeConverter.INSTANCE);
        addConverter(AuditConverters.OffsetDateTimeToLocalDateConverter.INSTANCE);

        addConverter(AuditConverters.LocalDateTimeToOffsetDateTimeConverter.INSTANCE);
        addConverter(AuditConverters.OffsetDateTimeToLocalDateTimeConverter.INSTANCE);

        conversionService.addConverter(AuditConverters.UserToStringConverter.INSTANCE);
    }

    @Override
    public boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }


    @Override
    public <T> T convert(@Nullable Object source, Class<T> targetType) {
        return conversionService.convert(source, targetType);
    }

    public void addConverter(Converter<?, ?> converter) {
        conversionService.addConverter(converter);
    }

    public void addConverters(Collection<Converter<?, ?>> converters) {
        for (Converter<?, ?> converter : converters) {
            conversionService.addConverter(converter);
        }
    }

    public GenericConversionService getConversionService() {
        return conversionService;
    }

}
