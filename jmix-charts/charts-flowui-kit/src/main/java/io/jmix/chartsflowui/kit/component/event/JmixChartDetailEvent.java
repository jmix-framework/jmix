/*
 * Copyright 2024 Haulmont.
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

package io.jmix.chartsflowui.kit.component.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import elemental.json.*;
import elemental.json.impl.JreJsonNull;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public interface JmixChartDetailEvent<T> {

    JsonObject getDetailJson();

    Object getDetail();

    void setDetail(Object detailObject);

    @SuppressWarnings("unchecked")
    default Class<T> reflectClassType() {
        return ((Class<T>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]);
    }

    default T loadDetail() {
        return convertDetail(reflectClassType());
    }

    default String getFieldClassName(Field field) {
        return getFieldClassName(field.getType());
    }

    default String getFieldClassName(Class type) {
        return type.getName().replace(type.getPackageName() + ".", "");
    }

    default String getFieldGetterName(String fieldClassName) {
        return switch (fieldClassName) {
            case "Long", "Integer", "Double" -> "Number";
            case "Boolean" -> "Boolean";
            default -> "String";
        };
    }

    default String getFieldName(Field field) {
        JsonProperty jsonAnnotation = field.getAnnotation(JsonProperty.class);
        if (jsonAnnotation != null && jsonAnnotation.value() != null && !"".equals(jsonAnnotation.value())) {
            return jsonAnnotation.value();
        }
        return field.getName();
    }

    default boolean isFieldIgnored(Field field) {
        JsonIgnore jsonAnnotation = field.getAnnotation(JsonIgnore.class);
        if (jsonAnnotation != null && jsonAnnotation.value()) {
            return jsonAnnotation.value();
        }
        return false;
    }

    default void convertPrimitiveField(Field field, JsonObject source, Object target, Class<?> ownerClazz)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String fieldName = getFieldName(field);
        String fieldClassName = getFieldClassName(field);
        String fieldGetterName = getFieldGetterName(fieldClassName);
        if (source.hasKey(fieldName)) {
            Method checker = source.getClass().getMethod("get", String.class);
            JsonValue value = (JsonValue) checker.invoke(source, fieldName);
            Class<?> reqClazz = Class.forName("elemental.json.Json" + fieldGetterName);
            if (value != null) {
                Object convertedValue = null;
                if (reqClazz.isInstance(value)) {
                    Method getter = source.getClass().getMethod("get" + fieldGetterName, String.class);
                    Object typedValue = getter.invoke(source, fieldName);
                    convertedValue = switch (fieldClassName) {
                        case "Long" -> typedValue.getClass().getMethod("longValue").invoke(typedValue);
                        case "Integer" -> typedValue.getClass().getMethod("intValue").invoke(typedValue);
                        default -> typedValue;
                    };
                } else if ("String".equals(fieldClassName)) {
                    if (value instanceof JsonNumber) {
                        convertedValue = value.asString();
                    } else if (value instanceof JsonObject || value instanceof JsonArray) {
                        convertedValue = value.toJson();
                    }
                }
                Method setter = ownerClazz.getMethod("set" + StringUtils.capitalize(field.getName()), Class.forName(field.getType().getName()));
                setter.invoke(target, convertedValue);
            }
        }
    }

    default void convertList(Field field, JsonArray source, Object target, Class<?> ownerClazz)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Type type = field.getGenericType();
        Class<?> genericType = null;
        if (type instanceof ParameterizedType pt) {
            genericType  = (Class<?>) pt.getActualTypeArguments()[0];
        }
        List<Object> instance = new ArrayList<>();
        Method setter = getAllMethods(ownerClazz).stream()
                .filter(m -> m.getName().equals("set" + StringUtils.capitalize(field.getName()))).findAny().orElseThrow();
        setter.invoke(target, instance);
        String fieldClassName = getFieldClassName(field);
        String fieldGetterName = getFieldGetterName(fieldClassName);
        if (genericType != null) {
            fieldClassName = getFieldClassName(genericType);
            fieldGetterName = getFieldGetterName(fieldClassName);
        }
        for (int i = 0; i < source.length(); i++) {
            if (source.get(i) instanceof JsonObject && genericType != null) {
                Object listItem = genericType.getConstructor().newInstance();
                convertClassFields(listItem, source.get(i), genericType);
                instance.add(listItem);
            } else {
                Object value = getPrimitiveValue(fieldGetterName, fieldClassName, source.get(i));
                instance.add(value);
            }
        }
    }

    default void convertMap(Field field, JsonObject source, Object target, Class<?> ownerClazz)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Type type = field.getGenericType();
        Class<?> genericType = null;
        if (type instanceof ParameterizedType pt) {
            genericType  = (Class<?>) pt.getActualTypeArguments()[1];
        }
        Map<Object, Object> instance = new HashMap<>();
        Method setter = getAllMethods(ownerClazz).stream()
                .filter(m -> m.getName().equals("set" + StringUtils.capitalize(field.getName())))
                .findAny().orElseThrow();
        setter.invoke(target, instance);
        String fieldClassName = getFieldClassName(field);
        String fieldGetterName = getFieldGetterName(fieldClassName);
        if (genericType != null) {
            fieldClassName = getFieldClassName(genericType);
            fieldGetterName = getFieldGetterName(fieldClassName);
        }
        Class<?> reqClazz = Class.forName("elemental.json.Json" + fieldGetterName);
        for (String key: source.keys()) {
            if (source.get(key) instanceof JsonObject && genericType != null) {
                Object listItem = genericType.getConstructor().newInstance();
                convertClassFields(listItem, source.get(key), genericType);
                instance.put(key, listItem);
            } else {
                Object value = getPrimitiveValue(fieldGetterName, fieldClassName, source.get(key));
                instance.put(key, value);
            }
        }
    }

    default Object getPrimitiveValue(String fieldGetterName, String fieldClassName, JsonValue source)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Class<?> reqClazz = Class.forName("elemental.json.Json" + fieldGetterName);
        Object value = null;
        if (reqClazz.isInstance(source)) {
            Method getter = source.getClass().getMethod("get" + fieldGetterName);
            value = getter.invoke(source);
            return switch (fieldClassName) {
                case "Long" -> value.getClass().getMethod("longValue").invoke(value);
                case "Integer" -> value.getClass().getMethod("intValue").invoke(value);
                default -> value;
            };
        } else if ("String".equals(fieldClassName)) {
            if (source instanceof JsonNumber) {
                value = source.asString();
            } else if (source instanceof JsonObject || source instanceof JsonArray) {
                value = source.toJson();
            } else {
                Method getter = source.getClass().getMethod("asString");
                value = getter.invoke(source);
            }
            return value;
        }
        return null;
    }

    default void convertClass(Field field, JsonObject source, Object target, Class<?> ownerClazz)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> fieldClazz = Class.forName(field.getType().getName());
        Object instance = fieldClazz.getConstructor().newInstance();
        Method setter = ownerClazz.getMethod("set" + StringUtils.capitalize(field.getName()), Class.forName(field.getType().getName()));
        setter.invoke(target, instance);

        Method getter = source.getClass().getMethod("getObject", String.class);
        JsonObject fieldValue = (JsonObject) getter.invoke(source, getFieldName(field));

        convertClassFields(instance, fieldValue, fieldClazz);
    }

    default void convertClassFields(Object instance, JsonObject source, Class<?> ownerClazz)
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        List<Field> fields = getAllFields(ownerClazz);
        JsonNull jsonNull = new JreJsonNull();
        for (Field field : fields) {
            if (isFieldIgnored(field)) {
                continue;
            }
            Method checker = source.getClass().getMethod("get", String.class);
            String fieldName = getFieldName(field);
            JsonValue value = (JsonValue) checker.invoke(source, fieldName);
            if (value != null && !value.jsEquals(jsonNull)) {
                switch (getFieldClassName(field)) {
                    case "String", "Long", "Integer", "Boolean", "Double" ->
                            convertPrimitiveField(field, source, instance, ownerClazz);
                    case "List" -> {
                        Method getter = source.getClass().getMethod("getArray", String.class);
                        Object result = getter.invoke(source, fieldName);
                        if (result instanceof JsonArray) {
                            JsonArray fieldValue = (JsonArray) result;
                            convertList(field, fieldValue, instance, ownerClazz);
                        }
                    }
                    case "Map" -> {
                        if (value instanceof JsonObject) {
                            Method getter = source.getClass().getMethod("getObject", String.class);
                            Object result = (JsonObject) getter.invoke(source, fieldName);
                            if (result instanceof JsonObject) {
                                JsonObject fieldValue = (JsonObject) result;
                                convertMap(field, fieldValue, instance, ownerClazz);
                            }
                        }
                    }
                    default -> convertClass(field, source, instance, ownerClazz);
                }
            }
        }
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
    public static List<Method> getAllMethods(Class<?> type) {
        List<Method> fields = new ArrayList<Method>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getMethods()));
        }
        return fields;
    }

    default <T> T convertDetail(Class<?> detailClazz) {
        //noinspection unchecked
        return (T) mapDetail(detailClazz);
    }

    default <M> M mapDetail(Class<?> detailClazz) {
        if (getDetail() == null) {
            try {
                Object instance = detailClazz.getConstructor().newInstance();
                setDetail(instance);
                convertClassFields(instance, getDetailJson(), detailClazz);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException
                     | NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        //noinspection unchecked
        return (M) getDetail().getClass().cast(getDetail());
    }

}