package io.jmix.appsettings.impl;

import io.jmix.appsettings.AppSettingsTools;
import io.jmix.appsettings.defaults.*;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.metamodel.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.Nullable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("appset_AppSettingsTools")
public class AppSettingsToolsImpl implements AppSettingsTools {

    private static final Logger log = LoggerFactory.getLogger(AppSettingsToolsImpl.class);

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Override
    public <T extends AppSettingsEntity> T loadAppSettingsEntityFromDataStore(Class<T> clazz) {
        //only one record for T can exist at the same time in database with default identifier
        return dataManager.load(clazz)
                .id(Id.of(1, clazz))
                .optional().orElse(metadata.create(clazz, 1));
    }

    @Override
    public Object getPropertyValue(Class<? extends AppSettingsEntity> clazz, String propertyName) {
        return EntityValues.getValue(loadAppSettingsEntityFromDataStore(clazz), propertyName);
    }

    @Nullable
    @Override
    public Object getDefaultPropertyValue(Class<? extends AppSettingsEntity> clazz, String propertyName) {
        Field field = ReflectionUtils.findField(clazz, propertyName);
        if (field == null) {
            throw new IllegalArgumentException("Unable to find property " + propertyName + " for class " + clazz);
        }

        if (field.isAnnotationPresent(AppSettingsDefaultBoolean.class)) {
            return field.getAnnotation(AppSettingsDefaultBoolean.class).value();
        } else if (field.isAnnotationPresent(AppSettingsDefaultDouble.class)) {
            return field.getAnnotation(AppSettingsDefaultDouble.class).value();
        } else if (field.isAnnotationPresent(AppSettingsDefaultInt.class)) {
            return field.getAnnotation(AppSettingsDefaultInt.class).value();
        } else if (field.isAnnotationPresent(AppSettingsDefaultLong.class)) {
            return field.getAnnotation(AppSettingsDefaultLong.class).value();
        } else if (field.isAnnotationPresent(AppSettingsDefault.class)) {
            String annotationValue = field.getAnnotation(AppSettingsDefault.class).value();
            Range range = metadata.getClass(clazz).getProperty(propertyName).getRange();

            try {
                if (range.isEnum()) {
                    return range.asEnumeration().parse(annotationValue);
                } else if (range.isClass() && !range.getCardinality().isMany()) {
                    MetaClass metaClass = range.asClass();
                    MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
                    if (primaryKeyProperty == null) {
                        log.warn("Primary pk property for metaClass {} cannot be determined", metaClass);
                        throw new IllegalStateException("Primary pk property for metaClass " + metaClass + " cannot be determined");
                    }

                    Object pkValue = Objects.requireNonNull(primaryKeyProperty.getRange().asDatatype().parse(annotationValue));
                    return dataManager.load(metaClass.getJavaClass())
                            .id(pkValue)
                            .optional().orElse(null);
                } else if (range.isDatatype()) {
                    return datatypeRegistry.get(range.asDatatype().getId()).parse(annotationValue);
                } else {
                    return null;
                }
            } catch (ParseException e) {
                log.warn("Unable to get default value for property {} and class {} due to exception :\n{}", propertyName, clazz, e.getMessage());
            }
        }

        return null;
    }

    @Override
    public <T extends AppSettingsEntity> List<String> getPropertyNames(Class<T> clazz) {
        return metadata.getClass(clazz).getProperties().stream()
                .filter(metaProperty -> !metadataTools.isSystem(metaProperty))
                .map(MetadataObject::getName)
                .collect(Collectors.toList());
    }
}
