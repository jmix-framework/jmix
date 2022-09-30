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

import com.google.common.base.Splitter;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityEntryHasUuid;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.IgnoreUserTimeZone;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.TimeZoneAwareDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.Id;
import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Utility class to provide common metadata-related functionality.
 * <p> Implemented as Spring bean to allow for extension in
 * application projects.
 */
@Component("core_MetadataTools")
public class MetadataTools {

    public static final String PRIMARY_KEY_ANN_NAME = "jmix.primaryKey";
    public static final String EMBEDDED_ANN_NAME = "jmix.embedded";
    public static final String TEMPORAL_ANN_NAME = "jmix.temporal";
    public static final String SYSTEM_ANN_NAME = "jmix.system";
    public static final String STORE_ANN_NAME = "jmix.storeName";
    public static final String LENGTH_ANN_NAME = "jmix.length";
    public static final String CASCADE_TYPES_ANN_NAME = "jmix.cascadeTypes";
    public static final String CASCADE_PROPERTIES_ANN_NAME = "jmix.cascadeProperties";
    public static final String EMBEDDED_PROPERTIES_ANN_NAME = "jmix.embeddedProperties";

    /**
     * Not applicable for legacy cuba entities
     */
    public static final String UUID_KEY_ANN_NAME = "jmix.uuidKey";

    public static final String DELETED_DATE_ANN_NAME = DeletedDate.class.getName();
    public static final String DELETED_BY_ANN_NAME = DeletedBy.class.getName();
    public static final String LAST_MODIFIED_DATE_ANN_NAME = LastModifiedDate.class.getName();


    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected Messages messages;

    @Autowired
    protected InstanceNameProvider instanceNameProvider;

    @Autowired(required = false)
    protected List<MetadataExtension> metadataExtensions;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected PersistentAttributesLoadChecker persistentAttributesLoadChecker;

    @Autowired(required = false)
    protected Collection<MetaPropertyPathResolver> metaPropertyPathResolvers;

    protected volatile Collection<Class<?>> enums;

    /**
     * Default constructor used by container at runtime and in server-side integration tests.
     */
    public MetadataTools() {
    }

