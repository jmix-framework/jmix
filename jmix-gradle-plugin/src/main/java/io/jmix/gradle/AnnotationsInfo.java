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

package io.jmix.gradle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collects and holds information about annotations needed to enhance entity.
 * Differs inherited and declared annotations. Stores references to related fields and classes.
 */
public class AnnotationsInfo {
    public static final String LEGACY_INTERFACE_CREATABLE = "com.haulmont.cuba.core.entity.Creatable";
    public static final String LEGACY_INTERFACE_UPDATABLE = "com.haulmont.cuba.core.entity.Updatable";
    public static final String LEGACY_INTERFACE_SOFT_DELETE = "com.haulmont.cuba.core.entity.SoftDelete";
    public static final String LEGACY_INTERFACE_HAS_UUID = "com.haulmont.cuba.core.entity.HasUuid";

    private Multimap<FieldAnnotation, CtField> declaredFields = HashMultimap.create();
    private Multimap<FieldAnnotation, CtField> inheritedFields = HashMultimap.create();

    private Set<ClassAnnotation> declaredClassAnnotations = new HashSet<>();
    private Map<ClassAnnotation, CtClass> inheritedClassAnnotations = new HashMap<>();

    private CtClass entityClass;

    private AnnotationsInfo(CtClass entityClass) {
        this.entityClass = entityClass;
    }

    public static AnnotationsInfo forClass(CtClass entityClass) throws NotFoundException {
        AnnotationsInfo info = new AnnotationsInfo(entityClass);

        Set<String> interfaceNames = new HashSet<>();
        CtClass current = entityClass;

        while (current.getSuperclass() != null) {
            List<String> currentInterfaces = Arrays.stream(current.getInterfaces()).map(CtClass::getName).collect(Collectors.toList());
            interfaceNames.addAll(currentInterfaces);

            if (currentInterfaces.contains(LEGACY_INTERFACE_HAS_UUID)) {//legacy support
                info.putClassAnnotation(ClassAnnotation.LEGACY_HAS_UUID, current);
            }

            AnnotationsAttribute attribute = (AnnotationsAttribute) current.getClassFile2().getAttribute(AnnotationsAttribute.visibleTag);
            if (attribute != null) {
                for (Annotation annotation : attribute.getAnnotations()) {
                    ClassAnnotation known = ClassAnnotation.find(annotation.getTypeName());
                    if (known != null) {
                        info.putClassAnnotation(known, current);
                    }
                }
            }

            for (CtField field : current.getDeclaredFields()) {
                AnnotationsAttribute annotationsInfo = (AnnotationsAttribute) field.getFieldInfo2().getAttribute(AnnotationsAttribute.visibleTag);

                if (annotationsInfo != null) {
                    for (Annotation annotation : annotationsInfo.getAnnotations()) {
                        FieldAnnotation known = FieldAnnotation.find(annotation.getTypeName());
                        if (known != null) {
                            info.putField(known, field);
                        }
                    }
                }
            }
            current = current.getSuperclass();
        }

        //legacy support
        if (interfaceNames.contains(LEGACY_INTERFACE_CREATABLE)) {
            info.putField(FieldAnnotation.CREATED_BY, entityClass.getField("createdBy"));
            info.putField(FieldAnnotation.CREATED_DATE, entityClass.getField("createTs"));
        }
        if (interfaceNames.contains(LEGACY_INTERFACE_UPDATABLE)) {
            info.putField(FieldAnnotation.LAST_MODIFIED_BY, entityClass.getField("updatedBy"));
            info.putField(FieldAnnotation.LAST_MODIFIED_DATE, entityClass.getField("updateTs"));
        }

        if (interfaceNames.contains(LEGACY_INTERFACE_SOFT_DELETE)) {
            info.putField(FieldAnnotation.DELETED_DATE, entityClass.getField("deleteTs"));
            info.putField(FieldAnnotation.DELETED_BY, entityClass.getField("deletedBy"));
        }

        return info;
    }


    protected void putField(FieldAnnotation annotation, CtField field) {
        CtField alreadyAnnotated = getAnnotatedField(annotation);
        if (alreadyAnnotated != null && annotation.unique
                && !isSameField(alreadyAnnotated, field)) {
            throw new RuntimeException(String.format("More than one @%s field in %s: %s#%s, %s#%s",
                    annotation.getSimpleName(),
                    entityClass.getName(),
                    alreadyAnnotated.getDeclaringClass().getSimpleName(), alreadyAnnotated.getName(),
                    field.getDeclaringClass().getSimpleName(), field.getName()
            ));
        }

        if (entityClass.getName().equals(field.getDeclaringClass().getName())) {
            declaredFields.put(annotation, field);
        } else {
            inheritedFields.put(annotation, field);
        }
    }


