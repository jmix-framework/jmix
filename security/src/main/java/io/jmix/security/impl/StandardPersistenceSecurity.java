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

package io.jmix.security.impl;

import com.google.common.collect.Multimap;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.jpql.JpqlSyntaxException;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.ConstraintOperationType;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.Security;
import io.jmix.data.PersistenceAttributeSecurity;
import io.jmix.data.PersistenceSecurity;
import io.jmix.data.RowLevelSecurityException;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.impl.JmixQuery;
import io.jmix.security.SecurityTokenException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.function.BiPredicate;

import static java.lang.String.format;

@Component(PersistenceSecurity.NAME)
public class StandardPersistenceSecurity implements PersistenceSecurity {

    private static final Logger log = LoggerFactory.getLogger(StandardPersistenceSecurity.class);

    @Autowired
    protected SecurityTokenManager securityTokenManager;

    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;

    @Autowired
    protected PersistenceAttributeSecurity persistenceAttributeSecurity;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected Security security;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public boolean applyConstraints(JmixQuery query) {
        QueryParser parser = QueryTransformerFactory.createParser(query.getQueryString());
        String entityName = parser.getEntityName();

        List<ConstraintData> constraints = ((StandardSecurity) security).getConstraints(metadata.getClass(entityName),
                constraint ->
                        constraint.getCheckType().database()
                                && (constraint.getOperationType() == ConstraintOperationType.READ
                                || constraint.getOperationType() == ConstraintOperationType.ALL));

        if (constraints.isEmpty())
            return false;

        QueryTransformer transformer = QueryTransformerFactory.createTransformer(query.getQueryString());

        for (ConstraintData constraint : constraints) {
            processConstraint(transformer, constraint, entityName);
        }
        query.setQueryString(transformer.getResult());

        for (String paramName : transformer.getAddedParams()) {
            setQueryParam(query, paramName);
        }
        return true;
    }

    // todo user substitution
    @Override
    public void setQueryParam(JmixQuery query, String paramName) {
        if (paramName.startsWith(CONSTRAINT_PARAM_SESSION_ATTR)) {
            String attrName = paramName.substring(CONSTRAINT_PARAM_SESSION_ATTR.length());

            if (CONSTRAINT_PARAM_USER_LOGIN.equals(attrName)) {
                String userLogin = /*userSession.getSubstitutedUser() != null ?
                        userSession.getSubstitutedUser().getLogin() :*/
                        currentAuthentication.getUser().getUsername();
                query.setParameter(paramName, userLogin);

            } else if (CONSTRAINT_PARAM_USER_ID.equals(attrName)) {
                UUID userId = /*userSession.getSubstitutedUser() != null ?
                        userSession.getSubstitutedUser().getId() :*/
                        UUID.fromString(currentAuthentication.getUser().getKey());
                query.setParameter(paramName, userId);

            } else if (CONSTRAINT_PARAM_USER_GROUP_ID.equals(attrName)) {
                //todo MG
//                Object groupId = /*userSession.getSubstitutedUser() != null ?
//                        userSession.getSubstitutedUser().getGroup().getId() :*/
//                        userSession.getUser().getGroup().getId();
//                query.setParameter(paramName, groupId);

            } else {
                //todo MG
//                Serializable value = userSession.getAttribute(attrName);
//                query.setParameter(paramName, value);
            }
        }
    }

