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

package io.jmix.eclipselink.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.common.util.StackTrace;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.data.AttributeChangesProvider;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.impl.*;
import io.jmix.eclipselink.impl.entitycache.QueryCacheManager;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.internal.descriptors.changetracking.AttributeChangeListener;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;
import static io.jmix.core.entity.EntitySystemAccess.getUncheckedEntityEntry;

@Component("eclipselink_EclipselinkPersistenceSupport")
public class EclipselinkPersistenceSupport implements ApplicationContextAware {

    public static final String RESOURCE_HOLDER_KEY = ContainerResourceHolder.class.getName();

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityListenerManager entityListenerManager;

    @Autowired
    protected QueryCacheManager queryCacheManager;

    @Autowired
    protected JpaCacheSupport jpaCacheSupport;

    @Autowired
    protected EntityChangedEventManager entityChangedEventManager;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected AttributeChangesProvider attributeChangesProvider;

    @Autowired(required = false)
    protected List<JpaLifecycleListener> lifecycleListeners = new ArrayList<>();

    @Autowired
    protected ObjectProvider<DeletePolicyProcessor> deletePolicyProcessorProvider;

    protected List<BeforeCommitTransactionListener> beforeCommitTxListeners;

    protected List<AfterCompleteTransactionListener> afterCompleteTxListeners;

    private static final Logger log = LoggerFactory.getLogger(EclipselinkPersistenceSupport.class.getName());

    private final Logger implicitFlushLog = LoggerFactory.getLogger("io.jmix.eclipselink.IMPLICIT_FLUSH");

    protected static Set<Object> createEntitySet() {
        return Sets.newIdentityHashSet();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, BeforeCommitTransactionListener> beforeCommitMap = applicationContext.getBeansOfType(BeforeCommitTransactionListener.class);
        beforeCommitTxListeners = new ArrayList<>(beforeCommitMap.values());
        beforeCommitTxListeners.sort(new OrderComparator());

        Map<String, AfterCompleteTransactionListener> afterCompleteMap = applicationContext.getBeansOfType(AfterCompleteTransactionListener.class);
        afterCompleteTxListeners = new ArrayList<>(afterCompleteMap.values());
        afterCompleteTxListeners.sort(new OrderComparator());
    }

    /**
     * INTERNAL.
     * Register synchronizations with a just started transaction.
     */
    public void registerSynchronizations(String transactionManagerKey) {
        log.trace("registerSynchronizations for transaction manager '{}'", transactionManagerKey);
        prepareInstanceContainerResourceHolder(transactionManagerKey);
    }

    public void registerInstance(Object entity, EntityManager entityManager) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        UnitOfWork unitOfWork = entityManager.unwrap(UnitOfWork.class);
        String storeName = getStorageName(unitOfWork);
        getInstanceContainerResourceHolder(storeName).registerInstanceForUnitOfWork(entity, unitOfWork, storeName);

