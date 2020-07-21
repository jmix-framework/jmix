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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.jmix.core.JmixEntity;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.common.util.StackTrace;
import io.jmix.core.entity.SoftDelete;
import io.jmix.data.EntityChangeType;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.event.EntityChangedEvent;
import io.jmix.data.impl.entitycache.QueryCacheManager;
import io.jmix.data.listener.AfterCompleteTransactionListener;
import io.jmix.data.listener.BeforeCommitTransactionListener;
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

@Component(PersistenceSupport.NAME)
public class PersistenceSupport implements ApplicationContextAware {

    public static final String NAME = "data_PersistenceImplSupport";

    public static final String RESOURCE_HOLDER_KEY = ContainerResourceHolder.class.getName();

    public static final String RUNNER_RESOURCE_HOLDER = RunnerResourceHolder.class.getName();

    public static final String PROP_NAME = "jmix.storeName";

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected EntityListenerManager entityListenerManager;

    @Autowired
    protected QueryCacheManager queryCacheManager;

    @Autowired
    protected OrmCacheSupport ormCacheSupport;

    @Autowired
    protected EntityChangedEventManager entityChangedEventManager;

    @Autowired(required = false)
    protected List<OrmLifecycleListener> lifecycleListeners = new ArrayList<>();

    @Autowired
    protected ObjectProvider<DeletePolicyProcessor> deletePolicyProcessorProvider;

    protected List<BeforeCommitTransactionListener> beforeCommitTxListeners;

    protected List<AfterCompleteTransactionListener> afterCompleteTxListeners;

    private static final Logger log = LoggerFactory.getLogger(PersistenceSupport.class.getName());

    private Logger implicitFlushLog = LoggerFactory.getLogger("com.haulmont.cuba.IMPLICIT_FLUSH");

    protected static Set<JmixEntity> createEntitySet() {
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

    public void registerInstance(JmixEntity entity, javax.persistence.EntityManager entityManager) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        UnitOfWork unitOfWork = entityManager.unwrap(UnitOfWork.class);
        getInstanceContainerResourceHolder(getStorageName(unitOfWork)).registerInstanceForUnitOfWork(entity, unitOfWork);

        entity.__getEntityEntry().setDetached(false);
    }

    public void registerInstance(JmixEntity entity, AbstractSession session) {
        // Can be called outside of a transaction when fetching lazy attributes
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            return;

        if (!(session instanceof UnitOfWork))
            throw new RuntimeException("Session is not a UnitOfWork: " + session);

        getInstanceContainerResourceHolder(getStorageName(session)).registerInstanceForUnitOfWork(entity, (UnitOfWork) session);
    }

    public Collection<JmixEntity> getSavedInstances(String storeName) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new RuntimeException("No transaction");

