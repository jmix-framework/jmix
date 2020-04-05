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

package io.jmix.core.impl;

import io.jmix.core.impl.scanning.EntitiesScanner;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.impl.*;
import io.jmix.core.Entity;
import io.jmix.core.entity.annotation.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * INTERNAL.
 * Creates metadata session and loads metadata from annotated Java classes.
 */
@Component(MetadataLoader.NAME)
@Scope("prototype")
public class MetadataLoader {

    public static final String NAME = "jmix_MetadataLoader";

    private final Logger log = LoggerFactory.getLogger(MetadataLoader.class);

//    @Inject
//    protected MetadataBuildSupport metadataBuildSupport;

//    @Inject
//    protected ExtendedEntities extendedEntities;

//    @Inject
//    protected DatatypeRegistry datatypeRegistry;

    protected Session session;

    protected List<String> basePackages;

    @Inject
    public MetadataLoader(EntitiesScanner entitiesScanner, MetaModelLoader metaModelLoader) {
        this.session = new SessionImpl();

        log.info("Initializing metadata");
        long startTime = System.currentTimeMillis();

        basePackages = entitiesScanner.getBasePackages();

//        List<MetadataBuildSupport.XmlFile> metadataXmlList = metadataBuildSupport.init();

        metaModelLoader.loadModel(session, entitiesScanner.getEntityClassNames());

        for (MetaClass metaClass : session.getClasses()) {
            postProcessClass(metaClass);
            initMetaAnnotations(metaClass);
        }

//        initStoreMetaAnnotations(entityPackages);

        // todo extended entities
//        initExtensionMetaAnnotations();

//        List<XmlAnnotations> xmlAnnotations = metadataBuildSupport.getEntityAnnotations(metadataXmlList);
//        for (MetaClass metaClass : session.getClasses()) {
//            addMetaAnnotationsFromXml(xmlAnnotations, metaClass);
//        }
//
//        replaceExtendedMetaClasses();

        log.info("Metadata initialized in {} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * @return loaded session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return list of root packages of all loaded meta-models
     */
    public List<String> getRootPackages() {
        return basePackages;
    }

//    protected void replaceExtendedMetaClasses() {
//            List<Pair<MetaClass, MetaClass>> replaceMap = new ArrayList<>();
//            for (MetaClass metaClass : session.getClasses()) {
//                MetaClass effectiveMetaClass = session.getClass(extendedEntities.getEffectiveClass(metaClass));
//
//                if (effectiveMetaClass != metaClass) {
//                    replaceMap.add(new Pair<>(metaClass, effectiveMetaClass));
//                }
//
//                for (MetaProperty metaProperty : metaClass.getOwnProperties()) {
//                    MetaPropertyImpl propertyImpl = (MetaPropertyImpl) metaProperty;
//
//                    // replace domain
//                    Class effectiveDomainClass = extendedEntities.getEffectiveClass(metaProperty.getDomain());
//                    MetaClass effectiveDomainMeta = session.getClass(effectiveDomainClass);
//                    if (metaProperty.getDomain() != effectiveDomainMeta) {
//                        propertyImpl.setDomain(effectiveDomainMeta);
//                    }
//
//                    if (metaProperty.getRange().isClass()) {
//                        // replace range class
//                        ClassRange range = (ClassRange) metaProperty.getRange();
//
//                        Class effectiveRangeClass = extendedEntities.getEffectiveClass(range.asClass());
//                        MetaClass effectiveRangeMeta = session.getClass(effectiveRangeClass);
//                        if (effectiveRangeMeta != range.asClass()) {
//                            ClassRange newRange = new ClassRange(effectiveRangeMeta);
//                            newRange.setCardinality(range.getCardinality());
//                            newRange.setOrdered(range.isOrdered());
//
//                            ((MetaPropertyImpl) metaProperty).setRange(newRange);
//                        }
//                    }
//                }
//            }
//
//            for (Pair<MetaClass, MetaClass> replace : replaceMap) {
//                MetaClass replacedMetaClass = replace.getFirst();
//                extendedEntities.registerReplacedMetaClass(replacedMetaClass);
//
//                MetaClassImpl effectiveMetaClass = (MetaClassImpl) replace.getSecond();
//                ((SessionImpl) session).registerClass(replacedMetaClass.getName(), replacedMetaClass.getJavaClass(), effectiveMetaClass);
//            }
//    }

//    protected void initStoreMetaAnnotations(Map<String, List<EntityClassInfo>> entityPackages) {
//        if (Stores.getAdditional().isEmpty())
//            return;
//
//        Map<String, String> nameToStoreMap = new HashMap<>();
//        for (List<EntityClassInfo> list : entityPackages.values()) {
//            for (EntityClassInfo entityClassInfo : list) {
//                if (nameToStoreMap.containsKey(entityClassInfo.name)) {
//                    throw new IllegalStateException("Entity cannot belong to more than one store: " + entityClassInfo.name);
//                }
//                nameToStoreMap.put(entityClassInfo.name, entityClassInfo.store);
//            }
//        }
//
//        for (MetaClass metaClass : session.getClasses()) {
//            String className = metaClass.getJavaClass().getName();
//            String store = nameToStoreMap.get(className);
//            if (store != null)
//                metaClass.getAnnotations().put(MetadataTools.STORE_ANN_NAME, store);
//        }
//    }

    /**
     * Initialize connections between extended and base entities.
     */
    protected void initExtensionMetaAnnotations() {
        for (MetaClass metaClass : session.getClasses()) {
            Class<?> javaClass = metaClass.getJavaClass();

            List<Class> superClasses = new ArrayList<>();
            Extends extendsAnnotation = javaClass.getAnnotation(Extends.class);
            while (extendsAnnotation != null) {
                Class<? extends Entity> superClass = extendsAnnotation.value();
                superClasses.add(superClass);
                extendsAnnotation = superClass.getAnnotation(Extends.class);
            }

            for (Class superClass : superClasses) {
                metaClass.getAnnotations().put(Extends.class.getName(), superClass);

                MetaClass superMetaClass = session.getClass(superClass);

                Class<?> extendedByClass = (Class) superMetaClass.getAnnotations().get(ExtendedBy.class.getName());
                if (extendedByClass != null && !javaClass.equals(extendedByClass)) {
                    if (javaClass.isAssignableFrom(extendedByClass))
                        continue;
                    else if (!extendedByClass.isAssignableFrom(javaClass))
                        throw new IllegalStateException(superClass + " is already extended by " + extendedByClass);
                }

                superMetaClass.getAnnotations().put(ExtendedBy.class.getName(), javaClass);
            }
        }
    }

//    /**
//     * Initialize entity annotations from definition in <code>metadata.xml</code>.
//     * <p>Can be overridden in application projects to handle application-specific annotations.</p>
//     *
//     * @param xmlAnnotations map of class name to annotations map
//     * @param metaClass      MetaClass instance to assign annotations
//     */
//    protected void addMetaAnnotationsFromXml(List<XmlAnnotations> xmlAnnotations, MetaClass metaClass) {
//        for (XmlAnnotations xmlAnnotation : xmlAnnotations) {
//            MetaClass metaClassFromXml = session.getClassNN(ReflectionHelper.getClass(xmlAnnotation.entityClass));
//            Class extendedClass = extendedEntities.getExtendedClass(metaClassFromXml);
//            MetaClass effectiveMetaClass = extendedClass != null ? session.getClassNN(extendedClass) : metaClassFromXml;
//            if (effectiveMetaClass.equals(metaClass)) {
//                for (Map.Entry<String, XmlAnnotation> entry : xmlAnnotation.annotations.entrySet()) {
//                    assignMetaAnnotationValueFromXml(entry.getKey(), entry.getValue(), metaClass.getAnnotations());
//                }
//                for (XmlAnnotations attributeAnnotation : xmlAnnotation.attributeAnnotations) {
//                    MetaProperty property = metaClass.getPropertyNN(attributeAnnotation.entityClass);
//                    for (Map.Entry<String, XmlAnnotation> entry : attributeAnnotation.annotations.entrySet()) {
//                        assignMetaAnnotationValueFromXml(entry.getKey(), entry.getValue(), property.getAnnotations());
//                    }
//                }
//                break;
//            }
//        }
//    }

//    @SuppressWarnings("unchecked")
//    protected void assignMetaAnnotationValueFromXml(String annName, XmlAnnotation xmlAnn, Map<String, Object> metaAnnotations) {
//        if (xmlAnn.value != null) {
//            metaAnnotations.put(annName, xmlAnn.value);
//            if (!xmlAnn.attributes.isEmpty()) {
//                log.warn("Attributes of {} meta-annotation are ignored because a value is set", annName);
//            }
//        } else {
//            Object annValue = metaAnnotations.computeIfAbsent(annName, k -> new LinkedHashMap<>());
//            if (annValue instanceof Map) {
//                ((Map) annValue).putAll(xmlAnn.attributes);
//            } else {
//                log.warn("Meta-annotation {} has value {} and cannot be re-assigned by annotation attributes", annName, annValue);
//            }
//        }
//    }

    protected void postProcessClass(MetaClass metaClass) {
        for (MetaProperty property : metaClass.getOwnProperties()) {
            postProcessProperty(metaClass, property);
        }

//        Collection<MetaClass> missingDescendants = new HashSet<>(1);
//
//        findMissingDescendants(metaClass, missingDescendants);
//
//        if (!missingDescendants.isEmpty()) {
//            CollectionUtils.addAll(metaClass.getDescendants(), missingDescendants.iterator());
//
//            MetaClass ancestorMetaClass = metaClass.getAncestor();
//            while (ancestorMetaClass != null) {
//                CollectionUtils.addAll(ancestorMetaClass.getDescendants(), missingDescendants.iterator());
//                ancestorMetaClass = ancestorMetaClass.getAncestor();
//            }
//        }
//
//        MetaClass ancestorMetaClass = metaClass.getAncestor();
//        while (ancestorMetaClass != null) {
//            ((MetaClassImpl) metaClass).addAncestor(ancestorMetaClass);
//            ancestorMetaClass = ancestorMetaClass.getAncestor();
//        }
    }

    protected void postProcessProperty(MetaClass metaClass, MetaProperty metaProperty) {
        // init inverse properties
        MetaProperty inverseProp = metaProperty.getInverse();
        if (inverseProp != null && inverseProp.getInverse() == null) {
            ((MetaPropertyImpl) inverseProp).setInverse(metaProperty);
        }

        if (!metaProperty.getRange().isClass())
            return;

        AnnotatedElement annotatedElement = metaProperty.getAnnotatedElement();

        OnDelete onDelete = annotatedElement.getAnnotation(OnDelete.class);
        if (onDelete != null) {
            Map<String, Object> metaAnnotations = metaClass.getAnnotations();

            MetaProperty[] properties = (MetaProperty[]) metaAnnotations.get(OnDelete.class.getName());
            properties = ArrayUtils.add(properties, metaProperty);
            metaAnnotations.put(OnDelete.class.getName(), properties);
        }

        OnDeleteInverse onDeleteInverse = annotatedElement.getAnnotation(OnDeleteInverse.class);
        if (onDeleteInverse != null) {
            Map<String, Object> metaAnnotations = metaProperty.getRange().asClass().getAnnotations();

            MetaProperty[] properties = (MetaProperty[]) metaAnnotations.get(OnDeleteInverse.class.getName());
            properties = ArrayUtils.add(properties, metaProperty);
            metaAnnotations.put(OnDeleteInverse.class.getName(), properties);
        }
    }

//    protected void findMissingDescendants(MetaClass ancestor, Collection<MetaClass> missingDescendants) {
//        Collection<MetaClass> descendants = ancestor.getDescendants();
//        for (Object descendant : descendants) {
//            missingDescendants.add((MetaClass) descendant);
//            findMissingDescendants((MetaClass) descendant, missingDescendants);
//        }
//    }

    protected void initMetaAnnotations(MetaClass metaClass) {
        for (Annotation annotation : metaClass.getJavaClass().getAnnotations()) {
            if (isOrmAnnotation(annotation)) {
                // todo ORM-based entity
                metaClass.getAnnotations().put("jmix.orm", true);
            }

            MetaAnnotation metaAnnotation = AnnotationUtils.findAnnotation(annotation.getClass(), MetaAnnotation.class);
            if (metaAnnotation != null) {
                String name = annotation.annotationType().getName();

                Map<String, Object> attributes = new LinkedHashMap<>(
                        AnnotationUtils.getAnnotationAttributes(metaClass.getJavaClass(), annotation));
                metaClass.getAnnotations().put(name, attributes);

                Object propagateToSubclasses = attributes.get("propagateToSubclasses");
                if (propagateToSubclasses == null || Boolean.TRUE.equals(propagateToSubclasses)) {
                    for (MetaClass descMetaClass : metaClass.getDescendants()) {
                        Annotation descAnnotation = descMetaClass.getJavaClass().getAnnotation(annotation.annotationType());
                        if (descAnnotation == null) {
                            descMetaClass.getAnnotations().put(name, attributes);
                        }
                    }
                }
            }
        }
    }

    private boolean isOrmAnnotation(Annotation annotation) {
        return javax.persistence.Entity.class.isAssignableFrom(annotation.getClass())
                || javax.persistence.MappedSuperclass.class.isAssignableFrom(annotation.getClass())
                || javax.persistence.Embeddable.class.isAssignableFrom(annotation.getClass());
    }
}
