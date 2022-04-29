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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.jmix.core.Entity;
import io.jmix.core.MetadataTools;
import io.jmix.core.Stores;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.MetaAnnotation;
import io.jmix.core.metamodel.annotation.*;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.datatype.impl.AdaptiveNumberDatatype;
import io.jmix.core.metamodel.datatype.impl.EnumerationImpl;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.*;
import io.jmix.core.metamodel.model.impl.*;
import io.jmix.core.validation.group.UiComponentChecks;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * INTERNAL.
 * Loads meta-model from a set of annotated Java classes.
 */
@Component("core_MetaModelLoader")
public class MetaModelLoader {

    protected static final String VALIDATION_MIN = "_min";
    protected static final String VALIDATION_MAX = "_max";

    protected static final String VALIDATION_NOTNULL_MESSAGE = "_notnull_message";
    protected static final String VALIDATION_NOTNULL_UI_COMPONENT = "_notnull_ui_component";

    protected static final List<Class<? extends Annotation>> SYSTEM_ANNOTATIONS = ImmutableList.of(
            Id.class,
            JmixId.class,
            Version.class,
            EmbeddedId.class,
            JmixGeneratedValue.class,
            CreatedDate.class,
            CreatedBy.class,
            LastModifiedDate.class,
            LastModifiedBy.class,
            DeletedDate.class,
            DeletedBy.class,
            TenantId.class
    );

    protected DatatypeRegistry datatypes;

    protected Stores stores;

    protected FormatStringsRegistry formatStringsRegistry;

    private static final Logger log = LoggerFactory.getLogger(MetaModelLoader.class);

    @Autowired
    public MetaModelLoader(DatatypeRegistry datatypes, Stores stores, FormatStringsRegistry formatStringsRegistry) {
        this.datatypes = datatypes;
        this.stores = stores;
        this.formatStringsRegistry = formatStringsRegistry;
    }

    public void loadModel(Session session, Set<String> classNames) {
        checkNotNullArgument(classNames, "classInfos is null");

        Set<Class<?>> classes = new LinkedHashSet<>();
        for (String className : classNames) {
            try {
                classes.add(ReflectionHelper.loadClass(className));
            } catch (ClassNotFoundException e) {
                log.warn("Class {} not found", className);
            }
        }

        for (Class<?> aClass : classes) {
            MetaClassImpl metaClass = createClass(session, aClass);
            if (metaClass == null) {
                log.warn("Class {} is not loaded into metadata", aClass.getName());
            }
        }

        for (MetaClass metaClass : session.getClasses()) {
            initAncestors(session, metaClass);
            assignStore(metaClass);
        }

        List<RangeInitTask> tasks = new ArrayList<>();
        for (Class<?> aClass : classes) {
            MetadataObjectInfo<MetaClass> info = loadClass(session, aClass);
            if (info != null) {
                tasks.addAll(info.getTasks());
            } else {
                log.warn("Class {} is not loaded into metadata", aClass.getName());
            }
        }

        for (RangeInitTask task : tasks) {
            task.execute();
        }

        for (MetaClass metaClass : session.getClasses()) {
            initInheritedProperties(metaClass);
        }
    }

    protected void initAncestors(Session session, MetaClass metaClass) {
        Class<?> ancestor = metaClass.getJavaClass().getSuperclass();
        if (ancestor != null) {
            List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(metaClass.getJavaClass());
            for (Class<?> superclass : superclasses) {
                MetaClass ancestorClass = session.findClass(superclass);
                if (ancestorClass != null) {
                    ((MetaClassImpl) metaClass).addAncestor(ancestorClass);
                }
            }
        }
    }

    @Nullable
    protected MetadataObjectInfo<MetaClass> loadClass(Session session, Class<?> javaClass) {
        MetaClassImpl metaClass = (MetaClassImpl) session.findClass(javaClass);
        if (metaClass == null)
            return null;

        Collection<RangeInitTask> tasks = new ArrayList<>();

        initProperties(session, javaClass, metaClass, tasks);

        return new MetadataObjectInfo<>(metaClass, tasks);
    }

    protected void assignStore(MetaClass metaClass) {
        Store store;
        Class<?> javaClass = metaClass.getJavaClass();
        io.jmix.core.metamodel.annotation.Store storeAnn = javaClass.getAnnotation(io.jmix.core.metamodel.annotation.Store.class);
        if (storeAnn != null) {
            store = stores.get(storeAnn.name());
        } else {
            if (javaClass.getAnnotation(javax.persistence.Entity.class) != null || javaClass.getAnnotation(Embeddable.class) != null) {
                store = stores.get(Stores.MAIN);
            } else {
                if (javaClass.getAnnotation(MappedSuperclass.class) != null) {
                    store = stores.get(Stores.UNDEFINED);
                } else {
                    store = stores.get(Stores.NOOP);
                }
            }
        }
        ((MetaClassImpl) metaClass).setStore(store);
    }

