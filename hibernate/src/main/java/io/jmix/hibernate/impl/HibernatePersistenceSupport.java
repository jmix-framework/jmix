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

package io.jmix.hibernate.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.common.util.StackTrace;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.impl.*;
import io.jmix.data.impl.entitycache.QueryCacheManager;
import io.jmix.data.listener.AfterCompleteTransactionListener;
import io.jmix.data.listener.BeforeCommitTransactionListener;
import org.hibernate.Session;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
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

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;
import static io.jmix.core.entity.EntitySystemAccess.getUncheckedEntityEntry;

@Component("hibernatePersistenceSupport")
public class HibernatePersistenceSupport implements ApplicationContextAware {

    public static final String RESOURCE_HOLDER_KEY = ContainerResourceHolder.class.getName();

    public static final String RUNNER_RESOURCE_HOLDER = RunnerResourceHolder.class.getName();

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
    protected HibernateEntityChangedEventManager entityChangedEventManager;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected ObjectProvider<DeletePolicyProcessor> deletePolicyProcessorProvider;

    @Autowired
    protected HibernateChangesProvider changesProvider;

    protected List<BeforeCommitTransactionListener> beforeCommitTxListeners;

    protected List<AfterCompleteTransactionListener> afterCompleteTxListeners;

    private static final Logger log = LoggerFactory.getLogger(HibernatePersistenceSupport.class.getName());

    private Logger implicitFlushLog = LoggerFactory.getLogger("com.haulmont.cuba.IMPLICIT_FLUSH");

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
    public void registerSynchronizations(String store) {
        log.trace("registerSynchronizations for store '{}'", store);
        getInstanceContainerResourceHolder(store);
        getRunnerResourceHolder(store);
    }

    /**
     * INTERNAL
     */
    public void addBeforeCommitAction(String storeName, Runnable action) {
        RunnerResourceHolder runner = getRunnerResourceHolder(storeName);
        runner.add(action);
    }

    private RunnerResourceHolder getRunnerResourceHolder(String storeName) {
        RunnerResourceHolder runner = (RunnerResourceHolder) TransactionSynchronizationManager.getResource(RUNNER_RESOURCE_HOLDER);
        if (runner == null) {
            runner = new RunnerResourceHolder(storeName);
            TransactionSynchronizationManager.bindResource(RUNNER_RESOURCE_HOLDER, runner);
        } else if (!storeName.equals(runner.getStoreName())) {
            throw new IllegalStateException("Cannot handle entity from " + storeName
                    + " datastore because active transaction is for " + runner.getStoreName());
        }
        if (TransactionSynchronizationManager.isSynchronizationActive() && !runner.isSynchronizedWithTransaction()) {
            runner.setSynchronizedWithTransaction(true);
            TransactionSynchronizationManager.registerSynchronization(new RunnerSynchronization(runner));
        }
        return runner;
    }

    public void registerInstance(Object entity, EntityManager entityManager) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        Session unitOfWork = entityManager.unwrap(Session.class);
        getInstanceContainerResourceHolder(getStorageName(unitOfWork)).registerInstanceForUnitOfWork(entity, unitOfWork);

