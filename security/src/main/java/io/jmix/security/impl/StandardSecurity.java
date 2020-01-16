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

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Scripting;
import io.jmix.core.entity.*;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.datatypes.impl.EnumClass;
import io.jmix.core.security.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.RowLevelSecurityException;
import io.jmix.security.entity.Permission;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import java.text.ParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.jmix.core.security.ConstraintOperationType.ALL;
import static io.jmix.core.security.ConstraintOperationType.CUSTOM;
import static java.lang.String.format;

@Component(Security.NAME)
public class StandardSecurity implements Security {

    private static final Logger log = LoggerFactory.getLogger(StandardSecurity.class);

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected ExtendedEntities extendedEntities;

    @Inject
    protected DatatypeRegistry datatypeRegistry;

    @Inject
    protected Scripting scripting;

    private StandardUserSession getUserSession() {
        return (StandardUserSession) userSessionSource.getUserSession();
    }

    @Override
    public boolean isScreenPermitted(String windowAlias) {
        return getUserSession().isPermitted(PermissionType.SCREEN, windowAlias, 1);
    }

    @Override
    public boolean isEntityOpPermitted(MetaClass metaClass, EntityOp entityOp) {
        return getUserSession().isPermitted(PermissionType.ENTITY_OP,
                metaClass.getName() + Permission.TARGET_PATH_DELIMETER + entityOp.getId(), 1);
    }

    @Override
    public boolean isEntityOpPermitted(Class<?> entityClass, EntityOp entityOp) {
        return isEntityOpPermitted(metadata.getClassNN(entityClass), entityOp);
    }

    @Override
    public boolean isEntityAttrPermitted(MetaClass metaClass, String property, EntityAttrAccess access) {
        return getUserSession().isPermitted(PermissionType.ENTITY_ATTR,
                metaClass.getName() + Permission.TARGET_PATH_DELIMETER + property, access.getId());
    }