    /**
     * Formats a value according to the property type.
     *
     * @param value    value of the passed property to format
     * @param property property
     * @return formatted value as string
     */
    public String format(@Nullable Object value, MetaProperty property) {
        checkNotNullArgument(property, "property is null");

        if (value == null) {
            return "";
        }

        Range range = property.getRange();
        // todo dynamic attributes
//        if (DynamicAttributesUtils.isDynamicAttribute(property)) {
//            CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(property);
//
//            if (categoryAttribute.getDataType().equals(PropertyType.ENUMERATION)) {
//                return LocaleHelper.getEnumLocalizedValue((String) value, categoryAttribute.getEnumerationLocales());
//            }
//
//            if (categoryAttribute.getIsCollection() && value instanceof Collection) {
//                return dynamicAttributesTools.getDynamicAttributeValueAsString(property, value);
//            }
//        }

        if (range.isEnum()) {
            return messages.getMessage((Enum) value);
        } else if (value instanceof Entity) {
            return getInstanceName((Entity) value);
        } else if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>) value;
            return collection.stream()
                    .map(this::format)
                    .collect(Collectors.joining(", "));
        } else if (range.isDatatype()) {
            Datatype datatype = range.asDatatype();
            if (datatype instanceof TimeZoneAwareDatatype) {
                Boolean ignoreUserTimeZone = getMetaAnnotationValue(property, IgnoreUserTimeZone.class);
                if (!Boolean.TRUE.equals(ignoreUserTimeZone)) {
                    return ((TimeZoneAwareDatatype) datatype).format(value,
                            currentAuthentication.getLocale(), currentAuthentication.getTimeZone());
                }
            }
            return datatype.format(value, currentAuthentication.getLocale());
        } else {
            return value.toString();
        }
    }

    /**
     * Formats a value according to the value type.
     *
     * @param value object to format
     * @return formatted value as string
     */
    public String format(@Nullable Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof Entity) {
            return getInstanceName((Entity) value);
        } else if (value instanceof Enum) {
            return messages.getMessage((Enum) value, currentAuthentication.getLocale());
        } else if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>) value;
            return collection.stream()
                    .map(this::format)
                    .collect(Collectors.joining(", "));
        } else {
            Datatype datatype = datatypeRegistry.find(value.getClass());
            if (datatype != null) {
                return datatype.format(value, currentAuthentication.getLocale());
            }

            return value.toString();
        }
    }

    /**
     * @param instance instance
     * @return Instance name as defined by {@link io.jmix.core.metamodel.annotation.InstanceName}
     * or <code>toString()</code>.
     */
    public String getInstanceName(Object instance) {
        return instanceNameProvider.getInstanceName(instance);
    }

    /**
     * @return name of a primary key attribute, or null if the entity has no primary key (e.g. embeddable)
     */
    @Nullable
    public String getPrimaryKeyName(MetaClass metaClass) {
        String pkProperty = (String) metaClass.getAnnotations().get(PRIMARY_KEY_ANN_NAME);
        if (pkProperty != null) {
            return pkProperty;
        } else {
            MetaClass ancestor = metaClass.getAncestor();
            while (ancestor != null) {
                pkProperty = (String) ancestor.getAnnotations().get(PRIMARY_KEY_ANN_NAME);
                if (pkProperty != null)
                    return pkProperty;
                ancestor = ancestor.getAncestor();
            }
        }
        return null;
    }

    /**
     * @return MetaProperty representing a primary key attribute, or null if the entity has no primary key (e.g.
     * embeddable)
     */
    @Nullable
    public MetaProperty getPrimaryKeyProperty(MetaClass metaClass) {
        String primaryKeyName = getPrimaryKeyName(metaClass);
        return primaryKeyName == null ? null : metaClass.getProperty(primaryKeyName);
    }

    /**
     * @return MetaProperty representing a primary key attribute, or null if the entity has no primary key (e.g.
     * embeddable)
     */
    @Nullable
    public MetaProperty getPrimaryKeyProperty(Class<?> entityClass) {
        return getPrimaryKeyProperty(metadata.getClass(entityClass));
    }

    /**
     * @return true if passed MetaClass has a composite primary key
     */
    public boolean hasCompositePrimaryKey(MetaClass metaClass) {
        MetaProperty primaryKeyProperty = getPrimaryKeyProperty(metaClass);
        return primaryKeyProperty != null && primaryKeyProperty.getAnnotatedElement().isAnnotationPresent(EmbeddedId.class);
    }

    /**
     * @return true if passed MetaClass has a db generated primary key
     */
    public boolean hasDbGeneratedPrimaryKey(MetaClass metaClass) {
        MetaProperty primaryKeyProperty = getPrimaryKeyProperty(metaClass);
        return primaryKeyProperty != null && primaryKeyProperty.getAnnotatedElement().isAnnotationPresent(GeneratedValue.class);
    }

    /**
     * @return true if the first MetaClass is equal or an ancestor of the second.
     */
    public boolean isAssignableFrom(MetaClass metaClass, MetaClass other) {
        checkNotNullArgument(metaClass);
        checkNotNullArgument(other);
        return metaClass.equals(other) || metaClass.getDescendants().contains(other);
    }

    /**
     * Determine whether an object denoted by the given property is merged into persistence context together with the
     * owning object. This is true if the property is ManyToMany, or if it is OneToMany with certain CascadeType
     * defined.
     *
     * @deprecated use {@link MetadataTools#getCascadeTypes(MetaProperty)} instead
     */
    @Deprecated
    public boolean isCascade(MetaProperty metaProperty) {
        Objects.requireNonNull(metaProperty, "metaProperty is null");
        OneToMany oneToMany = metaProperty.getAnnotatedElement().getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            CascadeType[] cascadeTypes = oneToMany.cascade();
            if (ArrayUtils.contains(cascadeTypes, CascadeType.ALL) ||
                    ArrayUtils.contains(cascadeTypes, CascadeType.MERGE)) {
                return true;
            }
        }
        ManyToMany manyToMany = metaProperty.getAnnotatedElement().getAnnotation(ManyToMany.class);
        if (manyToMany != null && StringUtils.isBlank(manyToMany.mappedBy())) {
            return true;
        }
        return false;
    }

    public List<CascadeType> getCascadeTypes(MetaProperty metaProperty) {
        Objects.requireNonNull(metaProperty, "metaProperty is null");

        if (metaProperty.getAnnotations().containsKey(CASCADE_TYPES_ANN_NAME)) {
            //noinspection unchecked
            return (List<CascadeType>) metaProperty.getAnnotations().get(CASCADE_TYPES_ANN_NAME);
        }
        return Collections.emptyList();
    }

    /**
     * @param metaClass
     * @param type      - type of operation to find properties.
     * @return properties with {@link CascadeType} containg {@link CascadeType#ALL} or specified type
     */
    public List<MetaProperty> getCascadeProperties(MetaClass metaClass, @Nullable CascadeType type) {
        if (metaClass.getAnnotations().containsKey(CASCADE_PROPERTIES_ANN_NAME)) {
            List<MetaProperty> result = new LinkedList<>();
            //noinspection unchecked
            for (String propertyName : ((List<String>) metaClass.getAnnotations().get(CASCADE_PROPERTIES_ANN_NAME))) {
                MetaProperty property = metaClass.getProperty(propertyName);
                //noinspection unchecked
                List<CascadeType> types = (List<CascadeType>) property.getAnnotations().get(CASCADE_TYPES_ANN_NAME);
                if (type == null || types.contains(CascadeType.ALL) || types.contains(type)) {
                    result.add(property);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * @return Names of embedded properties or empty list if no such properties contained in {@code metaClass}
     */
    public List<String> getEmbeddedProperties(MetaClass metaClass) {
        if (metaClass.getAnnotations().containsKey(EMBEDDED_PROPERTIES_ANN_NAME))
            //noinspection unchecked
            return (List<String>) metaClass.getAnnotations().get(EMBEDDED_PROPERTIES_ANN_NAME);
        return Collections.emptyList();
    }

    /**
     * Determine whether the entity supports <em>Soft Deletion</em>.
     *
     * @param entityClass entity class
     * @return {@code true} if the entity has @{@link DeletedDate} field
     */
    public boolean isSoftDeletable(Class<?> entityClass) {
        return findDeletedDateProperty(entityClass) != null;
    }

    /**
     * Determine whether the given property is system. A property is considered system if it has one of the following
     * annotations:
     * <ul>
     *     <li>{@link Id}</li>
     *     <li>{@link JmixId}</li>
     *     <li>{@link JmixGeneratedValue}</li>
     *     <li>{@link Version}</li>
     *     <li>{@link CreatedDate}</li>
     *     <li>{@link CreatedBy}</li>
     *     <li>{@link LastModifiedDate}</li>
     *     <li>{@link LastModifiedBy}</li>
     *     <li>{@link DeletedDate}</li>
     *     <li>{@link DeletedBy}</li>
     * </ul>
     */
    public boolean isSystem(MetaProperty metaProperty) {
        Objects.requireNonNull(metaProperty, "metaProperty is null");
        return Boolean.TRUE.equals(metaProperty.getAnnotations().get(SYSTEM_ANN_NAME));
    }

    /**
     * <b>System Properties</b> - is important properties used for identification, audit, soft-delete and optimistic lock
     * purposes
     * More formally, it is properties with annotations:
     * <ul>
     *     <li>{@link Id},</li>
     *     <li>{@link JmixId},</li>
     *     <li>{@link EmbeddedId}</li>
     *     <li>{@link JmixGeneratedValue}</li>
     *     <li>{@link CreatedDate}</li>
     *     <li>{@link CreatedBy}</li>
     *     <li>{@link LastModifiedDate}</li>
     *     <li>{@link LastModifiedBy}</li>
     *     <li>{@link DeletedDate}</li>
     *     <li>{@link DeletedBy}</li>
     *     <li>{@link javax.persistence.Version}</li>
     * </ul>
     *
     * @return names of system properties used in Entity determined by {@code metaClass} parameter
     */
    public List<String> getSystemProperties(MetaClass metaClass) {
        List<String> result = new LinkedList<>();
        while (metaClass != null) {
            if (metaClass.getAnnotations().containsKey(SYSTEM_ANN_NAME)) {
                //noinspection unchecked
                result.addAll((Collection<String>) metaClass.getAnnotations().get(SYSTEM_ANN_NAME));
            }
            metaClass = metaClass.getAncestor();
        }
        return result;
    }

    /**
     * Determine whether all the properties defined by the given property path are persistent attributes
     * of JPA entities or embeddables.
     *
     * @see #isJpa(io.jmix.core.metamodel.model.MetaProperty)
     */
    public boolean isJpa(MetaPropertyPath metaPropertyPath) {
        Objects.requireNonNull(metaPropertyPath, "metaPropertyPath is null");
        for (MetaProperty metaProperty : metaPropertyPath.getMetaProperties()) {
            if (!isJpa(metaProperty))
                return false;
        }
        return true;
    }

    /**
     * Determine whether the given property is a persistent attribute of a JPA entity or embeddable.
     */
    public boolean isJpa(MetaProperty metaProperty) {
        Objects.requireNonNull(metaProperty, "metaProperty is null");
        return metaProperty.getStore().getDescriptor().isJpa();
    }

    /**
     * Determine whether the given property denotes an embedded object.
     *
     * @see Embedded
     * @see EmbeddedId
     */
    public boolean isEmbedded(MetaProperty metaProperty) {
        Objects.requireNonNull(metaProperty, "metaProperty is null");
        return metaProperty.getAnnotatedElement() != null
                && (metaProperty.getAnnotatedElement().isAnnotationPresent(Embedded.class) ||
                metaProperty.getAnnotatedElement().isAnnotationPresent(EmbeddedId.class));
    }

    /**
     * Determine whether the given property is a LOB.
     *
     * @see Lob
     */
    public boolean isLob(MetaProperty metaProperty) {
        Objects.requireNonNull(metaProperty, "metaProperty is null");
        return metaProperty.getAnnotatedElement() != null
                && metaProperty.getAnnotatedElement().isAnnotationPresent(Lob.class);
    }

    /**
     * Determine whether the given property is on the owning side of an association.
     */
    public boolean isOwningSide(MetaProperty metaProperty) {
        checkNotNullArgument(metaProperty, "metaProperty is null");
        if (!metaProperty.getRange().isClass())
            return false;

        AnnotatedElement el = metaProperty.getAnnotatedElement();
        for (Annotation annotation : el.getAnnotations()) {
            if (annotation instanceof ManyToOne)
                return true;
            if (annotation instanceof OneToMany || annotation instanceof OneToOne)
                return el.isAnnotationPresent(JoinColumn.class) || el.isAnnotationPresent(JoinTable.class);
            if (annotation instanceof ManyToMany)
                return el.isAnnotationPresent(JoinTable.class);
        }

        return false;
    }

    /**
     * Determine whether the given entity is marked as {@link SystemLevel}.
     */
    public boolean isSystemLevel(MetaClass metaClass) {
        Objects.requireNonNull(metaClass, "metaClass is null");
        Map<String, Object> metaAnnotationAttributes = getMetaAnnotationAttributes(metaClass.getAnnotations(), SystemLevel.class);
        return Boolean.TRUE.equals(metaAnnotationAttributes.get("value"));
    }

    /**
     * Determine whether the given property is marked as {@link SystemLevel}.
     */
    public boolean isSystemLevel(MetaProperty metaProperty) {
        Objects.requireNonNull(metaProperty, "metaProperty is null");
        Map<String, Object> metaAnnotationAttributes = getMetaAnnotationAttributes(metaProperty.getAnnotations(), SystemLevel.class);
        return Boolean.TRUE.equals(metaAnnotationAttributes.get("value"));
    }

    public Map<String, Object> getMetaAnnotationAttributes(Map<String, Object> metaAnnotations, Class metaAnnotationClass) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map) metaAnnotations.get(metaAnnotationClass.getName());
        return map != null ? map : Collections.emptyMap();
    }

    /**
     * @return annotation value for specified metaProperty and annotation
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetaAnnotationValue(MetaProperty metaProperty, Class metaAnnotationClass) {
        Map<String, Object> metaAnnotationAttributes = getMetaAnnotationAttributes(metaProperty.getAnnotations(), metaAnnotationClass);
        return (T) metaAnnotationAttributes.get("value");
    }

    /**
     * Determine whether the given annotation is present in the object's class or in any of its superclasses.
     *
     * @param object          entity instance
     * @param property        property name
     * @param annotationClass annotation class
     */
    public boolean isAnnotationPresent(Object object, String property, Class<? extends Annotation> annotationClass) {
        Objects.requireNonNull(object, "object is null");
        return isAnnotationPresent(object.getClass(), property, annotationClass);
    }

    /**
     * Determine whether the given annotation is present in the object's class or in any of its superclasses.
     *
     * @param javaClass       entity class
     * @param property        property name
     * @param annotationClass annotation class
     * @return true if the annotation is present
     */
    public boolean isAnnotationPresent(Class javaClass, String property, Class<? extends Annotation> annotationClass) {
        Field field;
        try {
            field = javaClass.getDeclaredField(property);
            return field.isAnnotationPresent(annotationClass);
        } catch (NoSuchFieldException e) {
            Class superclass = javaClass.getSuperclass();
            while (superclass != null) {
                try {
                    field = superclass.getDeclaredField(property);
                    return field.isAnnotationPresent(annotationClass);
                } catch (NoSuchFieldException e1) {
                    superclass = superclass.getSuperclass();
                }
            }
            throw new RuntimeException("Property not found: " + property);
        }
    }

    /**
     * Determine whether the given meta-class represents a JPA entity.
     */
    public boolean isJpaEntity(MetaClass metaClass) {
        checkNotNullArgument(metaClass, "metaClass is null");
        return metaClass.getStore().getDescriptor().isJpa()
                && metaClass.getJavaClass().isAnnotationPresent(javax.persistence.Entity.class);
    }

    /**
     * Determine whether the given class represents a JPA entity.
     */
    public boolean isJpaEntity(Class<?> aClass) {
        checkNotNullArgument(aClass, "class is null");
        return isJpaEntity(metadata.getClass(aClass));
    }

    /**
     * Determine whether the given meta-class is JPA embeddable.
     */
    public boolean isJpaEmbeddable(MetaClass metaClass) {
        checkNotNullArgument(metaClass, "metaClass is null");
        return metaClass.getStore().getDescriptor().isJpa()
                && metaClass.getJavaClass().isAnnotationPresent(javax.persistence.Embeddable.class);
    }

    /**
     * Determine whether the given entity class is JPA embeddable.
     */
    public boolean isJpaEmbeddable(Class<?> aClass) {
        checkNotNullArgument(aClass, "Class is null");
        return isJpaEmbeddable(metadata.getClass(aClass));
    }

    public boolean isCacheable(MetaClass metaClass) {
        checkNotNullArgument(metaClass, "metaClass is null");
        return Boolean.TRUE.equals(metaClass.getAnnotations().get("cacheable"));
    }

    /**
     * Get metaclass that contains metaproperty for passed propertyPath.
     * Resolves real metaclass for property in consideration of inherited entity classes and extended classes.
     *
     * @param propertyPath Property path
     * @return metaclass
     */
    public MetaClass getPropertyEnclosingMetaClass(MetaPropertyPath propertyPath) {
        checkNotNullArgument(propertyPath, "Property path should not be null");

        MetaProperty[] propertyChain = propertyPath.getMetaProperties();
        if (propertyChain.length > 1) {
            MetaProperty chainProperty = propertyChain[propertyChain.length - 2];
            return chainProperty.getRange().asClass();
        } else {
            return propertyPath.getMetaClass();
        }
    }

    /**
     * Return a collection of properties included into entity's name pattern (see {@link InstanceName}).
     *
     * @param metaClass entity metaclass
     * @return collection of the name pattern properties
     */
    @Nonnull
    public Collection<MetaProperty> getInstanceNameRelatedProperties(MetaClass metaClass) {
        return getInstanceNameRelatedProperties(metaClass, false);
    }

    /**
     * Return a collection of properties included into entity's name pattern (see {@link InstanceName}).
     *
     * @param metaClass   entity metaclass
     * @param useOriginal if true, and if the given metaclass doesn't define a {@link InstanceName} and if it is an
     *                    extended entity, this method tries to find a name pattern in an original entity
     * @return collection of the name pattern properties
     */

    @Nonnull
    public Collection<MetaProperty> getInstanceNameRelatedProperties(MetaClass metaClass, boolean useOriginal) {
        return instanceNameProvider.getInstanceNameRelatedProperties(metaClass, useOriginal);
    }

    /**
     * @return collection of properties owned by this metaclass and all its ancestors in the form of {@link
     * MetaPropertyPath}s containing one property each
     */
    public Collection<MetaPropertyPath> getPropertyPaths(MetaClass metaClass) {
        List<MetaPropertyPath> res = new ArrayList<>();
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            res.add(new MetaPropertyPath(metaClass, metaProperty));
        }
        return res;
    }

    /**
     * Converts a collection of properties to collection of {@link MetaPropertyPath}s containing one property each
     */
    public Collection<MetaPropertyPath> toPropertyPaths(Collection<MetaProperty> properties) {
        List<MetaPropertyPath> res = new ArrayList<>();
        for (MetaProperty metaProperty : properties) {
            res.add(new MetaPropertyPath(metaProperty.getDomain(), metaProperty));
        }
        return res;
    }

    /**
     * Collects all meta-properties of the given meta-class included to the given fetchPlan as {@link MetaPropertyPath}s.
     *
     * @param fetchPlan fetch plan
     * @param metaClass meta-class
     * @return collection of paths
     */
    public Collection<MetaPropertyPath> getFetchPlanPropertyPaths(FetchPlan fetchPlan, MetaClass metaClass) {
        List<MetaPropertyPath> propertyPaths = new ArrayList<>(metaClass.getProperties().size());
        for (final MetaProperty metaProperty : metaClass.getProperties()) {
            final MetaPropertyPath metaPropertyPath = new MetaPropertyPath(metaClass, metaProperty);
            if (fetchPlanContainsProperty(fetchPlan, metaPropertyPath)) {
                propertyPaths.add(metaPropertyPath);
            }
        }
        return propertyPaths;
    }

    /**
     * Determine whether the fetch plan contains a property, traversing a fetch plan branch according to the given property path.
     *
     * @param fetchPlan    fetch plan instance. If null, return false immediately.
     * @param propertyPath property path defining the property
     */
    public boolean fetchPlanContainsProperty(@Nullable FetchPlan fetchPlan, MetaPropertyPath propertyPath) {
        FetchPlan currentFetchPlan = fetchPlan;
        for (MetaProperty metaProperty : propertyPath.getMetaProperties()) {
            if (currentFetchPlan == null)
                return false;

            FetchPlanProperty property = currentFetchPlan.getProperty(metaProperty.getName());
            if (property == null)
                return false;

            currentFetchPlan = property.getFetchPlan();
        }
        return true;
    }

    /**
     * @return collection of all meta-classes representing JPA entities
     */
    public Collection<MetaClass> getAllJpaEntityMetaClasses() {
        return metadata.getSession().getClasses().stream()
                .filter(this::isJpaEntity)
                .collect(Collectors.toList());
    }

    /**
     * @return collection of all meta-classes representing JPA embeddable classes
     */
    public Collection<MetaClass> getAllJpaEmbeddableMetaClasses() {
        return metadata.getSession().getClasses().stream()
                .filter(this::isJpaEmbeddable)
                .collect(Collectors.toList());
    }

    /**
     * @return collection of all Java enums used as a type of an entity attribute
     */
    public Collection<Class<?>> getAllEnums() {
        if (enums == null) {
            synchronized (this) {
                enums = new HashSet<>();
                for (MetaClass metaClass : metadata.getSession().getClasses()) {
                    for (MetaProperty metaProperty : metaClass.getProperties()) {
                        if (metaProperty.getRange().isEnum()) {
                            Class<?> c = metaProperty.getRange().asEnumeration().getJavaClass();
                            enums.add(c);
                        }
                    }
                }
            }
        }
        return enums;
    }

    /**
     * @return table name for the given entity, or null if the entity is Embeddable, MappedSuperclass or non-persistent
     */
    @Nullable
    public String getDatabaseTable(MetaClass metaClass) {
        if (isJpaEmbeddable(metaClass) || !isJpaEntity(metaClass))
            return null;

        Class<?> javaClass = metaClass.getJavaClass();
        javax.persistence.Table annotation = javaClass.getAnnotation(javax.persistence.Table.class);
        if (annotation != null && StringUtils.isNotEmpty(annotation.name())) {
            return annotation.name();
        } else if (metaClass.getAncestor() != null) {
            return getDatabaseTable(metaClass.getAncestor());
        }

        return null;
    }

    @Nullable
    public String getDatabaseColumn(MetaProperty metaProperty) {
        if (!isJpa(metaProperty))
            return null;
        Column column = metaProperty.getAnnotatedElement().getAnnotation(Column.class);
        if (column != null) {
            return StringUtils.isEmpty(column.name()) ? metaProperty.getName() : column.name();
        }
        JoinColumn joinColumn = metaProperty.getAnnotatedElement().getAnnotation(JoinColumn.class);
        if (joinColumn != null) {
            return StringUtils.isEmpty(joinColumn.name()) ? metaProperty.getName() : joinColumn.name();
        }
        return null;
    }

    /**
     * @return list of properties defined in {@link io.jmix.core.metamodel.annotation.DependsOnProperties}
     * or empty list
     */
    public List<String> getDependsOnProperties(Class<?> entityClass, String property) {
        checkNotNullArgument(entityClass, "entityClass is null");

        MetaClass metaClass = metadata.getClass(entityClass);
        return getDependsOnProperties(metaClass.getProperty(property));
    }

    /**
     * @return list of properties defined in {@link io.jmix.core.metamodel.annotation.DependsOnProperties}
     * or empty list
     */
    public List<String> getDependsOnProperties(MetaProperty metaProperty) {
        checkNotNullArgument(metaProperty, "metaProperty is null");

        String dependsOnProperties = (String) metaProperty.getAnnotations().get("dependsOnProperties");
        List<String> result = Collections.emptyList();
        if (dependsOnProperties != null) {
            result = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(dependsOnProperties);
        }
        return result;
    }

    /**
     * @return field annotated with @DeletedDate
     * @throws IllegalArgumentException if entity has no @{@link DeletedDate} field
     */
    public String getDeletedDateProperty(Object entity) throws IllegalArgumentException {
        String result = findDeletedDateProperty(entity.getClass());

        if (result != null) {
            return result;
        }

        throw new IllegalArgumentException("Entity is not soft deletable");
    }

    /**
     * @return field annotated with @DeletedDate or null if annotation is not present
     */
    @Nullable
    public String findDeletedDateProperty(Class<?> clazz) {
        return findPropertyByAnnotation(clazz, DELETED_DATE_ANN_NAME);
    }

    /**
     * @return field annotated with @DeletedBy or null if annotation is not present
     */
    @Nullable
    public String findDeletedByProperty(Class<?> clazz) {
        return findPropertyByAnnotation(clazz, DELETED_BY_ANN_NAME);
    }

    /**
     * @return field annotated with @LastModifiedDate or null if annotation is not present
     */
    @Nullable
    public String findLastModifiedDateProperty(Class<?> clazz) {
        return findPropertyByAnnotation(clazz, LAST_MODIFIED_DATE_ANN_NAME);
    }

    @Nullable
    protected String findPropertyByAnnotation(Class<?> clazz, String annotationName) {
        MetaClass metaClass = metadata.getClass(clazz);

        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getAnnotations().containsKey(annotationName)) {
                return property.getName();
            }
        }

        return null;
    }


    /**
     * @return list contains @{@link DeletedDate}, @{@link DeletedBy} property names if present.
     */
    public List<String> getSoftDeleteProperties(Class<?> clazz) {
        LinkedList<String> result = new LinkedList<>();

        Optional.ofNullable(findDeletedDateProperty(clazz)).ifPresent(result::add);
        Optional.ofNullable(findDeletedByProperty(clazz)).ifPresent(result::add);

        return result;
    }

    /**
     * Checks whether an entity has uuid key (primary or not)
     *
     * @return true if entity's {@link EntityEntry} implements {@link EntityEntryHasUuid}
     */
    public boolean hasUuid(MetaClass metaClass) {
        return metaClass.getAnnotations().containsKey(UUID_KEY_ANN_NAME);
    }

    @Nullable
    public String getUuidPropertyName(Class<?> clazz) {
        return (String) metadata.getClass(clazz).getAnnotations().get(UUID_KEY_ANN_NAME);
    }

    /**
     * If the given property is a reference to an entity from different data store, returns the name of a persistent
     * property which stores the identifier of the related entity.
     *
     * @param thisStore    name of a base data store
     * @param metaProperty property
     * @return name of the ID property or null if the given property is not a cross-datastore reference or it does not
     * satisfy the convention of declaring related properties for such references
     */
    @Nullable
    public String getCrossDataStoreReferenceIdProperty(String thisStore, MetaProperty metaProperty) {
        checkNotNullArgument(metaProperty, "metaProperty is null");
        if (!metaProperty.getRange().isClass())
            return null;

        String propStore = metaProperty.getRange().asClass().getStore().getName();
        if (Objects.equals(thisStore, propStore))
            return null;

        List<String> dependsOnProperties = getDependsOnProperties(metaProperty);
        if (dependsOnProperties.size() == 1)
            return dependsOnProperties.get(0);
        else
            return null;
    }

    /**
     * Returns a {@link MetaPropertyPath} which can include the special MetaProperty for a dynamic attribute.
     *
     * @param metaClass    originating meta-class
     * @param propertyPath path to the attribute
     * @return MetaPropertyPath instance
     */
    @Nullable
    public MetaPropertyPath resolveMetaPropertyPathOrNull(MetaClass metaClass, String propertyPath) {
        checkNotNullArgument(metaClass, "metaClass is null");

        MetaPropertyPath metaPropertyPath = metaClass.getPropertyPath(propertyPath);
        if (metaPropertyPath == null) {
            if (metaPropertyPathResolvers != null) {
                for (MetaPropertyPathResolver resolver : metaPropertyPathResolvers) {
                    metaPropertyPath = resolver.resolveMetaPropertyPath(metaClass, propertyPath);
                    if (metaPropertyPath != null) {
                        break;
                    }
                }
            }
        }
        return metaPropertyPath;
    }

    /**
     * Returns a {@link MetaPropertyPath} which can include the special MetaProperty for a dynamic attribute.
     * Throws an IllegalArgumentException if MetaPropertyPath can't be resolved.
     *
     * @param metaClass    originating meta-class
     * @param propertyPath path to the attribute
     * @return MetaPropertyPath instance
     */
    public MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String propertyPath) {
        MetaPropertyPath metaPropertyPath = resolveMetaPropertyPathOrNull(metaClass, propertyPath);
        if (metaPropertyPath == null) {
            throw new IllegalStateException(String.format("Could not resolve property path '%s' in '%s'", propertyPath, metaClass));
        }
        return metaPropertyPath;
    }

    /**
     * Depth-first traversal of the object graph starting from the specified entity instance.
     * Visits all attributes.
     *
     * @param entity  entity graph entry point
     * @param visitor the attribute visitor implementation
     */
    public void traverseAttributes(Object entity, EntityAttributeVisitor visitor) {
        checkNotNullArgument(entity, "entity is null");
        checkNotNullArgument(visitor, "visitor is null");
        EntityPreconditions.checkEntityType(entity);

        internalTraverseAttributes((Entity) entity, visitor, new HashSet<>());
    }

    /**
     * Depth-first traversal of the object graph by the fetch plan starting from the specified entity instance.
     * Visits attributes defined in the fetch plan.
     *
     * @param fetchPlan fetchPlan instance
     * @param entity    entity graph entry point
     * @param visitor   the attribute visitor implementation
     */
    public void traverseAttributesByFetchPlan(FetchPlan fetchPlan, Object entity, EntityAttributeVisitor visitor) {
        checkNotNullArgument(fetchPlan, "fetchPlan is null");
        checkNotNullArgument(entity, "entity is null");
        checkNotNullArgument(visitor, "visitor is null");
        EntityPreconditions.checkEntityType(entity);

        internalTraverseAttributesByFetchPlan(fetchPlan, (Entity) entity, visitor, new HashMap<>(), false);
    }

    /**
     * Depth-first traversal of the object graph by the fetch plan starting from the specified entity instance.
     * Visits attributes defined in the fetch plan.
     *
     * @param fetchPlan   fetchPlan instance
     * @param entity      entity graph entry point
     * @param checkLoaded if true, skips not loaded attributes
     * @param visitor     the attribute visitor implementation
     */
    public void traverseAttributesByFetchPlan(FetchPlan fetchPlan, Object entity, boolean checkLoaded, EntityAttributeVisitor visitor) {
        checkNotNullArgument(fetchPlan, "fetchPlan is null");
        checkNotNullArgument(entity, "entity is null");
        checkNotNullArgument(visitor, "visitor is null");
        EntityPreconditions.checkEntityType(entity);

        internalTraverseAttributesByFetchPlan(fetchPlan, (Entity) entity, visitor, new HashMap<>(), checkLoaded);
    }

    /**
     * Create a new instance and make it a shallow copy of the instance given.
     * <p> This method copies attributes according to the metadata.</p>
     * <br>
     * <p><b>WARNING:</b></p>
     * <ol>
     *     <li>
     *         New instance will not be initialized. Entity id generation, embedded objects creation,
     *         {@link PostConstruct} actions, etc. will not be performed for cloned object
     *         (see {@link EntityInitializer}).
     *     </li>
     *     <li>
     *         Unfetched or lazy-fetched attributes will not be cloned.
     *     </li>
     * </ol>
     *
     * @param source source instance
     * @return new instance of the same Java class as source
     */
    public <T> T copy(T source) {
        checkNotNullArgument(source, "source is null");

        @SuppressWarnings("unchecked")
        T dest = createInstance((Class<T>) source.getClass());

        copy(source, dest);
        return dest;
    }

    /**
     * Make a shallow copy of an instance.
     * <p> This method copies attributes according to the metadata.
     * <p> The source and destination instances don't have to be of the same Java class or metaclass. Copying
     * is performed in the following scenario: get each source property and copy the value to the destination if it
     * contains a property with the same name and it is not read-only.
     *
     * @param source source instance
     * @param dest   destination instance
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void copy(Object source, Object dest) {
        checkNotNullArgument(source, "source is null");
        checkNotNullArgument(dest, "dest is null");
        EntityPreconditions.checkEntityType(source);
        EntityPreconditions.checkEntityType(dest);

        MetaClass sourceMetaClass = metadata.getClass(source);
        MetaClass destMetaClass = metadata.getClass(dest);
        for (MetaProperty srcProperty : sourceMetaClass.getProperties()) {
            String name = srcProperty.getName();
            MetaProperty dstProperty = destMetaClass.findProperty(name);
            if (dstProperty != null && !dstProperty.isReadOnly() && persistentAttributesLoadChecker.isLoaded(source, name)) {
                try {
                    EntityValues.setValue(dest, name, EntityValues.getValue(source, name));
                } catch (RuntimeException e) {
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    if (cause == null)
                        cause = e;
                    // ignore exception on copy for not loaded fields
                    if (!isNotLoadedAttributeException(cause)) {
                        throw e;
                    }
                }
            }
        }

        if (dest instanceof CopyingSystemState && dest.getClass().isAssignableFrom(source.getClass())) {
            ((CopyingSystemState) dest).copyFrom(source);
        }

        // todo dynamic attributes
//        if (source instanceof BaseGenericIdEntity && dest instanceof BaseGenericIdEntity) {
//            ((BaseGenericIdEntity) dest).setDynamicAttributes(((BaseGenericIdEntity<?>) source).getDynamicAttributes());
//        }
    }

    /**
     * INTERNAL
     */
    @Internal
    public interface EntitiesHolder {
        Object create(Class<?> entityClass, Object id);

        Object find(Class<?> entityClass, Object id);

        void put(Object entity);
    }

    /**
     * INTERNAL
     */
    @Internal
    public static class CachingEntitiesHolder implements EntitiesHolder {

        private static class CacheKey {
            private Class<?> entityClass;
            private Object id;

            public CacheKey(Class<?> entityClass, Object id) {
                this.entityClass = entityClass;
                this.id = id;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CacheKey cacheKey = (CacheKey) o;
                return entityClass.equals(cacheKey.entityClass) &&
                        id.equals(cacheKey.id);
            }

            @Override
            public int hashCode() {
                return Objects.hash(entityClass, id);
            }
        }

        protected Map<CacheKey, Object> cache = new HashMap<>();

        @Override
        public Object create(Class<?> entityClass, Object id) {
            CacheKey key = new CacheKey(entityClass, id);
            Object entity = cache.get(key);
            if (entity == null) {
                entity = createInstanceWithId(entityClass, id);
                cache.put(key, entity);
            }

            return entity;
        }

        @Override
        public Object find(Class<?> entityClass, Object id) {
            return cache.get(new CacheKey(entityClass, id));
        }

        @Override
        public void put(Object entity) {
            cache.put(new CacheKey(entity.getClass(), EntityValues.getId(entity)), entity);
        }
    }

    /**
     * Makes a deep copy of the source entity. All referenced entities and collections will be copied as well.
     */
    @SuppressWarnings("unchecked")
    public <T> T deepCopy(T source) {
        EntityPreconditions.checkEntityType(source);
        CachingEntitiesHolder entityFinder = new CachingEntitiesHolder();
        Object destination = entityFinder.create(source.getClass(), EntityValues.getId(source));

        deepCopy(source, destination, entityFinder);

        return (T) destination;
    }

    /**
     * Copies all property values from source to destination excluding null values.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void deepCopy(Object source, Object destination, EntitiesHolder entitiesHolder) {
        EntityPreconditions.checkEntityType(source);
        EntityPreconditions.checkEntityType(destination);
        for (MetaProperty srcProperty : metadata.getClass(source).getProperties()) {
            String name = srcProperty.getName();

            if (srcProperty.isReadOnly() || !persistentAttributesLoadChecker.isLoaded(source, name)) {
                continue;
            }

            Object value = EntityValues.getValue(source, name);
            if (value == null) {
                continue;
            }

            if (srcProperty.getRange().isClass()) {
                if (srcProperty.getRange().getCardinality().isMany()) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> srcCollection = (Collection) value;
                    Collection<Object> dstCollection = value instanceof List ? new ArrayList<>() : new LinkedHashSet<>();

                    for (Object srcRef : srcCollection) {
                        Object reloadedRef = entitiesHolder.find(srcRef.getClass(), EntityValues.getId(srcRef));
                        if (reloadedRef == null) {
                            reloadedRef = entitiesHolder.create(srcRef.getClass(), EntityValues.getId(srcRef));
                            deepCopy(srcRef, reloadedRef, entitiesHolder);
                        }
                        dstCollection.add(reloadedRef);
                    }
                    EntityValues.setValue(destination, name, dstCollection);
                } else {
                    Object srcRef = value;
                    Object reloadedRef = entitiesHolder.find(srcRef.getClass(), EntityValues.getId(srcRef));
                    if (reloadedRef == null) {
                        reloadedRef = entitiesHolder.create(srcRef.getClass(), EntityValues.getId(srcRef));
                        deepCopy(srcRef, reloadedRef, entitiesHolder);
                    }
                    EntityValues.setValue(destination, name, reloadedRef);
                }
            } else {
                EntityValues.setValue(destination, name, value);
            }
        }

        if (destination instanceof CopyingSystemState && destination.getClass().isAssignableFrom(source.getClass())) {
            ((CopyingSystemState) destination).copyFrom(source);
        }


        // todo dynamic attributes
//        if (source instanceof BaseGenericIdEntity && destination instanceof BaseGenericIdEntity) {
//            ((BaseGenericIdEntity) destination).setDynamicAttributes(((BaseGenericIdEntity<?>) source).getDynamicAttributes());
//        }
    }

    protected void internalTraverseAttributes(Entity entity, EntityAttributeVisitor visitor, HashSet<Object> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (visitor.skip(property))
                continue;

            visitor.visit(entity, property);
            if (property.getRange().isClass()) {
                if (persistentAttributesLoadChecker.isLoaded(entity, property.getName())) {
                    Object value = EntityValues.getValue(entity, property.getName());
                    if (value != null) {
                        if (value instanceof Collection) {
                            for (Object item : ((Collection) value)) {
                                internalTraverseAttributes((Entity) item, visitor, visited);
                            }
                        } else {
                            internalTraverseAttributes((Entity) value, visitor, visited);
                        }
                    }
                }
            }
        }
    }

    protected void internalTraverseAttributesByFetchPlan(FetchPlan fetchPlan, Object entity, EntityAttributeVisitor visitor,
                                                         Map<Object, Set<FetchPlan>> visited, boolean checkLoaded) {
        Set<FetchPlan> fetchPlans = visited.get(entity);
        if (fetchPlans == null) {
            fetchPlans = new HashSet<>();
            visited.put(entity, fetchPlans);
        } else if (fetchPlans.contains(fetchPlan)) {
            return;
        }
        fetchPlans.add(fetchPlan);

        MetaClass metaClass = metadata.getClass(entity);

        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            MetaProperty metaProperty = metaClass.getProperty(property.getName());
            if (visitor.skip(metaProperty))
                continue;

            if (checkLoaded && !persistentAttributesLoadChecker.isLoaded(entity, metaProperty.getName()))
                continue;

            FetchPlan propertyFetchPlan = property.getFetchPlan();

            visitor.visit(entity, metaProperty);

            Object value = EntityValues.getValue(entity, property.getName());

            if (value != null && propertyFetchPlan != null) {
                if (value instanceof Collection) {
                    for (Object item : ((Collection) value)) {
                        if (item instanceof Entity)
                            internalTraverseAttributesByFetchPlan(propertyFetchPlan, item, visitor, visited, checkLoaded);
                    }
                } else if (value instanceof Entity) {
                    internalTraverseAttributesByFetchPlan(propertyFetchPlan, value, visitor, visited, checkLoaded);
                }
            }
        }
    }

    protected static <T> T createInstance(Class<T> aClass) {
        try {
            return aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create entity instance with constructor", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected static Object createInstanceWithId(Class<?> entityClass, Object id) {
        Object entity = createInstance(entityClass);
        EntityValues.setId(entity, id);
        return entity;
    }

    private boolean isNotLoadedAttributeException(Throwable e) {
        return e instanceof IllegalStateException
                || e.getClass().getName().equals("org.eclipse.persistence.exceptions.ValidationException") && e.getMessage() != null
                && e.getMessage().contains("An attempt was made to traverse a relationship using indirection that had a null Session");
    }

    /**
     * Returns all additional properties which are providing classes implement MetadataExtension interface
     *
     * @param metaClass instance MetaClass for getting additional properties
     * @return all additional properties
     */
    public Set<MetaProperty> getAdditionalProperties(MetaClass metaClass) {
        if (metadataExtensions == null) {
            return Collections.emptySet();
        }
        return metadataExtensions.stream()
                .flatMap(metadataExtension -> metadataExtension.getAdditionalProperties(metaClass).stream())
                .collect(Collectors.toSet());
    }

    /**
     * @param metaClass
     * @param propertyName
     */
    public boolean isAdditionalProperty(MetaClass metaClass, String propertyName) {
        if (metadataExtensions == null)
            return false;

        for (MetadataExtension extension : metadataExtensions) {
            if (extension.isAdditionalProperty(metaClass, propertyName))
                return true;
        }
        return false;
    }
}