    private boolean isSameField(CtField first, CtField second) {
        return Objects.equals(first.getName(), second.getName())
                && Objects.equals(first.getDeclaringClass().getName(), second.getDeclaringClass().getName());
    }

    protected void putClassAnnotation(ClassAnnotation annotation, CtClass ctClass) {
        if (entityClass.getName().equals(ctClass.getName())) {
            declaredClassAnnotations.add(annotation);
        } else {
            inheritedClassAnnotations.put(annotation, ctClass);
        }
    }

    @Nullable
    public CtField getAnnotatedField(FieldAnnotation annotation) {
        if (declaredFields.containsKey(annotation)) {
            Collection<CtField> ctFields = declaredFields.get(annotation);
            return ctFields.isEmpty() ? null : ctFields.iterator().next();
        }

        Collection<CtField> ctFields = inheritedFields.get(annotation);
        return ctFields.isEmpty() ? null : ctFields.iterator().next();
    }

    public List<CtField> getAnnotatedFields(FieldAnnotation annotation) {
        List<CtField> list = new ArrayList<>();
        list.addAll(inheritedFields.get(annotation));
        list.addAll(declaredFields.get(annotation));
        return list;
    }

    public boolean hasClassAnnotation(ClassAnnotation annotation) {
        return declaredClassAnnotations.contains(annotation) || inheritedClassAnnotations.containsKey(annotation);
    }

    public boolean hasMetadataChanges() {
        return !declaredFields.isEmpty() || !declaredClassAnnotations.isEmpty();
    }

    @Nullable
    public CtField getPrimaryKey() {
        CtField result = getAnnotatedField(FieldAnnotation.ID);
        if (result == null) {
            result = getAnnotatedField(FieldAnnotation.EMBEDDED_ID);
            if (result == null) {
                result = getAnnotatedField(FieldAnnotation.JMIX_ID);
            }
        }
        return result;
    }

    public enum FieldAnnotation {
        CREATED_BY("org.springframework.data.annotation.CreatedBy"),
        CREATED_DATE("org.springframework.data.annotation.CreatedDate"),
        LAST_MODIFIED_BY("org.springframework.data.annotation.LastModifiedBy"),
        LAST_MODIFIED_DATE("org.springframework.data.annotation.LastModifiedDate"),

        DELETED_BY("io.jmix.core.annotation.DeletedBy"),
        DELETED_DATE("io.jmix.core.annotation.DeletedDate"),

        VERSION("jakarta.persistence.Version"),

        ID("jakarta.persistence.Id"),
        EMBEDDED_ID("jakarta.persistence.EmbeddedId"),
        JMIX_ID("io.jmix.core.entity.annotation.JmixId"),
        JMIX_GENERATED_VALUE("io.jmix.core.entity.annotation.JmixGeneratedValue", false);

        private final String className;
        private final boolean unique;


        FieldAnnotation(String className) {
            this(className, true);
        }

        FieldAnnotation(String className, boolean unique) {
            this.className = className;
            this.unique = unique;
        }

        public static FieldAnnotation find(String className) {
            for (FieldAnnotation annotation : values()) {
                if (annotation.className.equals(className))
                    return annotation;
            }
            return null;
        }

        /**
         * @return non-qualified annotation class name
         */
        public String getSimpleName() {
            return className.substring(className.lastIndexOf('.') + 1);
        }
    }


    public enum ClassAnnotation {
        JMIX_ENTITY(MetaModelUtil.JMIX_ENTITY_ANNOTATION_TYPE),
        EMBEDDABLE(MetaModelUtil.EMBEDDABLE_ANNOTATION_TYPE),
        LEGACY_HAS_UUID("LEGACY_HAS_UUID");

        private final String className;

        ClassAnnotation(String className) {
            this.className = className;
        }

        public static ClassAnnotation find(String className) {
            for (ClassAnnotation annotation : values()) {
                if (annotation.className.equals(className))
                    return annotation;
            }
            return null;
        }
    }
}
