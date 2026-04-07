/*
 * Copyright 2026 Haulmont.
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

package io.jmix.core.impl.metadata;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.impl.keyvalue.KeyValueMetaProperty;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Creates detached clones of the metadata session graph for generation-based publication.
 * <p>
 * Intended for code that needs to prepare a new metadata generation without mutating the
 * currently published {@code Session}, {@code MetaClass}, and {@code MetaProperty} objects.
 */
@Component("core_MetadataSessionCloneSupport")
public class MetadataSessionCloneSupport {

    @Autowired(required = false)
    protected List<MetadataSessionCloneMetaPropertyHandler> metaPropertyHandlers;

    /**
     * Clones metadata session state of the specified generation into a detached snapshot that can be safely mutated.
     *
     * @param generation source generation
     * @return cloned session data and remapped metadata objects
     */
    public SessionCloneResult cloneSession(MetadataGeneration generation) {
        return cloneSession(generation.getSession(), generation.getExtendedEntitiesState());
    }

    /**
     * Clones the given metadata session together with its extended-entities state.
     *
     * @param sourceSession source session to copy
     * @param extendedEntitiesState extended-entities state aligned with the source session
     * @return cloned session data and remapped metadata objects
     */
    public SessionCloneResult cloneSession(Session sourceSession,
                                           ExtendedEntities.ExtendedEntitiesState extendedEntitiesState) {
        if (!(sourceSession instanceof SessionImpl sourceSessionImpl)) {
            throw new IllegalArgumentException("Unsupported Session implementation: " + sourceSession.getClass());
        }

        SessionImpl targetSession = new SessionImpl();
        LinkedHashSet<MetaClass> sourceClasses = new LinkedHashSet<>(sourceSession.getClasses());
        IdentityHashMap<MetaClass, MetaClass> metaClassMap = new IdentityHashMap<>();
        IdentityHashMap<MetaProperty, MetaProperty> metaPropertyMap = new IdentityHashMap<>();

        for (MetaClass sourceMetaClass : sourceClasses) {
            MetaClass targetMetaClass = cloneMetaClassSkeleton(targetSession, sourceMetaClass);
            metaClassMap.put(sourceMetaClass, targetMetaClass);
        }

        for (MetaClass sourceMetaClass : sourceClasses) {
            for (MetaProperty sourceMetaProperty : sourceMetaClass.getProperties()) {
                metaPropertyMap.computeIfAbsent(sourceMetaProperty,
                        ignored -> cloneMetaPropertySkeleton(sourceMetaProperty, metaClassMap));
            }
            for (MetaProperty sourceMetaProperty : sourceMetaClass.getOwnProperties()) {
                metaPropertyMap.computeIfAbsent(sourceMetaProperty,
                        ignored -> cloneMetaPropertySkeleton(sourceMetaProperty, metaClassMap));
            }
        }

        for (Map.Entry<MetaProperty, MetaProperty> entry : metaPropertyMap.entrySet()) {
            copyMetaPropertyState(entry.getKey(), entry.getValue(), metaClassMap, metaPropertyMap);
        }

        for (MetaClass sourceMetaClass : sourceClasses) {
            MetaClass targetMetaClass = metaClassMap.get(sourceMetaClass);
            targetMetaClass.getAnnotations().putAll(remapAnnotations(sourceMetaClass.getAnnotations(), metaClassMap, metaPropertyMap));

            if (targetMetaClass instanceof MetaClassImpl targetMetaClassImpl) {
                targetMetaClassImpl.replaceProperties(
                        mapProperties(sourceMetaClass.getProperties(), metaPropertyMap),
                        mapProperties(sourceMetaClass.getOwnProperties(), metaPropertyMap)
                );
                targetMetaClassImpl.replaceHierarchy(
                        mapMetaClasses(sourceMetaClass.getAncestors(), metaClassMap),
                        mapMetaClasses(sourceMetaClass.getDescendants(), metaClassMap)
                );
            } else if (targetMetaClass instanceof KeyValueMetaClass targetKeyValueMetaClass) {
                for (MetaProperty property : mapProperties(sourceMetaClass.getOwnProperties(), metaPropertyMap)) {
                    targetKeyValueMetaClass.addProperty(property);
                }
            }
        }

        targetSession.replaceMappings(
                mapMetaClassMap(sourceSessionImpl.getClassByNameSnapshot(), metaClassMap),
                mapMetaClassMap(sourceSessionImpl.getClassByClassSnapshot(), metaClassMap)
        );

        return new SessionCloneResult(
                targetSession,
                Collections.unmodifiableMap(metaClassMap),
                Collections.unmodifiableMap(metaPropertyMap),
                extendedEntitiesState.remap(metaClassMap)
        );
    }