        getEntityEntry(entity).setDetached(false);
    }

    public void registerInstance(Object entity, Session session) {
        // Can be called outside of a transaction when fetching lazy attributes
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            return;

        if (session == null)
            throw new RuntimeException("Session is not a UnitOfWork: " + session);

        getInstanceContainerResourceHolder(getStorageName(session)).registerInstanceForUnitOfWork(entity, session);
    }

    public Collection<Object> getInstances(EntityManager entityManager) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        Session unitOfWork = entityManager.unwrap(Session.class);
        return getInstanceContainerResourceHolder(getStorageName(unitOfWork)).getInstances(unitOfWork);
    }

    public Collection<Object> getSavedInstances(String storeName) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        return getInstanceContainerResourceHolder(storeName).getSavedInstances();
    }

    public String getStorageName(Session session) {
        String storeName = (String) session.getProperties().get(PersistenceUnitProperties.STORE_NAME_PROPERTY);
        return Strings.isNullOrEmpty(storeName) ? Stores.MAIN : storeName;
    }

    public ContainerResourceHolder getInstanceContainerResourceHolder(String storeName) {
        ContainerResourceHolder holder =
                (ContainerResourceHolder) TransactionSynchronizationManager.getResource(RESOURCE_HOLDER_KEY);
        if (holder == null) {
            holder = new ContainerResourceHolder(storeName);
            TransactionSynchronizationManager.bindResource(RESOURCE_HOLDER_KEY, holder);
        } else if (!storeName.equals(holder.getStoreName())) {
            throw new IllegalStateException("Cannot handle entity from " + storeName
                    + " datastore because active transaction is for " + holder.getStoreName());
        }

        if (TransactionSynchronizationManager.isSynchronizationActive() && !holder.isSynchronizedWithTransaction()) {
            holder.setSynchronizedWithTransaction(true);
            TransactionSynchronizationManager.registerSynchronization(
                    new ContainerResourceSynchronization(holder, RESOURCE_HOLDER_KEY));
        }
        return holder;
    }

    public void processFlush(EntityManager entityManager, boolean warnAboutImplicitFlush) {
        Session unitOfWork = entityManager.unwrap(Session.class);
        String storeName = getStorageName(unitOfWork);
        traverseEntities(getInstanceContainerResourceHolder(storeName), new OnSaveEntityVisitor(storeName), warnAboutImplicitFlush);
    }

    protected void fireBeforeDetachEntityListener(Object entity, String storeName) {
        if (!getEntityEntry(entity).isDetached()) {
//            JmixEntityFetchGroup.setAccessLocalUnfetched(false);
            try {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_DETACH, storeName);
            } finally {
//                JmixEntityFetchGroup.setAccessLocalUnfetched(true);
            }
        }
    }