    @Override
    public boolean isEntityAttrPermitted(Class<?> entityClass, String property, EntityAttrAccess access) {
        return isEntityAttrPermitted(metadata.getClassNN(entityClass), property, access);
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaClass metaClass, String propertyPath) {
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, propertyPath);
        return metaPropertyPath != null && isEntityAttrUpdatePermitted(metaPropertyPath);
    }

    @Override
    public boolean isEntityAttrUpdatePermitted(MetaPropertyPath metaPropertyPath) {
        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);

        if (metadataTools.isEmbeddable(propertyMetaClass)) {
            return isEntityOpPermitted(propertyMetaClass, EntityOp.UPDATE)
                    && isEntityAttrPermitted(propertyMetaClass, metaPropertyPath, EntityAttrAccess.MODIFY);
        }

        return (isEntityOpPermitted(propertyMetaClass, EntityOp.CREATE)
                || isEntityOpPermitted(propertyMetaClass, EntityOp.UPDATE))
                && isEntityAttrPermitted(propertyMetaClass, metaPropertyPath, EntityAttrAccess.MODIFY);
    }

    @Override
    public boolean isEntityAttrReadPermitted(MetaPropertyPath metaPropertyPath) {
        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
        return isEntityOpPermitted(propertyMetaClass, EntityOp.READ)
                && isEntityAttrPermitted(propertyMetaClass, metaPropertyPath, EntityAttrAccess.VIEW);
    }

    @Override
    public boolean isEntityAttrReadPermitted(MetaClass metaClass, String propertyPath) {
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPath(metaClass, propertyPath);
        return metaPropertyPath != null && isEntityAttrReadPermitted(metaPropertyPath);
    }

    @Override
    public boolean isSpecificPermitted(String name) {
        return getUserSession().isPermitted(PermissionType.SPECIFIC, name, 1);
    }

    @Override
    public void checkSpecificPermission(String name) {
        if (!isSpecificPermitted(name))
            throw new AccessDeniedException(PermissionType.SPECIFIC, name);
    }

    @Override
    public boolean isPermitted(Entity entity, ConstraintOperationType operationType) {
        return isPermitted(entity,
                constraint -> {
                    ConstraintOperationType opType = constraint.getOperationType();
                    return constraint.getCheckType().memory()
                            && (
                            (operationType == ALL && opType != CUSTOM)
                                    || opType == operationType
                                    || opType == ALL
                    );
                });
    }

    @Override
    public boolean isPermitted(Entity entity, String customCode) {
        return isPermitted(entity,
                constraint -> customCode.equals(constraint.getCode()) && constraint.getCheckType().memory());
    }

    @Override
    public boolean hasConstraints() {
        return getUserSession().hasConstraints();
    }

    @Override
    public boolean hasConstraints(MetaClass metaClass) {
        List<ConstraintData> constraints = getConstraints(metaClass);
        return !constraints.isEmpty();
    }

    @Override
    public boolean hasInMemoryConstraints(MetaClass metaClass, ConstraintOperationType... operationTypes) {
        List<ConstraintData> constraints = getConstraints(metaClass, constraint ->
                constraint.getCheckType().memory() && constraint.getOperationType() != null
                        && Arrays.asList(operationTypes).contains(constraint.getOperationType())
        );
        return !constraints.isEmpty();
    }

    @Override
    public Object evaluateConstraintScript(Entity entity, String groovyScript) {
        Map<String, Object> context = new HashMap<>();
        context.put("__entity__", entity);
        context.put("parse", new MethodClosure(this, "parseValue"));
        context.put("userSession", userSessionSource.getUserSession());
        fillGroovyConstraintsContext(context);
        return scripting.evaluateGroovy(groovyScript.replace("{E}", "__entity__"), context);
    }

    protected boolean isEntityAttrPermitted(MetaClass metaClass, MetaPropertyPath propertyPath, EntityAttrAccess access) {
        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        if (originalMetaClass != null) {
            metaClass = originalMetaClass;
        }
        return isEntityAttrPermitted(metaClass, propertyPath.getMetaProperty().getName(), access);
    }

    protected boolean isPermitted(Entity entity, Predicate<ConstraintData> predicate) {
        List<ConstraintData> constraints = getConstraints(metadata.getClass(entity), predicate);
        for (ConstraintData constraint : constraints) {
            if (!isPermitted(entity, constraint)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isPermitted(Entity entity, ConstraintData constraint) {
        String metaClassName = metadata.getClassNN(entity.getClass()).getName();
        String groovyScript = constraint.getGroovyScript();
        if (constraint.getCheckType().memory() && StringUtils.isNotBlank(groovyScript)) {
            try {
                Object o = evaluateConstraintScript(entity, groovyScript);
                if (Boolean.FALSE.equals(o)) {
                    log.trace("Entity does not match security constraint. Entity class [{}]. Entity [{}]. Constraint [{}].",
                            metaClassName, entity.getId(), constraint.getCheckType());
                    return false;
                }
            } catch (Exception e) {
                log.error("An error occurred while applying constraint's Groovy script. The entity has been filtered out." +
                        "Entity class [{}]. Entity [{}].", metaClassName, entity.getId(), e);
                return false;
            }
        }
        return true;
    }

    public List<ConstraintData> getConstraints(MetaClass metaClass, Predicate<ConstraintData> predicate) {
        return getConstraints(metaClass).stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<ConstraintData> getConstraints(MetaClass metaClass) {
        StandardUserSession userSession = getUserSession();
        MetaClass mainMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);

        List<ConstraintData> constraints = new ArrayList<>(userSession.getConstraints(mainMetaClass.getName()));
        for (MetaClass parent : mainMetaClass.getAncestors()) {
            constraints.addAll(userSession.getConstraints(parent.getName()));
        }
        return constraints;
    }

    /**
     * Override if you need specific context variables in Groovy constraints.
     *
     * @param context passed to Groovy evaluator
     */
    protected void fillGroovyConstraintsContext(Map<String, Object> context) {
    }

    @SuppressWarnings({"unused", "unchecked"})
    protected Object parseValue(Class<?> clazz, String string) {
        try {
            if (Entity.class.isAssignableFrom(clazz)) {
                Object entity = metadata.create((Class<Entity>)clazz);
                if (entity instanceof BaseIntegerIdEntity) {
                    ((BaseIntegerIdEntity) entity).setId(Integer.valueOf(string));
                } else if (entity instanceof BaseLongIdEntity) {
                    ((BaseLongIdEntity) entity).setId(Long.valueOf(string));
                } else if (entity instanceof BaseStringIdEntity) {
                    ((BaseStringIdEntity) entity).setId(string);
                } else if (entity instanceof BaseIdentityIdEntity) {
                    ((BaseIdentityIdEntity) entity).setId(IdProxy.of(Long.valueOf(string)));
                } else if (entity instanceof BaseIntIdentityIdEntity) {
                    ((BaseIntIdentityIdEntity) entity).setId(IdProxy.of(Integer.valueOf(string)));
                } else if (entity instanceof HasUuid) {
                    ((HasUuid) entity).setUuid(UUID.fromString(string));
                }
                return entity;
            } else if (EnumClass.class.isAssignableFrom(clazz)) {
                //noinspection unchecked
                Enum parsedEnum = Enum.valueOf((Class<Enum>) clazz, string);
                return parsedEnum;
            } else {
                Datatype datatype = datatypeRegistry.get(clazz);
                return datatype != null ? datatype.parse(string) : string;
            }
        } catch (ParseException | IllegalArgumentException e) {
            log.error("Could not parse a value in constraint. Class [{}], value [{}].", clazz, string, e);
            throw new RowLevelSecurityException(format("Could not parse a value in constraint. Class [%s], value [%s]. " +
                    "See the log for details.", clazz, string), null);
        }
    }
}
