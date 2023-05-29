/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dataimport.property.populator.impl;

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.property.populator.PropertyMappingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Component("datimp_SimplePropertyValueProvider")
public class SimplePropertyValueProvider {
    protected final static Logger log = LoggerFactory.getLogger(SimplePropertyValueProvider.class);

    @Nullable
    public Object getValue(PropertyMappingContext context) {
        MetaProperty metaProperty = context.getMetaProperty();
        Object rawValue = context.getRawValue();
        if (rawValue == null) {
            return null;
        }
        ImportConfiguration importConfiguration = context.getImportConfiguration();
        Class<?> javaType = metaProperty.getJavaType();
        Object resultValue = null;
        if (Integer.class.isAssignableFrom(javaType)) {
            resultValue = getIntegerValue(rawValue);
        } else if (Long.class.isAssignableFrom(javaType)) {
            resultValue = getLongValue(rawValue);
        } else if (Double.class.isAssignableFrom(javaType)) {
            resultValue = getDoubleValue(rawValue);
        } else if (Date.class.isAssignableFrom(javaType)) {
            resultValue = getDateValue(importConfiguration, rawValue);
        } else if (LocalDate.class.isAssignableFrom(javaType)) {
            resultValue = getLocalDateValue(importConfiguration, rawValue);
        } else if (Boolean.class.isAssignableFrom(javaType)) {
            resultValue = getBooleanValue(importConfiguration, rawValue);
        } else if (BigDecimal.class.isAssignableFrom(javaType)) {
            resultValue = getBigDecimalValue(rawValue);
        } else if (Enum.class.isAssignableFrom(javaType)) {
            resultValue = getEnumValue(rawValue, (Class<Enum>) javaType);
        } else if (isStringValue(rawValue)) {
            resultValue = rawValue;
        }
        return resultValue;
    }

    @Nullable
    protected Enum getEnumValue(Object rawValue, Class<Enum> javaType) {
        if (isStringValue(rawValue)) {
            String enumValue = ((String) rawValue).toUpperCase();
            if (javaType.isEnum()) {
                try {
                    return Enum.valueOf(javaType, enumValue);
                } catch (IllegalArgumentException e) {
                    log.info(String.format("Enum value could not be found: %s for Enum: '%s'. Will be ignored", enumValue, javaType.getSimpleName()));
                    log.debug("Details: ", e);
                }
            }
        }
        return null;
    }

    @Nullable
    protected BigDecimal getBigDecimalValue(Object rawValue) {
        if (isStringValue(rawValue)) {
            try {
                return new BigDecimal((String) rawValue);
            } catch (NumberFormatException e) {
                log.warn(String.format("Number could not be read: '%s' in.Will be ignored.", rawValue));
            }
        }
        return null;
    }

    @Nullable
    protected Double getDoubleValue(Object rawValue) {
        if (isStringValue(rawValue)) {
            try {
                return Double.parseDouble((String) rawValue);
            } catch (NumberFormatException e) {
                log.warn(String.format("Number could not be read: '%s'. Will be ignored.", rawValue));
            }
        }
        return null;
    }

    @Nullable
    protected Long getLongValue(Object rawValue) {
        if (isStringValue(rawValue)) {
            try {
                return Long.parseLong((String) rawValue);
            } catch (NumberFormatException e) {
                log.warn(String.format("Number could not be read: '%s'. Will be ignored.", rawValue));
            }
        }
        return null;
    }

    @Nullable
    protected Integer getIntegerValue(Object rawValue) {
        if (isStringValue(rawValue)) {
            try {
                return Integer.parseInt((String) rawValue);
            } catch (NumberFormatException e) {
                log.warn(String.format("Number could not be read: '%s'. Will be ignored.", rawValue));
            }
        }
        return null;
    }

    protected boolean isStringValue(Object rawValue) {
        return rawValue instanceof String;
    }

    @Nullable
    protected Boolean getBooleanValue(ImportConfiguration importConfiguration, Object rawValue) {
        if (isStringValue(rawValue)) {
            String customBooleanTrueValue = importConfiguration.getBooleanTrueValue();
            String customBooleanFalseValue = importConfiguration.getBooleanFalseValue();

            if (StringUtils.isNotEmpty(customBooleanTrueValue) || StringUtils.isNotEmpty(customBooleanFalseValue)) {
                if (StringUtils.equalsIgnoreCase(customBooleanTrueValue, (String) rawValue)) {
                    return true;
                }
                return StringUtils.equalsIgnoreCase(customBooleanFalseValue, (String) rawValue) ? false : null;
            }
            try {
                return Boolean.parseBoolean((String) rawValue);
            } catch (Exception e) {
                log.warn(String.format("Boolean could not be read: '%s'. Will be ignored.", rawValue));
            }
        }
        return null;
    }

    @Nullable
    protected Date getDateValue(ImportConfiguration importConfiguration, Object rawValue) {
        if (isStringValue(rawValue)) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(importConfiguration.getDateFormat());
                return formatter.parse((String) rawValue);
            } catch (ParseException e) {
                log.warn(String.format("Date could not be read: '%s'," +
                        " because it does not match the configured date format: '%s'. Will be ignored.", rawValue, importConfiguration.getDateFormat()));
            }
        }
        return null;
    }

    @Nullable
    protected LocalDate getLocalDateValue(ImportConfiguration importConfiguration, Object rawValue) {
        if (isStringValue(rawValue)) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(importConfiguration.getDateFormat());
                return LocalDate.parse((String) rawValue, formatter);
            } catch (DateTimeParseException e) {
                log.warn(String.format("Date could not be read: '%s'," +
                        " because it does not match the configured date format: '%s'. Will be ignored.", rawValue, importConfiguration.getDateFormat()));
            }
        }
        return null;
    }
}
