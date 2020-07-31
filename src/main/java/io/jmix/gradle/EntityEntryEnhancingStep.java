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
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.jmix.gradle.AnnotationsInfo.ClassAnnotation.EMBEDDABLE;
import static io.jmix.gradle.AnnotationsInfo.ClassAnnotation.LEGACY_HAS_UUID;
import static io.jmix.gradle.AnnotationsInfo.FieldAnnotation.*;
import static io.jmix.gradle.MetaModelUtil.*;

public class EntityEntryEnhancingStep extends BaseEnhancingStep {

    @Override
    protected boolean isAlreadyEnhanced(CtClass ctClass) throws NotFoundException {
        return isEntityEntryEnhanced(ctClass);
    }

    @Override
    protected String getEnhancingType() {
        return "Entity Entry Enhancer";
    }

    @Override
    protected void executeInternal(CtClass ctClass) throws IOException, CannotCompileException, NotFoundException {
        AnnotationsInfo info = AnnotationsInfo.forClass(ctClass);

        if (info.hasMetadataChanges()) {

            boolean embeddable = info.hasClassAnnotation(EMBEDDABLE);

            if (info.getPrimaryKey() != null) {

                makeEntityEntryClass(ctClass, info);

                makeEntityEntryField(ctClass);

                String entryClassName = String.format("%s.%s", ctClass.getName(), GEN_ENTITY_ENTRY_CLASS_NAME);

                makeEntityEntryMethods(ctClass, entryClassName);

                if (findGeneratedIdField(info) == null) {
                    initEntityEntry(ctClass, entryClassName);
                }

                if (!embeddable) {
                    makeEqualsMethod(ctClass);

                    makeHashCodeMethod(ctClass);

                    makeToStringMethod(ctClass);

                    makeWriteObjectMethod(ctClass);
                }
            } else if (embeddable) {
                makeEntityEntryField(ctClass);

                makeEntityEntryMethods(ctClass, EMBEDDABLE_ENTITY_ENTRY_TYPE);
            }

        }
        ctClass.addInterface(classPool.get(ENTITY_ENTRY_ENHANCED_TYPE));
    }

    /**
     * @param info must always contain info.getPrimaryKey() != null
     */
    protected void makeEntityEntryClass(CtClass ctClass, AnnotationsInfo info) throws CannotCompileException, NotFoundException, IOException {

        CtField primaryKeyField = Objects.requireNonNull(info.getPrimaryKey());

        CtClass nestedCtClass = ctClass.makeNestedClass(GEN_ENTITY_ENTRY_CLASS_NAME, true);

        CtField generatedIdField = findGeneratedIdField(info);

        CtClass idType = classPool.get(Object.class.getName());
        if (generatedIdField == null) {
            nestedCtClass.setSuperclass(classPool.get(NULLABLE_ID_ENTITY_ENTRY_TYPE));
        } else {
            nestedCtClass.setSuperclass(classPool.get(BASE_ENTITY_ENTRY_TYPE));
        }

        CtMethod getIdMethod = CtNewMethod.make(idType, "getEntityId",
                null, null,
                String.format("return ((%s)getSource()).get%s();",
                        ctClass.getName(),
                        StringUtils.capitalize(primaryKeyField.getName())),
                nestedCtClass);
        nestedCtClass.addMethod(getIdMethod);

        CtMethod setIdMethod = CtNewMethod.make(CtClass.voidType, "setEntityId",
                new CtClass[]{idType}, null,
                String.format("((%s)getSource()).set%s((%s)$1);",
                        ctClass.getName(),
                        StringUtils.capitalize(primaryKeyField.getName()),
                        primaryKeyField.getType().getName()),
                nestedCtClass);
        nestedCtClass.addMethod(setIdMethod);

        if (generatedIdField != null) {
            CtMethod getGeneratedIdMethod = CtNewMethod.make(idType, "getGeneratedIdOrNull",
                    null, null,
                    String.format("return ((%s)getSource()).get%s();",
                            ctClass.getName(),
                            StringUtils.capitalize(generatedIdField.getName())),
                    nestedCtClass);
            nestedCtClass.addMethod(getGeneratedIdMethod);
        }

        setupAuditing(nestedCtClass, ctClass, info);
        setupSoftDelete(nestedCtClass, ctClass, info);
        setupHasUuid(nestedCtClass, ctClass, info);

        nestedCtClass.writeFile(outputDir);
    }

