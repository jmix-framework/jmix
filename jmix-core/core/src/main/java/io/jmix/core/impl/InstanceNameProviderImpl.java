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
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("core_InstanceNameProvider")
public class InstanceNameProviderImpl implements InstanceNameProvider {

    public static final String UNFETCHED_EXCEPTION_MESSAGE_PREFIX = "Cannot get unfetched attribute [";

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

    @Autowired
    protected CoreProperties coreProperties;

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

    protected static class EvaluationResult {

        public final String instanceName;
        public final IllegalStateException exception;

        public EvaluationResult(String instanceName) {
            this.instanceName = instanceName;
            this.exception = null;
        }

        public EvaluationResult(IllegalStateException exception) {
            this.instanceName = "";
            this.exception = exception;
        }

        public boolean isSuccessful() {
            return exception == null;
        }

        //should be called only in case of EvaluationResult#exception is specified
        public String getUnfetchedAttributeName() {
            String message = Objects.requireNonNull(exception, "Invalid EvaluationResult usage").getMessage();
            return message.substring(UNFETCHED_EXCEPTION_MESSAGE_PREFIX.length(), message.indexOf("]"));
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
    public boolean isInstanceNameDefined(Class<?> aClass) {
        MetaClass metaClass = metadata.getClass(aClass);
        return instanceNameRecCache.getUnchecked(metaClass).isPresent();
    }

    @Override
    public String getInstanceName(Object instance) {
        return getInstanceName(instance, metadata.getClass(instance));
    }

    @Override
    public String getInstanceName(Object instance, Class<?> clazz) {
        return getInstanceName(instance, metadata.getClass(clazz));
    }

    @Override
    public String getInstanceName(Object instance, MetaClass metaClass) {
        checkNotNullArgument(instance, "instance is null");

        Optional<InstanceNameRec> optional = instanceNameRecCache.getUnchecked(metaClass);
        if (optional.isEmpty()) {
            return instance.toString();
        }

        InstanceNameRec rec = optional.get();

        EvaluationResult result = getInstanceName(rec, instance);
        if (result.isSuccessful()) {
            return result.instanceName;
        } else {
            if (metaClass.getAncestor() != null) {
                if (coreProperties.isInstanceNameFallbackEnabled()) {
                    log.debug("Error getting instance name for {} as instance of {} because of unfetched attribute '{}'. " +
                                    "Trying to get instance name for entity as instance of {}.",
                            instance,
                            metaClass.getName(),
                            result.getUnfetchedAttributeName(),
                            metaClass.getAncestor().getName());
                    return getInstanceName(instance, metaClass.getAncestor());
                } else {
                    throw new RuntimeException(
                            String.format("Error getting instance name for %s as instance of %s because of unfetched attributes. " +
                                            "Fallback to ancestor instance name definition is disabled " +
                                            "(see `jmix.core.instance-name-fallback-enabled` property). `).",
                                    instance,
                                    metaClass.getName()),
                            result.exception);
                }
            } else {
                throw new RuntimeException(String.format("Error getting instance name for %s as instance of %s because of unfetched attributes. " +
                                "No ancestors to get fallback instance name definition.",
                        instance,
                        metaClass.getName()),
                        result.exception);
            }
        }
    }

    protected EvaluationResult getInstanceName(InstanceNameRec rec, Object instance) {
        if (rec.method != null) {
            try {
                Object result = rec.method.invoke(instance, methodArgumentsProvider.getMethodArgumentValues(rec.method));
                return new EvaluationResult((String) result);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    Throwable target = ((InvocationTargetException) e).getTargetException();
                    if (target instanceof IllegalStateException && target.getMessage().startsWith(UNFETCHED_EXCEPTION_MESSAGE_PREFIX)) {
                        return new EvaluationResult((IllegalStateException) target);
                    }
                }
                throw new RuntimeException("Error getting instance name", e);
            }
        } else {
            try {
                Object[] values = new Object[rec.nameProperties.length];
                for (int i = 0; i < rec.nameProperties.length; i++) {
                    MetaProperty property = rec.nameProperties[i];

                    Object value = EntityValues.getValue(instance, property.getName());
                    values[i] = metadataTools.format(value, property);
                }

                return new EvaluationResult(String.format(rec.format, values));
            } catch (Exception e) {
                if (e instanceof IllegalStateException && e.getMessage().startsWith(UNFETCHED_EXCEPTION_MESSAGE_PREFIX)) {
                    return new EvaluationResult((IllegalStateException) e);
                }
                throw new RuntimeException("Error getting instance name", e);
            }
        }
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
        MetaProperty selectedNameProperty = null;
        List<Method> instanceNameMethods = Stream.of(metaClass.getJavaClass().getDeclaredMethods())
                .filter(m -> AnnotatedElementUtils.findMergedAnnotation(m, InstanceName.class) != null)
                .collect(Collectors.toList());
        List<MetaProperty> nameProperties = metaClass.getProperties().stream()
                .filter(p -> p.getAnnotatedElement().getAnnotation(InstanceName.class) != null)
                .filter(p -> !metadataTools.isMethodBased(p))
                .collect(Collectors.toList());
        if (!instanceNameMethods.isEmpty()) {
            method = instanceNameMethods.get(0);
            method.setAccessible(true);
        } else if (!nameProperties.isEmpty()) {
            selectedNameProperty = nameProperties.get(0);

            for (int i = 1; i < nameProperties.size(); i++) {
                MetaProperty current = nameProperties.get(i);
                //check for null just in case: should not happen for @InstanceName-annotated property
                if (selectedNameProperty.getDeclaringClass() != null && current.getDeclaringClass() != null
                        && !current.getDeclaringClass().isAssignableFrom(selectedNameProperty.getDeclaringClass())) {
                    selectedNameProperty = current;//use the one declared in extending class
                }
            }
        } else {
            if (metaClass.getAncestor() != null) {
                InstanceNameRec ancestorRec = parseNamePattern(metaClass.getAncestor());
                if (ancestorRec != null) {
                    //find descendant class corresponding MetaProperties
                    MetaProperty[] actualProperties = new MetaProperty[ancestorRec.nameProperties.length];
                    for (int i = 0; i < ancestorRec.nameProperties.length; i++) {
                        actualProperties[i] = metaClass.getProperty(ancestorRec.nameProperties[i].getName());
                        if (actualProperties[i] == null)
                            throw new RuntimeException("Ancestor property is not registered in descendant. Should not happen.");
                    }
                    return new InstanceNameRec(ancestorRec.format, ancestorRec.method, actualProperties);
                }
            }
            return null;
        }
        validateInstanceNameAnnotation(metaClass, instanceNameMethods, nameProperties, selectedNameProperty);
        return new InstanceNameRec("%s", method,
                getInstanceNameProperties(metaClass, method, selectedNameProperty).stream()
                        .toArray(MetaProperty[]::new));
    }

    private void validateInstanceNameAnnotation(MetaClass metaClass,
                                                List<Method> instanceNameMethods,
                                                List<MetaProperty> nameProperties,
                                                @Nullable MetaProperty selectedNameProperty) {
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
                    selectedNameProperty);
        }
    }
}
