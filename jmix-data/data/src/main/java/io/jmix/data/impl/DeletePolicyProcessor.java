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
package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.persistence.DbmsSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.util.*;

@Component("data_DeletePolicyProcessor")
@Scope("prototype")
public class DeletePolicyProcessor {

    private static final Logger log = LoggerFactory.getLogger(DeletePolicyProcessor.class);

    protected Object entity;
    protected MetaClass metaClass;
    protected String primaryKeyName;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected DbmsSpecifics dbmsSpecifics;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityStates entityStates;

    protected EntityManager entityManager;

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
        this.metaClass = metadata.getClass(entity);
        primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);

        String storeName = metaClass.getStore().getName();
        entityManager = getEntityManager(storeName);
    }

    private EntityManager getEntityManager(String storeName) {
        return storeAwareLocator.getEntityManager(storeName);
    }

    public void process() {
        List<MetaProperty> properties = new ArrayList<>();

        fillProperties(properties, OnDeleteInverse.class.getName());
        if (!properties.isEmpty())
            processOnDeleteInverse(properties);

        fillProperties(properties, OnDelete.class.getName());
        if (!properties.isEmpty())
            processOnDelete(properties);
    }

    protected void fillProperties(List<MetaProperty> properties, String annotationName) {
        properties.clear();
        MetaProperty[] metaProperties = (MetaProperty[]) metaClass.getAnnotations().get(annotationName);
        if (metaProperties != null)
            properties.addAll(Arrays.asList(metaProperties));
        for (MetaClass aClass : metaClass.getAncestors()) {
            metaProperties = (MetaProperty[]) aClass.getAnnotations().get(annotationName);
            if (metaProperties != null)
                properties.addAll(Arrays.asList(metaProperties));
        }
    }

    protected void processOnDeleteInverse(List<MetaProperty> properties) {
        for (MetaProperty property : properties) {
            MetaClass metaClass = property.getDomain();

            List<MetaClass> persistentEntities = new ArrayList<>();
            if (isPersistent(metaClass))
                persistentEntities.add(metaClass);
            for (MetaClass descendant : metaClass.getDescendants()) {
                if (isPersistent(descendant))
                    persistentEntities.add(descendant);
            }

            for (MetaClass persistentEntity : persistentEntities) {
                OnDeleteInverse annotation = property.getAnnotatedElement().getAnnotation(OnDeleteInverse.class);
                DeletePolicy deletePolicy = annotation.value();
                switch (deletePolicy) {
                    case DENY:
                        if (referenceExists(persistentEntity.getName(), property))
                            throw new DeletePolicyException(this.metaClass.getName(), persistentEntity.getName());
                        break;
                    case CASCADE:
                        cascade(persistentEntity.getName(), property);
                        break;
                    case UNLINK:
                        unlink(persistentEntity.getName(), property);
                        break;
                }
            }

        }
    }

    protected void processOnDelete(List<MetaProperty> properties) {
        for (MetaProperty property : properties) {
            MetaClass metaClass = property.getRange().asClass();
            OnDelete annotation = property.getAnnotatedElement().getAnnotation(OnDelete.class);
            DeletePolicy deletePolicy = annotation.value();
            switch (deletePolicy) {
                case DENY:
                    if (property.getRange().getCardinality().isMany()) {
                        if (!isCollectionEmpty(property))
                            throw new DeletePolicyException(this.metaClass.getName(), metaClass.getName());
                    } else {
                        Object value = getReference(entity, property);
                        if (value != null)
                            throw new DeletePolicyException(this.metaClass.getName(), metaClass.getName());
                    }
                    break;
                case CASCADE:
                    if (property.getRange().getCardinality().isMany()) {
                        Collection<Object> value = getCollection(property);
                        if (value != null && !value.isEmpty()) {
                            for (Object e : value) {
                                entityManager.remove(e);
                            }
                        }
                    } else {
                        Object value = getReference(entity, property);
                        if (value != null && checkIfEntityBelongsToMaster(property, value)) {
                            if (!(EntityValues.isSoftDeletionSupported(value))) {
                                if (entityStates.isLoaded(entity, property.getName())) {
                                    EntityValues.setValue(entity, property.getName(), null);
                                    entityManager.remove(value);
                                } else {
                                    hardDeleteNotLoadedReference(entity, property, value);
                                }
                            } else {
                                entityManager.remove(value);
                            }
                        }
                    }
                    break;
                case UNLINK:
                    if (property.getRange().getCardinality().isMany()) {
                        if (metadataTools.isOwningSide(property)) {
                            Collection<Object> value = EntityValues.getValue(entity, property.getName());
                            if (value != null) {
                                value.clear();
                            }
                        } else if (property.getInverse() != null) {
                            Collection<Object> value = getCollection(property);
                            if (value != null) {
                                value.forEach(e -> setReferenceNull(e, property.getInverse()));
                            }
                        } else {
                            throw new UnsupportedOperationException("Unable to unlink nested collection items");
                        }
                    } else {
                        if (metadataTools.isOwningSide(property)) {
                            setReferenceNull(entity, property);
                        } else {
                            Object value = getReference(entity, property);
                            if (value != null && property.getInverse() != null) {
                                setReferenceNull(value, property.getInverse());
                            }
                        }
                    }
                    break;
            }
        }
    }

    protected void hardDeleteNotLoadedReference(Object entity, MetaProperty property, Object reference) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCompletion() {
                try {
                    String column = metadataTools.getDatabaseColumn(property);
                    if (column != null) { // is null for mapped-by property
                        String updateMasterSql = "update " + metadataTools.getDatabaseTable(metaClass)
                                + " set " + column + " = null where "
                                + metadataTools.getPrimaryKeyName(metaClass) + " = ?";
                        log.debug("Hard delete un-fetched reference: {}, bind: [{}]", updateMasterSql, EntityValues.getId(entity));
                        getJdbcTemplate().update(updateMasterSql, dbmsSpecifics.getDbTypeConverter().getSqlObject(EntityValues.getId(entity)));
                    }

                    MetaClass refMetaClass = property.getRange().asClass();
                    String deleteRefSql = "delete from " + metadataTools.getDatabaseTable(refMetaClass) + " where "
                            + metadataTools.getPrimaryKeyName(refMetaClass) + " = ?";
                    log.debug("Hard delete un-fetched reference: {}, bind: [{}]", deleteRefSql, EntityValues.getId(reference));
                    getJdbcTemplate().update(deleteRefSql, dbmsSpecifics.getDbTypeConverter().getSqlObject(EntityValues.getId(reference)));
                } catch (DataAccessException e) {
                    throw new RuntimeException("Error processing deletion of " + entity, e);
                }
            }
        });
    }

    protected JdbcTemplate getJdbcTemplate() {
        return storeAwareLocator.getJdbcTemplate(metaClass.getStore().getName());
    }

    protected void setReferenceNull(Object entity, MetaProperty property) {
        Range range = property.getRange();
        if (metadataTools.isOwningSide(property) && !range.getCardinality().isMany()) {
            if (entityStates.isLoaded(entity, property.getName())) {
                EntityValues.setValue(entity, property.getName(), null);
            } else {
                hardSetReferenceNull(entity, property);
            }
        }
    }

    protected void hardSetReferenceNull(Object entity, MetaProperty property) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCompletion() {
                MetaClass entityMetaClass = metadata.getClass(entity);
                while (!entityMetaClass.equals(property.getDomain())) {
                    MetaClass ancestor = entityMetaClass.getAncestor();
                    if (ancestor == null)
                        throw new IllegalStateException("Cannot determine a persistent entity for property " + property);
                    if (metadataTools.isJpaEntity(ancestor)) {
                        entityMetaClass = ancestor;
                    } else {
                        break;
                    }
                }
                String sql = String.format("update %s set %s = null where %s = ?",
                        metadataTools.getDatabaseTable(entityMetaClass),
                        metadataTools.getDatabaseColumn(property),
                        metadataTools.getPrimaryKeyName(entityMetaClass));
                try {
                    log.debug("Set reference to null: {}, bind: [{}]", sql, EntityValues.getId(entity));
                    getJdbcTemplate().update(sql, dbmsSpecifics.getDbTypeConverter().getSqlObject(EntityValues.getId(entity)));
                } catch (DataAccessException e) {
                    throw new RuntimeException("Error processing deletion of " + entity, e);
                }
            }
        });
    }

    @Nullable
    protected Object getReference(Object entity, MetaProperty property) {
        if (entityStates.isLoaded(entity, property.getName()))
            return EntityValues.getValue(entity, property.getName());
        else {
            Query query = entityManager.createQuery(
                    "select e." + property.getName() + " from " + metadata.getClass(entity).getName()
                            + " e where e." + primaryKeyName + " = ?1");
            query.setParameter(1, EntityValues.getId(entity));
            List list = query.getResultList();
            Object refEntity = list.isEmpty() ? null : list.get(0);
            return refEntity;
        }
    }

    protected boolean checkIfEntityBelongsToMaster(MetaProperty property, Object entityToRemove) {
        MetaProperty inverseProperty = property.getInverse();
        if (inverseProperty != null && !inverseProperty.getRange().getCardinality().isMany()) {
            Object master = EntityValues.getValue(entityToRemove, inverseProperty.getName());
            return entity.equals(master);
        } else {
            return true;
        }
    }

    protected boolean isCollectionEmpty(MetaProperty property) {
        MetaProperty inverseProperty = property.getInverse();
        if (inverseProperty == null) {
            log.warn("Inverse property not found for property {}", property);
            Collection<Object> value = EntityValues.getValue(entity, property.getName());
            return value == null || value.isEmpty();
        }

        String invPropName = inverseProperty.getName();
        String collectionPkName = metadataTools.getPrimaryKeyName(property.getRange().asClass());

        String qlStr = "select e." + collectionPkName + " from " + property.getRange().asClass().getName() +
                " e where e." + invPropName + "." + primaryKeyName + " = ?1";

        Query query = entityManager.createQuery(qlStr);
        query.setParameter(1, EntityValues.getId(entity));
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<Object> list = query.getResultList();

        return list.isEmpty();
    }

    protected Collection<Object> getCollection(MetaProperty property) {
        MetaProperty inverseProperty = property.getInverse();
        if (inverseProperty == null) {
            log.warn("Inverse property not found for property {}", property);
            Collection<Object> value = EntityValues.getValue(entity, property.getName());
            return value == null ? Collections.emptyList() : value;
        }

        String invPropName = inverseProperty.getName();
        String qlStr = "select e from " + property.getRange().asClass().getName() + " e where e." + invPropName + "." +
                primaryKeyName + " = ?1";

        Query query = entityManager.createQuery(qlStr);
        query.setParameter(1, EntityValues.getId(entity));
        @SuppressWarnings("unchecked")
        List<Object> list = query.getResultList();

        // If the property is not loaded, it means it was not modified and further check is not needed
        if (!entityStates.isLoaded(entity, property.getName())) {
            return list;
        }
        // Check whether the collection items still belong to the master entity, because they could be changed in the
        // current transaction that did not affect the database yet
        List<Object> result = new ArrayList<>(list.size());
        for (Object item : list) {
            Object master = EntityValues.getValue(item, invPropName);
            if (entity.equals(master))
                result.add(item);
        }
        return result;
    }

    protected boolean referenceExists(String entityName, MetaProperty property) {
        String template = property.getRange().getCardinality().isMany() ?
                "select count(e) from %s e join e.%s c where c." + primaryKeyName + "= ?1" :
                "select count(e) from %s e where e.%s." + primaryKeyName + " = ?1";
        String qstr = String.format(template, entityName, property.getName());
        Query query = entityManager.createQuery(qstr);
        query.setParameter(1, EntityValues.getId(entity));
        query.setMaxResults(1);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    protected boolean isPersistent(MetaClass metaClass) {
        return metaClass.getJavaClass().isAnnotationPresent(javax.persistence.Entity.class);
    }

    protected void cascade(String entityName, MetaProperty property) {
        String template = property.getRange().getCardinality().isMany() ?
                "select e from %s e join e.%s c where c." + primaryKeyName + " = ?1" :
                "select e from %s e where e.%s." + primaryKeyName + " = ?1";
        String qstr = String.format(template, entityName, property.getName());
        Query query = entityManager.createQuery(qstr);
        query.setParameter(1, EntityValues.getId(entity));
        @SuppressWarnings("unchecked")
        List<Object> list = query.getResultList();
        for (Object e : list) {
            entityManager.remove(e);
        }
    }

    protected void unlink(String entityName, MetaProperty property) {
        if (metadataTools.isOwningSide(property)) {
            String template = property.getRange().getCardinality().isMany() ?
                    "select e from %s e join e.%s c where c." + primaryKeyName + " = ?1" :
                    "select e from %s e where e.%s." + primaryKeyName + " = ?1";
            String qstr = String.format(template, entityName, property.getName());
            Query query = entityManager.createQuery(qstr);
            query.setParameter(1, EntityValues.getId(entity));
            @SuppressWarnings("unchecked")
            List<Object> list = query.getResultList();
            for (Object e : list) {
                if (property.getRange().getCardinality().isMany()) {
                    Collection<?> collection = EntityValues.getValue(e, property.getName());
                    if (collection != null) {
                        collection.removeIf(o -> entity.equals(o));
                    }
                } else {
                    setReferenceNull(e, property);
                }
            }
        } else {
            MetaProperty inverseProp = property.getInverse();
            if (inverseProp != null && inverseProp.getDomain().equals(metaClass)) {
                setReferenceNull(entity, inverseProp);
            }
        }
    }
}