    protected MetaClass cloneMetaClassSkeleton(Session targetSession, MetaClass sourceMetaClass) {
        if (sourceMetaClass instanceof MetaClassImpl sourceMetaClassImpl) {
            MetaClassImpl targetMetaClass = new MetaClassImpl(targetSession, sourceMetaClassImpl.getName());
            targetMetaClass.setJavaClass(sourceMetaClassImpl.getJavaClass());
            targetMetaClass.setStore(sourceMetaClassImpl.getStore());
            return targetMetaClass;
        } else if (sourceMetaClass instanceof KeyValueMetaClass sourceKeyValueMetaClass) {
            KeyValueMetaClass targetMetaClass = new KeyValueMetaClass();
            targetMetaClass.setStore(sourceKeyValueMetaClass.getStore());
            return targetMetaClass;
        }

        throw new IllegalArgumentException("Unsupported MetaClass implementation: " + sourceMetaClass.getClass());
    }

    protected MetaProperty cloneMetaPropertySkeleton(MetaProperty sourceMetaProperty,
                                                     IdentityHashMap<MetaClass, MetaClass> metaClassMap) {
        MetaClass targetDomain = metaClassMap.get(sourceMetaProperty.getDomain());
        Range targetRange = cloneRange(sourceMetaProperty.getRange(), metaClassMap);
        MetadataSessionCloneMetaPropertyHandler handler = findMetaPropertyHandler(sourceMetaProperty);
        if (handler != null) {
            return handler.cloneMetaProperty(sourceMetaProperty, targetDomain, targetRange);
        } else if (sourceMetaProperty instanceof MetaPropertyImpl sourceMetaPropertyImpl) {
            MetaPropertyImpl targetMetaProperty = new MetaPropertyImpl(targetDomain, sourceMetaPropertyImpl.getName(), false);
            targetMetaProperty.setStore(sourceMetaPropertyImpl.getStore());
            targetMetaProperty.setMandatory(sourceMetaPropertyImpl.isMandatory());
            targetMetaProperty.setReadOnly(sourceMetaPropertyImpl.isReadOnly());
            targetMetaProperty.setType(sourceMetaPropertyImpl.getType());
            targetMetaProperty.setRange(targetRange);
            targetMetaProperty.setAnnotatedElement(sourceMetaPropertyImpl.getAnnotatedElement());
            targetMetaProperty.setJavaType(sourceMetaPropertyImpl.getJavaType());
            targetMetaProperty.setDeclaringClass(sourceMetaPropertyImpl.getDeclaringClass());
            return targetMetaProperty;
        } else if (sourceMetaProperty instanceof KeyValueMetaProperty sourceKeyValueMetaProperty) {
            KeyValueMetaProperty targetMetaProperty = new KeyValueMetaProperty(
                    targetDomain,
                    sourceKeyValueMetaProperty.getName(),
                    sourceKeyValueMetaProperty.getJavaType(),
                    targetRange,
                    sourceKeyValueMetaProperty.getType()
            );
            targetMetaProperty.setStore(sourceKeyValueMetaProperty.getStore());
            return targetMetaProperty;
        }

        throw new IllegalArgumentException("Unsupported MetaProperty implementation: " + sourceMetaProperty.getClass());
    }