//    protected boolean isDeleted(Object entity, AttributeChangeListener changeListener) {
//        if (EntityValues.isSoftDeletionSupported(entity)) {
//            ObjectChangeSet changeSet = changeListener.getObjectChangeSet();
//            return changeSet != null
//                    && changeSet.getAttributesToChanges().containsKey(metadataTools.findDeletedDateProperty(entity.getClass()))
//                    && EntityValues.isSoftDeleted(entity);
//
//        } else {
//            return getEntityEntry(entity).isRemoved();
//        }
//    }

    protected void traverseEntities(ContainerResourceHolder container, EntityVisitor visitor, boolean warnAboutImplicitFlush) {
        beforeStore(container, visitor, container.getAllInstances(), createEntitySet(), warnAboutImplicitFlush);
    }

    protected void beforeStore(ContainerResourceHolder container, EntityVisitor visitor,
                               Collection<Object> instances, Set<Object> processed, boolean warnAboutImplicitFlush) {
        boolean possiblyChanged = false;
        Set<Object> withoutPossibleChanges = createEntitySet();
        for (Object entity : instances) {
            processed.add(entity);

//            if (!(entity instanceof ChangeTracker))
//                continue;

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
//                        ChangeTracker changeTracker = (ChangeTracker) instance;
//                        AttributeChangeListener changeListener =
//                                (AttributeChangeListener) changeTracker._persistence_getPropertyChangeListener();
                        return true;
                    })
                    .collect(Collectors.toList());
            if (!afterProcessing.isEmpty()) {
                beforeStore(container, visitor, afterProcessing, processed, false);
            }
        }
    }

    public void detach(EntityManager entityManager, Object entity) {
        Session unitOfWork = entityManager.unwrap(Session.class);
        String storeName = getStorageName(unitOfWork);

        fireBeforeDetachEntityListener(entity, storeName);

        ContainerResourceHolder container = getInstanceContainerResourceHolder(storeName);
        container.unregisterInstance(entity, unitOfWork);
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
//        if (instance instanceof FetchGroupTracker) {
//            ((FetchGroupTracker) instance)._persistence_setSession(null);
//        }
//        if (instance instanceof ChangeTracker) {
//            ((ChangeTracker) instance)._persistence_setPropertyChangeListener(null);
//        }
    }

    protected void fireFlush(String storeName) {
//        if (lifecycleListeners == null) {
//            return;
//        }
//        for (JpaDataStoreListener listener : lifecycleListeners) {
//            listener.onFlush(storeName);
//        }
    }

    public boolean isDeleted(Object entity, EntityEntry entry) {
        if (entry != null && EntityValues.isSoftDeletionSupported(entity)) {
            String deletedDateProperty = metadataTools.findDeletedDateProperty(entity.getClass());
            Object deleteDate = EntityValues.getValue(entity, deletedDateProperty);
            Object originalDeleteDate = entry.getLoadedValue(deletedDateProperty);
            return deleteDate != null
                    && originalDeleteDate == null
                    && EntityValues.isSoftDeleted(entity);

        } else {
            return getEntityEntry(entity).isRemoved();
        }
    }

    public interface EntityVisitor {
        boolean visit(Object entity);
    }

    public static class ContainerResourceHolder extends ResourceHolderSupport {

        protected Map<Session, Set<Object>> unitOfWorkMap = new HashMap<>();

        protected Set<Object> savedInstances = createEntitySet();

        protected Set<Object> newDetachedInstances = createEntitySet();

        protected String storeName;

        public ContainerResourceHolder(String storeName) {
            this.storeName = storeName;
        }

        public String getStoreName() {
            return storeName;
        }

        protected void registerInstanceForUnitOfWork(Object instance, Session unitOfWork) {
            if (log.isTraceEnabled())
                log.trace("ContainerResourceHolder.registerInstanceForUnitOfWork: instance = " +
                        instance + ", UnitOfWork = " + unitOfWork);

            getEntityEntry(instance).setManaged(true);

            Set<Object> instances = unitOfWorkMap.get(unitOfWork);
            if (instances == null) {
                instances = createEntitySet();
                unitOfWorkMap.put(unitOfWork, instances);
            }
            instances.add(instance);
        }

        protected void unregisterInstance(Object instance, Session unitOfWork) {
            Set<Object> instances = unitOfWorkMap.get(unitOfWork);
            if (instances != null) {
                instances.remove(instance);
            }
        }

        protected Collection<Object> getInstances(Session unitOfWork) {
            Set<Object> set = new HashSet<>();
            Set<Object> entities = unitOfWorkMap.get(unitOfWork);
            if (entities != null)
                set.addAll(entities);
            return set;
        }

        protected Collection<Object> getAllInstances() {
            Set<Object> set = createEntitySet();
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
                    "storeName='" + storeName + '\'' +
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
            resourceHolder.unitOfWorkMap.clear();
            resourceHolder.savedInstances.clear();
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            if (log.isTraceEnabled())
                log.trace("ContainerResourceSynchronization.beforeCommit: instances=" + container.getAllInstances() + ", readOnly=" + readOnly);

            if (!readOnly) {
                traverseEntities(container, new OnSaveEntityVisitor(container.getStoreName()), false);
                fireFlush(container.getStoreName());
            }

            Collection<Object> instances = container.getAllInstances();
            Set<String> typeNames = new HashSet<>();
            for (Object instance : instances) {
                if (instance instanceof Entity) {

                    if (readOnly) {
//                        AttributeChangeListener changeListener =
//                                (AttributeChangeListener) ((ChangeTracker) instance)._persistence_getPropertyChangeListener();
//                        if (changeListener != null && changeListener.hasChanges())
//                            throw new IllegalStateException("Changed instance " + instance + " in read-only transaction");
                    }

                    // if cache is enabled, the entity can have EntityFetchGroup instead of JmixEntityFetchGroup
//                    if (instance instanceof FetchGroupTracker) {
//                        FetchGroupTracker fetchGroupTracker = (FetchGroupTracker) instance;
//                        FetchGroup fetchGroup = fetchGroupTracker._persistence_getFetchGroup();
//                        if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
//                            fetchGroupTracker._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup, entityStates));
//                    }
                    if (getEntityEntry(instance).isNew()) {
                        typeNames.add(metadata.getClass(instance).getName());
                    }
                    fireBeforeDetachEntityListener(instance, container.getStoreName());
                }
            }

            if (!readOnly) {
                Collection<Object> allInstances = container.getAllInstances();
                for (BeforeCommitTransactionListener transactionListener : beforeCommitTxListeners) {
                    transactionListener.beforeCommit(container.getStoreName(), allInstances);
                }
                queryCacheManager.invalidate(typeNames);

                List<EntityChangedEventInfo> eventsInfo = entityChangedEventManager.collect((SessionImplementor) storeAwareLocator.getEntityManager(container.getStoreName()), container.getAllInstances());

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

            EntityManager jmixEm = storeAwareLocator.getEntityManager(container.getStoreName());
            Session jpaEm = jmixEm.unwrap(Session.class);
            jpaEm.flush();
            jpaEm.clear();

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

        private String storeName;

        public OnSaveEntityVisitor(String storeName) {
            this.storeName = storeName;
        }

        @Override
        public boolean visit(Object entity) {
            if (getEntityEntry(entity).isNew()
                    && !getSavedInstances(storeName).contains(entity)) {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_INSERT, storeName);
                return true;
            }

            EntityEntry entry = HibernateUtils.getEntityEntry(
                    (SessionImplementor) storeAwareLocator.getEntityManager(storeName), entity);
            if (entry == null)
                return false;

            if (isDeleted(entity, entry)) {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_DELETE, storeName);

                if (EntityValues.isSoftDeletionSupported(entity))
                    processDeletePolicy(entity);

                return true;

            } else if (changesProvider.hasChanges(entity, entry)) {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_UPDATE, storeName);
                return true;
            }

            return false;
        }

        // todo fts
