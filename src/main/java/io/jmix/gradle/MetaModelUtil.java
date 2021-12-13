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

package io.jmix.gradle;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class MetaModelUtil {
    public static final String ENTITY_TYPE = "io.jmix.core.Entity";
    public static final String ENTITY_ENTRY_TYPE = "io.jmix.core.EntityEntry";
    public static final String BASE_ENTITY_ENTRY_TYPE = "io.jmix.core.entity.BaseEntityEntry";
    public static final String EMBEDDABLE_ENTITY_ENTRY_TYPE = "io.jmix.core.entity.EmbeddableEntityEntry";
    public static final String NULLABLE_ID_ENTITY_ENTRY_TYPE = "io.jmix.core.entity.NullableIdEntityEntry";
    public static final String NO_ID_ENTITY_ENTRY_TYPE = "io.jmix.core.entity.NoIdEntityEntry";

    public static final String SETTERS_ENHANCED_TYPE = "io.jmix.core.entity.JmixSettersEnhanced";
    public static final String ENTITY_ENTRY_ENHANCED_TYPE = "io.jmix.core.entity.JmixEntityEntryEnhanced";

    public static final String TRANSIENT_ANNOTATION_TYPE = "javax.persistence.Transient";
    public static final String JMIX_PROPERTY_ANNOTATION_TYPE = "io.jmix.core.metamodel.annotation.JmixProperty";
    public static final String DISABLE_ENHANCING_ANNOTATION_TYPE = "io.jmix.core.entity.annotation.DisableEnhancing";
    public static final String JMIX_ENTITY_ANNOTATION_TYPE = "io.jmix.core.metamodel.annotation.JmixEntity";
    public static final String ENTITY_ANNOTATION_TYPE = "javax.persistence.Entity";
    public static final String EMBEDDABLE_ANNOTATION_TYPE = "javax.persistence.Embeddable";
    public static final String CONVERTER_ANNOTATION_TYPE = "javax.persistence.Converter";
    public static final String STORE_ANNOTATION_TYPE = "io.jmix.core.metamodel.annotation.Store";
    public static final String REPLACE_ENTITY_ANNOTATION_TYPE = "io.jmix.core.entity.annotation.ReplaceEntity";

    public static final String GET_ENTITY_ENTRY_METHOD_NAME = "__getEntityEntry";
    public static final String COPY_ENTITY_ENTRY_METHOD_NAME = "__copyEntityEntry";
    public static final String WRITE_OBJECT_METHOD_NAME = "writeObject";
    public static final String READ_OBJECT_METHOD_NAME = "readObject";

    public static final String GEN_ENTITY_ENTRY_VAR_NAME = "_jmixEntityEntry";
    public static final String GEN_ENTITY_ENTRY_CLASS_NAME = "JmixEntityEntry";

    public static boolean isSettersEnhanced(CtClass ctClass) throws NotFoundException {
        for (CtClass ctInterface : ctClass.getInterfaces()) {
            if (Objects.equals(ctInterface.getName(), SETTERS_ENHANCED_TYPE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEntityEntryEnhanced(CtClass ctClass) throws NotFoundException {
        for (CtClass ctInterface : ctClass.getInterfaces()) {
            if (Objects.equals(ctInterface.getName(), ENTITY_ENTRY_ENHANCED_TYPE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEnhancingDisabled(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(DISABLE_ENHANCING_ANNOTATION_TYPE) != null;
    }

    public static boolean subtypeOfEntityInterface(CtClass ctClass, ClassPool pool) throws NotFoundException {
        return ctClass.subtypeOf(pool.get(ENTITY_TYPE));
    }

    public static boolean isJpaEntity(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(ENTITY_ANNOTATION_TYPE) != null;
    }

    public static boolean isJpaEmbeddable(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(EMBEDDABLE_ANNOTATION_TYPE) != null;
    }

    public static boolean isJpaConverter(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(CONVERTER_ANNOTATION_TYPE) != null;
    }

    public static boolean isJpaMappedSuperclass(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation("javax.persistence.MappedSuperclass") != null;
    }

    public static boolean isModuleConfig(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null
                && (attribute.getAnnotation("io.jmix.core.annotation.JmixModule") != null
                || attribute.getAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication") != null
                || attribute.getAnnotation("org.springframework.boot.autoconfigure.EnableAutoConfiguration") != null);
    }

    public static boolean isJmixEntity(CtClass ctClass) {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        return attribute != null && attribute.getAnnotation(JMIX_ENTITY_ANNOTATION_TYPE) != null;
    }

    public static boolean isJmixPropertiesAnnotatedOnly(CtClass ctClass) {
        if (isJmixEntity(ctClass)) {
            AnnotationsAttribute attribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
            BooleanMemberValue annotatedPropertiesOnly = (BooleanMemberValue) attribute.getAnnotation(JMIX_ENTITY_ANNOTATION_TYPE).getMemberValue("annotatedPropertiesOnly");
            return annotatedPropertiesOnly != null && annotatedPropertiesOnly.getValue();
        }
        return true;
    }

    public static boolean isPkGeneratedValue(CtField field) {
        AnnotationsAttribute annotationsInfo = (AnnotationsAttribute) field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);

        if (annotationsInfo != null) {
            if (annotationsInfo.getAnnotation("javax.persistence.GeneratedValue") != null) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPersistentMethod(CtMethod ctMethod) {
        return ctMethod.getName().startsWith("_persistence_get_") ||
                ctMethod.getName().startsWith("_persistence_set_");
    }

    public static boolean isPersistentField(CtClass ctClass, String fieldName) {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (Objects.equals(method.getName(), "_persistence_set_" + fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isJmixProperty(CtClass ctClass, String fieldName) {
        CtField ctField = findDeclaredField(ctClass, fieldName);
        CtMethod ctMethod = findDeclaredMethod(ctClass, "get" + StringUtils.capitalize(fieldName));
        return ctField != null && hasAnnotationOnField(ctField, JMIX_PROPERTY_ANNOTATION_TYPE)
                || ctMethod != null && hasAnnotationOnMethod(ctMethod, JMIX_PROPERTY_ANNOTATION_TYPE);
    }

    public static boolean isTransientField(CtClass ctClass, String fieldName) {
        CtField ctField = findDeclaredField(ctClass, fieldName);
        return ctField != null && hasAnnotationOnField(ctField, TRANSIENT_ANNOTATION_TYPE);
    }

    public static boolean isSetterMethod(CtMethod ctMethod) throws NotFoundException {
        return !Modifier.isAbstract(ctMethod.getModifiers())
                && ctMethod.getName().startsWith("set")
                && ctMethod.getReturnType() == CtClass.voidType
                && ctMethod.getParameterTypes().length == 1;
    }

    public static String generateFieldNameByMethod(String methodName) {
        return StringUtils.uncapitalize(methodName.substring(3));
    }

    public static CtField findDeclaredFieldByAccessor(CtClass ctClass, String accessorName) {
        String fieldName = accessorName.substring(3);
        for (CtField field : ctClass.getDeclaredFields()) {
            if (field.getName().equals(fieldName) || field.getName().equals(StringUtils.uncapitalize(fieldName))) {
                return field;
            }
        }
        return null;
    }

    /**
     * Handles a special case of Kotlin property with a name starting with "is*"
     * if the Kotlin property name is "isApproved" then the generated getter will be "isApproved()" and the setter is "setApproved()"
     * <p>
     * Note that getter for field "isaField" will still be generated in common "get-" form: "getIsaField()"
     */
    public static CtField findDeclaredKotlinFieldByAccessor(CtClass ctClass, String accessorName) {

        String kotlinPropertyName = "is" + StringUtils.capitalize(accessorName.substring(3));
        for (CtField field : ctClass.getDeclaredFields()) {
            if (field.getName().equals(kotlinPropertyName)) {
                return field;
            }
        }
        return null;
    }

    public static CtField findDeclaredField(CtClass ctClass, String fieldName) {
        for (CtField field : ctClass.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public static CtMethod findDeclaredMethod(CtClass ctClass, String name) {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;

    }

    public static CtMethod findEqualsMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if ("equals".equals(method.getName())) {
                if (CtClass.booleanType.equals(method.getReturnType())
                        && method.getParameterTypes().length == 1
                        && Object.class.getName().equals(method.getParameterTypes()[0].getName())) {
                    return method;
                }
            }
        }
        return null;
    }

    public static CtMethod findHashCodeMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if ("hashCode".equals(method.getName())) {
                if (CtClass.intType.equals(method.getReturnType())
                        && method.getParameterTypes().length == 0) {
                    return method;
                }
            }
        }
        return null;
    }

    public static CtMethod findToStringMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if ("toString".equals(method.getName())) {
                if (String.class.getName().equals(method.getReturnType().getName())
                        && method.getParameterTypes().length == 0) {
                    return method;
                }
            }
        }
        return null;
    }

    public static CtMethod findWriteObjectMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (WRITE_OBJECT_METHOD_NAME.equals(method.getName())) {
                if (CtClass.voidType.equals(method.getReturnType())
                        && method.getParameterTypes().length == 1) {
                    return method;
                }
            }
        }
        return null;
    }

    public static CtMethod findReadObjectMethod(CtClass ctClass) throws NotFoundException {
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (READ_OBJECT_METHOD_NAME.equals(method.getName())) {
                if (CtClass.voidType.equals(method.getReturnType())
                        && method.getParameterTypes().length == 1) {
                    return method;
                }
            }
        }
        return null;
    }

    public static boolean hasAnnotationOnField(CtField ctField, String annotationType) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        return annotationsAttribute != null && annotationsAttribute.getAnnotation(annotationType) != null;
    }

    public static boolean hasAnnotationOnMethod(CtMethod ctMethod, String annotationType) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctMethod.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        return annotationsAttribute != null && annotationsAttribute.getAnnotation(annotationType) != null;
    }

    @Nullable
    public static String findStoreName(CtClass ctClass) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        Annotation annotation = annotationsAttribute.getAnnotation(STORE_ANNOTATION_TYPE);
        return annotation == null ? null : ((StringMemberValue) annotation.getMemberValue("name")).getValue();
    }

    @Nullable
    public static String findReplacedEntity(CtClass ctClass) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        Annotation annotation = annotationsAttribute.getAnnotation(REPLACE_ENTITY_ANNOTATION_TYPE);
        return annotation == null ? null : ((ClassMemberValue) annotation.getMemberValue("value")).getValue();
    }

    public static boolean isCollection(CtField field) throws NotFoundException {
        HashSet<String> classNames = new HashSet<>();
        CtClass current = field.getType();
        while (current != null) {
            classNames.add(current.getName());
            classNames.addAll(Arrays.stream(current.getInterfaces()).map(CtClass::getName).collect(Collectors.toList()));
            current = current.getSuperclass();
        }

        return classNames.contains(Collection.class.getCanonicalName());
    }
}