    protected void copyMetaPropertyState(MetaProperty sourceMetaProperty,
                                         MetaProperty targetMetaProperty,
                                         IdentityHashMap<MetaClass, MetaClass> metaClassMap,
                                         IdentityHashMap<MetaProperty, MetaProperty> metaPropertyMap) {
        MetaProperty targetInverse = mapMetaProperty(sourceMetaProperty.getInverse(), metaPropertyMap);
        MetadataSessionCloneMetaPropertyHandler handler = findMetaPropertyHandler(sourceMetaProperty);
        if (handler != null) {
            handler.copyMetaPropertyState(sourceMetaProperty, targetMetaProperty, targetInverse);
        } else if (targetMetaProperty instanceof MetaPropertyImpl targetMetaPropertyImpl) {
            targetMetaPropertyImpl.setInverse(targetInverse);
        }

        targetMetaProperty.getAnnotations().putAll(remapAnnotations(
                sourceMetaProperty.getAnnotations(),
                metaClassMap,
                metaPropertyMap
        ));
    }

    protected Range cloneRange(Range sourceRange, IdentityHashMap<MetaClass, MetaClass> metaClassMap) {
        Range targetRange;
        if (sourceRange.isClass()) {
            ClassRange classRange = new ClassRange(metaClassMap.get(sourceRange.asClass()));
            classRange.setCardinality(sourceRange.getCardinality());
            classRange.setOrdered(sourceRange.isOrdered());
            targetRange = classRange;
        } else if (sourceRange.isEnum()) {
            EnumerationRange enumerationRange = new EnumerationRange(sourceRange.asEnumeration());
            enumerationRange.setCardinality(sourceRange.getCardinality());
            enumerationRange.setOrdered(sourceRange.isOrdered());
            targetRange = enumerationRange;
        } else {
            DatatypeRange datatypeRange = new DatatypeRange(sourceRange.asDatatype());
            datatypeRange.setCardinality(sourceRange.getCardinality());
            datatypeRange.setOrdered(sourceRange.isOrdered());
            targetRange = datatypeRange;
        }
        return targetRange;
    }

    protected List<MetaProperty> mapProperties(Collection<MetaProperty> sourceProperties,
                                               IdentityHashMap<MetaProperty, MetaProperty> metaPropertyMap) {
        List<MetaProperty> targetProperties = new ArrayList<>(sourceProperties.size());
        for (MetaProperty sourceProperty : sourceProperties) {
            targetProperties.add(metaPropertyMap.get(sourceProperty));
        }
        return targetProperties;
    }

    protected List<MetaClass> mapMetaClasses(Collection<MetaClass> sourceMetaClasses,
                                             IdentityHashMap<MetaClass, MetaClass> metaClassMap) {
        List<MetaClass> targetMetaClasses = new ArrayList<>(sourceMetaClasses.size());
        for (MetaClass sourceMetaClass : sourceMetaClasses) {
            targetMetaClasses.add(metaClassMap.get(sourceMetaClass));
        }
        return targetMetaClasses;
    }

    protected <K> Map<K, MetaClass> mapMetaClassMap(Map<K, MetaClass> source,
                                                    IdentityHashMap<MetaClass, MetaClass> metaClassMap) {
        Map<K, MetaClass> target = new LinkedHashMap<>();
        for (Map.Entry<K, MetaClass> entry : source.entrySet()) {
            target.put(entry.getKey(), metaClassMap.get(entry.getValue()));
        }
        return target;
    }

