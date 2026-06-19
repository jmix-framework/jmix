/*
 * Copyright 2026 Haulmont.
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

package io.jmix.appsettings.impl;

import io.jmix.appsettings.AppSettingsEntityLoadMode;
import io.jmix.appsettings.AppSettingsProperties;
import io.jmix.appsettings.AppSettingsTenantProvider;
import io.jmix.appsettings.AppSettingsTools;
import io.jmix.appsettings.defaults.*;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.data.PersistenceHints;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import io.jmix.data.StoreAwareLocator;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@NullMarked
@Component("appset_AppSettingsTools")
public class AppSettingsToolsImpl implements AppSettingsTools {

    private static final Logger log = LoggerFactory.getLogger(AppSettingsToolsImpl.class);

    protected static final int LEGACY_GLOBAL_ENTITY_ID = 1;
    protected static final int TENANT_ENTITY_ID_SEQUENCE_START = 1001;

    protected static final String TENANT_SETTINGS_SEQUENCE_NAME_PREFIX = "appsettings_";
    protected static final String TENANT_ID_PROPERTY = "tenantId";

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected AppSettingsProperties appSettingsProperties;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected Sequences sequences;

    @Autowired
    protected AppSettingsTenantSupport tenantSupport;

    @Override
    public <T extends AppSettingsEntity> T loadAppSettingsEntityFromDataStore(Class<T> clazz,
                                                                              AppSettingsEntityLoadMode mode,
                                                                              boolean softDeletion) {
        boolean effectiveSoftDeletion = mode != AppSettingsEntityLoadMode.FOR_SAVE && softDeletion;
        String currentTenantId = tenantSupport.getCurrentTenantId();
        if (currentTenantId != null) {
            T tenantEntity = loadTenantSpecificAppSettingsEntity(clazz, currentTenantId, effectiveSoftDeletion);
            if (tenantEntity != null) {
                return mode == AppSettingsEntityLoadMode.FOR_SAVE
                        ? restoreSoftDeletedEntity(tenantEntity)
                        : tenantEntity;
            }
            if (mode == AppSettingsEntityLoadMode.FOR_SAVE) {
                return createTenantAppSettingsEntity(clazz, currentTenantId);
            }
        }

        T globalEntity = loadGlobalAppSettingsEntity(clazz, effectiveSoftDeletion);
        if (globalEntity != null) {
            return mode == AppSettingsEntityLoadMode.FOR_SAVE
                    ? restoreSoftDeletedEntity(globalEntity)
                    : globalEntity;
        }
        return createGlobalAppSettingsEntity(clazz);
    }

    @Nullable
    @Override
    public Object getPropertyValue(Class<? extends AppSettingsEntity> clazz, String propertyName) {
        return EntityValues.getValue(loadAppSettingsEntityFromDataStore(clazz, AppSettingsEntityLoadMode.FOR_READ),
                propertyName);
    }

    @Nullable
    @Override
    public Object getDefaultPropertyValue(Class<? extends AppSettingsEntity> clazz, String propertyName) {
        Field field = ReflectionHelper.findField(clazz, propertyName);
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

                    Object pkValue = primaryKeyProperty.getRange().asDatatype().parse(annotationValue);
                    if (pkValue == null) {
                        throw new RuntimeException("Primary key property value cannot be null");
                    }
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

    protected UnconstrainedDataManager getDataManagerForAppSettingsEntity() {
        return appSettingsProperties.isCheckPermissionsForAppSettingsEntity() ? dataManager : unconstrainedDataManager;
    }

    protected FetchPlan createFetchPlan(Class<?> clazz) {
        FetchPlanBuilder builder = fetchPlans.builder(clazz).addFetchPlan(FetchPlan.LOCAL);
        for (MetaProperty property : metadata.getClass(clazz).getProperties()) {
            if (property.getRange().isClass()) {
                builder.add(property.getName(), FetchPlan.BASE);
            } else if (metadataTools.isElementCollection(property)) {
                builder.add(property.getName());
            }
        }
        return builder.build();
    }

    @Override
    public <T extends AppSettingsEntity> List<String> getPropertyNames(Class<T> clazz) {
        return metadata.getClass(clazz).getProperties().stream()
                .filter(metaProperty -> !metadataTools.isSystem(metaProperty))
                .map(MetadataObject::getName)
                .filter(name -> !TENANT_ID_PROPERTY.equals(name))
                .collect(Collectors.toList());
    }

    @Nullable
    protected <T extends AppSettingsEntity> T loadTenantSpecificAppSettingsEntity(Class<T> clazz,
                                                                                  String tenantId,
                                                                                  boolean softDeletion) {
        List<T> entities = getDataManagerForAppSettingsEntity().load(clazz)
                .condition(PropertyCondition.equal(TENANT_ID_PROPERTY, tenantId))
                .hint(PersistenceHints.SOFT_DELETION, softDeletion)
                .fetchPlan(createFetchPlan(clazz))
                .list();

        if (entities.size() > 1) {
            throw new IllegalStateException(
                    "More than one tenant settings record exists for entity %s and tenant '%s'"
                            .formatted(metadata.getClass(clazz).getName(), tenantId));
        }
        return entities.isEmpty() ? null : entities.get(0);
    }

    @Nullable
    protected <T extends AppSettingsEntity> T loadGlobalAppSettingsEntity(Class<T> clazz, boolean softDeletion) {
        MetaClass metaClass = metadata.getClass(clazz);
        String storeName = metaClass.getStore().getName();

        TransactionTemplate transactionTemplate = storeAwareLocator.getTransactionTemplate(storeName);
        return transactionTemplate.execute(status -> {
            EntityManager entityManager = storeAwareLocator.getEntityManager(storeName);
            boolean softDeletionBackup = PersistenceHints.isSoftDeletion(entityManager);
            Object tenantIdBackup = entityManager.getProperties().get("tenantId");
            try {
                entityManager.setProperty(PersistenceHints.SOFT_DELETION, softDeletion);
                entityManager.setProperty("tenantId", AppSettingsTenantProvider.NO_TENANT);
                return getDataManagerForAppSettingsEntity().load(clazz)
                        .id(LEGACY_GLOBAL_ENTITY_ID)
                        .fetchPlan(createFetchPlan(clazz))
                        .hint(PersistenceHints.SOFT_DELETION, softDeletion)
                        .optional()
                        .orElse(null);
            } finally {
                entityManager.setProperty(PersistenceHints.SOFT_DELETION, softDeletionBackup);
                entityManager.setProperty("tenantId", tenantIdBackup);
            }
        });
    }

    protected <T extends AppSettingsEntity> T createGlobalAppSettingsEntity(Class<T> clazz) {
        return metadata.create(clazz, LEGACY_GLOBAL_ENTITY_ID);
    }

    protected <T extends AppSettingsEntity> T createTenantAppSettingsEntity(Class<T> clazz, String tenantId) {
        T entity = metadata.create(clazz, Math.toIntExact(generateTenantSpecificSettingsId(clazz)));
        entity.setTenantId(tenantId);
        return entity;
    }

    /**
     * Generates a synthetic identifier for a tenant-specific App Settings record.
     * The legacy global record keeps the reserved identifier {@code 1}.
     */
    protected <T extends AppSettingsEntity> long generateTenantSpecificSettingsId(Class<T> clazz) {
        MetaClass metaClass = metadata.getClass(clazz);
        Sequence sequence = Sequence.withName(getTenantSettingsSequenceName(metaClass))
                .setStore(metaClass.getStore().getName())
                .setStartValue(TENANT_ENTITY_ID_SEQUENCE_START);
        return sequences.createNextValue(sequence);
    }

    protected <T extends AppSettingsEntity> T restoreSoftDeletedEntity(T entity) {
        if (EntityValues.isSoftDeleted(entity)) {
            EntityValues.setDeletedDate(entity, null);
            EntityValues.setDeletedBy(entity, null);
        }
        return entity;
    }

    protected String getTenantSettingsSequenceName(MetaClass metaClass) {
        String sanitizedName = metaClass.getName().chars()
                .mapToObj(ch -> Character.isLetterOrDigit(ch) ? String.valueOf((char) ch) : "_")
                .collect(Collectors.joining());
        return TENANT_SETTINGS_SEQUENCE_NAME_PREFIX + sanitizedName;
    }

}
