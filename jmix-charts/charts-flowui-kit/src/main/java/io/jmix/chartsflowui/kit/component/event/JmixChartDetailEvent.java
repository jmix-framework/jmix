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

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import org.apache.commons.lang3.StringUtils;

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
        return field.getType().getName().replace(field.getType().getPackageName() + ".", "");
    }

    default String getFieldGetterName(String fieldClassName) {
        return switch (fieldClassName) {
            case "Long", "Integer", "Double" -> "Number";
            default -> "String";
        };
    }

    default void convertPrimitiveField(Field field, JsonObject source, Object target, Class<?> ownerClazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String fieldClassName = getFieldClassName(field);
        String fieldGetterName = switch (fieldClassName) {
            case "Long", "Integer", "Double" -> "Number";
            default -> "String";
        };
        if (source.hasKey(field.getName())) {
            Method getter = source.getClass().getMethod("get" + fieldGetterName, String.class);
            Object value = getter.invoke(source, field.getName());
            Object convertedValue = switch (fieldClassName) {
                case "Long" -> value.getClass().getMethod("longValue").invoke(value);
                case "Integer" -> value.getClass().getMethod("intValue").invoke(value);
                default -> value;
            };
            Method setter = ownerClazz.getMethod("set" + StringUtils.capitalize(field.getName()), Class.forName(field.getType().getName()));
            setter.invoke(target, convertedValue);
        }
    }

    default void convertList(Field field, JsonArray source, Object target, Class<?> ownerClazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Type type = field.getGenericType();
        Class<?> genericType = null;
        if (type instanceof ParameterizedType pt) {
            genericType  = (Class<?>) pt.getActualTypeArguments()[0];
        }
        List<Object> instance = new ArrayList<>();
        Method setter = Arrays.stream(ownerClazz.getDeclaredMethods())
                .filter(m -> m.getName().equals("set" + StringUtils.capitalize(field.getName()))).findAny().orElseThrow();
        setter.invoke(target, instance);
        String fieldClassName = getFieldClassName(field);
        String fieldGetterName = getFieldGetterName(fieldClassName);
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

    default void convertMsp(Field field, JsonObject source, Object target, Class<?> ownerClazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Type type = field.getGenericType();
        Class<?> genericType = null;
        if (type instanceof ParameterizedType pt) {
            genericType  = (Class<?>) pt.getActualTypeArguments()[1];
        }
        Map<Object, Object> instance = new HashMap<>();
        Method setter = Arrays.stream(ownerClazz.getDeclaredMethods())
                .filter(m -> m.getName().equals("set" + StringUtils.capitalize(field.getName()))).findAny().orElseThrow();
        setter.invoke(target, instance);
        String fieldClassName = getFieldClassName(field);
        String fieldGetterName = getFieldGetterName(fieldClassName);
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

    default Object getPrimitiveValue(String fieldGetterName, String fieldClassName, JsonValue source) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getter = source.getClass().getMethod("get" + fieldGetterName);
        Object detailValue = getter.invoke(source);
        return switch (fieldClassName) {
            case "Long" -> detailValue.getClass().getMethod("longValue").invoke(detailValue);
            case "Integer" -> detailValue.getClass().getMethod("intValue").invoke(detailValue);
            default -> detailValue;
        };
    }

    default void convertClass(Field field, JsonObject source, Object target, Class<?> ownerClazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> fieldClazz = Class.forName(field.getType().getName());
        Object instance = fieldClazz.getConstructor().newInstance();
        Method setter = ownerClazz.getMethod("set" + StringUtils.capitalize(field.getName()), Class.forName(field.getType().getName()));
        setter.invoke(target, instance);

        Method getter = source.getClass().getMethod("getObject", String.class);
        JsonObject fieldValue = (JsonObject) getter.invoke(source, field.getName());

        convertClassFields(instance, fieldValue, fieldClazz);
    }

    default void convertClassFields(Object instance, JsonObject source, Class<?> ownerClazz) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Field[] fields = ownerClazz.getDeclaredFields();
        for (Field field : fields) {
            switch (getFieldClassName(field)) {
                case "String", "Long", "Integer" -> convertPrimitiveField(field, source, instance, ownerClazz);
                case "List" -> {
                    Method getter = source.getClass().getMethod("getArray", String.class);
                    JsonArray fieldValue = (JsonArray) getter.invoke(source, field.getName());
                    convertList(field, fieldValue, instance, ownerClazz);
                }
                case "Map" -> {
                    Method getter = source.getClass().getMethod("getObject", String.class);
                    JsonObject fieldValue = (JsonObject) getter.invoke(source, field.getName());
                    convertMsp(field, fieldValue, instance, ownerClazz);
                }
                default -> convertClass(field, source, instance, ownerClazz);
            }
        }
    }
    default <T> T convertDetail(Class<?> detailClazz) {
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
        return (T) getDetail().getClass().cast(getDetail());
    }
}
