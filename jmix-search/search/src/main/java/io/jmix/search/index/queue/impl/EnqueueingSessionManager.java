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

package io.jmix.search.index.queue.impl;

import io.jmix.core.DataManager;
import io.jmix.core.FluentLoader;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.index.*;
import io.jmix.search.index.impl.IndexLayout;
import io.jmix.search.index.impl.MultitenancyAdapter;
import io.jmix.search.index.impl.IndexingLocker;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.queue.entity.EnqueueingSession;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@NullMarked
@Component("search_EnqueueingSessionManager")
public class EnqueueingSessionManager {

    private static final Logger log = LoggerFactory.getLogger(EnqueueingSessionManager.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected IndexingLocker locker;
    @Autowired
    protected MultitenancyAdapter multitenancyAdapter;

    @Autowired
    protected IndexLayout indexLayout;

    /**
     * Initializes session for provided entity.
     * Existing session will be removed and created again.
     *
     * @param entityName entity name
     * @return tenantless or tenant-specific result of session initialization
     */
    public List<IndexOperationResult<IndexManipulationResult>> initSession(String entityName) {
        return applyForAllTenantsOrTenantless(entityName, this::initSessionInternal);
    }

    /**
     * Initializes session for provided entity and tenant.
     * Existing session will be removed and created again.
     *
     * @param entityName entity name
     * @param tenantId   tenant id
     * @return result of session initialization, one row per affected index
     * @throws IllegalArgumentException if the entity is not tenant-aware
     * @throws IllegalStateException    if multitenancy is not available
     */
    public List<IndexOperationResult<IndexManipulationResult>> initSession(String entityName, @Nullable String tenantId) {
        IndexManipulationResult result = initSessionInternal(entityName, tenantId);
        return toResult(entityName, tenantId, result);
    }

    protected IndexManipulationResult initSessionInternal(String entityName, @Nullable String tenantId) {
        return executeManagementAction(entityName, tenantId, 10000, () -> {
            EnqueueingSession existingSession = getSession(entityName, tenantId);
            if (existingSession != null) {
                dataManager.remove(existingSession);
            }

            EnqueueingSession effectiveSession = metadata.create(EnqueueingSession.class);

            MetaClass entityClass = metadata.getClass(entityName);
            MetaProperty orderingProperty = resolveOrderingProperty(entityClass);

            effectiveSession.setEntityName(entityName);
            effectiveSession.setTenantId(tenantId);
            effectiveSession.setStatus(EnqueueingSessionStatus.ACTIVE);
            effectiveSession.setOrderingProperty(orderingProperty.getName());
            effectiveSession.setLastProcessedValue(null);

            dataManager.save(effectiveSession);

            return IndexManipulationResult.SUCCESS;
        });
    }

    /**
     * Gets entity names of all existing enqueueing sessions.
     *
     * @return list of entity names
     */
    public List<String> loadEntityNamesOfSessions() {
        String queryString = "select distinct e.entityName from search_EnqueueingSession e";
        List<KeyValueEntity> loadedValues = dataManager.loadValues(queryString)
                .properties("entityName")
                .list();
        return loadedValues.stream()
                .map(v -> (String) v.getValue("entityName"))
                .collect(Collectors.toList());
    }

    public List<String> loadEntityNamesOfSessions(@Nullable String tenantId) {
        String queryString = "select distinct e.entityName from search_EnqueueingSession e";
        if (tenantId == null) {
            queryString += " where e.tenantId is null";
        } else {
            queryString += " where e.tenantId = :tenantId";
        }
        var valuesLoader = dataManager.loadValues(queryString).properties("entityName");
        if (tenantId != null) {
            valuesLoader.parameter("tenantId", tenantId);
        }
        List<KeyValueEntity> loadedValues = valuesLoader.list();
        return loadedValues.stream().map(v -> (String) v.getValue("entityName")).collect(Collectors.toList());
    }

    /**
     * Prevents session from being executed.
     *
     * @param entityName entity name
     * @return tenantless or tenant-specific result of session suspension
     */
    public List<IndexOperationResult<IndexManipulationResult>> suspendSession(String entityName) {
        return applyForAllTenantsOrTenantless(entityName, this::suspendSessionInternal);
    }

    /**
     * Prevents session from being executed for provided entity and tenant.
     *
     * @param entityName entity name
     * @param tenantId   tenant id
     * @return result of session suspension, one row per affected index
     * @throws IllegalArgumentException if the entity is not tenant-aware
     * @throws IllegalStateException    if multitenancy is not available
     */
    public List<IndexOperationResult<IndexManipulationResult>> suspendSession(String entityName,
                                                                           @Nullable String tenantId) {
        IndexManipulationResult result = suspendSessionInternal(entityName, tenantId);
        return toResult(entityName, tenantId, result);
    }

    protected IndexManipulationResult suspendSessionInternal(String entityName, @Nullable String tenantId) {
        return executeManagementAction(entityName, tenantId, 10000, () -> {
            EnqueueingSession session = getSession(entityName, tenantId);
            if (session != null) {
                if (EnqueueingSessionStatus.ACTIVE.equals(session.getStatus())) {
                    session.setStatus(EnqueueingSessionStatus.SUSPENDED);
                    dataManager.save(session);
                }
                return IndexManipulationResult.SUCCESS;
            }
            return IndexManipulationResult.FAILURE;
        });
    }

    /**
     * Resumes previously suspended session.
     *
     * @param entityName entity name
     * @return tenantless or tenant-specific result of session resumption
     */
    public List<IndexOperationResult<IndexManipulationResult>> resumeSession(String entityName) {
        return applyForAllTenantsOrTenantless(entityName, this::resumeSessionInternal);
    }

    /**
     * Resumes previously suspended session for provided entity and tenant.
     *
     * @param entityName entity name
     * @param tenantId   tenant id
     * @return result of session resumption, one row per affected index
     * @throws IllegalArgumentException if the entity is not tenant-aware
     * @throws IllegalStateException    if multitenancy is not available
     */
    public List<IndexOperationResult<IndexManipulationResult>> resumeSession(String entityName,
                                                                           @Nullable String tenantId) {
        IndexManipulationResult result = resumeSessionInternal(entityName, tenantId);
        return toResult(entityName, tenantId, result);
    }

    protected IndexManipulationResult resumeSessionInternal(String entityName, @Nullable String tenantId) {
        return executeManagementAction(entityName, tenantId, 10000, () -> {
            EnqueueingSession session = getSession(entityName, tenantId);
            if (session != null) {
                if (EnqueueingSessionStatus.SUSPENDED.equals(session.getStatus())) {
                    session.setStatus(EnqueueingSessionStatus.ACTIVE);
                    dataManager.save(session);
                }
                return IndexManipulationResult.SUCCESS;
            }
            return IndexManipulationResult.FAILURE;
        });
    }

    /**
     * Removes provided session.
     *
     * @param session session
     * @return result of session removal, one row per affected index
     */
    public List<IndexOperationResult<IndexManipulationResult>> removeSession(EnqueueingSession session) {
        String entityName = session.getEntityName();
        IndexManipulationResult result = executeManagementAction(entityName, session.getTenantId(), 10000, () -> {
            Optional<EnqueueingSession> currentSessionOpt = reloadSession(session);
            currentSessionOpt.ifPresent(currentSession -> dataManager.remove(currentSession));
            return IndexManipulationResult.SUCCESS;
        }, false);
        return toResult(entityName, session.getTenantId(), result);
    }

    /**
     * Removes session by provided entity name.
     *
     * @param entityName entity name
     * @return tenantless or tenant-specific result of session removal
     */
    public List<IndexOperationResult<IndexManipulationResult>> removeSession(String entityName) {
        return applyForAllTenantsOrTenantless(entityName, this::removeSessionInternal);
    }

    /**
     * Removes session by provided entity name and tenant.
     *
     * @param entityName entity name
     * @param tenantId   tenant id
     * @return result of session removal, one row per affected index
     * @throws IllegalArgumentException if the entity is not tenant-aware
     * @throws IllegalStateException    if multitenancy is not available
     */
    public List<IndexOperationResult<IndexManipulationResult>> removeSession(String entityName,
                                                                           @Nullable String tenantId) {
        IndexManipulationResult result = removeSessionInternal(entityName, tenantId);
        return toResult(entityName, tenantId, result);
    }

    /**
     * Removing a session is how an entity that left the indexed set gets its leftovers cleared, so it does not
     * demand that the entity still be indexed.
     */
    protected IndexManipulationResult removeSessionInternal(String entityName, @Nullable String tenantId) {
        return executeManagementAction(entityName, tenantId, 10000, () -> {
            EnqueueingSession session = loadEnqueueingSessionEntityByEntityName(entityName, tenantId).orElse(null);
            if (session != null) {
                dataManager.remove(session);
            }
            return IndexManipulationResult.SUCCESS;
        }, false);
    }

    /**
     * Gets session for provided entity.
     *
     * @param entityName entity name
     * @return existing session or null if it doesn't exist
     */
    @Nullable
    public EnqueueingSession getSession(String entityName) {
        if (!indexConfigurationManager.isDirectlyIndexed(entityName)) {
            throw new IllegalArgumentException(
                    String.format("Unable to get enqueuing session for non-indexed entity '%s'", entityName)
            );
        }
        if (indexLayout.isSplitByTenants(indexConfigurationManager.getIndexConfigurationByEntityName(entityName))) {
            throw new IllegalArgumentException(
                    String.format("Unable to get enqueuing session for tenant-aware entity '%s' without tenant id", entityName)
            );
        }
        return getSession(entityName, null);
    }

    @Nullable
    public EnqueueingSession getSession(String entityName, @Nullable String tenantId) {
        if (indexConfigurationManager.isDirectlyIndexed(entityName)) {
            validateTenantId(entityName, tenantId);
            Optional<EnqueueingSession> enqueueingSessionEntityOpt = loadEnqueueingSessionEntityByEntityName(entityName, tenantId);
            return enqueueingSessionEntityOpt.orElse(null);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unable to get enqueuing session for non-indexed entity '%s'", entityName)
            );
        }
    }

