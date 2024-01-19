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

package io.jmix.chartsflowui.kit.component.event;

import elemental.json.JsonObject;
import io.jmix.chartsflowui.kit.component.event.dto.JmixClickEventDetail;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface JmixChartDetailEvent<T> {
    JsonObject getDetailJson();
    Object getDetail();
    void setDetail(Object detailObject);
    default T loadDetail() {
        return convertDetail(JmixClickEventDetail.class);
    }

    default String getFieldClassName(Field field) {
        return field.getType().getName().replace(field.getType().getPackageName() + ".", "");
    }

    default void convertPrimitiveField(Field field, JsonObject source, Object target, Class<?> detailClazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String fieldClassName = getFieldClassName(field);
        String fieldGetterName = switch (fieldClassName) {
            case "Long", "Integer", "Double" -> "Number";
            default -> "String";
        };
        Method getter = source.getClass().getMethod("get" + fieldGetterName, String.class);
        Object detailValue = getter.invoke(source, field.getName());
        Object convertedValue = switch (fieldClassName) {
            case "Long" -> detailValue.getClass().getMethod("longValue").invoke(detailValue);
            case "Integer" -> detailValue.getClass().getMethod("intValue").invoke(detailValue);
            default -> detailValue;
        };
        Method setter = detailClazz.getMethod("set" + StringUtils.capitalize(field.getName()), Class.forName(field.getType().getName()));
        setter.invoke(target, convertedValue);
    }

    default void convertClass(Field field, JsonObject source, Object target, Class<?> detailClazz) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> fieldClazz = Class.forName(field.getType().getName());
        Object instance = fieldClazz.getConstructor().newInstance();
        Method setter = detailClazz.getMethod("set" + StringUtils.capitalize(field.getName()), Class.forName(field.getType().getName()));
        setter.invoke(target, instance);

        Method getter = source.getClass().getMethod("getObject", String.class);
        JsonObject fieldValue = (JsonObject) getter.invoke(source, field.getName());

        Field[] instanceFields = fieldClazz.getDeclaredFields();
        for (Field instanceField : instanceFields) {

            switch (getFieldClassName(instanceField)) {
                case "String", "Long", "Integer" -> convertPrimitiveField(instanceField, fieldValue, instance, fieldClazz);
                case "List" -> System.out.println("List");
                default -> convertClass(instanceField, fieldValue, instance, detailClazz);
            }
        }
    }

    default <T> T convertDetail(Class<?> detailClazz) {
        if (getDetail() == null) {
            Field[] fields = detailClazz.getDeclaredFields();
            try {
                setDetail(detailClazz.getConstructor().newInstance());
                for (Field field : fields) {
                    switch (getFieldClassName(field)) {
                        case "String", "Long", "Integer" -> convertPrimitiveField(field, getDetailJson(), getDetail(), detailClazz);
                        default -> convertClass(field, getDetailJson(), getDetail(), detailClazz);
                    }
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException
                     | NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) getDetail().getClass().cast(getDetail());
    }
}
