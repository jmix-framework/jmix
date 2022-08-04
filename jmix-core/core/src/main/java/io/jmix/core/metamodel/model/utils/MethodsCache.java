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
package io.jmix.core.metamodel.model.utils;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MethodsCache {

    private final Map<String, Function> getters = new HashMap<>();
    private final Map<String, BiConsumer> setters = new HashMap<>();
    private String className;

    private static final Map<Class, Class> primitivesToObjects = new ImmutableMap.Builder<Class, Class>()
            .put(byte.class, Byte.class)
            .put(char.class, Character.class)
            .put(short.class, Short.class)
            .put(int.class, Integer.class)
            .put(long.class, Long.class)
            .put(float.class, Float.class)
            .put(double.class, Double.class)
            .put(boolean.class, Boolean.class)
            .build();

    private static final Map<Class, MethodsCache> methodCacheMap = new ConcurrentHashMap<>();

    public static MethodsCache getOrCreate(Class clazz) {
        return methodCacheMap.computeIfAbsent(clazz, MethodsCache::new);
    }

    private MethodsCache(Class clazz) {
        final Method[] methods = clazz.getMethods();
        Map<String, Method> getterMethods = new HashMap<>();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()))
                continue;

            String name = method.getName();
            if (name.startsWith("get") && method.getParameterTypes().length == 0) {
                name = StringUtils.uncapitalize(name.substring(3));
                getterMethods.put(name, chooseGetter(clazz, name, method, getterMethods.get(name)));
            } else if (name.startsWith("is") && method.getParameterTypes().length == 0) {
                // for Kotlin entity with a property which name starts with "is*" the getter name will be the same as property name,
                // e.g "isApproved".
                Field isField = ReflectionUtils.findField(clazz, name);
                Field regularField = ReflectionUtils.findField(clazz, StringUtils.uncapitalize(name.substring(3)));
                if (isField == null || regularField != null) {//property name is not the same as getter name
                    name = StringUtils.uncapitalize(name.substring(2));
                }
                getterMethods.put(name, chooseGetter(clazz, name, method, getterMethods.get(name)));
            } else if (name.startsWith("set") && method.getParameterTypes().length == 1) {
                BiConsumer setter = createSetter(clazz, method);
                Field isField = ReflectionUtils.findField(clazz, "is" + name.substring(3));
                Field regularField = ReflectionUtils.findField(clazz, StringUtils.uncapitalize(name.substring(3)));//must prefer exact field instead of kotlin-style field getter
                if (isField != null && regularField == null) {
                    name = "is" + name.substring(3);
                } else {
                    name = StringUtils.uncapitalize(name.substring(3));
                }
                if (setters.containsKey(name)) {
                    BiConsumer containedSetter = setters.get(name);
                    Class valueType = method.getParameterTypes()[0];
                    ((SettersHolder) containedSetter).addSetter(valueType.isPrimitive() ?
                            primitivesToObjects.get(valueType) : valueType, setter);
                } else {
                    Class valueType = method.getParameterTypes()[0];
                    SettersHolder settersHolder = new SettersHolder(name,
                            valueType.isPrimitive() ? primitivesToObjects.get(valueType) : valueType,
                            setter);
                    setters.put(name, settersHolder);
                }
            }
        }

        for (Map.Entry<String, Method> entry : getterMethods.entrySet()) {
            getters.put(entry.getKey(), createGetter(clazz, entry.getValue()));
        }

        className = clazz.toString();
    }

    /**
     * <p>Resolves conflicts between "is&lt;propertyName&gt;()" and "get&lt;propertyName&gt()" getters.</p>
     * <b>"isProperty()"</b> will be returned in two cases:
     * <ol>
     *     <li>{@code isProperty()} is a method-based attribute </li>
     *     or
     *     <li>{@code isProperty()} represents getter for {@link Boolean} field while {@code getProperty()} does not, i.e.:
     *         <ul>
     *             <li>{@code clazz} contains {@link Boolean} property with corresponding name (see {@code propertyName})</li>
     *             <li>{@code isProperty()} returns {@link Boolean} (not {@code boolean})</li>
     *             <li>{@code getProperty()} does not return {@link Boolean}</li>
     *             <li>{@code getProperty()} is not a method-based attribute</li>
     *          </ul>
     *     </li>
     * </ol>
     * <p><b>"getProperty()"</b> will be returned otherwise</p>
     *
     * @param clazz        entity class with property
     * @param propertyName name of property
     * @param found        new getter for property with name {@code propertyName}
     * @param existed      already processed getter for the same property
     * @return method chosen as described above or {@code found} method if {@code existed} is null
     */
    private Method chooseGetter(Class clazz, String propertyName, Method found, @Nullable Method existed) {
        if (existed == null)
            return found;
        //check if one of getters is a method-based attribute
        if (found.getAnnotation(JmixProperty.class) != null)
            return found;
        if (existed.getAnnotation(JmixProperty.class) != null)
            return existed;

        Method isMethod = found.getName().startsWith("is") ? found : existed;
        Method getMethod = found.getName().startsWith("is") ? existed : found;

        Field propertyField = ReflectionUtils.findField(clazz, propertyName);//it may be entity attribute

        if (propertyField != null
                && Boolean.class.isAssignableFrom(propertyField.getType())
                && isMethod.getReturnType().equals(Boolean.class)
                && !getMethod.getReturnType().equals(Boolean.class)) {
            return isMethod;
        }

        return getMethod;
    }

    private Function createGetter(Class clazz, Method method) {
        Function getter;
        try {
            MethodHandles.Lookup caller = MethodHandles.lookup();
            CallSite site = LambdaMetafactory.metafactory(caller,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    caller.findVirtual(clazz, method.getName(), MethodType.methodType(method.getReturnType())),
                    MethodType.methodType(method.getReturnType(), clazz));
            MethodHandle factory = site.getTarget();
            getter = (Function) factory.invoke();
        } catch (Throwable t) {
            throw new RuntimeException("Can not create getter", t);
        }

        return getter;
    }

    private BiConsumer createSetter(Class clazz, Method method) {
        Class valueType = method.getParameterTypes()[0];
        BiConsumer setter;
        try {
            MethodHandles.Lookup caller = MethodHandles.lookup();
            CallSite site = LambdaMetafactory.metafactory(caller,
                    "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    caller.findVirtual(clazz, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()[0])),
                    MethodType.methodType(void.class, clazz, valueType.isPrimitive() ? primitivesToObjects.get(valueType) : valueType));
            MethodHandle factory = site.getTarget();
            setter = (BiConsumer) factory.invoke();
        } catch (Throwable t) {
            throw new RuntimeException("Can not create setter", t);
        }

        return setter;
    }

    /**
     * @param property name of property associated with getter
     * @return lambda {@link Function} which represents getter
     * @throws IllegalArgumentException if getter for property not found
     */
    public Function getGetter(String property) {
        Function getter = getters.get(property);
        if (getter == null) {
            throw new IllegalArgumentException(
                    String.format("Can't find getter for property '%s' at %s", property, className));
        }
        return getter;
    }

    /**
     * @param property name of property associated with setter
     * @return lambda {@link BiConsumer} which represents setter
     * @throws IllegalArgumentException if setter for property not found
     */
    public BiConsumer getSetter(String property) {
        BiConsumer setter = setters.get(property);
        if (setter == null) {
            throw new IllegalArgumentException(
                    String.format("Can't find setter for property '%s' at %s", property, className));
        }
        return setter;
    }

    private class SettersHolder implements BiConsumer {

        protected Map<Class, BiConsumer> setters = new HashMap<>();
        protected BiConsumer defaultSetter;
        protected String property;

        SettersHolder(String property, Class defaultArgType, BiConsumer defaultSetter) {
            this.property = property;
            this.defaultSetter = defaultSetter;
            this.setters.put(defaultArgType, defaultSetter);
        }

        public void addSetter(Class argType, BiConsumer setter) {
            setters.put(argType, setter);
        }

        @Override
        public void accept(Object object, Object value) {
            if (setters.size() == 1 || value == null) {
                defaultSetter.accept(object, value);
                return;
            }
            boolean setterNotFound = true;
            for (Map.Entry<Class, BiConsumer> entry : setters.entrySet()) {
                if (entry.getKey().isInstance(value)) {
                    setterNotFound = false;
                    entry.getValue().accept(object, value);
                }
            }
            if (setterNotFound) {
                throw new IllegalArgumentException(String.format(
                        "Can't find setter for property '%s' at %s for value class: %s",
                        property,
                        className,
                        value.getClass().getSimpleName()
                ));
            }
        }
    }
}