    /**
     * Gets next active session.
     *
     * @return some active session or null if there are no sessions at all
     */
    @Nullable
    public EnqueueingSession getNextActiveSession() {
        String query = "WHERE e.status = :status ORDER BY e.createdDate ASC";
        Optional<EnqueueingSession> session = dataManager.load(EnqueueingSession.class)
                .query(query)
                .parameter("status", EnqueueingSessionStatus.ACTIVE)
                .optional();
        return session.orElse(null);
    }

    /**
     * Gets the next active session of the provided entity, whatever tenant it belongs to.
     * <p>
     * A session belongs to one entity of one tenant, so an entity split by tenants has a session per tenant. An
     * operation addressed to the entity alone covers all of them and takes them one at a time, oldest first.
     *
     * @param entityName entity name
     * @return some active session of that entity, or null if it has none
     */
    @Nullable
    public EnqueueingSession getNextActiveSessionOfEntity(String entityName) {
        String query = "WHERE e.status = :status AND e.entityName = :entityName ORDER BY e.createdDate ASC";
        return dataManager.load(EnqueueingSession.class)
                .query(query)
                .parameter("status", EnqueueingSessionStatus.ACTIVE)
                .parameter("entityName", entityName)
                .optional()
                .orElse(null);
    }

    @Nullable
    public EnqueueingSession getNextActiveSession(@Nullable String tenantId) {
        validateTenantId(null, tenantId);
        String query = "WHERE e.status = :status";
        if (tenantId == null) {
            query += " AND e.tenantId is null";
        } else {
            query += " AND e.tenantId = :tenantId";
        }
        query += " ORDER BY e.createdDate ASC";

        FluentLoader.ByQuery<EnqueueingSession> loader = dataManager.load(EnqueueingSession.class)
                .query(query)
                .parameter("status", EnqueueingSessionStatus.ACTIVE);
        if (tenantId != null) {
            loader.parameter("tenantId", tenantId);
        }
        Optional<EnqueueingSession> session = loader.optional();
        return session.orElse(null);
    }

