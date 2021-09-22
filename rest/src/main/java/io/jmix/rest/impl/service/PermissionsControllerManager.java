/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.impl.service;

import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SpecificPolicyInfoRegistry;
import io.jmix.rest.impl.controller.PermissionsController;
import io.jmix.rest.impl.service.filter.data.PermissionsInfo;
import io.jmix.rest.impl.service.filter.data.ShortPermissionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class is used for getting current user permissions for the REST API. It contains a business logic required by the
 * {@link PermissionsController}
 */
@Component("rest_PermissionsControllerManager")
public class PermissionsControllerManager {
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected SpecificPolicyInfoRegistry specificPolicyInfoRegistry;

    protected static final int ALLOWED_CRUD_PERMISSION = 1;
    protected static final int VIEW_ATTRIBUTE_PERMISSION = 1;
    protected static final int MODIFY_ATTRIBUTE_PERMISSION = 2;

    public PermissionsInfo getPermissions() {
        PermissionsInfo permissionsInfo = new PermissionsInfo();

        List<String> authorities = currentAuthentication.getUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        permissionsInfo.setAuthorities(authorities);

        List<ShortPermissionInfo> entityPermissions = new ArrayList<>();
        List<ShortPermissionInfo> entityAttributePermissions = new ArrayList<>();

        permissionsInfo.setEntities(entityPermissions);
        permissionsInfo.setEntityAttributes(entityAttributePermissions);

        for (MetaClass metaClass : metadata.getSession().getClasses()) {
            CrudEntityContext entityContext = new CrudEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);


            if (entityContext.isCreatePermitted()) {
                entityPermissions.add(new ShortPermissionInfo(getEntityTarget(metaClass, "create"),
                        ALLOWED_CRUD_PERMISSION));
            }
            if (entityContext.isReadPermitted()) {
                entityPermissions.add(new ShortPermissionInfo(getEntityTarget(metaClass, "read"),
                        ALLOWED_CRUD_PERMISSION));
            }
            if (entityContext.isUpdatePermitted()) {
                entityPermissions.add(new ShortPermissionInfo(getEntityTarget(metaClass, "update"),
                        ALLOWED_CRUD_PERMISSION));
            }
            if (entityContext.isDeletePermitted()) {
                entityPermissions.add(new ShortPermissionInfo(getEntityTarget(metaClass, "delete"),
                        ALLOWED_CRUD_PERMISSION));
            }

            for (MetaProperty metaProperty : metaClass.getProperties()) {
                EntityAttributeContext attributeContext = new EntityAttributeContext(metaClass, metaProperty.getName());
                accessManager.applyRegisteredConstraints(attributeContext);

                if (attributeContext.canModify()) {
                    entityAttributePermissions.add(new ShortPermissionInfo(
                            getEntityAttributeTarget(metaClass, metaProperty),
                            MODIFY_ATTRIBUTE_PERMISSION));
                } else if (attributeContext.canView()) {
                    entityAttributePermissions.add(new ShortPermissionInfo(
                            getEntityAttributeTarget(metaClass, metaProperty),
                            VIEW_ATTRIBUTE_PERMISSION));
                }
            }
        }

        List<ShortPermissionInfo> grantedSpecificPolicies = specificPolicyInfoRegistry.getSpecificPolicyInfos().stream()
                .map(SpecificPolicyInfoRegistry.SpecificPolicyInfo::getName)
                .filter(specificPolicyName -> {
                    SpecificOperationAccessContext accessContext = new SpecificOperationAccessContext(specificPolicyName);
                    accessManager.applyRegisteredConstraints(accessContext);
                    return accessContext.isPermitted();
                })
                .map(specificPolicyName -> new ShortPermissionInfo(specificPolicyName, 1))
                .collect(Collectors.toList());
        permissionsInfo.setSpecifics(grantedSpecificPolicies);

        return permissionsInfo;
    }

    protected String getEntityTarget(MetaClass metaClass, String operation) {
        return metaClass.getName() + ":" + operation;
    }

    protected String getEntityAttributeTarget(MetaClass metaClass, MetaProperty metaProperty) {
        return metaClass.getName() + ":" + metaProperty.getName();
    }
}