//        protected void enqueueForFts(Entity entity, FtsChangeType changeType) {
//            if (!FtsConfigHelper.getEnabled())
//                return;
//            try {
//                if (ftsSender == null) {
//                    if (AppBeans.containsBean(FtsSender.NAME)) {
//                        ftsSender = AppBeans.get(FtsSender.NAME);
//                    } else {
//                        log.error("Error enqueueing changes for FTS: " + FtsSender.NAME + " bean not found");
//                    }
//                }
//                if (ftsSender != null)
//                    ftsSender.enqueue(entity, changeType);
//            } catch (Exception e) {
//                log.error("Error enqueueing changes for FTS", e);
//            }
//        }

        protected void processDeletePolicy(Object entity) {
            DeletePolicyProcessor processor = deletePolicyProcessorProvider.getObject(); // prototype
            processor.setEntity(entity);
            processor.process();
        }
    }

    private static class RunnerResourceHolder extends ResourceHolderSupport {

        private List<Runnable> list = new ArrayList<>();
        private String storeName;

        public RunnerResourceHolder(String storeName) {
            this.storeName = storeName;
        }

        public String getStoreName() {
            return storeName;
        }

        private void add(Runnable action) {
            list.add(action);
        }

        private void run() {
            for (Runnable runnable : list) {
                runnable.run();
            }
        }
    }

    private static class RunnerSynchronization extends ResourceHolderSynchronization<RunnerResourceHolder, String> {

        private RunnerResourceHolder runner;

        public RunnerSynchronization(RunnerResourceHolder runner) {
            super(runner, RUNNER_RESOURCE_HOLDER);
            this.runner = runner;
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            runner.run();
        }
    }
}
