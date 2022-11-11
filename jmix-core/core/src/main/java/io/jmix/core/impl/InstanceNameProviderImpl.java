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

package io.jmix.core.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.method.ArgumentResolverComposite;
import io.jmix.core.impl.method.ContextArgumentResolverComposite;
import io.jmix.core.impl.method.MethodArgumentsProvider;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("core_InstanceNameProvider")
public class InstanceNameProviderImpl implements InstanceNameProvider {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MetadataTools metadataTools;

    private final Logger log = LoggerFactory.getLogger(InstanceNameProviderImpl.class);

    protected ContextArgumentResolverComposite resolvers;

    protected MethodArgumentsProvider methodArgumentsProvider;

    // stores methods in the execution order, all methods are accessible
    protected LoadingCache<MetaClass, Optional<InstanceNameRec>> instanceNameRecCache =
            CacheBuilder.newBuilder()
                    .build(new CacheLoader<MetaClass, Optional<InstanceNameRec>>() {
                        @Override
                        public Optional<InstanceNameRec> load(@Nonnull MetaClass metaClass) {
                            return Optional.ofNullable(parseNamePattern(metaClass));
                        }
                    });

    public static class InstanceNameRec {
        /**
         * Name pattern string format
         */
        public final String format;

        /**
         * Formatting method name or null
         */
        @Nullable
        public final Method method;

        /**
         * Array of name properties
         */
        public final MetaProperty[] nameProperties;

        public InstanceNameRec(String format, @Nullable Method method, MetaProperty[] nameProperties) {
            this.format = format;
            this.method = method;
            this.nameProperties = nameProperties;
        }
    }

    public ArgumentResolverComposite getResolvers() {
        return resolvers;
    }

    @Autowired
    public void setResolvers(ContextArgumentResolverComposite resolvers) {
        this.resolvers = resolvers;
        this.methodArgumentsProvider = new MethodArgumentsProvider(resolvers);
    }

    @Override
    public String getInstanceName(Object instance) {
        checkNotNullArgument(instance, "instance is null");

        MetaClass metaClass = metadata.getClass(instance);

        Optional<InstanceNameRec> optional = instanceNameRecCache.getUnchecked(metaClass);
        if (!optional.isPresent()) {
            return instance.toString();
        }

        InstanceNameRec rec = optional.get();

        if (rec.method != null) {
            try {
                Object result = rec.method.invoke(instance, methodArgumentsProvider.getMethodArgumentValues(rec.method));
                return (String) result;
            } catch (Exception e) {
                throw new RuntimeException("Error getting instance name", e);
            }
        }

        Object[] values = new Object[rec.nameProperties.length];
        for (int i = 0; i < rec.nameProperties.length; i++) {
            MetaProperty property = rec.nameProperties[i];

            Object value = EntityValues.getValue(instance, property.getName());
            values[i] = metadataTools.format(value, property);
        }

        return String.format(rec.format, values);
    }

    @Override
    public Collection<MetaProperty> getInstanceNameRelatedProperties(MetaClass metaClass, boolean useOriginal) {
        Optional<InstanceNameRec> optional = instanceNameRecCache.getUnchecked(metaClass);
        if (!optional.isPresent() && useOriginal) {
            MetaClass original = extendedEntities.getOriginalMetaClass(metaClass);
            if (original != null) {
                optional = instanceNameRecCache.getUnchecked(original);
            }
        }
        return optional.map(instanceNameRec -> Arrays.asList(instanceNameRec.nameProperties)).orElse(Collections.emptyList());
    }

    protected Collection<MetaProperty> getInstanceNameProperties(MetaClass metaClass, @Nullable Method nameMethod, @Nullable MetaProperty nameProperty) {
        final Collection<MetaProperty> properties = new HashSet<>();
        if (nameMethod != null) {
            return getPropertiesFromAnnotation(metaClass, nameMethod.getAnnotation(DependsOnProperties.class));
        }
        if (nameProperty != null) {
            properties.add(nameProperty);
            DependsOnProperties annotation = nameProperty.getAnnotatedElement().getAnnotation(DependsOnProperties.class);
            properties.addAll(getPropertiesFromAnnotation(metaClass, annotation));
        }
        return properties;
    }

    private List<MetaProperty> getPropertiesFromAnnotation(MetaClass metaClass, @Nullable DependsOnProperties annotation) {
        return annotation == null ?
                Collections.emptyList() :
                Arrays.stream(annotation.value())
                        .map(metaClass::getProperty)
                        .collect(Collectors.toList());
    }

    @Nullable
    public InstanceNameRec parseNamePattern(MetaClass metaClass) {
        Method method = null;
        MetaProperty nameProperty = null;
        List<Method> instanceNameMethods = Stream.of(metaClass.getJavaClass().getMethods())
                .filter(m -> AnnotatedElementUtils.findMergedAnnotation(m, InstanceName.class) != null)
                .collect(Collectors.toList());
        List<MetaProperty> nameProperties = metaClass.getProperties().stream()
                .filter(p -> p.getAnnotatedElement().getAnnotation(InstanceName.class) != null)
                .collect(Collectors.toList());
        if (!instanceNameMethods.isEmpty()) {
            method = instanceNameMethods.get(0);
        } else if (!nameProperties.isEmpty()) {
            nameProperty = nameProperties.get(0);
        }
        if (instanceNameMethods.isEmpty() && nameProperties.isEmpty()) {
            return null;
        }
        validateInstanceNameAnnotation(metaClass, instanceNameMethods, nameProperties);
        return new InstanceNameRec("%s", method,
                getInstanceNameProperties(metaClass, method, nameProperty).stream()
                        .toArray(MetaProperty[]::new));
    }

    private void validateInstanceNameAnnotation(MetaClass metaClass, List<Method> instanceNameMethods, List<MetaProperty> nameProperties) {
        if (instanceNameMethods.size() > 1) {
            log.warn("Multiple @InstanceName annotated methods found in {} class, method {} will be used for instance name",
                    metaClass.getName(),
                    instanceNameMethods.get(0));

        } else if (instanceNameMethods.size() == 1 && !nameProperties.isEmpty()) {
            log.warn("@InstanceName annotated method and @InstanceName annotated properties found in {} class, " +
                            "method {} will be used for instance name",
                    metaClass.getName(),
                    instanceNameMethods.get(0));
        } else if (nameProperties.size() > 1) {
            log.warn("Multiple @InstanceName annotated properties found in {} class, property {} will be used for instance name",
                    metaClass.getName(),
                    nameProperties.get(0));
        }
    }
}
