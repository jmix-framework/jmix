package io.jmix.texttodata.introspection.introspector;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.texttodata.TextToDataProperties;
import io.jmix.texttodata.introspection.model.EntityDescriptor;
import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import io.jmix.texttodata.introspection.model.RelationPropertyDescriptor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds and provides in-memory descriptors of JPA entities and their properties.
 */
public class JpaDomainModelIntrospector {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected TextToDataProperties textToDataProperties;
    @Autowired
    protected List<MetaPropertyIntrospector> propertyIntrospects;

    protected Map<String, EntityDescriptor> entitiesByName = Map.of();
    protected Map<String, Map<String, EntityPropertyDescriptor>> propertiesByEntityName = Map.of();

    @EventListener
    protected void onApplicationStarted(ApplicationStartedEvent event) {
        introspect();
    }

    /**
     * Rebuilds entity and property descriptor indexes from current metadata. It considers inclusion and exclusions
     * from the application properties.
     *
     * @see TextToDataProperties#getExcludeEntities()
     * @see TextToDataProperties#getExcludePackages()
     * @see TextToDataProperties#getIncludeEntities()
     * @see TextToDataProperties#getIncludePackages()
     */
    public void introspect() {
        entitiesByName.clear();

        Collection<MetaClass> classes = metadata.getClasses();
        Map<String, EntityDescriptor> entitiesMap = new HashMap<>(classes.size());
        Map<String, Map<String, EntityPropertyDescriptor>> propertiesMap = new HashMap<>();

        for (MetaClass metaClass : classes) {
            if (shouldInclude(metaClass)) {
                EntityDescriptor entityDescriptor = introspect(metaClass);
                entitiesMap.put(metaClass.getName(), entityDescriptor);
                propertiesMap.put(entityDescriptor.getName(), indexProperties(entityDescriptor));
            }
        }

        entitiesByName = Collections.unmodifiableMap(entitiesMap);
        propertiesByEntityName = Collections.unmodifiableMap(propertiesMap);
    }

    /**
     * Returns a descriptor for the given metaclass.
     *
     * @param metaClass entity metaclass
     * @return entity descriptor, or {@code null} if the entity is not indexed
     */
    @Nullable
    public EntityDescriptor getEntityDescriptor(MetaClass metaClass) {
        return getEntityDescriptor(metaClass.getName());
    }

    /**
     * Returns descriptor by entity name.
     *
     * @param entityName Jmix entity name
     * @return entity descriptor, or {@code null} if the entity is not indexed
     */
    @Nullable
    public EntityDescriptor getEntityDescriptor(String entityName) {
        return entitiesByName.get(entityName);
    }

    /**
     * Returns all indexed entity descriptors.
     *
     * @return collection of all indexed entity descriptors
     */
    public Collection<EntityDescriptor> getEntityDescriptors() {
        return entitiesByName.values();
    }

    /**
     * Checks whether the entity is indexed.
     *
     * @param entityName entity name
     * @return {@code true} if entity is indexed, {@code false} otherwise
     */
    public boolean containsEntity(String entityName) {
        return entitiesByName.containsKey(entityName);
    }

    /**
     * Checks whether the entity contains the given property.
     *
     * @param entityName   Jmix entity name
     * @param propertyName property name
     * @return {@code true} if entity contains given property, {@code false} otherwise
     */
    public boolean containsProperty(String entityName, String propertyName) {
        return getPropertyDescriptor(entityName, propertyName) != null;
    }

    /**
     * Returns property descriptor for an entity property.
     *
     * @param entityName   Jmix entity name
     * @param propertyName property name
     * @return property descriptor, or {@code null} if entity or property is not indexed
     */
    @Nullable
    public EntityPropertyDescriptor getPropertyDescriptor(String entityName, String propertyName) {
        Map<String, EntityPropertyDescriptor> properties = propertiesByEntityName.get(entityName);
        if (properties == null) {
            return null;
        }
        return properties.get(propertyName);
    }

    /**
     * Returns all property descriptors for the entity.
     *
     * @param entityName entity name
     * @return property descriptors, or empty collection if entity is not indexed
     */
    public Collection<EntityPropertyDescriptor> getPropertyDescriptors(String entityName) {
        Map<String, EntityPropertyDescriptor> properties = propertiesByEntityName.get(entityName);
        if (properties == null) {
            return List.of();
        }
        return properties.values();
    }

    /**
     * Returns relation descriptor for the given property if it is a relation.
     *
     * @param entityName   entity name
     * @param propertyName property name
     * @return relation descriptor, or {@code null} if property is missing or not a relation
     */
    @Nullable
    public RelationPropertyDescriptor getRelationPropertyDescriptor(String entityName, String propertyName) {
        EntityPropertyDescriptor propertyDescriptor = getPropertyDescriptor(entityName, propertyName);
        return propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor
                ? relationPropertyDescriptor
                : null;
    }

    /**
     * Checks whether a dot-separated property path can be resolved for the entity.
     *
     * @param entityName   entity name
     * @param propertyPath dot-separated property path
     */
    public boolean containsPropertyPath(String entityName, String propertyPath) {
        return resolvePropertyPath(entityName, propertyPath) != null;
    }