    @Nullable
    protected CtField findGeneratedIdField(AnnotationsInfo info) {
        CtField primaryKeyField = info.getPrimaryKey();
        List<CtField> generatedValueFields = info.getAnnotatedFields(JMIX_GENERATED_VALUE);
        return generatedValueFields.stream()
                .filter(ctField -> ctField.equals(primaryKeyField))
                .findFirst()
                .orElseGet(() -> generatedValueFields.stream()
                        .filter(this::isFieldOfUuidType)
                        .findFirst()
                        .orElse(null));
    }

    protected boolean isFieldOfUuidType(CtField ctField) {
        try {
            return ctField.getType().getName().equals("java.util.UUID");
        } catch (NotFoundException e) {
            // ignore
        }
        return false;
    }

    private void setupSoftDelete(CtClass nestedClass, CtClass ctClass, AnnotationsInfo info)
            throws NotFoundException, CannotCompileException {
        CtField deletedDateField = info.getAnnotatedField(DELETED_DATE);
        CtField deletedByField = info.getAnnotatedField(DELETED_BY);

        if (deletedDateField != null) {
            createStandardMethods("DeletedDate", deletedDateField, nestedClass, ctClass);
            createStandardMethods("DeletedBy", deletedByField, nestedClass, ctClass);

            CtMethod isDeletedMethod = CtNewMethod.make(CtClass.booleanType, "isDeleted",
                    null, null,
                    String.format("return ((%s)getSource()).get%s() != null;",
                            ctClass.getName(),
                            StringUtils.capitalize(deletedDateField.getName())),
                    nestedClass);
            nestedClass.addMethod(isDeletedMethod);

            logger.debug(String.format("Entity %s is soft-deletable. Fields: deletedDate: %s, deletedBy: %s",
                    ctClass.getSimpleName(),
                    deletedDateField.getName(),
                    deletedByField == null ? '-' : deletedByField.getName()));

            nestedClass.addInterface(classPool.get("io.jmix.core.entity.EntityEntrySoftDelete"));
        } else if (deletedByField != null) {
            throw new RuntimeException("@DeletedBy annotation cannot be used without @DeletedDate. Class: "
                    + ctClass.getName());
        }
    }

    protected void setupAuditing(CtClass nestedClass, CtClass ctClass, AnnotationsInfo info)
            throws NotFoundException, CannotCompileException {

        CtField createdDateField = info.getAnnotatedField(CREATED_DATE);
        CtField createdByField = info.getAnnotatedField(CREATED_BY);
        CtField lastModifiedDateField = info.getAnnotatedField(LAST_MODIFIED_DATE);
        CtField lastModifiedByField = info.getAnnotatedField(LAST_MODIFIED_BY);

        createSetterAndTypeGetter("CreatedDate", createdDateField, nestedClass, ctClass);
        createSetterAndTypeGetter("CreatedBy", createdByField, nestedClass, ctClass);
        createSetterAndTypeGetter("LastModifiedDate", lastModifiedDateField, nestedClass, ctClass);
        createSetterAndTypeGetter("LastModifiedBy", lastModifiedByField, nestedClass, ctClass);

        if (createdDateField != null || createdByField != null || lastModifiedDateField != null || lastModifiedByField != null) {
            nestedClass.addInterface(classPool.get("io.jmix.core.entity.EntityEntryAuditable"));
            logger.debug(String.format("Auditing enabled for %s. Fields: createdDate: %s, createdBy: %s, lastModifiedDate: %s, lastModifiedBy: %s",
                    ctClass.getSimpleName(),
                    createdDateField == null ? '-' : createdDateField.getName(),
                    createdByField == null ? '-' : createdByField.getName(),
                    lastModifiedDateField == null ? '-' : lastModifiedDateField.getName(),
                    lastModifiedByField == null ? '-' : lastModifiedByField.getName()
            ));
        }
    }

