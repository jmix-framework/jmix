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

package io.jmix.hibernate.impl.lazyloading;

import io.jmix.core.*;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.CollectionDataAccess;
import org.hibernate.cache.spi.entry.CollectionCacheEntry;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.CacheHelper;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.InitializeCollectionEvent;
import org.hibernate.event.spi.InitializeCollectionEventListener;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class JmixInitializeCollectionEventListener implements InitializeCollectionEventListener {

    private static final CoreMessageLogger LOG = CoreLogging.messageLogger(JmixInitializeCollectionEventListener.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected MeterRegistry meterRegistry;

    @Override
    public void onInitializeCollection(InitializeCollectionEvent event) throws HibernateException {
        PersistentCollection collection = event.getCollection();
        SessionImplementor source = event.getSession();

        CollectionEntry ce = source.getPersistenceContextInternal().getCollectionEntry(collection);
        if (ce == null) {
            throw new HibernateException("collection was evicted");
        }
        if (!collection.wasInitialized()) {
            final CollectionPersister ceLoadedPersister = ce.getLoadedPersister();
            if (LOG.isTraceEnabled()) {
                LOG.tracev(
                        "Initializing collection {0}",
                        MessageHelper.collectionInfoString(
                                ceLoadedPersister,
                                collection,
                                ce.getLoadedKey(),
                                source
                        )
                );
                LOG.trace("Checking second-level cache");
            }

            final boolean foundInCache = initializeCollectionFromCache(
                    ce.getLoadedKey(),
                    ceLoadedPersister,
                    collection,
                    source
            );

            if (foundInCache) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Collection initialized from cache");
                }
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Collection not cached");
                }
                Object owner = collection.getOwner();
                LoadOptionsState loadOptionsState = EntitySystemAccess.getExtraState(owner, LoadOptionsState.class);
                String property = getPropertyName(collection);
                boolean loadedByDataManager = false;
                if (entityStates.isDetached(owner) && loadOptionsState != null) {
                    Timer.Sample timer = Timer.start(meterRegistry);
                    try {
                        collection.beforeInitialize(ceLoadedPersister, -1);
                        collection.afterInitialize();

                        LoadContext<?> lc = createLoadContext((Entity) owner, property, loadOptionsState);
                        Object reloadedOwner = dataManager.load(lc);

                        PersistentCollection value = EntityValues.getValue(reloadedOwner, property);
                        loadedByDataManager = PersistentCollectionSupport.fillInternalCollection(collection, value);
                        if (loadedByDataManager) {
                            EntitySystemAccess.getSecurityState(owner).addErasedIds(property,
                                    EntitySystemAccess.getSecurityState(reloadedOwner).getErasedIds(property));
                        }
                    } finally {
                        timer.stop(meterRegistry.timer("jmix.JmixInitializeCollectionEventListener",
                                "entity", owner.getClass().getSimpleName(),
                                "property", property));
                    }
                }
                if (!loadedByDataManager) {
                    ceLoadedPersister.initialize(ce.getLoadedKey(), source);

                    final StatisticsImplementor statistics = source.getFactory().getStatistics();
                    if (statistics.isStatisticsEnabled()) {
                        statistics.fetchCollection(
                                ceLoadedPersister.getRole()
                        );
                    }
                }
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Collection initialized");
                }
            }
        }
    }

    protected String getPropertyName(PersistentCollection collection) {
        String role = collection.getRole();
        if (role == null) {
            throw new RuntimeException("Empty role in PersistentCollection");
        }
        int substringIndex = role.lastIndexOf(".") + 1;
        return substringIndex > 0 ? role.substring(substringIndex) : role;
    }

    protected LoadContext<?> createLoadContext(Entity owner, String property, LoadOptionsState state) {
        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(owner.getClass());
        Object id = EntitySystemAccess.getEntityEntry(owner).getEntityId();
        return new LoadContext<>(metaClass)
                .setId(id)
                .setFetchPlan(
                        fetchPlans.builder(metaClass.getJavaClass())
                                .add(property, builder -> builder.addFetchPlan(FetchPlan.BASE))
                                .build())
                .setSoftDeletion(state.isSoftDeletion())
                .setAccessConstraints(state.getAccessConstraints())
                .setHints(state.getHints());
    }

    /**
     * Try to initialize a collection from the cache
     *
     * @param id         The id of the collection to initialize
     * @param persister  The collection persister
     * @param collection The collection to initialize
     * @param source     The originating session
     * @return true if we were able to initialize the collection from the cache;
     * false otherwise.
     */
    private boolean initializeCollectionFromCache(
            Serializable id,
            CollectionPersister persister,
            PersistentCollection collection,
            SessionImplementor source) {

        if (source.getLoadQueryInfluencers().hasEnabledFilters()
                && persister.isAffectedByEnabledFilters(source)) {
            LOG.trace("Disregarding cached version (if any) of collection due to enabled filters");
            return false;
        }

        final boolean useCache = persister.hasCache() && source.getCacheMode().isGetEnabled();

        if (!useCache) {
            return false;
        }

        final SessionFactoryImplementor factory = source.getFactory();
        final CollectionDataAccess cacheAccessStrategy = persister.getCacheAccessStrategy();
        final Object ck = cacheAccessStrategy.generateCacheKey(id, persister, factory, source.getTenantIdentifier());
        final Object ce = CacheHelper.fromSharedCache(source, ck, cacheAccessStrategy);

        final StatisticsImplementor statistics = factory.getStatistics();
        if (statistics.isStatisticsEnabled()) {
            if (ce == null) {
                statistics.collectionCacheMiss(
                        persister.getNavigableRole(),
                        cacheAccessStrategy.getRegion().getName()
                );
            } else {
                statistics.collectionCacheHit(
                        persister.getNavigableRole(),
                        cacheAccessStrategy.getRegion().getName()
                );
            }
        }

        if (ce == null) {
            return false;
        }

        CollectionCacheEntry cacheEntry = (CollectionCacheEntry) persister.getCacheEntryStructure().destructure(
                ce,
                factory
        );

        final PersistenceContext persistenceContext = source.getPersistenceContextInternal();
        cacheEntry.assemble(collection, persister, persistenceContext.getCollectionOwner(id, persister));
        persistenceContext.getCollectionEntry(collection).postInitialize(collection);
        // addInitializedCollection(collection, persister, id);
        return true;
    }
}
