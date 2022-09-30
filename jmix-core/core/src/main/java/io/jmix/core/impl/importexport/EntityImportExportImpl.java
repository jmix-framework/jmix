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

package io.jmix.core.impl.importexport;

import io.jmix.core.*;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.impl.serialization.EntityTokenException;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.validation.EntityValidationException;
import io.jmix.core.validation.group.RestApiChecks;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import static io.jmix.core.entity.EntitySystemAccess.getSecurityState;
import static java.lang.String.format;

@Component("core_EntityImportExport")
public class EntityImportExportImpl implements EntityImportExport {

    @Autowired
    protected EntitySerialization entitySerialization;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected Validator validator;

    @Autowired
    protected Stores stores;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected AccessConstraintsRegistry accessConstraintsRegistry;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected EntityAttributeImportExtensionResolver extensionResolver;

    @Override
    public byte[] exportEntitiesToZIP(Collection<Object> entities, FetchPlan fetchPlan) {
        return exportEntitiesToZIP(reloadEntities(entities, fetchPlan));
    }

    @Override
    public byte[] exportEntitiesToZIP(Collection<Object> entities) {
        String json = entitySerialization.toJson(entities, null, EntitySerializationOption.COMPACT_REPEATED_ENTITIES);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(StandardCharsets.UTF_8.name());
        ArchiveEntry singleDesignEntry = newStoredEntry("entities.json", jsonBytes);
        try {
            zipOutputStream.putArchiveEntry(singleDesignEntry);
            zipOutputStream.write(jsonBytes);
            zipOutputStream.closeArchiveEntry();
        } catch (Exception e) {
            throw new RuntimeException("Error on creating zip archive during entities export", e);
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public String exportEntitiesToJSON(Collection<Object> entities, FetchPlan fetchPlan) {
        return exportEntitiesToJSON(reloadEntities(entities, fetchPlan));
    }

    @Override
    public String exportEntitiesToJSON(Collection<Object> entities) {
        return entitySerialization.toJson(entities, null,
                EntitySerializationOption.COMPACT_REPEATED_ENTITIES, EntitySerializationOption.PRETTY_PRINT);
    }

    protected Collection reloadEntities(Collection<Object> entities, FetchPlan fetchPlan) {
        List ids = new ArrayList(entities.size());
        for (Object entity : entities) {
            ids.add(EntityValues.getId(entity));
        }

        MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
        LoadContext.Query query = new LoadContext.Query("select e from " + metaClass.getName() + " e where e.id in :ids")
                .setParameter("ids", ids);
        LoadContext<?> ctx = new LoadContext(metadata.getClass(fetchPlan.getEntityClass()))
                .setQuery(query)
                .setFetchPlan(fetchPlan);

        return dataManager.loadList(ctx);
    }

    protected ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    @Override
    public Collection importEntitiesFromJson(String json, EntityImportPlan importPlan) {
        Collection<?> result = new ArrayList<>();
        Collection<?> entities = entitySerialization.entitiesCollectionFromJson(json,
                null,
                EntitySerializationOption.COMPACT_REPEATED_ENTITIES);
        result.addAll(importEntities(entities, importPlan));
        return result;
    }

    @Override
    public Collection<Object> importEntitiesFromZIP(byte[] zipBytes, EntityImportPlan importPlan) {
        Collection<Object> result = new ArrayList<>();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        try {
            try {
                while (archiveReader.getNextZipEntry() != null) {
                    String json = new String(readBytesFromEntry(archiveReader), StandardCharsets.UTF_8);
                    Collection<?> entities = entitySerialization.entitiesCollectionFromJson(json,
                            null,
                            EntitySerializationOption.COMPACT_REPEATED_ENTITIES);
                    result.addAll(importEntities(entities, importPlan));
                }
            } catch (IOException e) {
                throw new RuntimeException("Exception occurred while importing report", e);
            }
        } finally {
            IOUtils.closeQuietly(archiveReader);
        }
        return result;
    }

    protected byte[] readBytesFromEntry(ZipArchiveInputStream archiveReader) throws IOException {
        return IOUtils.toByteArray(archiveReader);
    }

    @Override
    public Collection importEntities(Collection entities, EntityImportPlan importPlan) {
        return importEntities(entities, importPlan, false, false);
    }

    @Override
    public Collection importEntities(Collection entities, EntityImportPlan importPlan, boolean validate) {
        return importEntities(entities, importPlan, validate, false);
    }

    @Override
    public Collection importEntities(Collection entities, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking) {
        return importEntities(entities, importPlan, validate, optimisticLocking, false);
    }

    @Override
    public Collection<Object> importEntities(Collection<Object> entities, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking, boolean additionComposition) {
        List<ReferenceInfo> referenceInfoList = new ArrayList<>();
        SaveContext saveContext = new SaveContext();
        saveContext.setHint("jmix.softDeletion", false);

        //import is performed in two steps. We have to do so, because imported entity may have a reference to
        //the reference that is imported in the same batch.
        //
        //1. entities that should be persisted are processed first, fields that should be references to existing entities
        //are stored in the referenceInfoList variable
        for (Object srcEntity : entities) {
            EntityPreconditions.checkEntityType(srcEntity);
            FetchPlan fetchPlan = constructFetchPlanFromImportPlan(importPlan).build();
            //set softDeletion to false because we can import deleted entity, so we'll restore it and update

            Object dstEntity = null;
            Object entityId = EntityValues.getId(srcEntity);
            if (entityId != null) {
                LoadContext<?> ctx = new LoadContext<>(metadata.getClass(srcEntity))
                        .setFetchPlan(fetchPlan)
                        .setHint("jmix.dynattr", true)
                        .setHint("jmix.softDeletion", false)
                        .setId(entityId)
                        .setAccessConstraints(accessConstraintsRegistry.getConstraints());
                dstEntity = dataManager.load(ctx);
            }
            importEntity(srcEntity, dstEntity, importPlan, fetchPlan, saveContext, referenceInfoList, optimisticLocking, additionComposition);
        }

        //2. references to existing entities are processed

        //store a list of loaded entities in the collection to prevent unnecessary database requests for searching the
        //same instance
        Set<Object> loadedEntities = new HashSet<>();
        for (ReferenceInfo referenceInfo : referenceInfoList) {
            processReferenceInfo(referenceInfo, saveContext, loadedEntities);
        }

        for (Object instance : saveContext.getEntitiesToSave()) {
            if (!entityStates.isNew(instance)) {
                if (EntityValues.isSoftDeleted(instance)) {
                    EntityValues.setDeletedDate(instance, null);
                }
            }
        }

        if (validate) {
            validateEntities(new LinkedHashSet<>(saveContext.getEntitiesToSave()));
        }

        //we shouldn't remove entities with the softDeletion = false
        if (!saveContext.getEntitiesToRemove().isEmpty()) {
            saveContext.setHint("jmix.softDeletion", true);
        }

        saveContext.setAccessConstraints(accessConstraintsRegistry.getConstraints());

        return dataManager.save(saveContext);
    }

    @Override
    public void importEntityIntoSaveContext(SaveContext saveContext, Object srcEntity, EntityImportPlan importPlan, boolean validate) {
        importEntityIntoSaveContext(saveContext, srcEntity, importPlan, validate, false);
    }

    @Override
    public void importEntityIntoSaveContext(SaveContext saveContext, Object srcEntity, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking) {
        importEntityIntoSaveContext(saveContext, srcEntity, importPlan, validate, optimisticLocking, false);
    }

    @Override
    public void importEntityIntoSaveContext(SaveContext saveContext, Object srcEntity, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking, boolean additionComposition) {
        List<ReferenceInfo> referenceInfoList = new ArrayList<>();
        if (saveContext == null) {
            return;
        }
        saveContext.setHint("jmix.softDeletion", false);

        EntityPreconditions.checkEntityType(srcEntity);
        FetchPlan fetchPlan = constructFetchPlanFromImportPlan(importPlan).build();

        LoadContext<?> ctx = new LoadContext<>(metadata.getClass(srcEntity))
                .setFetchPlan(fetchPlan)
                .setHint("jmix.dynattr", true)
                .setHint("jmix.softDeletion", false)
                .setId(EntityValues.getId(srcEntity))
                .setAccessConstraints(accessConstraintsRegistry.getConstraints());
        Object dstEntity = dataManager.load(ctx);

        importEntity(srcEntity, dstEntity, importPlan, fetchPlan, saveContext, referenceInfoList, optimisticLocking, additionComposition);

        Set<Object> loadedEntities = new HashSet<>();
        for (ReferenceInfo referenceInfo : referenceInfoList) {
            processReferenceInfo(referenceInfo, saveContext, loadedEntities);
        }

        for (Object instance : saveContext.getEntitiesToSave()) {
            if (!entityStates.isNew(instance)) {
                if (EntityValues.isSoftDeleted(instance)) {
                    EntityValues.setDeletedDate(instance, null);
                }
            }
        }

        if (validate) {
            validateEntities(new LinkedHashSet<>(saveContext.getEntitiesToSave()));
        }

        if (!saveContext.getEntitiesToRemove().isEmpty()) {
            saveContext.setHint("jmix.softDeletion", true);
        }

        saveContext.setAccessConstraints(accessConstraintsRegistry.getConstraints());
    }

    @SuppressWarnings("unchecked")
    protected void validateEntities(Collection<Object> entitiesToValidate) {
        Collection<Pair<Object, Object>> referencesToExclude = new ArrayList<>();
        for (Object entity : entitiesToValidate) {
            for (MetaProperty metaProperty : metadata.getClass(entity).getProperties()) {
                //we need to exclude entities marked as @Valid starting checking the references of the root entity
                if (metaProperty.getRange().isClass()
                        && metadataTools.isAnnotationPresent(entity, metaProperty.getName(), Valid.class)) {
                    Object validated = EntityValues.getValue(entity, metaProperty.getName());
                    if (validated != null && !(validated instanceof Collection)) {
                        validated = Collections.singletonList(validated);
                    }
                    //to handle one-to-many composition
                    if (validated != null) {
                        ((Collection<Object>) validated).stream()
                                .filter(x -> !referencesToExclude.contains(new Pair<>(x, entity)))
                                .forEach(x -> referencesToExclude.add(new Pair<>(entity, x)));
                    }
                }
            }
        }
        entitiesToValidate.removeAll(referencesToExclude.stream().map(Pair::getSecond).collect(Collectors.toList()));

        Set<ConstraintViolation<Object>> violations = new LinkedHashSet<>();
        entitiesToValidate.forEach(entity ->
                violations.addAll(validator.validate(entity, Default.class, RestApiChecks.class)));
        if (!violations.isEmpty()) {
            throw new EntityValidationException("Entity validation failed", violations);
        }
    }

    /**
     * Method imports the entity.
     *
     * @param srcEntity         entity that came to the {@code EntityImportExport} bean
     * @param dstEntity         reloaded srcEntity or null if entity doesn't exist in the database
     * @param importPlan        importPlan used for importing the entity
     * @param fetchPlan         fetchPlan that was used for loading dstEntity
     * @param saveContext       entities that must be commited or deleted will be set to the saveContext
     * @param referenceInfoList list of referenceInfos for further processing
     * @param optimisticLocking whether the passed entity version should be validated before entity is persisted
     * @return dstEntity that has fields values from the srcEntity
     */
    protected Object importEntity(Object srcEntity,
                                  @Nullable Object dstEntity,
                                  EntityImportPlan importPlan,
                                  FetchPlan fetchPlan,
                                  SaveContext saveContext,
                                  Collection<ReferenceInfo> referenceInfoList,
                                  boolean optimisticLocking,
                                  boolean additionComposition) {
        MetaClass metaClass = metadata.getClass(srcEntity);
        boolean createOp = false;
        if (dstEntity == null) {
            dstEntity = metadata.create(metaClass);
            EntityValues.setId(dstEntity, EntityValues.getId(srcEntity));
            createOp = true;
        }

        //we must specify a fetchPlan here because otherwise we may get UnfetchedAttributeException during merge
        saveContext.saving(dstEntity, fetchPlan);

        if (!createOp) {
            assertToken(srcEntity, fetchPlan);
        }

        for (EntityImportPlanProperty importPlanProperty : importPlan.getProperties()) {
            String propertyName = importPlanProperty.getName();
            MetaProperty metaProperty = metaClass.getProperty(propertyName);

            EntityAttributeImportExtension extension = extensionResolver.findExtension(metaProperty);
            if (extension != null) {
                extension.importEntityAttribute(metaProperty, srcEntity, dstEntity);
                continue;
            }

            if (metaProperty.getRange().isDatatype()) {
                if (!"version".equals(metaProperty.getName())) {
                    EntityValues.setValue(dstEntity, propertyName, EntityValues.getValue(srcEntity, propertyName));
                } else if (optimisticLocking) {
                    EntityValues.setValue(dstEntity, propertyName, EntityValues.getValue(srcEntity, propertyName));
                }
            } else if (metaProperty.getRange().isEnum()) {
                EntityValues.setValue(dstEntity, propertyName, EntityValues.getValue(srcEntity, propertyName));
            } else if (metaProperty.getRange().isClass()) {
                FetchPlan propertyFetchPlan = fetchPlan.getProperty(propertyName) != null ? fetchPlan.getProperty(propertyName).getFetchPlan() : null;
                if (metadataTools.isEmbedded(metaProperty)) {
                    if (importPlanProperty.getPlan() != null) {
                        Object embeddedEntity = importEmbeddedAttribute(srcEntity, dstEntity, createOp, importPlanProperty, propertyFetchPlan,
                                saveContext, referenceInfoList, optimisticLocking);
                        EntityValues.setValue(dstEntity, propertyName, embeddedEntity);
                    }
                } else {
                    switch (metaProperty.getRange().getCardinality()) {
                        case MANY_TO_MANY:
                            importManyToManyCollectionAttribute(srcEntity, dstEntity,
                                    importPlanProperty, propertyFetchPlan, saveContext, referenceInfoList, optimisticLocking);
                            break;
                        case ONE_TO_MANY:
                            importOneToManyCollectionAttribute(srcEntity, dstEntity,
                                    importPlanProperty, propertyFetchPlan, saveContext, referenceInfoList,
                                    optimisticLocking, additionComposition);
                            break;
                        default:
                            importReference(srcEntity, dstEntity, importPlanProperty, propertyFetchPlan, saveContext, referenceInfoList, optimisticLocking);
                    }
                }
            }
        }
        Set<MetaProperty> additionalMetaProperties = metadataTools.getAdditionalProperties(metaClass);
        if (!additionalMetaProperties.isEmpty()) {
            for (MetaProperty dynAttr : additionalMetaProperties) {
                EntityValues.setValue(dstEntity, dynAttr.getName(), EntityValues.getValue(srcEntity, dynAttr.getName()));
            }
        }
        return dstEntity;
    }

    protected void importReference(Object srcEntity,
                                   Object dstEntity,
                                   EntityImportPlanProperty importPlanProperty,
                                   @Nullable FetchPlan fetchPlan,
                                   SaveContext saveContext,
                                   Collection<ReferenceInfo> referenceInfoList,
                                   boolean optimisticLocking) {
        Object srcPropertyValue = EntityValues.getValue(srcEntity, importPlanProperty.getName());
        Object dstPropertyValue = EntityValues.getValue(dstEntity, importPlanProperty.getName());
        if (importPlanProperty.getPlan() == null) {
            ReferenceInfo referenceInfo = new ReferenceInfo(dstEntity, null, importPlanProperty, srcPropertyValue, dstPropertyValue);
            referenceInfoList.add(referenceInfo);
        } else {
            dstPropertyValue = srcPropertyValue != null ?
                    importEntity(srcPropertyValue, dstPropertyValue, importPlanProperty.getPlan(), fetchPlan, saveContext, referenceInfoList,
                            optimisticLocking, false) :
                    null;
            EntityValues.setValue(dstEntity, importPlanProperty.getName(), dstPropertyValue);
        }
    }

    protected void importOneToManyCollectionAttribute(Object srcEntity,
                                                      Object dstEntity,
                                                      EntityImportPlanProperty importPlanProperty,
                                                      @Nullable FetchPlan fetchPlan,
                                                      SaveContext saveContext,
                                                      Collection<ReferenceInfo> referenceInfoList,
                                                      boolean optimisticLocking,
                                                      boolean additionalComposition) {
        Collection<Object> collectionValue = EntityValues.getValue(srcEntity, importPlanProperty.getName());
        Collection<Object> prevCollectionValue = EntityValues.getValue(dstEntity, importPlanProperty.getName());

        MetaProperty metaProperty = metadata.getClass(srcEntity).getProperty(importPlanProperty.getName());
        MetaProperty inverseMetaProperty = metaProperty.getInverse();

        Collection<Object> dstFilteredIds = getFilteredIds(getSecurityState(dstEntity), metaProperty.getName());
        Collection<Object> srcFilteredIds = getFilteredIds(getSecurityState(srcEntity), metaProperty.getName());

        Collection<Object> newCollectionValue = createNewCollection(metaProperty);
        CollectionCompare.with()
                .onCreate(e -> {
                    if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(e))) {
                        Object result = importEntity(e, null, importPlanProperty.getPlan(), fetchPlan,
                                saveContext, referenceInfoList, optimisticLocking, additionalComposition);
                        if (inverseMetaProperty != null) {
                            EntityValues.setValue(result, inverseMetaProperty.getName(), dstEntity);
                        }
                        newCollectionValue.add(result);
                    }
                })
                .onUpdate((src, dst) -> {
                    if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(src))) {
                        Object result = importEntity(src, dst, importPlanProperty.getPlan(), fetchPlan,
                                saveContext, referenceInfoList, optimisticLocking, additionalComposition);
                        if (inverseMetaProperty != null) {
                            EntityValues.setValue(result, inverseMetaProperty.getName(), dstEntity);
                        }
                        newCollectionValue.add(result);
                    }
                })
                .onDelete(e -> {
                    if (!additionalComposition) {
                        Object refId = referenceToEntitySupport.getReferenceId(e);
                        if (importPlanProperty.getCollectionImportPolicy() == CollectionImportPolicy.REMOVE_ABSENT_ITEMS) {
                            if (!dstFilteredIds.contains(refId) && !srcFilteredIds.contains(refId)) {
                                saveContext.removing(e);
                            }
                        }
                        if (srcFilteredIds.contains(refId)) {
                            newCollectionValue.add(e);
                        }
                    }
                })
                .compare(collectionValue, prevCollectionValue);
        EntityValues.setValue(dstEntity, metaProperty.getName(), newCollectionValue);
    }

    protected void importManyToManyCollectionAttribute(Object srcEntity,
                                                       Object dstEntity,
                                                       EntityImportPlanProperty importPlanProperty,
                                                       @Nullable FetchPlan fetchPlan,
                                                       SaveContext saveContext,
                                                       Collection<ReferenceInfo> referenceInfoList,
                                                       boolean optimisticLocking) {
        Collection collectionValue = EntityValues.getValue(srcEntity, importPlanProperty.getName());
        Collection prevCollectionValue = EntityValues.getValue(dstEntity, importPlanProperty.getName());
        MetaProperty metaProperty = metadata.getClass(srcEntity).getProperty(importPlanProperty.getName());

        Collection dstFilteredIds = getFilteredIds(getSecurityState(dstEntity), metaProperty.getName());
        Collection srcFilteredIds = getFilteredIds(getSecurityState(srcEntity), metaProperty.getName());

        if (importPlanProperty.getPlan() != null) {
            Collection newCollectionValue = createNewCollection(metaProperty);
            CollectionCompare.with()
                    .onCreate(e -> {
                        if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(e))) {
                            Object result = importEntity(e, null, importPlanProperty.getPlan(), fetchPlan,
                                    saveContext, referenceInfoList, optimisticLocking, false);
                            newCollectionValue.add(result);
                        }
                    })
                    .onUpdate((src, dst) -> {
                        if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(src))) {
                            Object result = importEntity(src, dst, importPlanProperty.getPlan(), fetchPlan,
                                    saveContext, referenceInfoList, optimisticLocking, false);
                            newCollectionValue.add(result);
                        }
                    })
                    .onDelete(e -> {
                        if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(e))) {
                            if (srcFilteredIds.contains(referenceToEntitySupport.getReferenceId(e))) {
                                newCollectionValue.add(e);
                            } else if (importPlanProperty.getCollectionImportPolicy() == CollectionImportPolicy.KEEP_ABSENT_ITEMS) {
                                newCollectionValue.add(e);
                            }
                        }
                    })
                    .compare(collectionValue, prevCollectionValue);
            EntityValues.setValue(dstEntity, metaProperty.getName(), newCollectionValue);
        } else {
            //create ReferenceInfo objects - they will be parsed later
            ReferenceInfo referenceInfo = new ReferenceInfo(dstEntity, getSecurityState(srcEntity), importPlanProperty, collectionValue, prevCollectionValue);
            referenceInfoList.add(referenceInfo);
        }
    }

    @Nullable
    protected Object importEmbeddedAttribute(Object srcEntity,
                                             Object dstEntity,
                                             boolean createOp,
                                             EntityImportPlanProperty importPlanProperty,
                                             FetchPlan fetchPlan,
                                             SaveContext saveContext,
                                             Collection<ReferenceInfo> referenceInfoList,
                                             boolean optimisticLock) {
        String propertyName = importPlanProperty.getName();
        MetaProperty metaProperty = metadata.getClass(srcEntity).getProperty(propertyName);
        Object srcEmbeddedEntity = EntityValues.getValue(srcEntity, propertyName);
        if (srcEmbeddedEntity == null) {
            return null;
        }
        Object dstEmbeddedEntity = EntityValues.getValue(dstEntity, propertyName);
        MetaClass embeddedAttrMetaClass = metaProperty.getRange().asClass();
        if (dstEmbeddedEntity == null) {
            dstEmbeddedEntity = metadata.create(embeddedAttrMetaClass);
        }

        if (!createOp) {
            assertToken(srcEntity, fetchPlan);
        }

        for (EntityImportPlanProperty vp : importPlanProperty.getPlan().getProperties()) {
            MetaProperty mp = embeddedAttrMetaClass.getProperty(vp.getName());
            if ((mp.getRange().isDatatype() && !"version".equals(mp.getName())) || mp.getRange().isEnum()) {
                EntityValues.setValue(dstEmbeddedEntity, vp.getName(), EntityValues.getValue(srcEmbeddedEntity, vp.getName()));
            } else if (mp.getRange().isClass()) {
                FetchPlan fetchPlanProperty = fetchPlan.getProperty(propertyName) != null ? fetchPlan.getProperty(propertyName).getFetchPlan() : null;
                if (metaProperty.getRange().getCardinality() == Range.Cardinality.ONE_TO_MANY) {
                    importOneToManyCollectionAttribute(srcEmbeddedEntity, dstEmbeddedEntity,
                            vp, fetchPlanProperty, saveContext, referenceInfoList, optimisticLock, false);
                } else if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_MANY) {
                    importManyToManyCollectionAttribute(srcEmbeddedEntity, dstEmbeddedEntity,
                            vp, fetchPlanProperty, saveContext, referenceInfoList, optimisticLock);
                } else {
                    importReference(srcEmbeddedEntity, dstEmbeddedEntity, vp, fetchPlanProperty, saveContext, referenceInfoList, optimisticLock);
                }
            }
        }

        return dstEmbeddedEntity;
    }

    /**
     * Method finds and set a reference value to the entity or throws EntityImportException if ERROR_ON_MISSING policy
     * is violated
     */
    protected void processReferenceInfo(ReferenceInfo referenceInfo, SaveContext saveContext, Set<Object> loadedEntities) {
        Object entity = referenceInfo.getEntity();
        EntityImportPlanProperty importPlanProperty = referenceInfo.getPlanProperty();
        MetaProperty metaProperty = metadata.getClass(entity).getProperty(importPlanProperty.getName());

        Collection<Object> dstFilteredIds = getFilteredIds(getSecurityState(entity), metaProperty.getName());
        Collection<Object> srcFilteredIds = getFilteredIds(referenceInfo.getPrevSecurityState(), metaProperty.getName());

        if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_MANY) {
            @SuppressWarnings("unchecked")
            Collection<Object> collectionValue = (Collection<Object>) referenceInfo.getPropertyValue();
            @SuppressWarnings("unchecked")
            Collection<Object> prevCollectionValue = (Collection<Object>) referenceInfo.getPrevPropertyValue();
            if (collectionValue == null && srcFilteredIds.isEmpty()) {
                EntityValues.setValue(entity, metaProperty.getName(), createNewCollection(metaProperty));
                return;
            }
            Collection<Object> newCollectionValue = createNewCollection(metaProperty);
            CollectionCompare.with()
                    .onCreate(e -> {
                        if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(e))) {
                            Object result = findReferenceEntity(e, importPlanProperty, saveContext, loadedEntities);
                            if (result != null) {
                                newCollectionValue.add(result);
                            }
                        }
                    })
                    .onUpdate((src, dst) -> {
                        if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(dst))) {
                            Object result = findReferenceEntity(src, importPlanProperty, saveContext, loadedEntities);
                            if (result != null) {
                                newCollectionValue.add(result);
                            }
                        }
                    })
                    .onDelete(e -> {
                        if (!dstFilteredIds.contains(referenceToEntitySupport.getReferenceId(e))) {
                            if (srcFilteredIds.contains(referenceToEntitySupport.getReferenceId(e))) {
                                newCollectionValue.add(e);
                            } else if (importPlanProperty.getCollectionImportPolicy() == CollectionImportPolicy.KEEP_ABSENT_ITEMS) {
                                newCollectionValue.add(e);
                            }
                        }
                    })
                    .compare(collectionValue, prevCollectionValue);
            EntityValues.setValue(entity, metaProperty.getName(), newCollectionValue);
            //end of many-to-many processing block
        } else {
            //all other reference types (except many-to-many)
            Object entityValue = referenceInfo.getPropertyValue();
            if (entityValue == null) {
                if (dstFilteredIds.isEmpty()) {
                    EntityValues.setValue(entity, metaProperty.getName(), null);
//                    entity.setValue(metaProperty.getName(), null);
                    //in case of NULL value we must delete COMPOSITION entities
                    if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                        Object prevEntityValue = referenceInfo.getPrevPropertyValue();
                        if (prevEntityValue != null) {
                            saveContext.removing(prevEntityValue);
                        }
                    }
                }
            } else {
                if (dstFilteredIds.isEmpty()) {
                    Object result = findReferenceEntity(entityValue, importPlanProperty, saveContext, loadedEntities);
                    if (result != null) {
                        EntityValues.setValue(entity, metaProperty.getName(), result);
                    }
                }
            }
        }
    }

    /**
     * Method constructs {@link FetchPlanBuilder} for a regular {@link FetchPlan} from the {@link EntityImportPlan}. The
     * regular fetchPlan will include all properties defined in the import plan.
     */
    protected FetchPlanBuilder constructFetchPlanFromImportPlan(EntityImportPlan importPlan) {
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(importPlan.getEntityClass());
        MetaClass metaClass = metadata.getClass(importPlan.getEntityClass());
        for (EntityImportPlanProperty importPlanProperty : importPlan.getProperties()) {
            EntityImportPlan importPlanPropertyPlan = importPlanProperty.getPlan();
            if (importPlanPropertyPlan == null) {
                MetaProperty metaProperty = metaClass.getProperty(importPlanProperty.getName());
                if (metaProperty.isReadOnly()) continue;
                fetchPlanBuilder.add(importPlanProperty.getName());
            } else {
                fetchPlanBuilder.add(importPlanProperty.getName(), constructFetchPlanFromImportPlan(importPlanPropertyPlan));
            }
        }
        return fetchPlanBuilder;
    }

    protected Collection<Object> getFilteredIds(@Nullable SecurityState securityState, String propertyName) {
        if (securityState != null) {
            return Optional.ofNullable(securityState.getErasedData())
                    .map(v -> v.get(propertyName))
                    .orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }

    protected Collection<Object> createNewCollection(MetaProperty metaProperty) {
        Collection<Object> entities;
        Class<?> propertyType = metaProperty.getJavaType();
        if (List.class.isAssignableFrom(propertyType)) {
            entities = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(propertyType)) {
            entities = new LinkedHashSet<>();
        } else {
            throw new RuntimeException(String.format("Could not instantiate collection with class [%s].", propertyType));
        }
        return entities;
    }

    @Nullable
    protected Object findReferenceEntity(Object entity, EntityImportPlanProperty importPlanProperty, SaveContext saveContext,
                                         Set<Object> loadedEntities) {
        Object result = Stream.concat(loadedEntities.stream(), saveContext.getEntitiesToSave().stream())
                .filter(item -> item.equals(entity))
                .findFirst().orElse(null);
        if (result == null) {
            LoadContext<?> ctx = new LoadContext<>(metadata.getClass(entity))
                    .setHint("jmix.softDeletion", false)
                    .setFetchPlan(fetchPlanRepository.getFetchPlan(metadata.getClass(entity).getJavaClass(), FetchPlan.INSTANCE_NAME))
                    .setId(EntityValues.getId(entity));
            result = dataManager.load(ctx);
            if (result == null) {
                if (importPlanProperty.getReferenceImportBehaviour() == ReferenceImportBehaviour.ERROR_ON_MISSING) {
                    throw new EntityImportException(String.format("Referenced entity for property '%s' is missing",
                            importPlanProperty.getName()));
                }
            } else {
                loadedEntities.add(result);
            }
        }
        return result;
    }

    protected void assertToken(Object entity, FetchPlan fetchPlan) {
        if (coreProperties.isEntitySerializationTokenRequired()) {
            SecurityState securityState = EntitySystemAccess.getSecurityState(entity);
            if (securityState.getRestoreState() == SecurityState.RestoreState.RESTORED_FROM_NULL_TOKEN) {

                MetaClass metaClass = metadata.getClass(entity);
                for (MetaProperty metaProperty : metaClass.getProperties()) {
                    if (metaProperty.getRange().isClass() && metadataTools.isJpa(metaProperty)
                            && fetchPlan.containsProperty(metaProperty.getName())) {

                        InMemoryCrudEntityContext inMemoryContext =
                                new InMemoryCrudEntityContext(metaProperty.getRange().asClass(), applicationContext);
                        accessManager.applyRegisteredConstraints(inMemoryContext);

                        if (inMemoryContext.readPredicate() != null) {
                            throw new EntityTokenException(format("Could not read export/import token from entity %s.", entity));
                        }
                    }
                }
            }
        }
    }

    protected static class ReferenceInfo {
        protected Object entity;
        protected SecurityState prevSecurityState;
        protected EntityImportPlanProperty planProperty;
        protected Object propertyValue;
        protected Object prevPropertyValue;

        public ReferenceInfo(Object entity, SecurityState prevSecurityState, EntityImportPlanProperty planProperty, Object propertyValue, Object prevPropertyValue) {
            this.entity = entity;
            this.prevSecurityState = prevSecurityState;
            this.planProperty = planProperty;
            this.propertyValue = propertyValue;
            this.prevPropertyValue = prevPropertyValue;
        }

        public EntityImportPlanProperty getPlanProperty() {
            return planProperty;
        }

        public Object getPrevPropertyValue() {
            return prevPropertyValue;
        }

        public Object getEntity() {
            return entity;
        }

        public SecurityState getPrevSecurityState() {
            return prevSecurityState;
        }

        public Object getPropertyValue() {
            return propertyValue;
        }
    }
}
