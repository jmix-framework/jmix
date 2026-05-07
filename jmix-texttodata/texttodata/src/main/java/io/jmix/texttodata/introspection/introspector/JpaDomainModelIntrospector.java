package io.jmix.texttodata.introspection.introspector;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.texttodata.introspection.model.EntityDescriptor;
import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class JpaDomainModelIntrospector {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected List<MetaPropertyIntrospector> propertyIntrospectors;

    protected final Map<String, EntityDescriptor> entityDescriptors = new HashMap<>();

    @PostConstruct
    public void init() {
        introspect();
    }

    public void introspect() {
        entityDescriptors.clear();

        Collection<MetaClass> classes = metadata.getClasses();
        for (MetaClass metaClass : classes) {
            entityDescriptors.put(metaClass.getName(), introspect(metaClass));
        }
    }

    public EntityDescriptor getEntityDescriptor(MetaClass metaClass) {
        return entityDescriptors.get(metaClass.getName());
    }

    public EntityDescriptor getEntityDescriptor(String entityName) {
        return entityDescriptors.get(entityName);
    }

    public Collection<EntityDescriptor> getEntityDescriptors() {
        return entityDescriptors.values();
    }

    public EntityDescriptor introspect(MetaClass metaClass) {
        String name = metaClass.getName();
        List<String> localizedNames = getEntityLocalizedNames(metaClass);

        List<EntityPropertyDescriptor> properties = introspectProperties(metaClass);

        return new EntityDescriptor(name, localizedNames, properties);
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
        for (MetaPropertyIntrospector propertyIntrospector : propertyIntrospectors) {
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
}
