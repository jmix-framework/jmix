/*
 * Copyright 2020 Haulmont.
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

package io.jmix.flowui.app.jmxconsole;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AttributeHelper {

    public static Object convert(String type, String str) {
        if (str == null)
            return null;

        if (String.class.getName().equals(type)) {
            return str;
        } else if ("int".equals(type) || Integer.class.getName().equals(type)) {
            return Integer.valueOf(str);
        } else if ("long".equals(type) || Long.class.getName().equals(type)) {
            return Long.valueOf(str);
        } else if ("double".equals(type) || Double.class.getName().equals(type)) {
            return Double.valueOf(str);
        } else if ("float".equals(type) || Float.class.getName().equals(type)) {
            return Float.valueOf(str);
        } else if ("boolean".equals(type) || Boolean.class.getName().equals(type)) {
            return Boolean.valueOf(str);
        } else if (ObjectName.class.getName().equals(type)) {
            try {
                return new ObjectName(str);
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException(e);
            }
        } else if (UUID.class.getName().equals(type)) {
            return UUID.fromString(str);
        }
        return null;
    }

    public static boolean isBoolean(String type) {
        return "boolean".equals(type) || Boolean.class.getName().equals(type);
    }

    public static boolean isArrayOrCollection(String type) {
        return isCollection(type) || isArray(type);
    }

    public static boolean isCollection(String type) {
        return isList(type) || isSet(type);
    }

    public static boolean isArray(String type) {
        return type != null
                && (type.startsWith("[") && type.length() >= 2 && getArrayType(type) != null
                || type.endsWith("[]"));
    }

    public static boolean isObjectArrayOrCollection(String type) {
        return isCollection(type) || isObjectArray(type);
    }

    public static boolean isObjectArray(String type) {
        return type != null && isArray(type) && !isPrimitiveTypeOrString(type);
    }

    public static boolean isPrimitiveTypeOrString(String type) {
        if (type.endsWith("[]")) {
            try {
                type = type.substring(0, type.length() - 2);
                Class.forName(type);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Wrong array type");
            }
        } else {
            type = type.substring(0, 2);
        }
        switch (type) {
            case "java.lang.Boolean":
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.lang.Char":
            case "java.lang.String":
            case "[Z":
            case "[B":
            case "[S":
            case "[I":
            case "[J":
            case "[F":
            case "[D":
            case "[C":
                return true;
        }
        return false;
    }

    public static boolean isList(String type) {
        return "java.util.List".equals(type);
    }

    public static boolean isSet(String type) {
        return "java.util.Set".equals(type);
    }

    public static Class getArrayType(String type) {
        if (type.endsWith("[]")) {
            try {
                return Class.forName(type.substring(0, type.length() - 2));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Wrong array type");
            }
        }
        switch (type.substring(0, 2)) {
            case "[Z":
                return boolean.class;
            case "[B":
                return byte.class;
            case "[S":
                return short.class;
            case "[I":
                return int.class;
            case "[J":
                return long.class;
            case "[F":
                return float.class;
            case "[D":
                return double.class;
            case "[C":
                return char.class;
            case "[L":
                return Object.class;     // any non-primitives(Object)
        }
        throw new RuntimeException("Wrong array type");
    }

    public static String convertTypeToReadableName(String type) {
        if (isArray(type)) {
            Class clazz = getArrayType(type);
            return substringAfterLastDot(clazz.getName()) + "[]";
        } else {
            return substringAfterLastDot(type);
        }
    }

    public static String substringAfterLastDot(String type) {
        return type.substring(type.lastIndexOf(".") + 1).trim();
    }

    public static boolean isDate(String type) {
        return type != null && Date.class.getName().endsWith(type);
    }

    public static String convertToString(Object value) {
        if (value == null) {
            return null;
        }

        if (value.getClass().isArray()) {
            StringBuilder b = new StringBuilder("[");
            int length = Array.getLength(value);
            String values = IntStream.range(0, length)
                    .mapToObj(i -> convertToString(Array.get(value, i)))
                    .collect(Collectors.joining(","));
            b.append(values);
            return b.append("]").toString();
        } else if (value instanceof CompositeData) {
            return compositeToString((CompositeData) value);
        } else if (value instanceof TabularData) {
            return tabularToString((TabularData) value);
        }
        return value.toString();
    }

    private static String tabularToString(TabularData tabularData) {
        TabularType type = tabularData.getTabularType();
        StringBuilder b = new StringBuilder();
        b.append("(").append(type.getTypeName()).append(")\n");
        Collection<CompositeData> values = (Collection) tabularData.values();
        String rows = values.stream()
                .map(AttributeHelper::compositeToString)
                .collect(Collectors.joining());
        b.append(rows);
        return b.toString();
    }

    private static String compositeToString(CompositeData compositeData) {
        if (canConvertToTrueObject(compositeData)) {
            try {
                Object trueObject = convertToTrueObject(compositeData);
                return String.valueOf(trueObject);
            } catch (Exception e) {
                return e.getClass().getName() + " " + e.getMessage();
            }
        }

        CompositeType type = compositeData.getCompositeType();

        StringBuilder b = new StringBuilder();
        b.append("[");
        List<String> keys = new ArrayList<>(type.keySet());
        Collections.sort(keys); // alphabetically
        String keysAndValuesString = keys.stream()
                .map(key -> String.format("%s: %s", key, convertToString(compositeData.get(key))))
                .collect(Collectors.joining(","));
        b.append(keysAndValuesString);
        b.append("]\n");
        return b.toString();
    }

    /*
     * Try to find factory method with signature like
     * public static VMOption from(CompositeData compositeData) { ... }
     */
    private static Object convertToTrueObject(CompositeData compositeData) {
        CompositeType type = compositeData.getCompositeType();
        try {
            Class<?> _class = Class.forName(type.getTypeName());
            Method method = _class.getMethod("from", CompositeData.class);
            if (Modifier.isStatic(method.getModifiers()) && method.getReturnType() == _class) {
                return method.invoke(null, compositeData);
            }
            return null;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean canConvertToTrueObject(CompositeData compositeData) {
        CompositeType type = compositeData.getCompositeType();
        try {
            Class<?> _class = Class.forName(type.getTypeName());
            Method method = _class.getMethod("from", CompositeData.class);
            if (Modifier.isStatic(method.getModifiers())
                    && Modifier.isPublic(method.getModifiers())
                    && method.getReturnType() == _class) {
                return true;
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return false;
        }

        return false;
    }
}