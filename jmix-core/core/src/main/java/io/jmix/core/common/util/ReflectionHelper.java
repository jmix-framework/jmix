/*
 * Copyright 2019 Haulmont.
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
package io.jmix.core.common.util;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.dom4j.Element;

import jakarta.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to simplify work with Java reflection.
 */
public final class ReflectionHelper {

    private ReflectionHelper() {
    }

    /**
     * Load class by name.
     *
     * @param name class FQN or primitive type name
     * @return class instance
     * @throws ClassNotFoundException if not found
     */
    public static Class<?> loadClass(String name) throws ClassNotFoundException {
        switch (name) {
            case "int":
                return int.class;
            case "short":
                return short.class;
            case "char":
                return char.class;
            case "byte":
                return byte.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean":
                return boolean.class;
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            throw new IllegalStateException("Current thread context classloader is null. " +
                    "Consider setting it in a new thread using 'Thread.currentThread().setContextClassLoader()' " +
                    "to the classloader of the parent thread or executing class.");
        }
        return contextClassLoader.loadClass(name);
    }

    /**
     * Load class by name, wrapping a {@link ClassNotFoundException} into unchecked exception.
     *
     * @param name class FQN
     * @return class instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String name) {
        try {
            return (Class<T>) loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiates an object by appropriate constructor.
     *
     * @param cls    class
     * @param params constructor arguments
     * @return created object instance
     * @throws NoSuchMethodException if the class has no constructor matching the given arguments
     */
    public static <T> T newInstance(Class<T> cls, Object... params) throws NoSuchMethodException {
        Class[] paramTypes = getParamTypes(params);

        Constructor<T> constructor = ConstructorUtils.getMatchingAccessibleConstructor(cls, paramTypes);
        if (constructor == null)
            throw new NoSuchMethodException(
                    String.format("Cannot find a matching constructor for %s and given parameters", cls.getName()));
        try {
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot create instance of " + cls, e);
        }
    }

    /**
     * Searches for a method by its name and arguments.
     *
     * @param c      class
     * @param name   method name
     * @param params method arguments
     * @return method reference or null if a suitable method not found
     */
    @Nullable
    public static Method findMethod(Class<?> c, String name, Object... params) {
        Class[] paramTypes = getParamTypes(params);

        Method method = null;
        try {
            method = c.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            try {
                method = c.getMethod(name, paramTypes);
            } catch (NoSuchMethodException e1) {
                Class superclass = c.getSuperclass();
                if (superclass != null) {
                    method = findMethod(superclass, name, params);
                }
            }
        }
        if (method != null) {
            method.setAccessible(true);
        }

        return method;
    }

    /**
     * Invokes a method by reflection.
     *
     * @param obj    object instance
     * @param name   method name
     * @param params method arguments
     * @return method result
     * @throws NoSuchMethodException if a suitable method not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object obj, String name, Object... params) throws NoSuchMethodException {
        Class[] paramTypes = getParamTypes(params);

        final Class<?> aClass = obj.getClass();
        Method method;
        try {
            method = aClass.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            method = aClass.getMethod(name, paramTypes);
        }
        method.setAccessible(true);
        try {
            return (T) method.invoke(obj, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to invoke method " + name, e);
        }
    }

    /**
     * Constructs an array of argument types from an array of actual values. Values can not contain nulls.
     *
     * @param params arguments
     * @return the array of argument types
     */
    public static Class[] getParamTypes(Object... params) {
        List<Class> paramClasses = new ArrayList<>();
        for (Object param : params) {
            if (param == null)
                throw new IllegalStateException("Null parameter");

            final Class aClass = param.getClass();
            if (List.class.isAssignableFrom(aClass)) {
                paramClasses.add(List.class);
            } else if (Set.class.isAssignableFrom(aClass)) {
                paramClasses.add(Set.class);
            } else if (Map.class.isAssignableFrom(aClass)) {
                paramClasses.add(Map.class);
            } else if (Element.class.isAssignableFrom(aClass)) {
                paramClasses.add(Element.class);
            } else {
                paramClasses.add(aClass);
            }
        }
        return paramClasses.toArray(new Class<?>[0]);
    }

    /**
     * Useful to get real entity property value and avoid unfetched attribute exception.
     *
     * @return field value
     */
    public static Object getFieldValue(Object entity, String property) {
        Class javaClass = entity.getClass();
        Field field = null;
        try {
            field = javaClass.getDeclaredField(property);
        } catch (NoSuchFieldException e) {
            Class superclass = javaClass.getSuperclass();
            while (superclass != null) {
                try {
                    field = superclass.getDeclaredField(property);
                    break;
                } catch (NoSuchFieldException e1) {
                    superclass = superclass.getSuperclass();
                }
            }
            if (field == null)
                throw new RuntimeException("Field not found: " + property);
        }
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot get field:" + property);
        }
    }
}