        return getInstanceContainerResourceHolder(storeName).getSavedInstances();
    }

    public String getStorageName(Session session) {
        String storeName = (String) session.getProperty(PROP_NAME);
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

    public void processFlush(javax.persistence.EntityManager entityManager, boolean warnAboutImplicitFlush) {
        UnitOfWork unitOfWork = entityManager.unwrap(UnitOfWork.class);
        String storeName = getStorageName(unitOfWork);
        traverseEntities(getInstanceContainerResourceHolder(storeName), new OnSaveEntityVisitor(storeName), warnAboutImplicitFlush);
    }

    protected void fireBeforeDetachEntityListener(JmixEntity entity, String storeName) {
        if (!(entity.__getEntityEntry().isDetached())) {
            JmixEntityFetchGroup.setAccessLocalUnfetched(false);
            try {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_DETACH, storeName);
            } finally {
                JmixEntityFetchGroup.setAccessLocalUnfetched(true);
            }
        }
    }

    protected static boolean isDeleted(JmixEntity entity, AttributeChangeListener changeListener) {
        if ((entity instanceof SoftDelete)) {
            ObjectChangeSet changeSet = changeListener.getObjectChangeSet();
            return changeSet != null
                    && changeSet.getAttributesToChanges().containsKey("deleteTs")
                    && ((SoftDelete) entity).isDeleted();

        } else {
            return entity.__getEntityEntry().isRemoved();
        }
    }

    protected void traverseEntities(ContainerResourceHolder container, EntityVisitor visitor, boolean warnAboutImplicitFlush) {
        beforeStore(container, visitor, container.getAllInstances(), createEntitySet(), warnAboutImplicitFlush);
    }

    protected void beforeStore(ContainerResourceHolder container, EntityVisitor visitor,
                               Collection<JmixEntity> instances, Set<JmixEntity> processed, boolean warnAboutImplicitFlush) {
        boolean possiblyChanged = false;
        Set<JmixEntity> withoutPossibleChanges = createEntitySet();
        for (JmixEntity entity : instances) {
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

        Collection<JmixEntity> afterProcessing = container.getAllInstances();
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

    public void detach(javax.persistence.EntityManager entityManager, JmixEntity entity) {
        UnitOfWork unitOfWork = entityManager.unwrap(UnitOfWork.class);
        String storeName = getStorageName(unitOfWork);

        fireBeforeDetachEntityListener(entity, storeName);

        ContainerResourceHolder container = getInstanceContainerResourceHolder(storeName);
        container.unregisterInstance(entity, unitOfWork);
        if (entity.__getEntityEntry().isNew()) {
            container.getNewDetachedInstances().add(entity);
        }

        makeDetached(entity);
    }

    protected void makeDetached(Object instance) {
        if (instance instanceof JmixEntity) {
            ((JmixEntity) instance).__getEntityEntry().setNew(false);
            ((JmixEntity) instance).__getEntityEntry().setManaged(false);
            ((JmixEntity) instance).__getEntityEntry().setDetached(true);
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
        for (OrmLifecycleListener listener : lifecycleListeners) {
            listener.onFlush(storeName);
        }
    }

    protected void fireEntityChange(JmixEntity entity, EntityChangeType type, @Nullable EntityAttributeChanges changes) {
        if (lifecycleListeners == null) {
            return;
        }
        for (OrmLifecycleListener listener : lifecycleListeners) {
            listener.onEntityChange(entity, type, changes);
        }
    }

    public interface EntityVisitor {
        boolean visit(JmixEntity entity);
    }

    public static class ContainerResourceHolder extends ResourceHolderSupport {

        protected Map<UnitOfWork, Set<JmixEntity>> unitOfWorkMap = new HashMap<>();

        protected Set<JmixEntity> savedInstances = createEntitySet();

        protected Set<JmixEntity> newDetachedInstances = createEntitySet();

        protected String storeName;

        public ContainerResourceHolder(String storeName) {
            this.storeName = storeName;
        }

        public String getStoreName() {
            return storeName;
        }

        protected void registerInstanceForUnitOfWork(JmixEntity instance, UnitOfWork unitOfWork) {
            if (log.isTraceEnabled())
                log.trace("ContainerResourceHolder.registerInstanceForUnitOfWork: instance = " +
                        instance + ", UnitOfWork = " + unitOfWork);

            instance.__getEntityEntry().setManaged(true);

            Set<JmixEntity> instances = unitOfWorkMap.get(unitOfWork);
            if (instances == null) {
                instances = createEntitySet();
                unitOfWorkMap.put(unitOfWork, instances);
            }
            instances.add(instance);
        }

        protected void unregisterInstance(JmixEntity instance, UnitOfWork unitOfWork) {
            Set<JmixEntity> instances = unitOfWorkMap.get(unitOfWork);
            if (instances != null) {
                instances.remove(instance);
            }
        }

        protected Collection<JmixEntity> getInstances(UnitOfWork unitOfWork) {
            HashSet<JmixEntity> set = new HashSet<>();
            Set<JmixEntity> entities = unitOfWorkMap.get(unitOfWork);
            if (entities != null)
                set.addAll(entities);
            return set;
        }

        protected Collection<JmixEntity> getAllInstances() {
            Set<JmixEntity> set = createEntitySet();
            for (Set<JmixEntity> instances : unitOfWorkMap.values()) {
                set.addAll(instances);
            }
            return set;
        }

        protected Collection<JmixEntity> getSavedInstances() {
            return savedInstances;
        }

        public Set<JmixEntity> getNewDetachedInstances() {
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

            Collection<JmixEntity> instances = container.getAllInstances();
            Set<String> typeNames = new HashSet<>();
            for (Object instance : instances) {
                if (instance instanceof JmixEntity) {
                    JmixEntity entity = (JmixEntity) instance;

                    if (readOnly) {
                        AttributeChangeListener changeListener =
                                (AttributeChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener();
                        if (changeListener != null && changeListener.hasChanges())
                            throw new IllegalStateException("Changed instance " + entity + " in read-only transaction");
                    }

                    // if cache is enabled, the entity can have EntityFetchGroup instead of JmixEntityFetchGroup
                    if (instance instanceof FetchGroupTracker) {
                        FetchGroupTracker fetchGroupTracker = (FetchGroupTracker) entity;
                        FetchGroup fetchGroup = fetchGroupTracker._persistence_getFetchGroup();
                        if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                            fetchGroupTracker._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup));
                    }
                    if (entity.__getEntityEntry().isNew()) {
                        typeNames.add(metadata.getClass(entity).getName());
                    }
                    fireBeforeDetachEntityListener(entity, container.getStoreName());
                }
            }

            if (!readOnly) {
                Collection<JmixEntity> allInstances = container.getAllInstances();
                for (BeforeCommitTransactionListener transactionListener : beforeCommitTxListeners) {
                    transactionListener.beforeCommit(container.getStoreName(), allInstances);
                }
                queryCacheManager.invalidate(typeNames, true);
                List<EntityChangedEvent> collectedEvents = entityChangedEventManager.collect(container.getAllInstances());
                detachAll();
                publishEntityChangedEvents(collectedEvents);
            } else {
                detachAll();
            }
        }

        @Override
        public void afterCompletion(int status) {
            try {
                Collection<JmixEntity> instances = container.getAllInstances();
                if (log.isTraceEnabled())
                    log.trace("ContainerResourceSynchronization.afterCompletion: instances = " + instances);
                for (Object instance : instances) {
                    if (instance instanceof JmixEntity) {
                        if (status == TransactionSynchronization.STATUS_COMMITTED) {
                            if (((JmixEntity) instance).__getEntityEntry().isNew()) {
                                // new instances become not new and detached only if the transaction was committed
                                ((JmixEntity) instance).__getEntityEntry().setNew(false);
                            }
                        } else { // commit failed or the transaction was rolled back
                            makeDetached(instance);
                            for (JmixEntity entity : container.getNewDetachedInstances()) {
                                entity.__getEntityEntry().setNew(true);
                                entity.__getEntityEntry().setDetached(false);
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
            Collection<JmixEntity> instances = container.getAllInstances();
            for (JmixEntity instance : instances) {
                if (instance.__getEntityEntry().isNew()) {
                    container.getNewDetachedInstances().add((JmixEntity) instance);
                }
            }

            EntityManager jmixEm = storeAwareLocator.getEntityManager(container.getStoreName());
            JpaEntityManager jpaEm = jmixEm.unwrap(JpaEntityManager.class);
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
        public boolean visit(JmixEntity entity) {
            if (entity.__getEntityEntry().isNew()
                    && !getSavedInstances(storeName).contains(entity)) {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_INSERT, storeName);

                fireEntityChange(entity, EntityChangeType.CREATE, null);

                // todo fts
//                enqueueForFts(entity, FtsChangeType.INSERT);

                ormCacheSupport.evictMasterEntity(entity, null);
                return true;
            }

            AttributeChangeListener changeListener =
                    (AttributeChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener();
            if (changeListener == null)
                return false;

            if (isDeleted(entity, changeListener)) {
                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_DELETE, storeName);

                fireEntityChange(entity, EntityChangeType.DELETE, null);

                if (entity instanceof SoftDelete)
                    processDeletePolicy(entity);

                // todo fts
//                enqueueForFts(entity, FtsChangeType.DELETE);

                ormCacheSupport.evictMasterEntity(entity, null);
                return true;

            } else if (changeListener.hasChanges()) {

                EntityAttributeChanges changes = new EntityAttributeChanges();
                // add changes before listener
                changes.addChanges(entity);

                entityListenerManager.fireListener(entity, EntityListenerType.BEFORE_UPDATE, storeName);

                // add changes after listener
                changes.addChanges(entity);

                if (entity.__getEntityEntry().isNew()) {

                    // it can happen if flush was performed, so the entity is still New but was saved
                    fireEntityChange(entity, EntityChangeType.CREATE, null);

                    // todo fts
//                    enqueueForFts(entity, FtsChangeType.INSERT);
                } else {
                    fireEntityChange(entity, EntityChangeType.UPDATE, changes);

                    // todo fts
//                    enqueueForFts(entity, FtsChangeType.UPDATE);
                }

                ormCacheSupport.evictMasterEntity(entity, changes);
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

        protected void processDeletePolicy(JmixEntity entity) {
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
