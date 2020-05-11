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

import io.jmix.core.Entity;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.IdProxy;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.datatypes.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.*;
import io.jmix.data.RowLevelSecurityException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
    protected CurrentAuthentication currentAuthentication;

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected ExtendedEntities extendedEntities;

    @Inject
    protected DatatypeRegistry datatypeRegistry;

    protected ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

//    private StandardUserSession getUserSession() {
//        return (StandardUserSession) userSessionSource.getUserSession();
//    }

    @Override
    public boolean isScreenPermitted(String windowAlias) {
//        return getUserSession().isPermitted(PermissionType.SCREEN, windowAlias, 1);
        return true;
    }

    @Override
    public boolean isEntityOpPermitted(MetaClass metaClass, EntityOp entityOp) {
//        return getUserSession().isPermitted(PermissionType.ENTITY_OP,
//                metaClass.getName() + Permission.TARGET_PATH_DELIMETER + entityOp.getId(), 1);
        return true;
    }

    @Override
    public boolean isEntityOpPermitted(Class<?> entityClass, EntityOp entityOp) {
        return isEntityOpPermitted(metadata.getClass(entityClass), entityOp);
    }

    @Override
    public boolean isEntityAttrPermitted(MetaClass metaClass, String property, EntityAttrAccess access) {
//        return getUserSession().isPermitted(PermissionType.ENTITY_ATTR,
//                metaClass.getName() + Permission.TARGET_PATH_DELIMETER + property, access.getId());
        return true;
    }

    @Override
    public boolean isEntityAttrPermitted(Class<?> entityClass, String property, EntityAttrAccess access) {
        return isEntityAttrPermitted(metadata.getClass(entityClass), property, access);
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
//        return getUserSession().isPermitted(PermissionType.SPECIFIC, name, 1);
        return true;
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
//        return getUserSession().hasConstraints();
        return false;
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
        //todo MG
//        context.put("userSession", currentAuthentication.getUserSession());
        fillGroovyConstraintsContext(context);
        ScriptEngine engine = scriptEngineManager.getEngineByName("groovy");
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
        Object result;
        try {
            result = engine.eval(groovyScript.replace("{E}", "__entity__"));
        } catch (ScriptException e) {
            throw new RuntimeException("Error evaluating Groovy expression", e);
        }
        return result;
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
        String metaClassName = metadata.getClass(entity.getClass()).getName();
        String groovyScript = constraint.getGroovyScript();
        if (constraint.getCheckType().memory() && StringUtils.isNotBlank(groovyScript)) {
            try {
                Object o = evaluateConstraintScript(entity, groovyScript);
                if (Boolean.FALSE.equals(o)) {
                    log.trace("Entity does not match security constraint. Entity class [{}]. Entity [{}]. Constraint [{}].",
                            metaClassName, EntityValues.getId(entity), constraint.getCheckType());
                    return false;
                }
            } catch (Exception e) {
                log.error("An error occurred while applying constraint's Groovy script. The entity has been filtered out." +
                        "Entity class [{}]. Entity [{}].", metaClassName, EntityValues.getId(entity), e);
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
//        StandardUserSession userSession = getUserSession();
//        MetaClass mainMetaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
//
//        List<ConstraintData> constraints = new ArrayList<>(userSession.getConstraints(mainMetaClass.getName()));
//        for (MetaClass parent : mainMetaClass.getAncestors()) {
//            constraints.addAll(userSession.getConstraints(parent.getName()));
//        }
//        return constraints;
        return new ArrayList<>();
    }

    /**
     * Override if you need specific context variables in Groovy constraints.
     *
     * @param context passed to Groovy evaluator
     */
    protected void fillGroovyConstraintsContext(Map<String, Object> context) {
    }

    @SuppressWarnings({"unused", "unchecked"})
    @Nullable
    protected Object parseValue(Class<?> clazz, String strValue) {
        try {
            if (Entity.class.isAssignableFrom(clazz)) {
                MetaClass metaClass = metadata.getClass(clazz);
                Entity entity = metadata.create(metaClass);
                MetaProperty pkProperty = metadataTools.getPrimaryKeyProperty(metaClass);

                if (pkProperty != null) {
                    boolean dbGeneratedPrimaryKey = metadataTools.hasDbGeneratedPrimaryKey(metaClass);
                    Object pkValue = null;
                    if (Long.class.equals(pkProperty.getJavaType())) {
                        pkValue = Long.valueOf(strValue);
                        if (dbGeneratedPrimaryKey) {
                            pkValue = IdProxy.of((Long) pkValue);
                        }
                    } else if (Integer.class.equals(pkProperty.getJavaType())) {
                        pkValue = Integer.valueOf(strValue);
                        if (dbGeneratedPrimaryKey) {
                            pkValue = IdProxy.of((Integer) pkValue);
                        }
                    } else if (UUID.class.equals(pkProperty.getJavaType())) {
                        pkValue = UUID.fromString(strValue);
                    } else if (String.class.equals(pkProperty.getJavaType())) {
                        pkValue = strValue;
                    }
                    EntityValues.setId(entity, pkValue);
                }
                return entity;
            } else if (EnumClass.class.isAssignableFrom(clazz)) {
                //noinspection unchecked
                return Enum.valueOf((Class<Enum>) clazz, strValue);
            } else {
                Datatype datatype = datatypeRegistry.get(clazz);
                return datatype != null ? datatype.parse(strValue) : strValue;
            }
        } catch (ParseException | IllegalArgumentException e) {
            log.error("Could not parse a value in constraint. Class [{}], value [{}].", clazz, strValue, e);
            throw new RowLevelSecurityException(format("Could not parse a value in constraint. Class [%s], value [%s]. " +
                    "See the log for details.", clazz, strValue), null);
        }
    }
}
