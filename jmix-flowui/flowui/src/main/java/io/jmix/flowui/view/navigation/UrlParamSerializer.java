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

import io.jmix.core.common.util.URLEncodeUtils;
import io.jmix.flowui.FlowuiNavigationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("flowui_UrlParamSerializer")
public class UrlParamSerializer {

    protected FlowuiNavigationProperties navigationProperties;

    public UrlParamSerializer(FlowuiNavigationProperties navigationProperties) {
        this.navigationProperties = navigationProperties;
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

        String serialized;
        Class<?> type = value.getClass();

        if (String.class == type
                || Integer.class == type
                || Long.class == type) {
            serialized = serializePrimitive(value);

        } else if (UUID.class == type) {
            serialized = serializeUuid((UUID) value);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unable to serialize value '%s' of type '%s'", value, type));
        }

        return serialized;
    }

    protected String serializePrimitive(Object value) {
        return URLEncodeUtils.encodeUtf8(value.toString());
    }

    protected String serializeUuid(UUID value) {
        return navigationProperties.isUseCrockfordUuidEncoder()
                ? CrockfordUuidEncoder.encode(value)
                : URLEncodeUtils.encodeUtf8(value.toString());
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

            } else if (Integer.class == type) {
                return ((T) parseInteger(decoded));

            } else if (Long.class == type) {
                return ((T) parseLong(decoded));

            } else if (UUID.class == type) {
                return ((T) parseUuid(serializedValue));
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

    protected String parseString(String decoded) {
        return decoded;
    }

    protected Integer parseInteger(String decoded) {
        return Integer.valueOf(decoded);
    }

    protected Long parseLong(String decoded) {
        return Long.valueOf(decoded);
    }

    protected UUID parseUuid(String serializedValue) {
        if (navigationProperties.isUseCrockfordUuidEncoder()) {
            return CrockfordUuidEncoder.decode(serializedValue);
        } else {
            String decoded = URLEncodeUtils.decodeUtf8(serializedValue);
            return UUID.fromString(decoded);
        }
    }
}