    @Override
    public boolean filterByConstraints(Collection<Entity> entities) {
        boolean filtered = false;
        for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();
            if (!isPermittedInMemory(entity)) {
                //we ignore situations when the collection is immutable
                iterator.remove();
                filtered = true;
            }
        }
        return filtered;
    }

    @Override
    public boolean filterByConstraints(Entity entity) {
        return !isPermittedInMemory(entity);
    }

    @Override
    public void applyConstraints(Collection<Entity> entities) {
        Set<EntityId> handled = new LinkedHashSet<>();
        entities.forEach(entity -> applyConstraints(entity, handled));
    }

    @Override
    public void applyConstraints(Entity entity) {
        applyConstraints(entity, new HashSet<>());
    }

    @Override
    public void calculateFilteredData(Entity entity) {
        calculateFilteredData(entity, new HashSet<>(), false);
    }

    @Override
    public void calculateFilteredData(Collection<Entity> entities) {
        Set<EntityId> handled = new LinkedHashSet<>();
        entities.forEach(entity -> calculateFilteredData(entity, handled, false));
    }

    @Override
    public void restoreSecurityState(Entity entity) {
        try {
            securityTokenManager.readSecurityToken(entity);
        } catch (SecurityTokenException e) {
            throw new RowLevelSecurityException(
                    format("Could not restore security state for entity [%s] because security token isn't valid.",
                            entity), metadata.getClass(entity).getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restoreFilteredData(Entity entity) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
        String storeName = metadataTools.getStoreName(metaClass);
        EntityManager entityManager = storeAwareLocator.getEntityManager(storeName);

        EntityEntry entityEntry = entity.__getEntityEntry();

        Multimap<String, Object> filtered = entityEntry.getSecurityState().getFilteredData();
        if (filtered == null) {
            return;
        }

        for (Map.Entry<String, Collection<Object>> entry : filtered.asMap().entrySet()) {
            MetaProperty property = metaClass.getProperty(entry.getKey());
            Collection filteredIds = entry.getValue();

            if (property.getRange().isClass() && CollectionUtils.isNotEmpty(filteredIds)) {
                Class entityClass = property.getRange().asClass().getJavaClass();
                Class propertyClass = property.getJavaType();
                if (Collection.class.isAssignableFrom(propertyClass)) {
                    Collection currentCollection = EntityValues.getValue(entity, property.getName());
                    if (currentCollection == null) {
                        throw new RowLevelSecurityException(
                                format("Could not restore an object to currentValue because it is null [%s]. Entity [%s].",
                                        property.getName(), metaClass.getName()), metaClass.getName());
                    }

                    for (Object entityId : filteredIds) {
                        Entity reference = entityManager.getReference((Class<Entity>) entityClass, entityId);
                        //we ignore situations when the currentValue is immutable
                        currentCollection.add(reference);
                    }
                } else if (Entity.class.isAssignableFrom(propertyClass)) {
                    Object entityId = filteredIds.iterator().next();
                    Entity reference = entityManager.getReference((Class<Entity>) entityClass, entityId);
                    //we ignore the situation when the field is read-only
                    EntityValues.setValue(entity, property.getName(), reference);
                }
            }
        }
    }

    @Override
    public void assertToken(Entity entity) {
        EntityEntry entityEntry = entity.__getEntityEntry();
        if (entityEntry.getSecurityState().getSecurityToken() == null) {
            assertSecurityConstraints(entity, (e, metaProperty) -> entityStates.isDetached(entity)
                    && !entityStates.isLoaded(entity, metaProperty.getName()));
        }
    }

    @Override
    public void assertTokenForREST(Entity entity, FetchPlan view) {
        EntityEntry entityEntry = entity.__getEntityEntry();
        if (entityEntry.getSecurityState().getSecurityToken() == null) {
            assertSecurityConstraints(entity,
                    (e, metaProperty) -> view != null && !view.containsProperty(metaProperty.getName()));
        }
    }

    protected void assertSecurityConstraints(Entity entity, BiPredicate<Entity, MetaProperty> predicate) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (metaProperty.getRange().isClass() && metadataTools.isPersistent(metaProperty)) {
                if (predicate.test(entity, metaProperty)) {
                    continue;
                }
                if (security.hasInMemoryConstraints(metaProperty.getRange().asClass(), ConstraintOperationType.READ,
                        ConstraintOperationType.ALL)) {
                    throw new RowLevelSecurityException(format("Could not read security token from entity %s, " +
                                    "even though there are active READ/ALL constraints for the property: %s", entity,
                            metaProperty.getName()),
                            metaClass.getName());
                }
            }
        }
    }

    protected void processConstraint(QueryTransformer transformer, ConstraintData constraint, String entityName) {
        String join = constraint.getJoin();
        String where = constraint.getWhereClause();
        try {
            if (StringUtils.isBlank(join)) {
                if (!StringUtils.isBlank(where)) {
                    transformer.addWhere(where);
                }
            } else {
                transformer.addJoinAndWhere(join, where);
            }
        } catch (JpqlSyntaxException e) {
            log.error("Syntax errors found in constraint's JPQL expressions. Entity [{}]. Constraint ID [{}].",
                    entityName, constraint.getId(), e);

            throw new RowLevelSecurityException(
                    "Syntax errors found in constraint's JPQL expressions. Please see the logs.", entityName);
        } catch (Exception e) {
            log.error("An error occurred when applying security constraint. Entity [{}]. Constraint ID [{}].",
                    entityName, constraint.getId(), e);

            throw new RowLevelSecurityException(
                    "An error occurred when applying security constraint. Please see the logs.", entityName);
        }
    }

    @SuppressWarnings("unchecked")
    protected void applyConstraints(Entity entity, Set<EntityId> handled) {
        MetaClass metaClass = metadata.getClass(entity);
        EntityId entityId = new EntityId(referenceToEntitySupport.getReferenceId(entity), metaClass.getName());
        if (handled.contains(entityId)) {
            return;
        }
        handled.add(entityId);
        if (!entity.__getEntityEntry().isEmbeddable()) {
            EntityEntry entityEntry = entity.__getEntityEntry();
            Multimap<String, Object> filteredData = entityEntry.getSecurityState().getFilteredData();
            for (MetaProperty property : metaClass.getProperties()) {
                if (metadataTools.isPersistent(property) && entityStates.isLoaded(entity, property.getName())) {
                    Object value = EntityValues.getValue(entity, property.getName());
                    if (value instanceof Collection) {
                        Collection entities = (Collection) value;
                        for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); ) {
                            Entity item = iterator.next();
                            if (filteredData != null && filteredData.containsEntry(property.getName(),
                                    referenceToEntitySupport.getReferenceId(item))) {
                                iterator.remove();
                            } else {
                                applyConstraints(item, handled);
                            }
                        }
                    } else if (value instanceof Entity) {
                        if (filteredData != null && filteredData.containsEntry(property.getName(),
                                referenceToEntitySupport.getReferenceId((Entity) value))) {
                            EntityValues.setValue((Entity) value, property.getName(), null);
                        } else {
                            applyConstraints((Entity) value, handled);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean calculateFilteredData(Entity entity, Set<EntityId> handled, boolean checkPermitted) {
        if (referenceToEntitySupport.getReferenceId(entity) == null) {
            return false;
        }
        MetaClass metaClass = metadata.getClass(entity);
        if (!isPermittedInMemory(entity) && checkPermitted) {
            return true;
        }
        EntityId entityId = new EntityId(referenceToEntitySupport.getReferenceId(entity), metaClass.getName());
        if (handled.contains(entityId)) {
            return false;
        }
        handled.add(entityId);
        for (MetaProperty property : metaClass.getProperties()) {
            if (metadataTools.isPersistent(property) && entityStates.isLoaded(entity, property.getName())) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value instanceof Collection) {
                    Set filtered = new LinkedHashSet();
                    for (Entity item : (Collection<Entity>) value) {
                        if (calculateFilteredData(item, handled, true)) {
                            filtered.add(referenceToEntitySupport.getReferenceId(item));
                        }
                    }
                    if (!filtered.isEmpty()) {
                        securityTokenManager.addFiltered(entity, property.getName(), filtered);
                    }
                } else if (value instanceof Entity) {
                    Entity valueEntity = (Entity) value;
                    if (calculateFilteredData(valueEntity, handled, true)) {
                        securityTokenManager.addFiltered(entity, property.getName(),
                                referenceToEntitySupport.getReferenceId(valueEntity));
                    }
                }
            }
            securityTokenManager.writeSecurityToken(entity);
        }
        return false;
    }

    protected boolean isPermittedInMemory(Entity entity) {
        return ((StandardSecurity) security).isPermitted(entity, constraint ->
                constraint.getCheckType().memory()
                        && (constraint.getOperationType() == ConstraintOperationType.READ
                        || constraint.getOperationType() == ConstraintOperationType.ALL));
    }

    protected static class EntityId {
        Object id;
        String metaClassName;

        public EntityId(Object id, String metaClassName) {
            this.id = id;
            this.metaClassName = metaClassName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EntityId entityId = (EntityId) o;

            if (!id.equals(entityId.id)) return false;
            return metaClassName.equals(entityId.metaClassName);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + metaClassName.hashCode();
            return result;
        }
    }
}