    protected void setupHasUuid(CtClass nestedClass, CtClass ctClass, AnnotationsInfo info)
            throws NotFoundException, CannotCompileException {

        String uuidFieldName;

        if (info.hasClassAnnotation(LEGACY_HAS_UUID)) {//legacy support
            uuidFieldName = "uuid";
        } else {
            CtField primaryKeyField = info.getPrimaryKey();
            List<CtField> generatedValueUuidFields = info.getAnnotatedFields(JMIX_GENERATED_VALUE).stream()
                    .filter(this::isFieldOfUuidType)
                    .collect(Collectors.toList());

            CtField uuidField = generatedValueUuidFields.stream()
                    .filter(ctField -> ctField.equals(primaryKeyField))
                    .findFirst()
                    .orElseGet(() -> generatedValueUuidFields.stream()
                            .min(Comparator.comparing(CtField::getName))
                            .orElse(null));

            if (uuidField != null) {
                uuidFieldName = uuidField.getName();
            } else {
                uuidFieldName = null;
            }
        }

        if (uuidFieldName != null) {
            setupHasUuidForField(nestedClass, ctClass, uuidFieldName);
        }
    }

    protected void setupHasUuidForField(CtClass nestedClass, CtClass ctClass, String uuidFieldName)
            throws NotFoundException, CannotCompileException {
        CtClass uuidClass = classPool.get(UUID.class.getName());

        nestedClass.addMethod(CtNewMethod.make(uuidClass, "getUuid", null, null,
                String.format("return ((%s)getSource()).get%s();",
                        ctClass.getName(),
                        StringUtils.capitalize(uuidFieldName)),
                nestedClass));

        nestedClass.addMethod(CtNewMethod.make(CtClass.voidType, "setUuid", new CtClass[]{uuidClass}, null,
                String.format("((%s)getSource()).set%s($1);",
                        ctClass.getName(),
                        StringUtils.capitalize(uuidFieldName)),
                nestedClass));

        logger.debug(String.format("Entity '%s' uuid field: %s", ctClass.getSimpleName(), uuidFieldName));
        nestedClass.addInterface(classPool.get("io.jmix.core.entity.EntityEntryHasUuid"));
    }

    /**
     * Creates setter, getter, and type getter for specified {@code propName} referring to {@code propField}
     *
     * @param propName should be capitalized
     */
    protected void createStandardMethods(String propName, @Nullable CtField propField, CtClass nestedClass, CtClass ctClass)
            throws CannotCompileException, NotFoundException {

        if (propField == null)
            return;

        createSetterAndTypeGetter(propName, propField, nestedClass, ctClass);

        CtMethod getDeletedDateMethod = CtNewMethod.make(classPool.get(Object.class.getName()), "get" + propName,
                null, null,
                String.format("return ((%s)getSource()).get%s();",
                        ctClass.getName(),
                        StringUtils.capitalize(propField.getName())),
                nestedClass);
        nestedClass.addMethod(getDeletedDateMethod);
    }

    protected void createSetterAndTypeGetter(String propName, @Nullable CtField propField, CtClass nestedClass, CtClass ctClass)
            throws CannotCompileException, NotFoundException {
        if (propField == null)
            return;

        CtClass objectClass = classPool.get(Object.class.getName());
        CtClass classClass = classPool.get(Class.class.getName());

        nestedClass.addMethod(CtNewMethod.make(CtClass.voidType, "set" + propName, new CtClass[]{objectClass}, null,
                String.format("((%s)getSource()).set%s((%s)$1);",
                        ctClass.getName(),
                        StringUtils.capitalize(propField.getName()),
                        propField.getType().getName()),
                nestedClass));

        nestedClass.addMethod(CtNewMethod.make(classClass, "get" + propName + "Class", null, null,
                String.format("return %s.class;", propField.getType().getName()),
                nestedClass));
    }