    protected Map<String, Object> remapAnnotations(Map<String, Object> sourceAnnotations,
                                                   IdentityHashMap<MetaClass, MetaClass> metaClassMap,
                                                   IdentityHashMap<MetaProperty, MetaProperty> metaPropertyMap) {
        Map<String, Object> targetAnnotations = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : sourceAnnotations.entrySet()) {
            targetAnnotations.put(entry.getKey(), remapAnnotationValue(entry.getValue(), metaClassMap, metaPropertyMap));
        }
        return targetAnnotations;
    }

    protected Object remapAnnotationValue(Object value,
                                          IdentityHashMap<MetaClass, MetaClass> metaClassMap,
                                          IdentityHashMap<MetaProperty, MetaProperty> metaPropertyMap) {
        if (value instanceof MetaClass metaClass) {
            return metaClassMap.get(metaClass);
        }
        if (value instanceof MetaProperty metaProperty) {
            return metaPropertyMap.get(metaProperty);
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> remappedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                remappedMap.put(
                        remapAnnotationValue(entry.getKey(), metaClassMap, metaPropertyMap),
                        remapAnnotationValue(entry.getValue(), metaClassMap, metaPropertyMap)
                );
            }
            return remappedMap;
        }
        if (value instanceof Collection<?> collection) {
            Collection<Object> remappedCollection = value instanceof Set<?> ? new LinkedHashSet<>() : new ArrayList<>();
            for (Object item : collection) {
                remappedCollection.add(remapAnnotationValue(item, metaClassMap, metaPropertyMap));
            }
            return remappedCollection;
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object remappedArray = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(remappedArray, i,
                        remapAnnotationValue(Array.get(value, i), metaClassMap, metaPropertyMap));
            }
            return remappedArray;
        }
        return value;
    }

    protected MetaProperty mapMetaProperty(MetaProperty sourceMetaProperty,
                                           IdentityHashMap<MetaProperty, MetaProperty> metaPropertyMap) {
        return sourceMetaProperty == null ? null : metaPropertyMap.get(sourceMetaProperty);
    }

    protected MetadataSessionCloneMetaPropertyHandler findMetaPropertyHandler(MetaProperty sourceMetaProperty) {
        for (MetadataSessionCloneMetaPropertyHandler handler : metaPropertyHandlers) {
            if (handler.supports(sourceMetaProperty)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Result of cloning a metadata session together with the mappings needed to continue
     * updating the cloned graph.
     * <p>
     * Intended for metadata publication code that needs both the detached {@link SessionImpl}
     * snapshot and the source-to-clone object remapping produced during cloning.
     */
    public static class SessionCloneResult {
        protected final SessionImpl session;
        protected final Map<MetaClass, MetaClass> metaClassMap;
        protected final Map<MetaProperty, MetaProperty> metaPropertyMap;
        protected final ExtendedEntities.ExtendedEntitiesState extendedEntitiesState;

        /**
         * Creates a clone result that exposes the cloned session and remapped metadata objects.
         *
         * @param session cloned session snapshot
         * @param metaClassMap source-to-clone meta-class mapping
         * @param metaPropertyMap source-to-clone meta-property mapping
         * @param extendedEntitiesState remapped extended-entities state for the cloned session
         */
        public SessionCloneResult(SessionImpl session,
                                  Map<MetaClass, MetaClass> metaClassMap,
                                  Map<MetaProperty, MetaProperty> metaPropertyMap,
                                  ExtendedEntities.ExtendedEntitiesState extendedEntitiesState) {
            this.session = session;
            this.metaClassMap = metaClassMap;
            this.metaPropertyMap = metaPropertyMap;
            this.extendedEntitiesState = extendedEntitiesState;
        }

        /**
         * Returns the cloned session snapshot.
         */
        public SessionImpl getSession() {
            return session;
        }

        /**
         * Returns the mapping from source meta classes to their cloned counterparts.
         */
        public Map<MetaClass, MetaClass> getMetaClassMap() {
            return metaClassMap;
        }

        /**
         * Returns the mapping from source meta properties to their cloned counterparts.
         */
        public Map<MetaProperty, MetaProperty> getMetaPropertyMap() {
            return metaPropertyMap;
        }

        /**
         * Returns extended-entities state remapped to the cloned session.
         */
        public ExtendedEntities.ExtendedEntitiesState getExtendedEntitiesState() {
            return extendedEntitiesState;
        }
    }
}