        getEntityEntry(entity).setDetached(false);
    }

    public void registerInstance(Object entity, AbstractSession session) {
        // Can be called outside of a transaction when fetching lazy attributes
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            return;

        if (!(session instanceof UnitOfWork))
            throw new RuntimeException("Session is not a UnitOfWork: " + session);
        String storeName = getStorageName(session);
        getInstanceContainerResourceHolder(storeName).registerInstanceForUnitOfWork(entity, (UnitOfWork) session, storeName);
    }

    public Collection<Object> getInstances(EntityManager entityManager) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        UnitOfWork unitOfWork = entityManager.unwrap(UnitOfWork.class);
        String storeName = getStorageName(unitOfWork);
        return getInstanceContainerResourceHolder(storeName).getInstances(unitOfWork, storeName);
    }

    public Collection<Object> getSavedInstances(String storeName) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        return getInstanceContainerResourceHolder(storeName).getSavedInstances();
    }

    public Collection<Object> getSavedInstancesByTransactionManager(String tmKey) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        return prepareInstanceContainerResourceHolder(tmKey).getSavedInstances();
    }

    public String getStorageName(Session session) {
        String storeName = (String) session.getProperty(PersistenceUnitProperties.STORE_NAME_PROPERTY);
        return Strings.isNullOrEmpty(storeName) ? Stores.MAIN : storeName;
    }

    public ContainerResourceHolder prepareInstanceContainerResourceHolder(String transactionManagerKey) {
        ContainerResourceHolder holder =
                (ContainerResourceHolder) TransactionSynchronizationManager.getResource(RESOURCE_HOLDER_KEY);
        if (holder == null) {
            holder = new ContainerResourceHolder(transactionManagerKey);
            TransactionSynchronizationManager.bindResource(RESOURCE_HOLDER_KEY, holder);
        } else if (!holder.getTransactionManagerKey().equals(transactionManagerKey)) {
            throw new IllegalStateException("Cannot prepare resource holder for " + transactionManagerKey
                    + " transaction manager because active transaction is for " + holder.getTransactionManagerKey()
                    + " transaction manager");
        }

        if (TransactionSynchronizationManager.isSynchronizationActive() && !holder.isSynchronizedWithTransaction()) {
            holder.setSynchronizedWithTransaction(true);
            TransactionSynchronizationManager.registerSynchronization(
                    new ContainerResourceSynchronization(holder, RESOURCE_HOLDER_KEY));
        }
        return holder;
    }

    public ContainerResourceHolder getInstanceContainerResourceHolder(String storeName) {
        ContainerResourceHolder holder =
                (ContainerResourceHolder) TransactionSynchronizationManager.getResource(RESOURCE_HOLDER_KEY /*+ "." + storeName*/);
        String tmKey = storeAwareLocator.getTransactionManagerKey(storeName);
        if (holder == null) {
            holder = new ContainerResourceHolder(tmKey);
            holder.addStore(storeName);
            TransactionSynchronizationManager.bindResource(RESOURCE_HOLDER_KEY /*+ "." + storeName*/, holder);
        } else if (!holder.getStores().contains(storeName)) {
            if (tmKey.equals(holder.getTransactionManagerKey())) {
                holder.addStore(storeName);
            } else {
                throw new IllegalStateException("Cannot handle entity from " + tmKey
                        + " datastore because active transaction is for " + holder.getTransactionManagerKey()
                        + " transaction manager");
            }
        }

        if (TransactionSynchronizationManager.isSynchronizationActive() && !holder.isSynchronizedWithTransaction()) {
            holder.setSynchronizedWithTransaction(true);
            TransactionSynchronizationManager.registerSynchronization(
                    new ContainerResourceSynchronization(holder, RESOURCE_HOLDER_KEY /*+ "." + storeName*/));
        }
        return holder;
    }

    public void processFlush(EntityManager entityManager, boolean warnAboutImplicitFlush) {
        UnitOfWork unitOfWork = entityManager.unwrap(UnitOfWork.class);
        String storeName = getStorageName(unitOfWork);
        traverseEntities(getInstanceContainerResourceHolder(storeName),
                new OnSaveEntityVisitor(storeAwareLocator.getTransactionManagerKey(storeName)),
                warnAboutImplicitFlush);
    }

    protected void fireBeforeDetachEntityListener(Object entity, String storeName) {
        if (!getEntityEntry(entity).isDetached()) {
            JmixEntityFetchGroup.setAccessLocalUnfetched(false);
            try {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_DETACH, storeName);
            } finally {
                JmixEntityFetchGroup.setAccessLocalUnfetched(true);
            }
        }
    }

    protected boolean isDeleted(Object entity, AttributeChangeListener changeListener) {
        if (EntityValues.isSoftDeletionSupported(entity)) {
            //SoftDeletion may be disabled, so check it
            EntityManager jmixEm = storeAwareLocator.getEntityManager(metadata.getClass(entity.getClass()).getStore().getName());
            if (PersistenceHints.isSoftDeletion(jmixEm)) {
                ObjectChangeSet changeSet = changeListener.getObjectChangeSet();
                return changeSet != null
                        && changeSet.getAttributesToChanges().containsKey(metadataTools.findDeletedDateProperty(entity.getClass()))
                        && EntityValues.isSoftDeleted(entity);
            } else {
                return getEntityEntry(entity).isRemoved();
            }

        } else {
            return getEntityEntry(entity).isRemoved();
        }
    }

    protected void traverseEntities(ContainerResourceHolder container, EntityVisitor visitor, boolean warnAboutImplicitFlush) {
        beforeStore(container, visitor, container.getAllInstances(), createEntitySet(), warnAboutImplicitFlush);
    }

    protected void beforeStore(ContainerResourceHolder container, EntityVisitor visitor,
                               Collection<Object> instances, Set<Object> processed, boolean warnAboutImplicitFlush) {
        boolean possiblyChanged = false;
        Set<Object> withoutPossibleChanges = createEntitySet();
        for (Object entity : instances) {
            processed.add(entity);

            if (!(entity instanceof ChangeTracker))
                continue;

            boolean result = visitor.visit(entity);
            if (!result) {
                withoutPossibleChanges.add(entity);
            }
            possiblyChanged = result || possiblyChanged;
        }
        if (!possiblyChanged)
            return;

        if (warnAboutImplicitFlush) {
            if (implicitFlushLog.isTraceEnabled()) {
                implicitFlushLog.trace("Implicit flush due to query execution, see stack trace for the cause:\n"
                        + StackTrace.asString());
            } else {
                implicitFlushLog.debug("Implicit flush due to query execution");
            }
        }

        Collection<Object> afterProcessing = container.getAllInstances();
        if (afterProcessing.size() > processed.size()) {
            afterProcessing.removeAll(processed);
            beforeStore(container, visitor, afterProcessing, processed, false);
        }

        if (!withoutPossibleChanges.isEmpty()) {
            afterProcessing = withoutPossibleChanges.stream()
                    .filter(instance -> {
                        ChangeTracker changeTracker = (ChangeTracker) instance;
                        AttributeChangeListener changeListener =
                                (AttributeChangeListener) changeTracker._persistence_getPropertyChangeListener();
                        return changeListener != null
                                && changeListener.hasChanges();
                    })
                    .collect(Collectors.toList());
            if (!afterProcessing.isEmpty()) {
                beforeStore(container, visitor, afterProcessing, processed, false);
            }
        }
    }

    public void detach(EntityManager entityManager, Object entity) {
        UnitOfWork unitOfWork = entityManager.unwrap(UnitOfWork.class);
        String storeName = getStorageName(unitOfWork);

        fireBeforeDetachEntityListener(entity, storeName);

        ContainerResourceHolder container = getInstanceContainerResourceHolder(storeName);
        container.unregisterInstance(entity, unitOfWork, storeName);
        if (getEntityEntry(entity).isNew()) {
            container.getNewDetachedInstances().add(entity);
        }

        makeDetached(entity);
    }

    protected void makeDetached(Object instance) {
        if (instance instanceof Entity) {
            getUncheckedEntityEntry(instance).setNew(false);
            getUncheckedEntityEntry(instance).setManaged(false);
            getUncheckedEntityEntry(instance).setDetached(true);
        }
        if (instance instanceof FetchGroupTracker) {
            ((FetchGroupTracker) instance)._persistence_setSession(null);
        }
        if (instance instanceof ChangeTracker) {
            ((ChangeTracker) instance)._persistence_setPropertyChangeListener(null);
        }
    }

    protected void fireFlush(String storeName) {
        if (lifecycleListeners == null) {
            return;
        }
        for (JpaLifecycleListener listener : lifecycleListeners) {
            listener.onFlush(storeName);
        }
    }

    protected void fireEntityChange(Object entity, EntityOp entityOp, @Nullable AttributeChanges changes) {
        if (lifecycleListeners == null) {
            return;
        }
        for (JpaLifecycleListener listener : lifecycleListeners) {
            listener.onEntityChange(entity, entityOp, changes);
        }
    }

    public interface EntityVisitor {
        boolean visit(Object entity);
    }

    public static class ContainerResourceHolder extends ResourceHolderSupport {

        protected Map<String, Map<UnitOfWork, Set<Object>>> unitsOfWorkToStores = new HashMap<>();

        protected Set<Object> savedInstances = createEntitySet();

        protected Set<Object> newDetachedInstances = createEntitySet();

        protected String transactionManagerKey;

        protected Set<String> stores;

        public ContainerResourceHolder(String transactionManagerKey) {
            this.transactionManagerKey = transactionManagerKey;
            this.stores = new HashSet<>();
        }

        public String getTransactionManagerKey() {
            return transactionManagerKey;
        }

        public Set<String> getStores() {
            return stores;
        }

        public void addStore(String storeName) {
            stores.add(storeName);
        }

        protected void registerInstanceForUnitOfWork(Object instance, UnitOfWork unitOfWork, String store) {
            if (log.isTraceEnabled())
                log.trace("ContainerResourceHolder.registerInstanceForUnitOfWork: instance = " +
                        instance + ", UnitOfWork = " + unitOfWork);

            getEntityEntry(instance).setManaged(true);

            Map<UnitOfWork, Set<Object>> unitOfWorkMap = unitsOfWorkToStores.computeIfAbsent(store, s -> new HashMap<>());
            Set<Object> instances = unitOfWorkMap.computeIfAbsent(unitOfWork, u -> createEntitySet());
            instances.add(instance);
        }

        protected void unregisterInstance(Object instance, UnitOfWork unitOfWork, String store) {
            Map<UnitOfWork, Set<Object>> unitOfWorkMap = unitsOfWorkToStores.get(store);
            if (unitOfWorkMap != null) {
                Set<Object> instances = unitOfWorkMap.get(unitOfWork);
                if (instances != null) {
                    instances.remove(instance);
                }
            }
        }

        protected Collection<Object> getInstances(UnitOfWork unitOfWork, String store) {
            Set<Object> set = new HashSet<>();
            Map<UnitOfWork, Set<Object>> unitOfWorkMap = unitsOfWorkToStores.get(store);
            if (unitOfWorkMap != null) {
                Set<Object> entities = unitOfWorkMap.get(unitOfWork);
                if (entities != null) {
                    set.addAll(entities);
                }
            }
            return set;
        }

        protected Collection<Object> getStoreInstances(String store) {
            Set<Object> set = createEntitySet();
            Map<UnitOfWork, Set<Object>> unitOfWorkMap = unitsOfWorkToStores.get(store);
            if (unitOfWorkMap != null) {
                for (Set<Object> instances : unitOfWorkMap.values()) {
                    set.addAll(instances);
                }
            }
            return set;
        }

        protected Collection<Object> getAllInstances() {
            Set<Object> set = createEntitySet();
            for (Map<UnitOfWork, Set<Object>> unitOfWorkMap : unitsOfWorkToStores.values())
                for (Set<Object> instances : unitOfWorkMap.values()) {
                    set.addAll(instances);
                }
            return set;
        }

        protected Collection<Object> getSavedInstances() {
            return savedInstances;
        }

        public Set<Object> getNewDetachedInstances() {
            return newDetachedInstances;
        }

        @Override
        public String toString() {
            return "ContainerResourceHolder@" + Integer.toHexString(hashCode()) + "{" +
                    "transactionManagerKey='" + transactionManagerKey + '\'' +
                    '}';
        }
    }

    protected class ContainerResourceSynchronization
            extends ResourceHolderSynchronization<ContainerResourceHolder, String> implements Ordered {

        protected final ContainerResourceHolder container;

        public ContainerResourceSynchronization(ContainerResourceHolder resourceHolder, String resourceKey) {
            super(resourceHolder, resourceKey);
            this.container = resourceHolder;
        }

        @Override
        protected void cleanupResource(ContainerResourceHolder resourceHolder, String resourceKey, boolean committed) {
            resourceHolder.unitsOfWorkToStores.clear();
            resourceHolder.savedInstances.clear();
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            if (log.isTraceEnabled())
                log.trace("ContainerResourceSynchronization.beforeCommit: instances=" + container.getAllInstances() + ", readOnly=" + readOnly);

            if (!readOnly) {
                traverseEntities(container, new OnSaveEntityVisitor(container.getTransactionManagerKey()), false);
                for (String storeName : container.getStores()) {
                    fireFlush(storeName);
                }
            }

            Collection<Object> instances = container.getAllInstances();
            Set<String> typeNames = new HashSet<>();
            for (Object instance : instances) {
                if (instance instanceof Entity) {

                    if (readOnly) {
                        AttributeChangeListener changeListener =
                                (AttributeChangeListener) ((ChangeTracker) instance)._persistence_getPropertyChangeListener();
                        if (changeListener != null && changeListener.hasChanges())
                            throw new IllegalStateException("Changed instance " + instance + " in read-only transaction");
                    }

                    // if cache is enabled, the entity can have EntityFetchGroup instead of JmixEntityFetchGroup
                    if (instance instanceof FetchGroupTracker) {
                        FetchGroupTracker fetchGroupTracker = (FetchGroupTracker) instance;
                        FetchGroup fetchGroup = fetchGroupTracker._persistence_getFetchGroup();
                        if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                            fetchGroupTracker._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup, entityStates));
                    }
                    MetaClass metaClass = metadata.getClass(instance.getClass());
                    if (getEntityEntry(instance).isNew()) {
                        typeNames.add(metaClass.getName());
                    }
                    fireBeforeDetachEntityListener(instance, metaClass.getStore().getName());
                }
            }

            for (Object instance : container.getNewDetachedInstances()) {
                typeNames.add(metadata.getClass(instance).getName());
            }

            if (!readOnly) {
                for (String storeName : container.getStores()) {
                    Collection<Object> allInstances = container.getStoreInstances(storeName);
                    for (BeforeCommitTransactionListener transactionListener : beforeCommitTxListeners) {
                        transactionListener.beforeCommit(storeName, allInstances);
                    }
                }
                queryCacheManager.invalidate(typeNames);

                List<EntityChangedEventInfo> eventsInfo = entityChangedEventManager.collect(container.getAllInstances());

                detachAll();

                List<EntityChangedEvent> collectedEvents = new ArrayList<>(eventsInfo.size());
                for (EntityChangedEventInfo info : eventsInfo) {
                    collectedEvents.add(new EntityChangedEvent(info.getSource(),
                            Id.of(info.getEntity()), info.getType(), info.getChanges(), info.getOriginalMetaClass()));
                }

                publishEntityChangedEvents(collectedEvents);
            } else {
                detachAll();
            }
        }

        @Override
        public void afterCompletion(int status) {
            try {
                Collection<Object> instances = container.getAllInstances();
                if (log.isTraceEnabled())
                    log.trace("ContainerResourceSynchronization.afterCompletion: instances = " + instances);
                for (Object instance : instances) {
                    if (instance instanceof Entity) {
                        if (status == TransactionSynchronization.STATUS_COMMITTED) {
                            if (getEntityEntry(instance).isNew()) {
                                // new instances become not new and detached only if the transaction was committed
                                getEntityEntry(instance).setNew(false);
                            }
                        } else { // commit failed or the transaction was rolled back
                            makeDetached(instance);
                            for (Object entity : container.getNewDetachedInstances()) {
                                getEntityEntry(entity).setNew(true);
                                getEntityEntry(entity).setDetached(false);
                            }
                        }
                    }
                }
                for (AfterCompleteTransactionListener listener : afterCompleteTxListeners) {
                    listener.afterComplete(status == TransactionSynchronization.STATUS_COMMITTED, instances);
                }
            } finally {
                super.afterCompletion(status);
            }
        }

        private void detachAll() {
            Collection<Object> instances = container.getAllInstances();
            for (Object instance : instances) {
                if (getEntityEntry(instance).isNew()) {
                    container.getNewDetachedInstances().add(instance);
                }
            }

            for (String storeName : container.getStores()) {
                EntityManager jmixEm = storeAwareLocator.getEntityManager(storeName);
                JpaEntityManager jpaEm = jmixEm.unwrap(JpaEntityManager.class);
                jpaEm.flush();
                jpaEm.clear();
            }

            for (Object instance : instances) {
                makeDetached(instance);
            }
        }

        private void publishEntityChangedEvents(List<EntityChangedEvent> collectedEvents) {
            if (collectedEvents.isEmpty())
                return;

            List<TransactionSynchronization> synchronizationsBefore = new ArrayList<>(
                    TransactionSynchronizationManager.getSynchronizations());

            entityChangedEventManager.publish(collectedEvents);

            List<TransactionSynchronization> synchronizations = new ArrayList<>(
                    TransactionSynchronizationManager.getSynchronizations());

            if (synchronizations.size() > synchronizationsBefore.size()) {
                synchronizations.removeAll(synchronizationsBefore);
                for (TransactionSynchronization synchronization : synchronizations) {
                    synchronization.beforeCommit(false);
                }
            }
        }

        @Override
        public int getOrder() {
            return 100;
        }
    }

    protected class OnSaveEntityVisitor implements EntityVisitor {

        private String transactionManagerKey;

        public OnSaveEntityVisitor(String transactionManagerKey) {
            this.transactionManagerKey = transactionManagerKey;
        }

        @Override
        public boolean visit(Object entity) {
            if (getEntityEntry(entity).isNew()
                    && !getSavedInstancesByTransactionManager(transactionManagerKey).contains(entity)) {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_INSERT, transactionManagerKey);

                fireEntityChange(entity, EntityOp.CREATE, null);

                jpaCacheSupport.evictMasterEntity(entity, null);
                return true;
            }


            AttributeChangeListener changeListener =
                    (AttributeChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener();

            if (changeListener == null)
                return false;

            AttributeChanges changes = attributeChangesProvider.getAttributeChanges(entity);

            if (isDeleted(entity, changeListener)) {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_DELETE, transactionManagerKey);

                fireEntityChange(entity, EntityOp.DELETE, null);

                if (EntityValues.isSoftDeletionSupported(entity))
                    processDeletePolicy(entity);

                jpaCacheSupport.evictMasterEntity(entity, null);
                return true;

            } else if (changes.hasChanges()) {
                if (changeListener.hasChanges()) {
                    entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_UPDATE, transactionManagerKey);
                }

                // add changes after listener
                changes = AttributeChanges.Builder.ofChanges(changes)
                        .mergeChanges(attributeChangesProvider.getAttributeChanges(entity))
                        .build();

                if (getEntityEntry(entity).isNew()) {

                    // it can happen if flush was performed, so the entity is still New but was saved
                    fireEntityChange(entity, EntityOp.CREATE, null);
                } else {
                    fireEntityChange(entity, EntityOp.UPDATE, changes);
                }

                jpaCacheSupport.evictMasterEntity(entity, changes);
                return true;
            }

            return false;
        }

        protected void processDeletePolicy(Object entity) {
            DeletePolicyProcessor processor = deletePolicyProcessorProvider.getObject(); // prototype
            processor.setEntity(entity);
            processor.process();
        }
    }
}