    /**
     * Updates provided session with provided ordering value.
     *
     * @param session           session
     * @param lastOrderingValue value
     */
    public void updateOrderingValue(EnqueueingSession session, @Nullable Object lastOrderingValue) {
        String entityName = session.getEntityName();
        executeManagementAction(entityName, session.getTenantId(), 10000, () -> {
            EnqueueingSession currentSession = reloadSession(session).orElse(null);
            if (currentSession == null) {
                return IndexManipulationResult.FAILURE;
            }

            String rawOrderingValue;
            if (lastOrderingValue == null) {
                rawOrderingValue = null;
            } else {
                rawOrderingValue = convertOrderingValueToString(lastOrderingValue);
            }

            currentSession.setLastProcessedValue(rawOrderingValue);
            dataManager.save(currentSession);
            return IndexManipulationResult.SUCCESS;
        });
    }

    protected Optional<EnqueueingSession> reloadSession(EnqueueingSession session) {
        return dataManager.load(EnqueueingSession.class).id(session.getId()).optional();
    }

    protected Optional<EnqueueingSession> loadEnqueueingSessionEntityByEntityName(String entityName, @Nullable String tenantId) {
        if (tenantId == null) {
            return dataManager.load(EnqueueingSession.class)
                    .query("where e.entityName = ?1 and e.tenantId is null", entityName)
                    .optional();
        }
        return dataManager.load(EnqueueingSession.class)
                .query("where e.entityName = ?1 and e.tenantId = ?2", entityName, tenantId)
                .optional();
    }

    protected MetaProperty resolveOrderingProperty(MetaClass entityClass) {
        if (metadataTools.hasCompositePrimaryKey(entityClass) && metadataTools.hasUuid(entityClass)) {
            String uuidPropertyName = metadataTools.getUuidPropertyName(entityClass.getJavaClass());
            if (uuidPropertyName == null) {
                throw new IllegalArgumentException("Expected UUID property is null");
            }
            return entityClass.getProperty(uuidPropertyName);
        }

        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(entityClass);
        if (primaryKeyProperty == null) {
            throw new IllegalArgumentException(
                    String.format("Entity '%s' doesn't have primary key property", entityClass.getName())
            );
        }
        return primaryKeyProperty;
    }