    protected void makeEntityEntryField(CtClass ctClass) throws CannotCompileException, NotFoundException {
        CtField ctField = new CtField(classPool.get(ENTITY_ENTRY_TYPE), GEN_ENTITY_ENTRY_VAR_NAME, ctClass);
        ctField.setModifiers(Modifier.PRIVATE);
        ctClass.addField(ctField);
    }

    protected void makeEntityEntryMethods(CtClass ctClass, String entryClassName) throws NotFoundException, CannotCompileException {

        CtMethod entryMethod = CtNewMethod.make(classPool.get(ENTITY_ENTRY_TYPE), GET_ENTITY_ENTRY_METHOD_NAME, null, null,
                String.format("return %s == null ? %s = new %s(this) : %s;",
                        GEN_ENTITY_ENTRY_VAR_NAME, GEN_ENTITY_ENTRY_VAR_NAME, entryClassName, GEN_ENTITY_ENTRY_VAR_NAME),
                ctClass);

        ctClass.addMethod(entryMethod);

        CtMethod copyEntryMethod = CtNewMethod.make(CtClass.voidType, COPY_ENTITY_ENTRY_METHOD_NAME, null, null,
                String.format("{ %s newEntityEntry = new %s(this); newEntityEntry.copy(%s) ; %s = newEntityEntry; }",
                        entryClassName,
                        entryClassName,
                        GEN_ENTITY_ENTRY_VAR_NAME, GEN_ENTITY_ENTRY_VAR_NAME),
                ctClass);

        ctClass.addMethod(copyEntryMethod);
    }

    private void initEntityEntry(CtClass ctClass, String entryClassName) throws CannotCompileException {
        CtConstructor constructor;
        try {
            constructor = ctClass.getDeclaredConstructor(null);
        } catch (NotFoundException e) {
            constructor = CtNewConstructor.defaultConstructor(ctClass);
        }
        constructor.insertAfter(String.format("%s = new %s(this);", GEN_ENTITY_ENTRY_VAR_NAME, entryClassName));
    }

    protected void makeEqualsMethod(CtClass ctClass) throws NotFoundException, CannotCompileException {
        if (findEqualsMethod(ctClass) == null) {
            CtMethod entryMethod = CtNewMethod.make(CtClass.booleanType, "equals", new CtClass[]{classPool.get(Object.class.getName())}, null,
                    "return io.jmix.core.impl.EntityInternals.equals(this, $1);", ctClass);
            ctClass.addMethod(entryMethod);
        }
    }

    protected void makeHashCodeMethod(CtClass ctClass) throws NotFoundException, CannotCompileException {
        if (findHashCodeMethod(ctClass) == null) {
            CtMethod entryMethod = CtNewMethod.make(CtClass.intType, "hashCode", null, null,
                    "return io.jmix.core.impl.EntityInternals.hashCode(this);",
                    ctClass);
            ctClass.addMethod(entryMethod);
        }
    }

    protected void makeToStringMethod(CtClass ctClass) throws NotFoundException, CannotCompileException {
        if (findToStringMethod(ctClass) == null) {
            CtMethod entryMethod = CtNewMethod.make(classPool.get(String.class.getName()), "toString", null, null,
                    "return io.jmix.core.impl.EntityInternals.toString(this);", ctClass);
            ctClass.addMethod(entryMethod);
        }
    }

    protected void makeWriteObjectMethod(CtClass ctClass) throws NotFoundException, CannotCompileException {
        CtMethod ctMethod = findWriteObjectMethod(ctClass);
        if (ctMethod != null) {
            ctMethod.insertBefore("io.jmix.core.impl.EntityInternals.writeObject(this, $1)");
        } else {
            CtMethod entryMethod = CtNewMethod.make(Modifier.PRIVATE,
                    CtClass.voidType, WRITE_OBJECT_METHOD_NAME,
                    new CtClass[]{classPool.get(ObjectOutputStream.class.getName())},
                    new CtClass[]{classPool.get(IOException.class.getName())},
                    "{ io.jmix.core.impl.EntityInternals.writeObject(this, $1); $1.defaultWriteObject(); }", ctClass);
            ctClass.addMethod(entryMethod);
        }
    }
}
