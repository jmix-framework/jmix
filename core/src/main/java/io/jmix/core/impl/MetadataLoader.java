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

import io.jmix.core.entity.annotation.MetaAnnotation;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.impl.scanning.EntityDetector;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import io.jmix.core.metamodel.model.impl.SessionImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * INTERNAL.
 * Creates metadata session and loads metadata from annotated Java classes.
 */
@Component(MetadataLoader.NAME)
@Scope("prototype")
public class MetadataLoader {

    public static final String NAME = "core_MetadataLoader";

    private final Logger log = LoggerFactory.getLogger(MetadataLoader.class);

    protected Session session;

    @Autowired
    public MetadataLoader(JmixModulesClasspathScanner classpathScanner, MetaModelLoader metaModelLoader) {
        this.session = new SessionImpl();

        log.trace("Initializing metadata");
        long startTime = System.currentTimeMillis();

        metaModelLoader.loadModel(session, classpathScanner.getClassNames(EntityDetector.class));

        for (MetaClass metaClass : session.getClasses()) {
            postProcessClass(metaClass);
            initMetaAnnotations(metaClass);
        }

        // todo extended entities

        log.info("Metadata initialized in {} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * @return loaded session
     */
    public Session getSession() {
        return session;
    }

    protected void postProcessClass(MetaClass metaClass) {
        for (MetaProperty property : metaClass.getOwnProperties()) {
            postProcessProperty(metaClass, property);
        }
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