    /**
     * Resolves a dot-separated property path to property descriptors.
     *
     * @param entityName   root entity name
     * @param propertyPath dot-separated property path
     * @return resolved descriptors in traversal order, or {@code null} if a path is invalid
     */
    @Nullable
    public List<EntityPropertyDescriptor> resolvePropertyPath(String entityName, String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return null;
        }

        String currentEntityName = entityName;
        List<EntityPropertyDescriptor> resolvedPath = new ArrayList<>();

        for (String segment : propertyPath.split("\\.")) {
            EntityPropertyDescriptor propertyDescriptor = getPropertyDescriptor(currentEntityName, segment);
            if (propertyDescriptor == null) {
                return null;
            }

            resolvedPath.add(propertyDescriptor);

            if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                currentEntityName = relationPropertyDescriptor.getTargetEntityName();
            } else if (!segment.equals(lastSegment(propertyPath))) {
                return null;
            }
        }

        return Collections.unmodifiableList(resolvedPath);
    }

    /**
     * Creates a descriptor for a single entity.
     *
     * @param metaClass entity meta-class
     * @return entity descriptor with localized names and properties
     */
    public EntityDescriptor introspect(MetaClass metaClass) {
        String name = metaClass.getName();
        List<String> localizedNames = getEntityLocalizedNames(metaClass);

        List<EntityPropertyDescriptor> properties = introspectProperties(metaClass);

        String comment = metadataTools.getMetaAnnotationValue(metaClass, Comment.class);

        return new EntityDescriptor(name, localizedNames, properties, comment);
    }

    protected List<EntityPropertyDescriptor> introspectProperties(MetaClass metaClass) {
        List<EntityPropertyDescriptor> propertyDescriptors = new ArrayList<>();
        for (MetaProperty property : metaClass.getProperties()) {
            EntityPropertyDescriptor propertyDescriptor = introspectProperty(property);
            if (propertyDescriptor != null) {
                propertyDescriptors.add(propertyDescriptor);
            }
        }
        return propertyDescriptors;
    }

    @Nullable
    protected EntityPropertyDescriptor introspectProperty(MetaProperty property) {
        for (MetaPropertyIntrospector propertyIntrospector : propertyIntrospects) {
            if (propertyIntrospector.supports(property)) {
                return propertyIntrospector.introspect(property);
            }
        }
        return null;
    }

    protected List<String> getEntityLocalizedNames(MetaClass metaClass) {
        Collection<Locale> locales = messageTools.getAvailableLocalesMap().values();
        List<String> names = new ArrayList<>(locales.size());
        for (Locale locale : locales) {
            String localizedName = messageTools.getEntityCaption(metaClass, locale);
            if (!isEntityCaptionFallback(metaClass, localizedName)) {
                names.add(localizedName);
            }
        }
        return names;
    }

    protected boolean isEntityCaptionFallback(MetaClass metaClass, String localizedName) {
        return metaClass.getName().equals(localizedName)
                || metaClass.getJavaClass().getSimpleName().equals(localizedName);
    }

    protected boolean shouldInclude(MetaClass metaClass) {
        if (isExplicitlyExcluded(metaClass)) {
            return false;
        }

        if (isExplicitlyIncluded(metaClass)) {
            return true;
        }

        if (Boolean.TRUE.equals(textToDataProperties.getExcludeSystemLevelEntities())
                && metadataTools.isSystemLevel(metaClass)) {
            return false;
        }

        if (isDtoEntity(metaClass)) {
            return false;
        }

        if (matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(), textToDataProperties.getExcludePackages())) {
            return false;
        }

        if (!textToDataProperties.getIncludeEntities().isEmpty()
                || !textToDataProperties.getIncludePackages().isEmpty()) {
            return matchesEntityName(metaClass, textToDataProperties.getIncludeEntities())
                    || matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(), textToDataProperties.getIncludePackages());
        }

        return true;
    }

    protected boolean isExplicitlyIncluded(MetaClass metaClass) {
        return matchesEntityName(metaClass, textToDataProperties.getIncludeEntities())
                || matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(), textToDataProperties.getIncludePackages());
    }

    protected boolean isExplicitlyExcluded(MetaClass metaClass) {
        return matchesEntityName(metaClass, textToDataProperties.getExcludeEntities());
    }

    protected boolean matchesEntityName(MetaClass metaClass, List<String> entityNames) {
        return entityNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .anyMatch(metaClass.getName()::equals);
    }

    protected boolean matchesAnyPackagePrefix(String packageName, List<String> packagePrefixes) {
        return packagePrefixes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(prefix -> !prefix.isEmpty())
                .anyMatch(prefix -> packageName.equals(prefix) || packageName.startsWith(prefix + "."));
    }

    protected boolean isDtoEntity(MetaClass metaClass) {
        return !metadataTools.isJpaEntity(metaClass) && !metadataTools.isJpaEmbeddable(metaClass);
    }

    protected Map<String, EntityPropertyDescriptor> indexProperties(EntityDescriptor entityDescriptor) {
        return entityDescriptor.getProperties().stream()
                .collect(Collectors.toUnmodifiableMap(EntityPropertyDescriptor::getName, property -> property));
    }

    protected String lastSegment(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}
