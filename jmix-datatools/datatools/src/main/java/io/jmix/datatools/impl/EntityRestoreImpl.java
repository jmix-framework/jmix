/*
 * Copyright 2020 Haulmont.
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

package io.jmix.datatools.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.impl.converters.AuditConversionService;
import io.jmix.datatools.EntityRestore;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component("datatl_EntityRestore")
public class EntityRestoreImpl implements EntityRestore {

    protected static int RELATED_ENTITY_DELETED_DATE_START_OFFSET_MS = -100;
    protected static int RELATED_ENTITY_DELETED_DATE_END_OFFSET_MS = 1000;

    private static final Logger log = LoggerFactory.getLogger(EntityRestore.class);

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected AuditConversionService auditConversionService;

    @Override
    public int restoreEntities(Collection<Object> entities) {
        SaveContext saveContext = new SaveContext();
        log.debug("Restore {} root entities", entities.size());
        for (Object entity : entities) {
            log.debug("Process entity: {}", entity);
            if (!metadataTools.isSoftDeletable(entity.getClass())) {
                continue;
            }

            if (!metadataTools.isJpaEntity(entity.getClass())) {
                log.warn("Unable to restore non-JPA entity {}", entity);
                continue;
            }

            restoreEntity(entity, saveContext);
        }
        dataManager.save(saveContext);
        return saveContext.getEntitiesToSave().size();
    }

    protected void restoreEntity(Object entity, SaveContext saveContext) {
        Optional<Object> reloadedEntityOpt = dataManager.load(Id.of(entity))
                .hint("jmix.softDeletion", false)
                .optional();
        if (reloadedEntityOpt.isPresent() && EntityValues.isSoftDeleted(reloadedEntityOpt.get())) {
            Object reloadedEntity = reloadedEntityOpt.get();
            log.info("Restoring deleted entity {}", reloadedEntity);
            Object deletedDate = EntityValues.getDeletedDate(reloadedEntity);
            EntityValues.setDeletedDate(reloadedEntity, null);
            EntityValues.setDeletedBy(reloadedEntity, null);
            saveContext.saving(reloadedEntity);
            RestorationContext restorationContext = new RestorationContext(entity, deletedDate, saveContext);
            restoreDetails(restorationContext);
        }
    }

    protected void restoreDetails(RestorationContext restorationContext) {
        processOnDeleteProperties(restorationContext);
        processOnDeleteInverseProperties(restorationContext);
    }

    private void processOnDeleteProperties(RestorationContext restorationContext) {
        MetaClass metaClass = metadata.getClass(restorationContext.getEntity());
        List<MetaProperty> properties = new ArrayList<>();
        fillProperties(metaClass, properties, OnDelete.class.getName());
        log.trace("Restore Details of entity: {}. OnDelete properties: {}", restorationContext.getEntity(), properties);
        for (MetaProperty property : properties) {
            OnDelete annotation = property.getAnnotatedElement().getAnnotation(OnDelete.class);
            DeletePolicy deletePolicy = annotation.value();
            if (deletePolicy == DeletePolicy.CASCADE) {
                processOnDeleteCascadeProperty(metaClass, property, restorationContext);
            }
        }
    }

    private void processOnDeleteCascadeProperty(MetaClass metaClass, MetaProperty property, RestorationContext restorationContext) {
        MetaClass detailMetaClass = property.getRange().asClass();
        log.trace("Process OnDelete Cascade property {}. Meta Classes: {} -> {}", property, metaClass, detailMetaClass);
        if (!metadataTools.isSoftDeletable(detailMetaClass.getJavaClass())) {
            log.debug("Cannot restore {} because it is hard deleted", detailMetaClass);
            return;
        }
        if (metadataTools.isOwningSide(property)) {
            processOwningSideProperty(property, restorationContext);
        } else {
            MetaProperty inverseProperty = property.getInverse();
            if (inverseProperty == null) {
                log.debug("Cannot restore {} because it has no inverse property for {}", detailMetaClass, metaClass);
                return;
            }
            processInverseSideProperty(inverseProperty, detailMetaClass, restorationContext);
        }
    }

    private void processOwningSideProperty(MetaProperty property, RestorationContext restorationContext) {
        log.trace("Process owning side property {}", property);
        Object value = EntityValues.getValue(restorationContext.getEntity(), property.getName());
        if (value instanceof Entity) {
            restoreEntity((Entity) value, restorationContext.getSaveContext());
        } else if (value instanceof Collection) {
            for (Object detailEntity : (Collection<?>) value) {
                restoreEntity((Entity) detailEntity, restorationContext.getSaveContext());
            }
        }
    }

    private void processInverseSideProperty(MetaProperty inverseProperty, MetaClass detailMetaClass, RestorationContext restorationContext) {
        log.trace("Process inverse property {}", inverseProperty);
        String jpql = getOnDeleteCascadePropertyQueryString(detailMetaClass, inverseProperty);

        Object entityId = restorationContext.getEntityId();
        if (entityId == null) {
            throw new IllegalStateException("EntityId is null");
        }

        LoadContext<Entity> loadContext = createRestoreCandidatesLoadContext(
                jpql, detailMetaClass, entityId, restorationContext.getDeletedDate()
        );

        List<Entity> entities = dataManager.loadList(loadContext);
        for (Entity detailEntity : entities) {
            if (metadataTools.isSoftDeletable(restorationContext.getEntityClass())) {
                restoreEntity(detailEntity, restorationContext.getSaveContext());
            }
        }
    }

    private LoadContext<Entity> createRestoreCandidatesLoadContext(String queryString,
                                                                   MetaClass metaClass,
                                                                   Object entityId,
                                                                   Object deletedDate) {
        LoadContext.Query query = new LoadContext.Query(queryString);
        Class<?> candidateDeletedDatePropertyClass = getDeletedDatePropertyClassNN(metaClass);
        Object deletedDateInCandidateType =
                auditConversionService.convert(deletedDate, candidateDeletedDatePropertyClass);
        query.setParameter("id", entityId);
        query.setParameter("start",
                addOffsetToDateMs(deletedDateInCandidateType, RELATED_ENTITY_DELETED_DATE_START_OFFSET_MS));
        query.setParameter("end",
                addOffsetToDateMs(deletedDateInCandidateType, RELATED_ENTITY_DELETED_DATE_END_OFFSET_MS));
        return new LoadContext<Entity>(metaClass)
                .setQuery(query)
                .setHint("jmix.softDeletion", false);
    }

    protected Class<?> getDeletedDatePropertyClassNN(MetaClass metaClass) {
        String deletedDateProperty = metadataTools.findDeletedDateProperty(metaClass.getJavaClass());
        if (deletedDateProperty == null) {
            throw new IllegalArgumentException("Failed to find deletedDate property");
        }
        return metaClass.getProperty(deletedDateProperty).getJavaType();
    }

    protected Object addOffsetToDateMs(Object dateObject, int offset) {
        if (dateObject instanceof Date date) {
            return DateUtils.addMilliseconds(date, offset);
        } else if (dateObject instanceof LocalDate localDate) {
            return localDate;
        } else if (dateObject instanceof LocalDateTime localDateTime) {
            return localDateTime.plus(offset, ChronoUnit.MILLIS);
        } else if (dateObject instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.plus(offset, ChronoUnit.MILLIS);
        } else {
            throw new IllegalArgumentException("'%s' date class is not supported"
                    .formatted(dateObject.getClass().getSimpleName()));
        }
    }

    private void processOnDeleteInverseProperties(RestorationContext restorationContext) {
        MetaClass metaClass = metadata.getClass(restorationContext.getEntityClass());
        List<MetaProperty> properties = new ArrayList<>();
        fillProperties(metaClass, properties, OnDeleteInverse.class.getName());
        log.trace("Restore Details of entity: {}. OnDeleteInverse properties: {}", restorationContext.getEntity(), properties);
        for (MetaProperty property : properties) {
            OnDeleteInverse annotation = property.getAnnotatedElement().getAnnotation(OnDeleteInverse.class);
            DeletePolicy deletePolicy = annotation.value();
            if (deletePolicy == DeletePolicy.CASCADE) {
                processOnDeleteInverseCascadeProperty(property, restorationContext);
            }
        }
    }

    private void processOnDeleteInverseCascadeProperty(MetaProperty property, RestorationContext restorationContext) {
        MetaClass detailMetaClass = property.getDomain();
        log.trace("Process OnDeleteInverse Cascade property {}. MetaClass: {}", property, detailMetaClass);
        Object entityId = restorationContext.getEntityId();
        if (entityId == null) {
            throw new IllegalStateException("EntityId is null");
        }
        if (!metadataTools.isSoftDeletable(detailMetaClass.getJavaClass())) {
            log.debug("Cannot restore {} because it is hard deleted", property.getRange().asClass());
            return;
        }
        List<MetaClass> metaClassesToRestore = new ArrayList<>();
        metaClassesToRestore.add(detailMetaClass);
        metaClassesToRestore.addAll(detailMetaClass.getDescendants());
        for (MetaClass metaClassToRestore : metaClassesToRestore) {
            if (!metadataTools.isJpaEntity(metaClassToRestore)) {
                continue;
            }
            String jpql = getOnDeleteInverseCascadePropertyQueryString(metaClassToRestore, property);
            LoadContext<Entity> loadContext = createRestoreCandidatesLoadContext(jpql, detailMetaClass, entityId,
                    restorationContext.getDeletedDate());
            List<Entity> entities = dataManager.loadList(loadContext);

            for (Entity detailEntity : entities) {
                if (metadataTools.isSoftDeletable(restorationContext.getEntityClass())) {
                    restoreEntity(detailEntity, restorationContext.getSaveContext());
                }
            }
        }
    }

    private String getOnDeleteInverseCascadePropertyQueryString(MetaClass metaClassToRestore, MetaProperty property) {
        String queryString;
        String deletedDateProperty = metadataTools.findDeletedDateProperty(metaClassToRestore.getJavaClass());
        if (property.getRange().getCardinality().isMany()) {
            queryString = "select e from " + metaClassToRestore.getName() + " e join e." + property.getName() + " p"
                    + " where p.id = :id and e." + deletedDateProperty + " >= :start and e." + deletedDateProperty + " <= :end";
        } else {
            queryString = "select e from " + metaClassToRestore.getName() + " e where e." + property.getName()
                    + ".id = :id and e." + deletedDateProperty + " >= :start and e." + deletedDateProperty + " <= :end";
        }
        return queryString;
    }

    private String getOnDeleteCascadePropertyQueryString(MetaClass metaClassToRestore, MetaProperty inverseProperty) {
        String deletedDateProperty = metadataTools.findDeletedDateProperty(metaClassToRestore.getJavaClass());
        return "select e from " + metaClassToRestore.getName() + " e where e." + inverseProperty.getName() + ".id = :id " +
                "and e." + deletedDateProperty + " >= :start and e." + deletedDateProperty + " <= :end";
    }

    protected void fillProperties(MetaClass metaClass, List<MetaProperty> properties, String annotationName) {
        properties.clear();
        MetaProperty[] metaProperties = (MetaProperty[]) metaClass.getAnnotations().get(annotationName);
        log.debug("Fill properties: MetaClass={} ({}), Annotation={}, properties={}", metaClass, metaClass.getJavaClass(), annotationName, Arrays.deepToString(metaProperties));
        if (metaProperties != null)
            properties.addAll(Arrays.asList(metaProperties));
        for (MetaClass aClass : metaClass.getAncestors()) {
            metaProperties = (MetaProperty[]) aClass.getAnnotations().get(annotationName);
            log.debug("Fill properties - Ancestors: Ancestor={} ({}), Annotation={}, properties={}", aClass, aClass.getJavaClass(), annotationName, Arrays.deepToString(metaProperties));
            if (metaProperties != null)
                properties.addAll(Arrays.asList(metaProperties));
        }
    }

    private static class RestorationContext {
        private final Object entity;
        private final Object deletedDate;
        private final SaveContext saveContext;

        public RestorationContext(Object entity, Object deletedDate, SaveContext saveContext) {
            this.entity = entity;
            this.deletedDate = deletedDate;
            this.saveContext = saveContext;
        }

        public Object getEntity() {
            return entity;
        }

        public Object getEntityId() {
            return EntityValues.getId(entity);
        }

        public Class<?> getEntityClass() {
            return entity.getClass();
        }

        public Object getDeletedDate() {
            return deletedDate;
        }

        public SaveContext getSaveContext() {
            return saveContext;
        }
    }
}