    protected IndexManipulationResult executeManagementAction(String entityName, @Nullable String tenantId, int lockTimeoutMs, SessionManagementAction action) {
        return executeManagementAction(entityName, tenantId, lockTimeoutMs, action, true);
    }

    /**
     * Runs an action on the session of one entity and tenant, under the lock of that session.
     *
     * @param entityMustBeIndexed whether the entity has to be in the indexed set. An operation that creates or
     *                            resumes work needs it; an operation that removes what an entity left behind is
     *                            refused by it - and removing a session of an entity that is no longer indexed is
     *                            the only way such a session can ever be got rid of
     */
    protected IndexManipulationResult executeManagementAction(String entityName, @Nullable String tenantId,
                                                              int lockTimeoutMs, SessionManagementAction action,
                                                              boolean entityMustBeIndexed) {
        Preconditions.checkNotEmptyString(entityName);
        validateTenantId(entityName, tenantId);
        if (!entityMustBeIndexed || indexConfigurationManager.isDirectlyIndexed(entityName)) {
            log.debug("Try to lock enqueueing session for entity '{}' and tenant '{}'", entityName, tenantId);
            if (!locker.tryLockEnqueueingSession(entityName, tenantId, lockTimeoutMs, TimeUnit.MILLISECONDS)) {
                log.info("Unable to lock enqueuing session for entity '{}' and tenant '{}': session is locked",
                        entityName, tenantId);
                return IndexManipulationResult.FAILURE;
            }

            try {
                return action.execute();
            } finally {
                locker.unlockEnqueueingSession(entityName, tenantId);
                log.debug("Unlock enqueueing session for entity '{}' and tenant '{}'", entityName, tenantId);
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("Unable to perform management action on enqueuing session: entity '%s' is not indexed", entityName)
            );
        }
    }

    protected void validateTenantId(@Nullable String entityName, @Nullable String tenantId) {
        if (tenantId == null) {
            return;
        }
        Preconditions.checkNotEmptyString(tenantId);
        if (entityName != null && !indexConfigurationManager.isDirectlyIndexed(entityName)) {
            // An entity that left the indexed set has no configuration to ask about the split. Its leftovers are
            // addressed by the tenant they were written with, and refusing here would make them unreachable.
            return;
        }
        if (entityName == null) {
            // There is no configuration to ask, so the only thing that can be checked is the add-on itself.
            if (!multitenancyAdapter.isMultitenancyActive()) {
                throw new IllegalStateException("Multitenancy is not available");
            }
            return;
        }
        if (!indexLayout.isSplitByTenants(indexConfigurationManager.getIndexConfigurationByEntityName(entityName))) {
            throw new IllegalArgumentException(
                    String.format("Index of entity '%s' is not split by tenants", entityName)
            );
        }
    }

    protected String convertOrderingValueToString(Object orderingValue) {
        return orderingValue.toString();
    }

    protected List<IndexOperationResult<IndexManipulationResult>> applyForAllTenantsOrTenantless(
            String entityName,
            BiFunction<String, String, IndexManipulationResult> action) {
        IndexConfiguration config = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        return indexLayout.allIndexes(config)
                .stream()
                .map(index -> new IndexOperationResult<>(
                        entityName,
                        index.indexName(),
                        index.tenantId(),
                        action.apply(entityName, index.tenantId())))
                .toList();
    }

    /**
     * Turns the outcome of an operation on one session into the row that names the index it was performed on.
     * <p>
     * A session of a split entity that carries no tenant has no index to be named against - it is a leftover of
     * an application that was upgraded, or of a tenant that has been removed. The operation itself is still
     * performed, and the session is gone or suspended as asked; what cannot be produced is the row, so none is
     * returned rather than one naming an index that does not exist.
     */
    protected List<IndexOperationResult<IndexManipulationResult>> toResult(String entityName,
                                                                     @Nullable String tenantId,
                                                                     IndexManipulationResult result) {
        IndexConfiguration config = indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
        String indexName = indexLayout.indexName(config, tenantId);
        if (indexName == null) {
            log.warn("Session of entity '{}' carries no tenant while the entity is split by tenants: the "
                    + "operation is done, but there is no index to report it against", entityName);
            return List.of();
        }
        return List.of(new IndexOperationResult<>(entityName, indexName, tenantId, result));
    }

    @NullMarked
    protected interface SessionManagementAction {

        IndexManipulationResult execute();
    }

}
