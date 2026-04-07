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

package io.jmix.core;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.entity.annotation.ReplaceEntity;
import io.jmix.core.entity.annotation.ReplacedByEntity;
import io.jmix.core.impl.MetadataImpl;
import io.jmix.core.impl.metadata.MetadataGenerationManager;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.SessionImplementation;
import io.jmix.core.metamodel.model.impl.ClassRange;
import io.jmix.core.metamodel.model.impl.MetaClassImpl;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates functionality for working with extended entities.
 *
 */
@Component("core_ExtendedEntities")
public class ExtendedEntities {

    protected Metadata metadata;

    protected Map<Class<?>, MetaClass> replacedMetaClasses = new HashMap<>();
    protected volatile ExtendedEntitiesState currentState = ExtendedEntitiesState.of(replacedMetaClasses);

    @Autowired
    protected ObjectProvider<MetadataGenerationManager> metadataGenerationManagerProvider;

    @Autowired
    public ExtendedEntities(Metadata metadata) {
        this.metadata = metadata;
        replaceExtendedMetaClasses(getBootstrapSession());
        currentState = ExtendedEntitiesState.of(replacedMetaClasses);
    }

    protected Session getBootstrapSession() {
        if (metadata instanceof MetadataImpl metadataImpl) {
            return metadataImpl.getRawSession();
        }
        return metadata.getSession();
    }

    protected void replaceExtendedMetaClasses(Session session) {
        List<Pair<MetaClass, MetaClass>> replaceMap = new ArrayList<>();
        for (MetaClass metaClass : session.getClasses()) {
            MetaClass effectiveMetaClass = session.getClass(getEffectiveClass(metaClass));

            if (effectiveMetaClass != metaClass) {
                replaceMap.add(new Pair<>(metaClass, effectiveMetaClass));
            }

            for (MetaProperty metaProperty : metaClass.getOwnProperties()) {
                MetaPropertyImpl propertyImpl = (MetaPropertyImpl) metaProperty;

                // replace domain
                Class<?> effectiveDomainClass = getEffectiveClass(metaProperty.getDomain());
                MetaClass effectiveDomainMeta = session.getClass(effectiveDomainClass);
                if (metaProperty.getDomain() != effectiveDomainMeta) {
                    propertyImpl.setDomain(effectiveDomainMeta);
                }

                if (metaProperty.getRange().isClass()) {
                    // replace range class
                    ClassRange range = (ClassRange) metaProperty.getRange();

                    Class<?> effectiveRangeClass = getEffectiveClass(range.asClass());
                    MetaClass effectiveRangeMeta = session.getClass(effectiveRangeClass);
                    if (effectiveRangeMeta != range.asClass()) {
                        ClassRange newRange = new ClassRange(effectiveRangeMeta);
                        newRange.setCardinality(range.getCardinality());
                        newRange.setOrdered(range.isOrdered());

                        ((MetaPropertyImpl) metaProperty).setRange(newRange);
                    }
                }
            }
        }

        for (Pair<MetaClass, MetaClass> replace : replaceMap) {
            MetaClass replacedMetaClass = replace.getFirst();
            registerReplacedMetaClass(replacedMetaClass);

            MetaClassImpl effectiveMetaClass = (MetaClassImpl) replace.getSecond();
            ((SessionImplementation) session).registerClass(replacedMetaClass.getName(), replacedMetaClass.getJavaClass(), effectiveMetaClass);
        }
    }

    /**
     * Searches for an extended entity and returns it if found, otherwise returns the original entity.
     *
     * @param originalMetaClass original entity
     * @return extended or original entity
     */
    public Class<?> getEffectiveClass(MetaClass originalMetaClass) {
        Class<?> extClass = getExtendedClass(originalMetaClass);
        return extClass == null ? originalMetaClass.getJavaClass() : extClass;
    }

    /**
     * Searches for an extended entity and returns it if found, otherwise returns the original entity.
     *
     * @param originalClass original entity
     * @return extended or original entity
     */
    public Class<?> getEffectiveClass(Class<?> originalClass) {
        return getEffectiveClass(metadata.getSession().getClass(originalClass));
    }

    /**
     * Searches for an extended entity and returns it if found, otherwise returns the original entity.
     *
     * @param entityName original entity
     * @return extended or original entity
     */
    public Class<?> getEffectiveClass(String entityName) {
        return getEffectiveClass(metadata.getSession().getClass(entityName));
    }

    /**
     * Searches for an extended entity and returns it if found, otherwise returns the original entity.
     *
     * @param originalMetaClass original entity
     * @return extended or original entity
     */
    public MetaClass getEffectiveMetaClass(MetaClass originalMetaClass) {
        if (originalMetaClass instanceof KeyValueMetaClass)
            return originalMetaClass;
        return metadata.getSession().getClass(getEffectiveClass(originalMetaClass));
    }

    /**
     * Searches for an extended entity and returns it if found, otherwise returns the original entity.
     *
     * @param originalClass original entity
     * @return extended or original entity
     */
    public MetaClass getEffectiveMetaClass(Class<?> originalClass) {
        return metadata.getSession().getClass(getEffectiveClass(originalClass));
    }

