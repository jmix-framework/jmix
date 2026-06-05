/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.execution;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Converts raw JPQL parameter values to the Java types expected by the query.
 * <p>
 * Each {@link JpqlExecutionParameter} carries a declared type name and an untyped value (typically
 * a string or a number as produced by the LLM). This service resolves the target Java class and
 * coerces the value to it, supporting common JDK types and any type known to the
 * {@link DatatypeRegistry}. Values whose type cannot be resolved are passed through unchanged.
 */
@NullMarked
@Component("aitols_JpqlParameterConversionService")
public class JpqlParameterConversionService {

    private static final Logger log = LoggerFactory.getLogger(JpqlParameterConversionService.class);

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    /**
     * Converts a list of parameters to a name-to-value map of execution-ready values, preserving order.
     * <p>
     * Parameters whose value converts to {@code null} are skipped rather than bound.
     *
     * @param parameters parameters to convert
     * @return map of parameter names to converted values; parameters with a {@code null} value are omitted
     */
    public Map<String, Object> convert(@Nullable List<JpqlExecutionParameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> convertedParameters = new LinkedHashMap<>();
        for (JpqlExecutionParameter parameter : parameters) {
            Object convertedValue = convert(parameter);
            if (convertedValue == null) {
                log.debug("Skipping query parameter '{}' because its value is null", parameter.getName());
                continue;
            }
            convertedParameters.put(parameter.getName(), convertedValue);
        }
        return Map.copyOf(convertedParameters);
    }

    /**
     * Converts a single parameter's value to its declared Java type.
     *
     * @param parameter parameter whose value is converted
     * @return the converted value, or {@code null} if the parameter value is {@code null}
     * @throws IllegalArgumentException if the value cannot be parsed as the declared type
     */
    @Nullable
    public Object convert(JpqlExecutionParameter parameter) {
        Preconditions.checkNotNullArgument(parameter, "parameter is null");

        Object value = parameter.getValue();
        if (value == null) {
            return null;
        }

        Class<?> targetClass = resolveJavaClass(parameter.getType());
        if (targetClass == null || targetClass.isInstance(value)) {
            return value;
        }

        if (value instanceof String stringValue) {
            return convertStringValue(stringValue, targetClass);
        }

        if (value instanceof Number numberValue) {
            return convertNumberValue(numberValue, targetClass);
        }

        if (targetClass == String.class) {
            return String.valueOf(value);
        }

        return value;
    }

    @Nullable
    protected Object convertStringValue(String value, Class<?> targetClass) {
        if (targetClass == String.class) {
            return value;
        }
        if (targetClass == UUID.class) {
            return UUID.fromString(value);
        }
        if (targetClass == Integer.class || targetClass == int.class) {
            return Integer.valueOf(value);
        }
        if (targetClass == Long.class || targetClass == long.class) {
            return Long.valueOf(value);
        }
        if (targetClass == Double.class || targetClass == double.class) {
            return Double.valueOf(value);
        }
        if (targetClass == Float.class || targetClass == float.class) {
            return Float.valueOf(value);
        }
        if (targetClass == Short.class || targetClass == short.class) {
            return Short.valueOf(value);
        }
        if (targetClass == Byte.class || targetClass == byte.class) {
            return Byte.valueOf(value);
        }
        if (targetClass == Boolean.class || targetClass == boolean.class) {
            return Boolean.valueOf(value);
        }
        if (targetClass == BigDecimal.class) {
            return new BigDecimal(value);
        }
        if (targetClass == BigInteger.class) {
            return new BigInteger(value);
        }
        if (targetClass == LocalDate.class) {
            return LocalDate.parse(value);
        }
        if (targetClass == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        }
        if (targetClass == LocalTime.class) {
            return LocalTime.parse(value);
        }
        if (targetClass == OffsetDateTime.class) {
            return OffsetDateTime.parse(value);
        }
        if (targetClass == Instant.class) {
            return Instant.parse(value);
        }

        Datatype<?> datatype = datatypeRegistry.find(targetClass);
        if (datatype != null) {
            try {
                return datatype.parse(value, Locale.getDefault());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unable to parse parameter value '" + value
                        + "' as " + targetClass.getSimpleName(), e);
            }
        }

        return value;
    }

    @Nullable
    protected Object convertNumberValue(Number value, Class<?> targetClass) {
        if (targetClass == Integer.class || targetClass == int.class) {
            return value.intValue();
        }
        if (targetClass == Long.class || targetClass == long.class) {
            return value.longValue();
        }
        if (targetClass == Double.class || targetClass == double.class) {
            return value.doubleValue();
        }
        if (targetClass == Float.class || targetClass == float.class) {
            return value.floatValue();
        }
        if (targetClass == Short.class || targetClass == short.class) {
            return value.shortValue();
        }
        if (targetClass == Byte.class || targetClass == byte.class) {
            return value.byteValue();
        }
        if (targetClass == BigDecimal.class) {
            return new BigDecimal(value.toString());
        }
        if (targetClass == BigInteger.class) {
            return BigInteger.valueOf(value.longValue());
        }
        if (targetClass == String.class) {
            return String.valueOf(value);
        }
        return value;
    }

    @Nullable
    protected Class<?> resolveJavaClass(@Nullable String type) {
        if (type == null || type.isBlank()) {
            return null;
        }

        return switch (type) {
            case "String", "java.lang.String" -> String.class;
            case "UUID", "java.util.UUID" -> UUID.class;
            case "Integer", "int", "java.lang.Integer" -> Integer.class;
            case "Long", "long", "java.lang.Long" -> Long.class;
            case "Double", "double", "java.lang.Double" -> Double.class;
            case "Float", "float", "java.lang.Float" -> Float.class;
            case "Short", "short", "java.lang.Short" -> Short.class;
            case "Byte", "byte", "java.lang.Byte" -> Byte.class;
            case "Boolean", "boolean", "java.lang.Boolean" -> Boolean.class;
            case "BigDecimal", "java.math.BigDecimal" -> BigDecimal.class;
            case "BigInteger", "java.math.BigInteger" -> BigInteger.class;
            case "LocalDate", "java.time.LocalDate" -> LocalDate.class;
            case "LocalDateTime", "java.time.LocalDateTime" -> LocalDateTime.class;
            case "LocalTime", "java.time.LocalTime" -> LocalTime.class;
            case "OffsetDateTime", "java.time.OffsetDateTime" -> OffsetDateTime.class;
            case "Instant", "java.time.Instant" -> Instant.class;
            default -> resolveJavaClassByName(type);
        };
    }

    @Nullable
    protected Class<?> resolveJavaClassByName(String type) {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