    protected void initInheritedProperties(MetaClass metaClass) {
        for (MetaProperty property : metaClass.getProperties()) {
            if (!metaClass.getOwnProperties().contains(property)) {
                assignStore(property);
            }
        }
    }

    @Nullable
    protected MetaClassImpl createClass(Session session, Class<?> javaClass) {
        if (!Entity.class.isAssignableFrom(javaClass)) {
            return null;
        }

        MetaClassImpl metaClass = (MetaClassImpl) session.findClass(javaClass);
        if (metaClass != null) {
            return metaClass;

        } else {
            String name = getMetaClassName(javaClass);
            if (name == null)
                return null;

            metaClass = new MetaClassImpl(session, name);
            metaClass.setJavaClass(javaClass);

            return metaClass;
        }
    }

    @Nullable
    protected String getMetaClassName(Class<?> javaClass) {
        javax.persistence.Entity entityAnnotation = javaClass.getAnnotation(javax.persistence.Entity.class);

        JmixEntity jmixEntityAnnotation = javaClass.getAnnotation(JmixEntity.class);

        if (jmixEntityAnnotation == null) {
            log.trace("Class '{}' isn't annotated as JmixEntity, ignore it", javaClass.getName());
            return null;
        }

        String name = null;
        if (entityAnnotation != null) {
            name = entityAnnotation.name();

            String jmixName = jmixEntityAnnotation.name();
            if (!Strings.isNullOrEmpty(jmixName) && !jmixName.equals(name)) {
                throw new RuntimeException(
                        String.format("Different names in @Entity and @JmixEntity annotations for class %s: '%s' and '%s'.\n" +
                                        "@JmixEntity name should be empty or the same as @Entity name if @Entity annotation present.",
                                javaClass.getName(), name, jmixName));
            }
        } else {
            name = jmixEntityAnnotation.name();
        }

        if (StringUtils.isEmpty(name)) {
            name = javaClass.getSimpleName();
        }
        return name;
    }

    protected void initProperties(Session session, Class<?> clazz, MetaClassImpl metaClass, Collection<RangeInitTask> tasks) {
        if (!metaClass.getOwnProperties().isEmpty())
            return;

        // load collection properties after non-collection in order to have all inverse properties loaded up
        ArrayList<Field> collectionProps = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isSynthetic())
                continue;

            final String fieldName = field.getName();

