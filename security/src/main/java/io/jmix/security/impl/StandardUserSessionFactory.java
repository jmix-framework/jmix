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
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.*;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.Transaction;
import io.jmix.data.TypedQuery;
import io.jmix.security.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component(UserSessionFactory.NAME)
public class StandardUserSessionFactory implements UserSessionFactory {

    private static final Logger log = LoggerFactory.getLogger(StandardUserSessionFactory.class);

    @Inject
    protected Metadata metadata;

    @Inject
    protected ExtendedEntities extendedEntities;

    @Inject
    protected DefaultPermissionValuesConfig defaultPermissionValuesConfig;

    @Inject
    protected Persistence persistence;

    @Inject
    protected DatatypeRegistry datatypeRegistry;

    private final StandardUserSession SYSTEM_SESSION;

    public StandardUserSessionFactory() {
        User user = new User();
        user.setLogin("system");
        user.setLoginLowerCase("system");
        user.setPassword("");
        user.setName("System");
        SystemAuthenticationToken authentication = new SystemAuthenticationToken(user, Collections.emptyList());
        SYSTEM_SESSION = new BuiltInSystemUserSession(authentication);
    }

    @Override
    public UserSession create(Authentication authentication) {
        StandardUserSession session = new StandardUserSession(authentication);
        try (Transaction tx = persistence.createTransaction()) {
            compilePermissions(session);
            compileConstraints(session);
            compileSessionAttributes(session);
            tx.commit();
        }
        return session;
    }

    @Override
    public UserSession getSystemSession() {
        return SYSTEM_SESSION;
    }

    protected void compilePermissions(StandardUserSession session) {
        List<Role> roles = session.getUser().getUserRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        boolean superRole = false;
        for (Role role : roles) {
            session.addRole(role);
            if (role.getType() == RoleType.SUPER) {
                superRole = true;
            }
        }

        if (!superRole) { // Don't waste memory, as the user with SUPER role has all permissions.
            roles.stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .forEach(permission -> {
                        PermissionType type = permission.getType();
                        if (type != null && permission.getValue() != null) {
                            try {
                                session.addPermission(type,
                                        permission.getTarget(), convertToExtendedEntityTarget(permission), permission.getValue());
                            } catch (Exception ignored) {}
                        }
                    });

            defaultPermissionValuesConfig.getDefaultPermissionValues().forEach((target, permission) -> {
                if (session.getPermissionValue(permission.getType(), permission.getTarget()) == null) {
                    session.addPermission(permission.getType(), permission.getTarget(),
                            convertToExtendedEntityTarget(permission), permission.getValue());
                }
            });
        }
    }

    protected String convertToExtendedEntityTarget(Permission permission) {
        if (permission.getType() == PermissionType.ENTITY_OP || permission.getType() == PermissionType.ENTITY_ATTR) {
            String target = permission.getTarget();
            int pos = target.indexOf(Permission.TARGET_PATH_DELIMETER);
            if (pos > -1) {
                String entityName = target.substring(0, pos);
                Class extendedClass = extendedEntities.getExtendedClass(metadata.getClassNN(entityName));
                if (extendedClass != null) {
                    MetaClass extMetaClass = metadata.getClassNN(extendedClass);
                    return extMetaClass.getName() + Permission.TARGET_PATH_DELIMETER + target.substring(pos + 1);
                }
            }
        }
        return null;
    }

    protected void compileConstraints(StandardUserSession session) {
        Group group = session.getUser().getGroup();
        if (group == null)
            return;

        EntityManager em = persistence.getEntityManager();
        TypedQuery<Constraint> q = em.createQuery("select c from sec_GroupHierarchy h join h.parent.constraints c " +
                "where h.group.id = ?1", Constraint.class);
        q.setParameter(1, group.getId());
        List<Constraint> constraints = q.getResultList();
        List<Constraint> list = new ArrayList<>(constraints);
        list.addAll(group.getConstraints());
        for (Constraint constraint : list) {
            if (Boolean.TRUE.equals(constraint.getIsActive())) {
                session.addConstraint(constraint);
            }
        }
    }

    protected void compileSessionAttributes(StandardUserSession session) {
        Group group = session.getUser().getGroup();
        if (group == null)
            return;

        List<SessionAttribute> list = new ArrayList<>(group.getSessionAttributes());

        EntityManager em = persistence.getEntityManager();
        TypedQuery<SessionAttribute> q = em.createQuery("select a from sec_GroupHierarchy h join h.parent.sessionAttributes a " +
                "where h.group.id = ?1 order by h.level desc", SessionAttribute.class);
        q.setParameter(1, group.getId());
        List<SessionAttribute> attributes = q.getResultList();
        list.addAll(attributes);

        for (SessionAttribute attribute : list) {
            Datatype datatype = datatypeRegistry.get(attribute.getDatatype());
            try {
                if (session.getAttributeNames().contains(attribute.getName())) {
                    log.warn("Duplicate definition of '{}' session attribute in the group hierarchy", attribute.getName());
                }
                Serializable value = (Serializable) datatype.parse(attribute.getStringValue());
                if (value != null)
                    session.setAttribute(attribute.getName(), value);
                else
                    session.removeAttribute(attribute.getName());
            } catch (ParseException e) {
                throw new RuntimeException("Unable to set session attribute " + attribute.getName(), e);
            }
        }
    }

    private static class BuiltInSystemUserSession extends StandardUserSession implements SystemUserSession {

        private static final long serialVersionUID = 428868424041528894L;

        public BuiltInSystemUserSession(SystemAuthenticationToken authentication) {
            super(authentication);
            id = new UUID(1L, 1L);
            clientDetails = ClientDetails.builder().info("System authentication").build();
        }
    }
}