    /**
     * Searches for an extended entity and returns it if found, otherwise returns the original entity.
     *
     * @param entityName original entity
     * @return extended or original entity
     */
    public MetaClass getEffectiveMetaClass(String entityName) {
        return getEffectiveMetaClass(metadata.getSession().getClass(entityName));
    }

    /**
     * Searches for an extended entity and returns it if found.
     *
     * @param originalMetaClass original entity
     * @return extended entity or null if the provided entity has no extension
     */
    @Nullable
    public Class<?> getExtendedClass(MetaClass originalMetaClass) {
        return (Class<?>) originalMetaClass.getAnnotations().get(ReplacedByEntity.class.getName());
    }

    /**
     * Searches for an original entity for the provided extended entity.
     *
     * @param extendedMetaClass extended entity
     * @return original entity or null if the provided entity is not an extension
     */
    @Nullable
    public Class<?> getOriginalClass(MetaClass extendedMetaClass) {
        return (Class<?>) extendedMetaClass.getAnnotations().get(ReplaceEntity.class.getName());
    }

    /**
     * Searches for an original entity for the provided extended entity.
     *
     * @param extendedMetaClass extended entity
     * @return original entity or null if the provided entity is not an extension
     */
    @Nullable
    public MetaClass getOriginalMetaClass(MetaClass extendedMetaClass) {
        Class<?> originalClass = getOriginalClass(extendedMetaClass);
        if (originalClass == null) {
            return null;
        }

        MetaClass metaClass = getState().getReplacedMetaClasses().get(originalClass);
        if (metaClass != null) {
            return metaClass;
        }

        return metadata.getSession().getClass(originalClass);
    }

    /**
     * Searches for an original entity for the provided extended entity.
     *
     * @param extendedEntityName extended entity
     * @return original entity or null if the provided entity is not an extension
     */
    @Nullable
    public MetaClass getOriginalMetaClass(String extendedEntityName) {
        return getOriginalMetaClass(metadata.getSession().getClass(extendedEntityName));
    }

    /**
     * @return original meta class or received meta class if it's not extended
     */
    public MetaClass getOriginalOrThisMetaClass(MetaClass metaClass) {
        MetaClass originalMetaClass = getOriginalMetaClass(metaClass);
        return originalMetaClass != null ? originalMetaClass : metaClass;
    }

    /**
     * INTERNAL. Import replaced meta class from metadata.
     */
    @Internal
    public void registerReplacedMetaClass(MetaClass metaClass) {
        replacedMetaClasses.put(metaClass.getJavaClass(), metaClass);
        currentState = ExtendedEntitiesState.of(replacedMetaClasses);
    }

    /**
     * Returns the extended-entities state visible to the current metadata generation.
     *
     * <p>Intended for infrastructure that needs a stable replacement map for the whole request or data operation.</p>
     */
    public ExtendedEntitiesState getCurrentStateSnapshot() {
        MetadataGenerationManager metadataGenerationManager = metadataGenerationManagerProvider.getIfAvailable();
        if (metadataGenerationManager != null) {
            return metadataGenerationManager.getPinnedOrCurrentGeneration().getExtendedEntitiesState();
        }
        return currentState;
    }

    /**
     * Returns the extended-entities state built from the bootstrap metadata session.
     *
     * <p>Intended for initialization of the first published metadata generation.</p>
     */
    public ExtendedEntitiesState getBootstrapState() {
        return currentState;
    }

    protected ExtendedEntitiesState getState() {
        MetadataGenerationManager metadataGenerationManager = metadataGenerationManagerProvider.getIfAvailable();
        if (metadataGenerationManager != null) {
            return metadataGenerationManager.getPinnedOrCurrentGeneration().getExtendedEntitiesState();
        }
        return currentState;
    }

    public static class ExtendedEntitiesState {

        protected final Map<Class<?>, MetaClass> replacedMetaClasses;

        protected ExtendedEntitiesState(Map<Class<?>, MetaClass> replacedMetaClasses) {
            this.replacedMetaClasses = Collections.unmodifiableMap(replacedMetaClasses);
        }

        /**
         * Creates an immutable snapshot of replaced meta classes.
         *
         * @param replacedMetaClasses replacement mapping to snapshot
         * @return immutable state snapshot
         */
        public static ExtendedEntitiesState of(Map<Class<?>, MetaClass> replacedMetaClasses) {
            return new ExtendedEntitiesState(new LinkedHashMap<>(replacedMetaClasses));
        }

        /**
         * Returns replaced meta classes keyed by original Java type.
         */
        public Map<Class<?>, MetaClass> getReplacedMetaClasses() {
            return replacedMetaClasses;
        }

        /**
         * Rebuilds the state for a cloned metadata session.
         *
         * @param metaClassMap mapping from source meta classes to cloned meta classes
         * @return state remapped to the cloned session
         */
        public ExtendedEntitiesState remap(Map<MetaClass, MetaClass> metaClassMap) {
            Map<Class<?>, MetaClass> remapped = new LinkedHashMap<>();
            for (Map.Entry<Class<?>, MetaClass> entry : replacedMetaClasses.entrySet()) {
                remapped.put(entry.getKey(), metaClassMap.get(entry.getValue()));
            }
            return new ExtendedEntitiesState(remapped);
        }
    }
}
