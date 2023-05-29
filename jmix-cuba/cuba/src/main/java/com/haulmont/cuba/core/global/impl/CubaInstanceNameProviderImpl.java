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

package com.haulmont.cuba.core.global.impl;

import io.jmix.core.DevelopmentException;
import io.jmix.core.InstanceNameProvider;
import io.jmix.core.impl.InstanceNameProviderImpl;
import com.haulmont.chile.core.annotations.NamePattern;
import io.jmix.core.impl.MetadataLoader;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link InstanceNameProvider} implementation for CUBA compatible module to support {@link NamePattern} annotation
 */
public class CubaInstanceNameProviderImpl extends InstanceNameProviderImpl {

    private final Logger log = LoggerFactory.getLogger(CubaInstanceNameProviderImpl.class);

    private static final Pattern INSTANCE_NAME_SPLIT_PATTERN = Pattern.compile("[,;]");

    @Nullable
    @Override
    public InstanceNameRec parseNamePattern(MetaClass metaClass) {
        InstanceNameRec namePattern = super.parseNamePattern(metaClass);
        if (namePattern != null) {
            return namePattern;
        }
        Map attributes = (Map) metaClass.getAnnotations().get(NamePattern.class.getName());
        if (attributes == null)
            return null;
        String pattern = (String) attributes.get("value");
        if (StringUtils.isBlank(pattern))
            return null;

        int pos = pattern.indexOf("|");
        if (pos < 0)
            throw new DevelopmentException("Invalid name pattern: " + pattern);

        String format = StringUtils.substring(pattern, 0, pos);
        String trimmedFormat = format.trim();
        String methodName = trimmedFormat.startsWith("#") ? trimmedFormat.substring(1) : null;
        Method method = null;
        if (methodName != null) {
            try {
                method = Stream.of(metaClass.getJavaClass().getDeclaredMethods())
                        .filter(m -> m.getName().equals(methodName))
                        .findFirst().orElseThrow(NoSuchMethodException::new);
            } catch (NoSuchMethodException e) {
                log.error("Instance name method {} not found in meta class {}", methodName, metaClass.getName(), e);
                throw new RuntimeException(
                        String.format("Instance name method %s not found in meta class %s", methodName, metaClass.getName()),
                        e);
            }
        }
        String fieldsStr = StringUtils.substring(pattern, pos + 1);
        MetaProperty[] fields = INSTANCE_NAME_SPLIT_PATTERN.splitAsStream(fieldsStr)
                .map(metaClass::getProperty)
                .toArray(MetaProperty[]::new);
        return new InstanceNameRec(format, method, fields);
    }
}
