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

package io.jmix.flowui.xml.layout.loader;

import com.google.common.base.Splitter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import io.jmix.core.ClassManager;
import io.jmix.core.DevelopmentException;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringComponent("flowui_PropertiesLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PropertiesLoaderSupport {

    private static final Logger log = LoggerFactory.getLogger(PropertiesLoaderSupport.class);

    protected final ComponentLoader.Context context;

    protected List<PropertyParser> parsers;
    protected ClassManager classManager;
    protected LoaderSupport loaderSupport;

    public PropertiesLoaderSupport(ComponentLoader.Context context) {
        this.context = context;
    }

    @Autowired
    public void setParsers(List<PropertyParser> parsers) {
        this.parsers = parsers;
    }

    @Autowired
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    public void loadProperties(Object target, Element element) {
        Element propertiesElement = element.element("properties");
        if (propertiesElement != null) {
            for (Element propertyElement : propertiesElement.elements("property")) {
                loadProperty(target, propertyElement);
            }
        }
    }

    protected void loadProperty(Object target, Element element) {
        String propertyName = loaderSupport.loadString(element, "name")
                .orElseThrow(() -> new GuiDevelopmentException("Missing required 'name' attribute", context));

        String setterName = "set" + StringUtils.capitalize(propertyName);
        try {
            Method method = Arrays.stream(target.getClass().getMethods())
                    .filter(m -> m.getName().equals(setterName) && m.getParameterCount() == 1)
                    .findAny()
                    .orElseThrow(() -> new GuiDevelopmentException("Unable to set target property '" +
                            propertyName + "': cannot find setter method with single parameter", context));

            Class<?> parameterType = method.getParameterTypes()[0];
            Type genericParameterType = method.getGenericParameterTypes()[0];

            String stringValue = loaderSupport.loadString(element, "value")
                    .orElseThrow(() -> new GuiDevelopmentException("Missing required 'value' attribute", context));

            Object value = loaderSupport.loadString(element, "type")
                    .map(type ->
                            parseValueByType(new PropertyParsingContext(
                                    target, propertyName, stringValue, type, context
                            ))
                    )
                    .orElseGet(() ->
                            parseInternal(stringValue, parameterType, genericParameterType));

            method.invoke(target, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DevelopmentException("Unable to set target property '" + propertyName, e);
        }
    }

    protected Object parseValueByType(PropertyParsingContext context) {
        for (PropertyParser parser : parsers) {
            if (parser.supports(context)) {
                return parser.parse(context);
            }
        }

        throw new IllegalArgumentException(
                String.format("Can't parse component property for the '%s' type", context.type()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    protected Object parseInternal(String stringValue, Type propType, @Nullable Type genericParameterType) {
        Object value = null;

        if (String.class == propType) {
            value = stringValue;

        } else if (Class.class == propType) {
            value = classManager.loadClass(stringValue);

        } else if (Boolean.class == propType || Boolean.TYPE == propType) {
            value = Boolean.valueOf(stringValue);

        } else if (Byte.class == propType || Byte.TYPE == propType) {
            value = parseNumber(stringValue, Byte.class);

        } else if (Short.class == propType || Short.TYPE == propType) {
            value = parseNumber(stringValue, Short.class);

        } else if (Integer.class == propType || Integer.TYPE == propType) {
            value = parseNumber(stringValue, Integer.class);

        } else if (Long.class == propType || Long.TYPE == propType) {
            value = parseNumber(stringValue, Long.class);

        } else if (Float.class == propType || Float.TYPE == propType) {
            value = parseNumber(stringValue, Float.class);

        } else if (Double.class == propType || Double.TYPE == propType) {
            value = parseNumber(stringValue, Double.class);

        } else if (List.class == propType) {
            value = parseList(stringValue, genericParameterType);

        } else if (Set.class == propType) {
            value = parseSet(stringValue, genericParameterType);

        } else if (propType instanceof Class<?> aClass && aClass.isEnum()) {
            value = Enum.valueOf((Class<Enum>) aClass, stringValue);

        } else if (propType instanceof Class<?> aClass && aClass.isArray()) {
            value = parseArray(stringValue, aClass.getComponentType());

        }

        if (value == null) {
            log.warn("Unable to set value {} for property of type {}", stringValue, propType);
        }

        return value;
    }

    protected Object parseNumber(String stringValue, Class<? extends Number> numberType) {
        if (!NumberUtils.isParsable(stringValue)) {
            throw new GuiDevelopmentException(String.format("Unable to parse '%s' as '%s'", stringValue, numberType),
                    context);
        }
        return org.springframework.util.NumberUtils.parseNumber(stringValue, numberType);
    }

    @Nullable
    protected Object parseList(String stringValue, @Nullable Type genericParameterType) {
        Stream<?> stream = parseStream(stringValue, genericParameterType, null);
        return stream != null ? stream.toList() : null;
    }

    @Nullable
    protected Object parseSet(String stringValue, @Nullable Type genericParameterType) {
        Stream<?> stream = parseStream(stringValue, genericParameterType, null);
        return stream != null ? stream.collect(Collectors.toSet()) : null;
    }

    @Nullable
    protected Object parseArray(String stringValue, Type arrayItemType) {
        Preconditions.checkNotNullArgument(arrayItemType);

        Stream<?> stream = parseStream(stringValue, null, arrayItemType);
        return stream != null
                ? stream.toArray(size -> ((Object[]) Array.newInstance(((Class<?>) arrayItemType), size)))
                : null;
    }

    @Nullable
    protected Stream<?> parseStream(String stringValue,
                                    @Nullable Type genericParameterType,
                                    @Nullable Type arrayItemType) {
        Type itemType;
        if (genericParameterType instanceof ParameterizedType parameterizedType) {
            itemType = parameterizedType.getActualTypeArguments()[0];
        } else if (arrayItemType != null) {
            itemType = arrayItemType;
        } else {
            return null;
        }

        return split(stringValue).stream()
                .map(s -> parseInternal(s, itemType, null));
    }

    protected List<String> split(String stringValue) {
        return Splitter.onPattern("[\\s,]+")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(stringValue);
    }
}