            if (isJmixProperty(field) ||
                    (useNonAnnotatedProperties(clazz)
                            && (isMetaPropertyField(field) || isFieldWithGetter(field, clazz))
                            && !isTransientFieldOfJpaEntity(clazz, field))) {
                MetaPropertyImpl property = (MetaPropertyImpl) metaClass.findProperty(fieldName);
                if (property == null) {
                    MetadataObjectInfo<MetaProperty> info;
                    if (isCollection(field) || isMap(field)) {
                        collectionProps.add(field);
                    } else {
                        info = loadProperty(session, metaClass, field);
                        tasks.addAll(info.getTasks());
                        MetaProperty metaProperty = info.getObject();
                        onPropertyLoaded(metaProperty, field);
                    }
                } else {
                    log.warn("Field " + clazz.getSimpleName() + "." + field.getName()
                            + " is not included in metadata because property " + property + " already exists");
                }
            }
        }

        for (Field f : collectionProps) {
            MetadataObjectInfo<MetaProperty> info = loadCollectionProperty(session, metaClass, f);
            tasks.addAll(info.getTasks());
            MetaProperty metaProperty = info.getObject();
            onPropertyLoaded(metaProperty, f);
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isSynthetic())
                continue;

            String methodName = method.getName();
            if (!methodName.startsWith("get") || method.getReturnType() == void.class)
                continue;

            if (isMetaPropertyMethod(method)) {
                String name = StringUtils.uncapitalize(methodName.substring(3));

                MetaPropertyImpl property = (MetaPropertyImpl) metaClass.findProperty(name);
                if (property == null) {
                    MetadataObjectInfo<MetaProperty> info;
                    if (isCollection(method) || isMap(method)) {
                        throw new UnsupportedOperationException(String.format("Method-based property %s.%s doesn't support collections and maps", clazz.getSimpleName(), method.getName()));
                    } else if (method.getParameterCount() != 0) {
                        throw new UnsupportedOperationException(String.format("Method-based property %s.%s doesn't support arguments", clazz.getSimpleName(), method.getName()));
                    } else {
                        info = loadProperty(session, metaClass, method, name);
                        tasks.addAll(info.getTasks());
                    }
                    MetaProperty metaProperty = info.getObject();
                    onPropertyLoaded(metaProperty, method);
                } else {
                    log.warn("Method " + clazz.getSimpleName() + "." + method.getName()
                            + " is not included in metadata because property " + property + " already exists");
                }
            }
        }
    }

    private boolean useNonAnnotatedProperties(Class<?> javaClass) {
        JmixEntity jmixEntityAnnotation = javaClass.getAnnotation(JmixEntity.class);
        return jmixEntityAnnotation != null && !jmixEntityAnnotation.annotatedPropertiesOnly();
    }

    private boolean isFieldWithGetter(Field field, Class<?> javaClass) {
        for (Method method : javaClass.getDeclaredMethods()) {
            if (method.getName().equals("get" + StringUtils.capitalize(field.getName()))
                    && method.getReturnType().equals(field.getType())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isMetaPropertyField(Field field) {
        return field.isAnnotationPresent(Column.class)
                || field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(Embedded.class)
                || field.isAnnotationPresent(EmbeddedId.class);
    }

    protected boolean isJmixProperty(Field field) {
        return field.isAnnotationPresent(JmixProperty.class);
    }

    protected boolean isTransientFieldOfJpaEntity(Class clazz, Field field) {
        return field.isAnnotationPresent(Transient.class) && hasJpaAnnotation(clazz);
    }

    protected boolean isMetaPropertyMethod(Method method) {
        return method.isAnnotationPresent(JmixProperty.class);
    }

    protected MetadataObjectInfo<MetaProperty> loadProperty(Session session, MetaClassImpl metaClass, Field field) {
        MetaPropertyImpl property = new MetaPropertyImpl(metaClass, field.getName());

        Range.Cardinality cardinality = getCardinality(field);
        Map<String, Object> map = new HashMap<>();
        map.put("cardinality", cardinality);
        boolean mandatory = isMandatory(field);
        map.put("mandatory", mandatory);
        Datatype datatype = getAdaptiveDatatype(field);
        map.put("datatype", datatype);
        String inverseField = getInverseField(field);
        if (inverseField != null)
            map.put("inverseField", inverseField);

        Class<?> type;
        Class typeOverride = getTypeOverride(field);
        if (typeOverride != null)
            type = typeOverride;
        else
            type = field.getType();

        property.setMandatory(mandatory);
        property.setReadOnly(!setterExists(field));
        property.setAnnotatedElement(field);
        property.setDeclaringClass(field.getDeclaringClass());

        MetadataObjectInfo<Range> info = loadRange(session, property, type, map);
        Range range = info.getObject();
        if (range != null) {
            ((AbstractRange) range).setCardinality(cardinality);
            property.setRange(range);
            assignPropertyType(field, property, range);
            assignInverse(property, range, inverseField);
        }

        if (info.getObject() != null && info.getObject().isEnum()) {
            property.setJavaType(info.getObject().asEnumeration().getJavaClass());
        } else {
            property.setJavaType(field.getType());
        }

        Collection<RangeInitTask> tasks = new ArrayList<>(info.getTasks());

        return new MetadataObjectInfo<>(property, tasks);
    }

    protected MetadataObjectInfo<MetaProperty> loadProperty(Session session, MetaClassImpl metaClass,
                                                            Method method, String name) {

        MetaPropertyImpl property = new MetaPropertyImpl(metaClass, name);

        Map<String, Object> map = new HashMap<>();
        map.put("cardinality", Range.Cardinality.NONE);
        boolean mandatory = isMandatory(method);
        map.put("mandatory", mandatory);
        Datatype datatype = getAdaptiveDatatype(method);
        map.put("datatype", datatype);

        Class<?> type;
        Class typeOverride = getTypeOverride(method);
        if (typeOverride != null)
            type = typeOverride;
        else
            type = method.getReturnType();

        property.setMandatory(mandatory);
        property.setReadOnly(!setterExists(method));
        property.setAnnotatedElement(method);
        property.setDeclaringClass(method.getDeclaringClass());
        property.setJavaType(method.getReturnType());

        MetadataObjectInfo<Range> info = loadRange(session, property, type, map);
        Range range = info.getObject();
        if (range != null) {
            ((AbstractRange) range).setCardinality(Range.Cardinality.NONE);
            property.setRange(range);
            assignPropertyType(method, property, range);
        }

        Collection<RangeInitTask> tasks = new ArrayList<>(info.getTasks());

        return new MetadataObjectInfo<>(property, tasks);
    }

    protected MetadataObjectInfo<MetaProperty> loadCollectionProperty(Session session, MetaClassImpl metaClass, Field field) {

        MetaPropertyImpl property = new MetaPropertyImpl(metaClass, field.getName());

        Class type = getFieldType(field);

        Range.Cardinality cardinality = getCardinality(field);
        boolean ordered = isOrdered(field);
        boolean mandatory = isMandatory(field);
        String inverseField = getInverseField(field);

        Map<String, Object> map = new HashMap<>();
        map.put("cardinality", cardinality);
        map.put("ordered", ordered);
        map.put("mandatory", mandatory);
        if (inverseField != null)
            map.put("inverseField", inverseField);

        property.setAnnotatedElement(field);
        property.setDeclaringClass(field.getDeclaringClass());
        property.setJavaType(field.getType());

        MetadataObjectInfo<Range> info = loadRange(session, property, type, map);
        Range range = info.getObject();
        if (range != null) {
            ((AbstractRange) range).setCardinality(cardinality);
            ((AbstractRange) range).setOrdered(ordered);
            property.setRange(range);
            assignPropertyType(field, property, range);
            assignInverse(property, range, inverseField);
        }
        property.setMandatory(mandatory);
        property.setReadOnly(!setterExists(field));

        Collection<RangeInitTask> tasks = new ArrayList<>(info.getTasks());

        return new MetadataObjectInfo<>(property, tasks);
    }

    protected void onPropertyLoaded(MetaProperty metaProperty, Field field) {
        loadPropertyAnnotations(metaProperty, field);

        assignStore(metaProperty);

        if (isPrimaryKey(field)) {
            metaProperty.getAnnotations().put(MetadataTools.PRIMARY_KEY_ANN_NAME, true);
            metaProperty.getDomain().getAnnotations().put(MetadataTools.PRIMARY_KEY_ANN_NAME, metaProperty.getName());
        }

        if (isUuidGeneratedValue(metaProperty, field)) {
            metaProperty.getDomain().getAnnotations().put(MetadataTools.UUID_KEY_ANN_NAME, metaProperty.getName());
            metaProperty.getAnnotations().put(MetadataTools.UUID_KEY_ANN_NAME, true);
        }

        Column column = field.getAnnotation(Column.class);
        Lob lob = field.getAnnotation(Lob.class);
        if (column != null && column.length() != 0 && lob == null) {
            metaProperty.getAnnotations().put(MetadataTools.LENGTH_ANN_NAME, column.length());
        }

        Temporal temporal = field.getAnnotation(Temporal.class);
        if (temporal != null) {
            metaProperty.getAnnotations().put(MetadataTools.TEMPORAL_ANN_NAME, temporal.value());
        }

        if (isSystem(field, metaProperty)) {
            metaProperty.getAnnotations().put(MetadataTools.SYSTEM_ANN_NAME, true);
            MetaClass metaClass = metaProperty.getDomain();
            if (!metaClass.getAnnotations().containsKey(MetadataTools.SYSTEM_ANN_NAME)) {
                metaClass.getAnnotations().put(MetadataTools.SYSTEM_ANN_NAME, new LinkedList<String>());
            }
            //noinspection unchecked
            ((List<String>) metaClass.getAnnotations().get(MetadataTools.SYSTEM_ANN_NAME)).add(metaProperty.getName());
        }

        if (isEmbedded(field)) {
            registerPropertyAs(metaProperty, MetadataTools.EMBEDDED_PROPERTIES_ANN_NAME);
        }

        CascadeType[] types = getCascadeType(field);
        if (types != null && types.length > 0) {
            metaProperty.getAnnotations().put(MetadataTools.CASCADE_TYPES_ANN_NAME, Arrays.asList(types));
            registerPropertyAs(metaProperty, MetadataTools.CASCADE_PROPERTIES_ANN_NAME);
        }
    }

    private void registerPropertyAs(MetaProperty metaProperty, String tag) {
        MetaClass metaClass = metaProperty.getDomain();
        if (!metaClass.getAnnotations().containsKey(tag)) {
            metaClass.getAnnotations().put(tag, new LinkedList<String>());
        }
        //noinspection unchecked
        ((List<String>) metaClass.getAnnotations().get(tag)).add(metaProperty.getName());
    }

    @Nullable
    private CascadeType[] getCascadeType(Field field) {
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            return manyToMany.cascade();
        }

        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        if (manyToOne != null) {
            return manyToOne.cascade();
        }


        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            return oneToOne.cascade();
        }

        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            return oneToMany.cascade();
        }
        return null;
    }

    protected void assignStore(MetaProperty metaProperty) {
        Store classStore = metaProperty.getDomain().getStore();
        if (hasJpaAnnotation(metaProperty.getDomain().getJavaClass())
                && !hasJpaAnnotation(metaProperty.getAnnotatedElement())) {
            ((MetaPropertyImpl) metaProperty).setStore(stores.get(Stores.UNDEFINED));
        } else {
            ((MetaPropertyImpl) metaProperty).setStore(classStore);
        }
    }

    protected boolean isSystem(Field field, MetaProperty metaProperty) {
        for (Class<? extends Annotation> annotation : SYSTEM_ANNOTATIONS) {
            if (field.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    protected Class getFieldTypeAccordingAnnotations(Field field) {
        OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
        OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
        ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
        ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);

        Class result = null;
        if (oneToOneAnnotation != null) {
            result = oneToOneAnnotation.targetEntity();
        } else if (oneToManyAnnotation != null) {
            result = oneToManyAnnotation.targetEntity();
        } else if (manyToOneAnnotation != null) {
            result = manyToOneAnnotation.targetEntity();
        } else if (manyToManyAnnotation != null) {
            result = manyToManyAnnotation.targetEntity();
        }
        return result;
    }

    @Nullable
    protected Class getTypeOverride(AnnotatedElement element) {
        Temporal temporal = element.getAnnotation(Temporal.class);
        if (temporal != null && temporal.value().equals(TemporalType.DATE))
            return java.sql.Date.class;
        else if (temporal != null && temporal.value().equals(TemporalType.TIME))
            return java.sql.Time.class;
        else
            return null;
    }

    /**
     * @param base field or method for field-based or property-based access type
     * @return
     */
    protected boolean isMandatory(AccessibleObject base) {
        OneToMany oneToManyAnnotation = base.getAnnotation(OneToMany.class);
        ManyToMany manyToManyAnnotation = base.getAnnotation(ManyToMany.class);

        if (oneToManyAnnotation != null || manyToManyAnnotation != null) {
            return false;
        }

        Column columnAnnotation = base.getAnnotation(Column.class);
        OneToOne oneToOneAnnotation = base.getAnnotation(OneToOne.class);
        ManyToOne manyToOneAnnotation = base.getAnnotation(ManyToOne.class);

        JmixProperty jmixPropertyAnnotation =
                base.getAnnotation(JmixProperty.class);

        boolean superMandatory = (jmixPropertyAnnotation != null && jmixPropertyAnnotation.mandatory())
                || (base.getAnnotation(NotNull.class) != null
                && isDefinedForDefaultValidationGroup(base.getAnnotation(NotNull.class)));  // @NotNull without groups

        return (columnAnnotation != null && !columnAnnotation.nullable())
                || (oneToOneAnnotation != null && !oneToOneAnnotation.optional())
                || (manyToOneAnnotation != null && !manyToOneAnnotation.optional())
                || superMandatory;
    }

    protected Range.Cardinality getCardinality(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            return Range.Cardinality.NONE;
        } else if (field.isAnnotationPresent(OneToOne.class)) {
            return Range.Cardinality.ONE_TO_ONE;
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            return Range.Cardinality.ONE_TO_MANY;
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            return Range.Cardinality.MANY_TO_ONE;
        } else if (field.isAnnotationPresent(ManyToMany.class)) {
            return Range.Cardinality.MANY_TO_MANY;
        } else if (field.isAnnotationPresent(Embedded.class)) {
            return Range.Cardinality.ONE_TO_ONE;
        } else {
            Class<?> type = field.getType();
            if (Collection.class.isAssignableFrom(type)) {
                return Range.Cardinality.ONE_TO_MANY;
            } else if (type.isPrimitive() || datatypes.find(type) != null) {
                return Range.Cardinality.NONE;
            } else
                return Range.Cardinality.MANY_TO_ONE;
        }
    }

    @Nullable
    protected String getInverseField(Field field) {
        OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
        if (oneToManyAnnotation != null)
            return isBlank(oneToManyAnnotation.mappedBy()) ? null : oneToManyAnnotation.mappedBy();

        ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
        if (manyToManyAnnotation != null)
            return isBlank(manyToManyAnnotation.mappedBy()) ? null : manyToManyAnnotation.mappedBy();

        OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
        if (oneToOneAnnotation != null)
            return isBlank(oneToOneAnnotation.mappedBy()) ? null : oneToOneAnnotation.mappedBy();

        return null;
    }

    protected boolean isPrimaryKey(Field field) {
        return field.isAnnotationPresent(Id.class)
                || field.isAnnotationPresent(EmbeddedId.class)
                || field.isAnnotationPresent(JmixId.class);
    }

    protected boolean isUuidGeneratedValue(MetaProperty metaProperty, Field field) {
        return field.isAnnotationPresent(JmixGeneratedValue.class) && UUID.class.isAssignableFrom(field.getType());
    }

    protected boolean isEmbedded(Field field) {
        return field.isAnnotationPresent(Embedded.class) || field.isAnnotationPresent(EmbeddedId.class);
    }

    protected boolean hasJpaAnnotation(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(Id.class)
                || annotatedElement.isAnnotationPresent(Column.class)
                || annotatedElement.isAnnotationPresent(ManyToOne.class)
                || annotatedElement.isAnnotationPresent(OneToMany.class)
                || annotatedElement.isAnnotationPresent(ManyToMany.class)
                || annotatedElement.isAnnotationPresent(OneToOne.class)
                || annotatedElement.isAnnotationPresent(Embedded.class)
                || annotatedElement.isAnnotationPresent(EmbeddedId.class);
    }

    protected boolean hasJpaAnnotation(Class<?> javaClass) {
        return javaClass.isAnnotationPresent(javax.persistence.Entity.class)
                || javaClass.isAnnotationPresent(Embeddable.class)
                || javaClass.isAnnotationPresent(MappedSuperclass.class);
    }

    protected boolean isCollection(Field field) {
        Class<?> type = field.getType();
        return Collection.class.isAssignableFrom(type);
    }

    protected boolean isMap(Field field) {
        final Class<?> type = field.getType();
        return Map.class.isAssignableFrom(type);
    }

    protected boolean isMap(Method method) {
        final Class<?> type = method.getReturnType();
        return Map.class.isAssignableFrom(type);
    }

    protected boolean isCollection(Method method) {
        final Class<?> type = method.getReturnType();
        return Collection.class.isAssignableFrom(type);
    }

    protected void onPropertyLoaded(MetaProperty metaProperty, Method method) {
        loadPropertyAnnotations(metaProperty, method);
        assignStore(metaProperty);
    }

    protected void loadPropertyAnnotations(MetaProperty metaProperty, AnnotatedElement annotatedElement) {
        for (Annotation annotation : annotatedElement.getAnnotations()) {
            MetaAnnotation metaAnnotation = AnnotationUtils.findAnnotation(annotation.getClass(), MetaAnnotation.class);
            if (metaAnnotation != null) {
                Map<String, Object> attributes = new LinkedHashMap<>(AnnotationUtils.getAnnotationAttributes(annotatedElement, annotation));
                metaProperty.getAnnotations().put(annotation.annotationType().getName(), attributes);
            }
        }

        DependsOnProperties dependsOnAnnotation =
                annotatedElement.getAnnotation(DependsOnProperties.class);
        if (dependsOnAnnotation != null) {
            String[] dependsOn = dependsOnAnnotation.value();
            if (dependsOn.length != 0) {
                metaProperty.getAnnotations().put("dependsOnProperties", Joiner.on(',').join(dependsOn));
            }
        }

        LastModifiedDate lastModifiedDate =
                annotatedElement.getAnnotation(LastModifiedDate.class);
        if (lastModifiedDate != null) {
            metaProperty.getAnnotations().put(lastModifiedDate.annotationType().getName(), lastModifiedDate);
        }

        loadBeanValidationAnnotations(metaProperty, annotatedElement);
    }

    protected void loadBeanValidationAnnotations(MetaProperty metaProperty, AnnotatedElement annotatedElement) {
        NotNull notNull = annotatedElement.getAnnotation(NotNull.class);
        if (notNull != null) {
            if (isDefinedForDefaultValidationGroup(notNull)) {
                metaProperty.getAnnotations().put(NotNull.class.getName() + VALIDATION_NOTNULL_MESSAGE, notNull.message());
            }
            if (isDefinedForValidationGroup(notNull, UiComponentChecks.class, true)) {
                metaProperty.getAnnotations().put(NotNull.class.getName() + VALIDATION_NOTNULL_MESSAGE, notNull.message());
                metaProperty.getAnnotations().put(NotNull.class.getName() + VALIDATION_NOTNULL_UI_COMPONENT, true);
            }
        }

        Size size = annotatedElement.getAnnotation(Size.class);
        if (size != null && isDefinedForDefaultValidationGroup(size)) {
            metaProperty.getAnnotations().put(Size.class.getName() + VALIDATION_MIN, size.min());
            metaProperty.getAnnotations().put(Size.class.getName() + VALIDATION_MAX, size.max());
        }

        Length length = annotatedElement.getAnnotation(Length.class);
        if (length != null && isDefinedForDefaultValidationGroup(length)) {
            metaProperty.getAnnotations().put(Length.class.getName() + VALIDATION_MIN, length.min());
            metaProperty.getAnnotations().put(Length.class.getName() + VALIDATION_MAX, length.max());
        }

        Min min = annotatedElement.getAnnotation(Min.class);
        if (min != null && isDefinedForDefaultValidationGroup(min)) {
            metaProperty.getAnnotations().put(Min.class.getName(), min.value());
        }

        Max max = annotatedElement.getAnnotation(Max.class);
        if (max != null && isDefinedForDefaultValidationGroup(max)) {
            metaProperty.getAnnotations().put(Max.class.getName(), max.value());
        }

        DecimalMin decimalMin = annotatedElement.getAnnotation(DecimalMin.class);
        if (decimalMin != null && isDefinedForDefaultValidationGroup(decimalMin)) {
            metaProperty.getAnnotations().put(DecimalMin.class.getName(), decimalMin.value());
        }

        DecimalMax decimalMax = annotatedElement.getAnnotation(DecimalMax.class);
        if (decimalMax != null && isDefinedForDefaultValidationGroup(decimalMax)) {
            metaProperty.getAnnotations().put(DecimalMax.class.getName(), decimalMax.value());
        }

        Past past = annotatedElement.getAnnotation(Past.class);
        if (past != null && isDefinedForDefaultValidationGroup(past)) {
            metaProperty.getAnnotations().put(Past.class.getName(), true);
        }

        Future future = annotatedElement.getAnnotation(Future.class);
        if (future != null && isDefinedForDefaultValidationGroup(future)) {
            metaProperty.getAnnotations().put(Future.class.getName(), true);
        }

        PastOrPresent pastOrPresent = annotatedElement.getAnnotation(PastOrPresent.class);
        if (pastOrPresent != null && isDefinedForDefaultValidationGroup(pastOrPresent)) {
            metaProperty.getAnnotations().put(PastOrPresent.class.getName(), true);
        }

        FutureOrPresent futureOrPresent = annotatedElement.getAnnotation(FutureOrPresent.class);
        if (futureOrPresent != null && isDefinedForDefaultValidationGroup(futureOrPresent)) {
            metaProperty.getAnnotations().put(FutureOrPresent.class.getName(), true);
        }
    }

    protected boolean isDefinedForDefaultValidationGroup(Annotation annotation) {
        return isDefinedForValidationGroup(annotation, javax.validation.groups.Default.class, true);
    }

    protected boolean isDefinedForValidationGroup(Annotation annotation, Class groupClass, boolean inheritDefault) {
        try {
            Method groupsMethod = annotation.getClass().getMethod("groups");
            Class<?>[] groups = (Class<?>[]) groupsMethod.invoke(annotation);
            if (inheritDefault && groups.length == 0) {
                return true;
            }
            return ArrayUtils.contains(groups, groupClass);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Unable to use annotation metadata " + annotation);
        }
    }

    @Nullable
    protected Datatype getAdaptiveDatatype(AnnotatedElement annotatedElement) {
        PropertyDatatype annotation = annotatedElement.getAnnotation(PropertyDatatype.class);
        return annotation != null && !annotation.value().equals("") ? datatypes.get(annotation.value()) : null;
    }

    protected boolean setterExists(Field field) {
        List<String> setterNames = buildSetterNames(field);
        Method[] methods = field.getDeclaringClass().getDeclaredMethods();
        for (Method method : methods) {
            if (setterNames.contains(method.getName()))
                return true;
        }
        return false;
    }

    /**
     * Builds a list of possible setter names for the field. There may be two options:
     * <ul>
     *     <li>In most cases the setter name is "set&lt;FieldName&gt;", e.g. "setPrice" for a field "price"</li>
     *     <li>There may be a special case for Kotlin entity. If the field name starts with "is" (e.g. "isApproved") then a setter name will be
     *     "setApproved", not "setIsApproved"</li>
     * </ul>
     */
    protected List<String> buildSetterNames(Field field) {
        List<String> setterNames = new ArrayList<>();
        String fieldName = field.getName();
        if (fieldName.startsWith("is") && fieldName.length() > 2 && Character.isUpperCase(fieldName.charAt(2))) {
            setterNames.add("set" + fieldName.substring(2));
        }
        setterNames.add("set" + StringUtils.capitalize(fieldName));
        setterNames.add("set" + fieldName);
        return setterNames;
    }

    protected boolean setterExists(Method getter) {
        if (getter.getName().startsWith("get")) {
            String setterName;
            setterName = "set" + getter.getName().substring(3);
            Method[] methods = getter.getDeclaringClass().getDeclaredMethods();
            for (Method method : methods) {
                if (setterName.equals(method.getName())) {
                    return true;
                }
            }
        }
        if (getter.getName().startsWith("is")) {
            //handle a special case of Kotlin entity and a property with a name starting with "is*"
            String setterName = "set" + getter.getName().substring(2);
            Method[] methods = getter.getDeclaringClass().getDeclaredMethods();
            for (Method method : methods) {
                if (setterName.equals(method.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void assignPropertyType(AnnotatedElement field, MetaProperty property, Range range) {
        if (range.isClass()) {
            Composition composition = field.getAnnotation(Composition.class);
            Embedded embedded = field.getAnnotation(Embedded.class);
            EmbeddedId embeddedId = field.getAnnotation(EmbeddedId.class);
            if (embedded != null || embeddedId != null) {
                ((MetaPropertyImpl) property).setType(MetaProperty.Type.EMBEDDED);
            } else if (composition != null) {
                ((MetaPropertyImpl) property).setType(MetaProperty.Type.COMPOSITION);
            } else {
                ((MetaPropertyImpl) property).setType(MetaProperty.Type.ASSOCIATION);
            }
        } else if (range.isDatatype()) {
            ((MetaPropertyImpl) property).setType(MetaProperty.Type.DATATYPE);
        } else if (range.isEnum()) {
            ((MetaPropertyImpl) property).setType(MetaProperty.Type.ENUM);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("unchecked")
    protected MetadataObjectInfo<Range> loadRange(Session session, MetaProperty metaProperty, Class<?> type, Map<String, Object> map) {
        Datatype datatype = (Datatype) map.get("datatype");
        if (datatype != null) {
            // A datatype is assigned explicitly
            return new MetadataObjectInfo<>(new DatatypeRange(datatype));
        }

        datatype = getAdaptiveDatatype(metaProperty, type);
        if (datatype == null) {
            datatype = datatypes.find(type);
        }
        if (datatype != null) {
            MetaClass metaClass = metaProperty.getDomain();
            Class<?> javaClass = metaClass.getJavaClass();

            try {
                String name = "get" + StringUtils.capitalize(metaProperty.getName());
                Method method = javaClass.getMethod(name);

                Class<Enum> returnType = (Class<Enum>) method.getReturnType();
                if (returnType.isEnum()) {
                    return new MetadataObjectInfo<>(new EnumerationRange(new EnumerationImpl<>(returnType)));
                }
            } catch (NoSuchMethodException e) {
                // ignore
            }
            return new MetadataObjectInfo<>(new DatatypeRange(datatype));

        } else if (type.isEnum()) {
            return new MetadataObjectInfo<>(new EnumerationRange(new EnumerationImpl(type)));

        } else {
            return new MetadataObjectInfo<>(null, Collections.singletonList(new RangeInitTask(session, metaProperty, type, map)));
        }
    }

    @Nullable
    protected Datatype getAdaptiveDatatype(MetaProperty metaProperty, Class<?> type) {
        NumberFormat numberFormat = metaProperty.getAnnotatedElement().getAnnotation(NumberFormat.class);
        if (numberFormat != null) {
            if (Number.class.isAssignableFrom(type)) {
                return new AdaptiveNumberDatatype(type, numberFormat, formatStringsRegistry);
            } else {
                log.warn("NumberFormat annotation is ignored because " + metaProperty + " is not a Number");
            }
        }
        return null;
    }

    protected Class getFieldType(Field field) {
        Type genericType = field.getGenericType();
        Class type;
        if (genericType instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
            if (Map.class.isAssignableFrom(field.getType()))
                type = toClass(types[1]);
            else
                type = toClass(types[0]);
        } else {
            type = getFieldTypeAccordingAnnotations(field);
        }
        if (type == null)
            throw new IllegalArgumentException("Field " + field
                    + " must either be of parametrized type or have a JPA annotation declaring a targetEntity");
        return type;
    }

    protected Class toClass(Type type) {
        if (type instanceof WildcardType) {
            return (Class<?>) ((WildcardType) type).getUpperBounds()[0];
        }
        return (Class<?>) type;
    }

    protected void assignInverse(MetaPropertyImpl property, Range range, String inverseField) {
        if (inverseField == null)
            return;

        if (!range.isClass())
            throw new IllegalArgumentException("Range of class type expected");

        MetaClass metaClass = range.asClass();
        MetaProperty inverseProp = metaClass.findProperty(inverseField);
        if (inverseProp == null)
            throw new RuntimeException(String.format(
                    "Unable to assign inverse property '%s' for '%s'", inverseField, property));
        property.setInverse(inverseProp);
    }

    protected boolean isOrdered(Field field) {
        Class<?> type = field.getType();
        return List.class.isAssignableFrom(type) || LinkedHashSet.class.isAssignableFrom(type);
    }

    protected class RangeInitTask {

        private Session session;
        private MetaProperty metaProperty;
        private Class rangeClass;
        private Map<String, Object> map;

        public RangeInitTask(Session session, MetaProperty metaProperty, Class rangeClass, Map<String, Object> map) {
            this.session = session;
            this.metaProperty = metaProperty;
            this.rangeClass = rangeClass;
            this.map = map;
        }

        public String getWarning() {
            return String.format(
                    "Range for property '%s' wasn't initialized (range class '%s')",
                    metaProperty.getName(), rangeClass.getName());
        }

        public void execute() {
            MetaClass rangeClass = session.findClass(this.rangeClass);
            if (rangeClass == null) {
                throw new IllegalStateException(
                        String.format("Can't find range class '%s' for property '%s.%s'",
                                this.rangeClass.getName(), metaProperty.getDomain(), metaProperty.getName()));
            } else {
                ClassRange range = new ClassRange(rangeClass);

                Range.Cardinality cardinality = (Range.Cardinality) map.get("cardinality");
                range.setCardinality(cardinality);
                if (Range.Cardinality.ONE_TO_MANY.equals(cardinality) ||
                        Range.Cardinality.MANY_TO_MANY.equals(cardinality)) {
                    range.setOrdered((Boolean) map.get("ordered"));
                }

                Boolean mandatory = (Boolean) map.get("mandatory");
                if (mandatory != null) {
                    ((MetaPropertyImpl) metaProperty).setMandatory(mandatory);
                }

                ((MetaPropertyImpl) metaProperty).setRange(range);
                assignPropertyType(metaProperty.getAnnotatedElement(), metaProperty, range);

                assignInverse((MetaPropertyImpl) metaProperty, range, (String) map.get("inverseField"));
            }
        }
    }

    public static class MetadataObjectInfo<T> {

        private T object;
        private Collection<RangeInitTask> tasks;

        public MetadataObjectInfo(T object) {
            this.object = object;
            this.tasks = Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        public MetadataObjectInfo(@Nullable T object, Collection<? extends RangeInitTask> tasks) {
            this.object = object;
            this.tasks = (Collection<RangeInitTask>) tasks;
        }

        public T getObject() {
            return object;
        }

        public Collection<RangeInitTask> getTasks() {
            return tasks;
        }

        public void setTasks(Collection<RangeInitTask> tasks) {
            this.tasks = tasks;
        }
    }
}
