/*
 * Copyright 2021 Haulmont.
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

package io.jmix.audit.snapshot.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import io.jmix.audit.snapshot.EntitySnapshotManager;
import io.jmix.audit.snapshot.datastore.EntitySnapshotDataStore;
import io.jmix.audit.snapshot.model.EntitySnapshotModel;
import io.jmix.core.Entity;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dom4j.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

@Component("audit_EntitySnapshotManager")
public class EntitySnapshotManagerImpl implements EntitySnapshotManager {

    private final ExtendedEntities extendedEntities;
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final FetchPlans fetchPlans;
    private final MetadataTools metadataTools;
    private final Metadata metadata;
    private final ReferenceToEntitySupport referenceToEntitySupport;
    private final TimeSource timeSource;
    private final CurrentAuthentication currentAuthentication;
    private final EntitySerialization entitySerialization;
    private final FetchPlanSerialization fetchPlanSerialization;
    private final EntitySnapshotDataStore entitySnapshotDataStore;

    public EntitySnapshotManagerImpl(ExtendedEntities extendedEntities,
                                     UnconstrainedDataManager unconstrainedDataManager,
                                     FetchPlans fetchPlans,
                                     MetadataTools metadataTools,
                                     Metadata metadata,
                                     ReferenceToEntitySupport referenceToEntitySupport,
                                     TimeSource timeSource,
                                     CurrentAuthentication currentAuthentication,
                                     EntitySerialization entitySerialization,
                                     FetchPlanSerialization fetchPlanSerialization,
                                     EntitySnapshotDataStore entitySnapshotDataStore) {
        this.extendedEntities = extendedEntities;
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.fetchPlans = fetchPlans;
        this.metadataTools = metadataTools;
        this.metadata = metadata;
        this.referenceToEntitySupport = referenceToEntitySupport;
        this.timeSource = timeSource;
        this.currentAuthentication = currentAuthentication;
        this.entitySerialization = entitySerialization;
        this.fetchPlanSerialization = fetchPlanSerialization;
        this.entitySnapshotDataStore = entitySnapshotDataStore;
    }

    @Override
    public List<EntitySnapshotModel> getSnapshots(MetaClass metaClass, Object id) {
        metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        FetchPlan localFetchPlan = fetchPlans.builder(metaClass.getJavaClass())
                .addFetchPlan(FetchPlan.LOCAL)
                .build();
        Object entity = unconstrainedDataManager.load(new LoadContext<>(metaClass).setId(id).setFetchPlan(localFetchPlan));
        checkCompositePrimaryKey(metaClass, entity);
        return entitySnapshotDataStore.findEntitySnapshotByMetaClassAndEntity(entity, metaClass);
    }

    @Override
    public List<EntitySnapshotModel> getSnapshots(Object entity) {
        MetaClass entityMetaClass = metadata.getClass(entity);
        return getSnapshots(entityMetaClass, EntityValues.getId(entity));
    }

    @Override
    public void migrateSnapshots(MetaClass metaClass, Object id, Map<Class, Class> classMapping) {
        metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
        // load snapshots
        List<EntitySnapshotModel> snapshotList = getSnapshots(metaClass, id);
        Class javaClass = metaClass.getJavaClass();

        MetaClass mappedMetaClass = null;
        if (classMapping.containsKey(javaClass)) {
            Class mappedClass = classMapping.get(javaClass);
            mappedMetaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(mappedClass));
        }

        for (EntitySnapshotModel snapshot : snapshotList) {
            if (mappedMetaClass != null) {
                snapshot.setEntityMetaClass(mappedMetaClass.getName());
            }

            String snapshotXml = snapshot.getSnapshotXml();
            String fetchPlanXml = snapshot.getFetchPlanXml();

            snapshot.setSnapshotXml(processSnapshotXml(snapshotXml, classMapping));
            snapshot.setFetchPlanXml(processFetchPlanXml(fetchPlanXml, classMapping));
        }
        entitySnapshotDataStore.saveSnapshot(snapshotList);
    }

    @Override
    public EntitySnapshotModel createSnapshot(Object entity, FetchPlan fetchPlan) {
        return createSnapshot(entity, fetchPlan, timeSource.currentTimestamp());
    }

    @Override
    public EntitySnapshotModel createSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate) {
        String username = currentAuthentication.getUser().getUsername();
        return createSnapshot(entity, fetchPlan, snapshotDate, username);
    }

    @Override
    public EntitySnapshotModel createSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate, String authorUsername) {
        EntitySnapshotModel snapshot = createEntitySnapshot(entity, fetchPlan, snapshotDate, authorUsername);
        return entitySnapshotDataStore.save(snapshot);
    }

    @Override
    public Object extractEntity(EntitySnapshotModel snapshot) {
        String rawResult = snapshot.getSnapshotXml();
        Object entity;
        if (isXml(rawResult)) {
            entity = fromXML(snapshot.getSnapshotXml());
        } else {
            entity = entitySerialization.entityFromJson(rawResult, metadata.getClass(snapshot.getEntityMetaClass()));
        }
        return entity;
    }

    @Override
    public FetchPlan extractFetchPlan(EntitySnapshotModel snapshot) {
        String rawResult = snapshot.getFetchPlanXml();
        FetchPlan fetchPlan;
        if (isXml(rawResult)) {
            fetchPlan = (FetchPlan) fromXML(rawResult);
        } else {
            fetchPlan = fetchPlanSerialization.fromJson(rawResult);
        }
        return fetchPlan;
    }

    @Nullable
    @Override
    public EntitySnapshotModel getLastEntitySnapshot(Object entity) {
        MetaClass entityMetaClass = metadata.getClass(entity);
        checkCompositePrimaryKey(entityMetaClass, entity);
        return entitySnapshotDataStore.findLastSnapshot(entity, entityMetaClass);
    }

    @Nullable
    @Override
    public EntitySnapshotModel getLastEntitySnapshot(MetaClass metaClass, Object referenceId) {
        if (referenceId instanceof Entity) {
            throw new IllegalArgumentException(format("Reference id can not be an entity: %s", referenceId.getClass()));
        }
        return entitySnapshotDataStore.findLastSnapshotById(referenceId, metaClass);
    }

    @Override
    public EntitySnapshotModel createTempSnapshot(Object entity, FetchPlan fetchPlan) {
        return createTempSnapshot(entity, fetchPlan, timeSource.currentTimestamp());
    }

    @Override
    public EntitySnapshotModel createTempSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate) {
        String username = currentAuthentication.getUser().getUsername();
        return createTempSnapshot(entity, fetchPlan, snapshotDate, username);
    }

    @Override
    public EntitySnapshotModel createTempSnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate, String authorUsername) {
        return createEntitySnapshot(entity, fetchPlan, snapshotDate, authorUsername);
    }

    private void checkCompositePrimaryKey(MetaClass metaClass, Object entity) {
        if (metadataTools.hasCompositePrimaryKey(metaClass) && !EntityValues.isUuidSupported(entity)) {
            throw new UnsupportedOperationException(format("Entity %s has no persistent UUID attribute", entity));
        }
    }

    private String processSnapshotXml(String snapshotXml, Map<Class, Class> classMapping) {
        if (!isXml(snapshotXml)) {
            return snapshotXml;
        }
        Document document;
        try {
            document = DocumentHelper.parseText(snapshotXml);
        } catch (DocumentException e) {
            throw new RuntimeException("Couldn't parse snapshot xml content", e);
        }
        replaceClasses(document.getRootElement(), classMapping);
        replaceInXmlTree(document.getRootElement(), classMapping);
        return document.asXML();
    }

    private void replaceClasses(Element element, Map<Class, Class> classMapping) {
        // translate XML
        for (Map.Entry<Class, Class> classEntry : classMapping.entrySet()) {
            Class beforeClass = classEntry.getKey();
            Class afterClass = classEntry.getValue();

            checkNotNull(beforeClass);
            checkNotNull(afterClass);

            // If BeforeClass != AfterClass
            if (!beforeClass.equals(afterClass)) {
                String beforeClassName = beforeClass.getCanonicalName();
                String afterClassName = afterClass.getCanonicalName();

                if (beforeClassName.equals(element.getName())) {
                    element.setName(afterClassName);
                }

                Attribute classAttribute = element.attribute("class");
                if ((classAttribute != null) && beforeClassName.equals(classAttribute.getValue())) {
                    classAttribute.setValue(afterClassName);
                }
            }
        }
    }

    private void replaceInXmlTree(Element element, Map<Class, Class> classMapping) {
        for (int i = 0; i < element.nodeCount(); i++) {
            Node node = element.node(i);
            if (node instanceof Element) {
                Element childElement = (Element) node;
                replaceClasses(childElement, classMapping);
                replaceInXmlTree(childElement, classMapping);
            }
        }
    }

    private String processFetchPlanXml(String fetchPlanXml, Map<Class, Class> classMapping) {
        if (!isXml(fetchPlanXml)) {
            return fetchPlanXml;
        }
        for (Map.Entry<Class, Class> classEntry : classMapping.entrySet()) {
            Class beforeClass = classEntry.getKey();
            Class afterClass = classEntry.getValue();

            checkNotNull(beforeClass);
            checkNotNull(afterClass);

            String beforeClassName = beforeClass.getCanonicalName();
            String afterClassName = afterClass.getCanonicalName();

            fetchPlanXml = fetchPlanXml.replaceAll(beforeClassName, afterClassName);
        }
        return fetchPlanXml;
    }

    private boolean isXml(String value) {
        return value != null && value.trim().startsWith("<");
    }

    private Object fromXML(String xml) {
        final List exclUpdateFields = Arrays.asList("updateDate", "updatedBy");
        final List exclCreateFields = Arrays.asList("createTs", "createdBy");
        XStream xStream = new XStream() {

            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        boolean result = super.shouldSerializeMember(definedIn, fieldName);
                        if (!result) {
                            return false;
                        }
                        if (fieldName != null) {
                            if (exclUpdateFields.contains(fieldName)
                                    && isUpdatable(definedIn)) {
                                return false;
                            }
                            if (exclCreateFields.contains(fieldName)
                                    && isCreatable(definedIn)) {
                                return false;
                            }
                            if ("uuid".equals(fieldName)) {
                                if (EntityValues.isUuidSupported(definedIn)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                };
            }
        };
        XStream.setupDefaultSecurity(xStream);
        xStream.allowTypeHierarchy(Serializable.class);

        return xStream.fromXML(xml);
    }

    private boolean isUpdatable(Class<?> entityClass) {
        return Arrays.stream(FieldUtils.getAllFields(entityClass))
                .anyMatch(f -> f.isAnnotationPresent(LastModifiedBy.class) || f.isAnnotationPresent(LastModifiedDate.class));
    }

    private boolean isCreatable(Class<?> entityClass) {
        return Arrays.stream(FieldUtils.getAllFields(entityClass))
                .anyMatch(f -> f.isAnnotationPresent(CreatedBy.class) || f.isAnnotationPresent(CreatedDate.class));
    }

    private EntitySnapshotModel createEntitySnapshot(Object entity, FetchPlan fetchPlan, Date snapshotDate, String authorUsername) {
        Preconditions.checkNotNullArgument(entity);
        Preconditions.checkNotNullArgument(fetchPlan);
        Preconditions.checkNotNullArgument(snapshotDate);
        MetaClass entityMetaClass = metadata.getClass(entity);
        checkCompositePrimaryKey(entityMetaClass, entity);

        Class fetchPlanEntityClass = fetchPlan.getEntityClass();
        Class entityClass = entity.getClass();

        if (!fetchPlanEntityClass.isAssignableFrom(entityClass)) {
            throw new IllegalStateException("FetchPlan could not be used with this propertyValue");
        }

        MetaClass metaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(entity));

        EntitySnapshotModel snapshot = metadata.create(EntitySnapshotModel.class);
        snapshot.setObjectEntityId(referenceToEntitySupport.getReferenceId(entity));
        snapshot.setEntityMetaClass(metaClass.getName());
        snapshot.setFetchPlanXml(fetchPlanSerialization.toJson(fetchPlan, FetchPlanSerializationOption.COMPACT_FORMAT));
        snapshot.setSnapshotXml(entitySerialization.toJson(entity));
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setAuthorUsername(authorUsername);

        return snapshot;
    }
}
