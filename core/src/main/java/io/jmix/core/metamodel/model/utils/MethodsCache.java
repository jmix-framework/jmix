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
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MethodsCache {

    private final Map<String, Function> getters = new HashMap<>();
    private final Map<String, BiConsumer> setters = new HashMap<>();
    private String className;

    private static byte[] gimmeLookupClassDef() {
        return ("\u00CA\u00FE\u00BA\u00BE\0\0\0001\0\21\1\0\13GimmeLookup\7\0\1\1\0\20"
                + "java/lang/Object\7\0\3\1\0\10<clinit>\1\0\3()V\1\0\4Code\1\0\6lookup\1\0'Ljav"
                + "a/lang/invoke/MethodHandles$Lookup;\14\0\10\0\11\11\0\2\0\12\1\0)()Ljava/lang"
                + "/invoke/MethodHandles$Lookup;\1\0\36java/lang/invoke/MethodHandles\7\0\15\14\0"
                + "\10\0\14\12\0\16\0\17\26\1\0\2\0\4\0\0\0\1\20\31\0\10\0\11\0\0\0\1\20\11\0\5\0"
                + "\6\0\1\0\7\0\0\0\23\0\3\0\3\0\0\0\7\u00B8\0\20\u00B3\0\13\u00B1\0\0\0\0\0\0")
                .getBytes(StandardCharsets.ISO_8859_1);
    }

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
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get") && method.getParameterTypes().length == 0
                    && !Modifier.isStatic(method.getModifiers())) {
                Function getter = createGetter(clazz, method);
                name = StringUtils.uncapitalize(name.substring(3));
                getters.put(name, getter);
            } else if (name.startsWith("is") && method.getParameterTypes().length == 0
                    && !Modifier.isStatic(method.getModifiers())) {
                Function getter = createGetter(clazz, method);
                Field isField = ReflectionUtils.findField(clazz, name);
                if (isField != null) {
                    // for Kotlin entity with a property which name starts with "is*" the getter name will be the same as property name,
                    // e.g "isApproved"
                    getters.put(name, getter);
                } else {
                    name = StringUtils.uncapitalize(name.substring(2));
                    getters.put(name, getter);
                }
            } else if (name.startsWith("set") && method.getParameterTypes().length == 1
                    && !Modifier.isStatic(method.getModifiers())) {
                BiConsumer setter = createSetter(clazz, method);
                Field isField = ReflectionUtils.findField(clazz, "is" + name.substring(3));
                if (isField != null) {
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
        className = clazz.toString();
    }

    private Function createGetter(Class clazz, Method method) {
        Function getter;
        try {
            MethodHandles.Lookup caller = SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_11) ?
                    getLookup(clazz) :
                    MethodHandles.lookup();
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
            MethodHandles.Lookup caller = SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_11) ?
                    getLookup(clazz) :
                    MethodHandles.lookup();
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

    private MethodHandles.Lookup getLookup(Class clazz) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        Class gimmeLookup;
        //This code needed to classloader of clazz and classloader of lambda function be same
        try {
            gimmeLookup = ReflectUtils.defineClass("GimmeLookup", gimmeLookupClassDef(), clazz.getClassLoader());
        } catch (Exception e) {
            gimmeLookup = clazz.getClassLoader().loadClass("GimmeLookup");
        }
        return (MethodHandles.Lookup) gimmeLookup.getField("lookup").get(null);
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
