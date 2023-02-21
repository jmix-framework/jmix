/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.view.navigation;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.URLEncodeUtils;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.FlowuiNavigationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("flowui_UrlParamSerializer")
public class UrlParamSerializer {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH-mm-ss";
    public static final String DEFAULT_OFFSET_FORMAT = "Z";
    public static final String DEFAULT_DATE_TIME_FORMAT =
            DEFAULT_DATE_FORMAT + "'T'" + DEFAULT_TIME_FORMAT;
    public static final String DEFAULT_OFFSET_DATE_TIME_FORMAT =
            DEFAULT_DATE_FORMAT + "'T'" + DEFAULT_TIME_FORMAT + DEFAULT_OFFSET_FORMAT;
    public static final String DEFAULT_OFFSET_TIME_FORMAT =
            DEFAULT_TIME_FORMAT + DEFAULT_OFFSET_FORMAT;

    protected static final DateTimeFormatter TEMPORAL_DATE_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    protected static final DateTimeFormatter TEMPORAL_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
    protected static final DateTimeFormatter TEMPORAL_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
    protected static final DateTimeFormatter TEMPORAL_OFFSET_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_OFFSET_DATE_TIME_FORMAT);
    protected static final DateTimeFormatter TEMPORAL_OFFSET_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DEFAULT_OFFSET_TIME_FORMAT);

    protected FlowuiNavigationProperties navigationProperties;
    protected MetadataTools metadataTools;
    protected Metadata metadata;

    public UrlParamSerializer(FlowuiNavigationProperties navigationProperties,
                              MetadataTools metadataTools,
                              Metadata metadata) {
        this.navigationProperties = navigationProperties;
        this.metadataTools = metadataTools;
        this.metadata = metadata;
    }

    /**
     * Serializes the given {@code value} to string representation.
     * <p>
     * String, Integer, Long and UUID values are only supported.
     *
     * @param value value to serialize
     * @return serialized string representation of given value
     * @throws IllegalArgumentException if null value is passed or it has an unsupported type
     */
    public String serialize(Object value) {
        checkNotNullArgument(value, "Unable to serialize null value");

        Class<?> type = value.getClass();

        if (BigDecimal.class == type) {
            return serializePrimitive(value);

        } else if (BigInteger.class == type) {
            return serializePrimitive(value);

        } else if (Boolean.class == type) {
            return serializePrimitive(value);

        } else if (Character.class == type) {
            return serializePrimitive(value);

        } else if (Double.class == type) {
            return serializePrimitive(value);

        } else if (Float.class == type) {
            return serializePrimitive(value);

        } else if (Integer.class == type) {
            return serializePrimitive(value);

        } else if (Long.class == type) {
            return serializePrimitive(value);

        } else if (Short.class == type) {
            return serializePrimitive(value);

        } else if (String.class == type) {
            return serializePrimitive(value);

        } else if (java.sql.Date.class == type) {
            return serializeDate(((java.sql.Date) value));

        } else if (Date.class == type) {
            return serializeDateTime(((Date) value));

        } else if (LocalDate.class == type) {
            return serializeLocalDate(((LocalDate) value));

        } else if (LocalDateTime.class == type) {
            return serializeLocalDateTime(((LocalDateTime) value));

        } else if (OffsetDateTime.class == type) {
            return serializeOffsetDateTime(((OffsetDateTime) value));

        } else if (LocalTime.class == type) {
            return serializeLocalTime(((LocalTime) value));

        } else if (OffsetTime.class == type) {
            return serializeOffsetTime(((OffsetTime) value));

        } else if (Time.class == type) {
            return serializeTime(((Time) value));

        } if (UUID.class.equals(type)) {
            return serializeUuid((UUID) value);

        } else if (Enum.class.isAssignableFrom(type)) {
            return serializeEnum(((Enum<?>) value));

        } else if (EntityValues.isEntity(value)
                && metadataTools.isJpaEmbeddable(type)) {
            return serializeComposite(value);

        }

        throw new IllegalArgumentException(
                String.format("Unable to serialize value '%s' of type '%s'", value, type));
    }

    protected String serializeDateTime(Date value) {
        String stringValue = String.valueOf(value.getTime());
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeLocalDate(LocalDate value) {
        String stringValue = TEMPORAL_DATE_FORMATTER.format(value);
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeLocalDateTime(LocalDateTime value) {
        String stringValue = TEMPORAL_DATE_TIME_FORMATTER.format(value);
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeDate(java.sql.Date value) {
        String stringValue = String.valueOf(value.getTime());
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeOffsetDateTime(OffsetDateTime value) {
        String stringValue = TEMPORAL_OFFSET_DATE_TIME_FORMATTER.format(value);
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeLocalTime(LocalTime value) {
        String stringValue = TEMPORAL_TIME_FORMATTER.format(value);
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeOffsetTime(OffsetTime value) {
        String stringValue = TEMPORAL_OFFSET_TIME_FORMATTER.format(value);
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeTime(Time value) {
        String stringValue = String.valueOf(value.getTime());
        return URLEncodeUtils.encodeUtf8(stringValue);
    }

    protected String serializeEnum(Enum<?> value) {
        return value.name();
    }

    protected String serializePrimitive(Object value) {
        return URLEncodeUtils.encodeUtf8(value.toString());
    }

    protected String serializeUuid(UUID value) {
        return navigationProperties.isUseCrockfordUuidEncoder()
                ? CrockfordUuidEncoder.encode(value)
                : URLEncodeUtils.encodeUtf8(value.toString());
    }

    protected String serializeComposite(Object value) {
        MetaClass metaClass = metadata.getClass(value);
        Collection<MetaProperty> properties = metaClass.getProperties();

        List<String> params = new ArrayList<>();

        for (MetaProperty property : properties) {
            String propertyName = property.getName();
            Object propertyValue = EntityValues.getValue(value, propertyName);

            if (propertyValue != null) {
                String propertyStringValue = serialize(propertyValue);

                params.add(String.join("=", propertyName, propertyStringValue));
            }
        }

        return String.join("&", params);
    }

    /**
     * Deserializes the given {@code serializedValue} as a value with given {@code type}.
     * <p>
     * String, Integer, Long and UUID ids are only supported.
     *
     * @param type            value type
     * @param serializedValue serialized value
     * @return deserialized value
     * @throws IllegalArgumentException if null value and/or type are passed or the given value type is not supported
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(Class<T> type, String serializedValue) {
        checkNotNullArgument(type, "Unable to deserialize value without its type");
        checkNotNullArgument(serializedValue, "Unable to deserialize null value");

        String decoded = URLEncodeUtils.decodeUtf8(serializedValue);

        try {
            if (String.class == type) {
                return ((T) parseString(decoded));

            } else if (BigDecimal.class == type) {
                return ((T) parseBigDecimal(decoded));

            } else if (BigInteger.class == type) {
                return ((T) parseBigInteger(decoded));

            } else if (Boolean.class == type) {
                return ((T) parseBoolean(decoded));

            } else if (Character.class == type) {
                return ((T) parseCharacter(decoded));

            } else if (java.sql.Date.class == type) {
                return ((T) parseDate(decoded));

            } else if (Date.class == type) {
                return ((T) parseDateTime(decoded));

            } else if (Double.class == type) {
                return ((T) parseDouble(decoded));

            } else if (Float.class == type) {
                return ((T) parseFloat(decoded));

            } else if (Integer.class == type) {
                return ((T) parseInteger(decoded));

            } else if (LocalDate.class == type) {
                return ((T) parseLocalDate(decoded));

            } else if (LocalDateTime.class == type) {
                return ((T) parseLocalDateTime(decoded));

            } else if (LocalTime.class == type) {
                return ((T) parseLocalTime(decoded));

            } else if (Long.class == type) {
                return ((T) parseLong(decoded));

            } else if (OffsetDateTime.class == type) {
                return ((T) parseOffsetDateTime(decoded));

            } else if (OffsetTime.class == type) {
                return ((T) parseOffsetTime(decoded));

            } else if (Short.class == type) {
                return ((T) parseShort(decoded));

            } else if (Time.class == type) {
                return ((T) parseTime(decoded));

            } else if (UUID.class == type) {
                return ((T) parseUuid(serializedValue));

            } else if (Enum.class.isAssignableFrom(type)) {
                return parseEnum(type, decoded);

            } else if (Entity.class.isAssignableFrom(type)
                    && metadataTools.isJpaEmbeddable(type)) {
                return parseComposite(type, serializedValue);
            } else {
                throw new IllegalArgumentException(
                        String.format("Unable to deserialize id '%s' of type '%s'", serializedValue, type));
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    String.format("An error occurred while deserializing id: '%s' of type '%s'",
                            serializedValue, type));
        }
    }

    protected OffsetDateTime parseOffsetDateTime(String stringValue) {
        return OffsetDateTime.parse(stringValue, TEMPORAL_OFFSET_DATE_TIME_FORMATTER);
    }

    protected OffsetTime parseOffsetTime(String stringValue) {
        return OffsetTime.parse(stringValue, TEMPORAL_OFFSET_TIME_FORMATTER);
    }

    protected BigDecimal parseBigDecimal(String stringValue) {
        return new BigDecimal(stringValue);
    }

    protected BigInteger parseBigInteger(String stringValue) {
        return new BigInteger(stringValue);
    }

    protected Character parseCharacter(String stringValue) {
        return stringValue.charAt(0);
    }

    protected java.sql.Date parseDate(String stringValue) {
        return new java.sql.Date(Long.parseLong(stringValue));
    }

    protected Date parseDateTime(String stringValue) {
        return new Date(Long.parseLong(stringValue));
    }

    protected Double parseDouble(String stringValue) {
        return Double.valueOf(stringValue);
    }

    protected <T> T parseEnum(Class<T> type, String stringValue) {
        T[] enumConstants = type.getEnumConstants();
        for (T enumConst : enumConstants) {
            if (StringUtils.equalsIgnoreCase(((Enum<?>) enumConst).name(), stringValue)) {
                return enumConst;
            }
        }

        throw new IllegalArgumentException(
                "No enum constant " + type.getCanonicalName() + "." + stringValue);
    }

    protected Float parseFloat(String stringValue) {
        return Float.valueOf(stringValue);
    }

    protected LocalDate parseLocalDate(String stringValue) {
        return LocalDate.parse(stringValue, TEMPORAL_DATE_FORMATTER);
    }

    protected LocalDateTime parseLocalDateTime(String stringValue) {
        return LocalDateTime.parse(stringValue, TEMPORAL_DATE_TIME_FORMATTER);
    }

    protected LocalTime parseLocalTime(String stringValue) {
        return LocalTime.parse(stringValue, TEMPORAL_TIME_FORMATTER);
    }

    protected Short parseShort(String stringValue) {
        return Short.valueOf(stringValue);
    }

    protected Time parseTime(String stringValue) {
        return new Time(Long.parseLong(stringValue));
    }

    protected Boolean parseBoolean(String stringValue) {
        return Boolean.valueOf(stringValue);
    }

    protected String parseString(String stringValue) {
        return stringValue;
    }

    protected Integer parseInteger(String stringValue) {
        return Integer.valueOf(stringValue);
    }

    protected Long parseLong(String stringValue) {
        return Long.valueOf(stringValue);
    }

    protected UUID parseUuid(String serializedValue) {
        if (navigationProperties.isUseCrockfordUuidEncoder()) {
            return CrockfordUuidEncoder.decode(serializedValue);
        } else {
            String decoded = URLEncodeUtils.decodeUtf8(serializedValue);
            return UUID.fromString(decoded);
        }
    }

    protected <T> T parseComposite(Class<T> type, String serializedValue) {
        T composite = metadata.create(type);

        MetaClass metaClass = metadata.getClass(composite);
        Collection<MetaProperty> properties = metaClass.getProperties();

        Map<String, String> propertyNameValueMap = Arrays.stream(serializedValue.split("&"))
                .collect(Collectors.toMap(this::propertyNameMapper, this::propertyValueMapper));

        for (MetaProperty property : properties) {
            String propertyName = property.getName();
            String propertyValue = propertyNameValueMap.get(propertyName);

            if (StringUtils.isNotEmpty(propertyValue)) {
                Class<?> propertyClass = property.getJavaType();
                Object deserializedValue = deserialize(propertyClass, propertyValue);

                EntityValues.setValue(composite, propertyName, deserializedValue);
            }
        }

        return composite;
    }

    protected String propertyNameMapper(String property) {
        String[] splitProperty = property.split("=", 2);
        return splitProperty[0];
    }

    protected String propertyValueMapper(String property) {
        String[] splitProperty = property.split("=", 2);
        return splitProperty[1];
    }
}
