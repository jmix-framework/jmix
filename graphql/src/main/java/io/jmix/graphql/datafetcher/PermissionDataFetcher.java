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

package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetcher;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.accesscontext.GraphQLAccessContext;
import io.jmix.graphql.schema.permission.PermissionConfig;
import io.jmix.graphql.schema.permission.ShortPermissionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.jmix.graphql.accesscontext.GraphQLAccessContext.GRAPHQL_FILE_DOWNLOAD_ENABLED;
import static io.jmix.graphql.accesscontext.GraphQLAccessContext.GRAPHQL_FILE_UPLOAD_ENABLED;

@Component("gql_PermissionDataFetcher")
public class PermissionDataFetcher {

    protected static final int ALLOWED_CRUD_PERMISSION = 1;
    protected static final int VIEW_ATTRIBUTE_PERMISSION = 1;
    protected static final int MODIFY_ATTRIBUTE_PERMISSION = 2;

    @Autowired
    Metadata metadata;
    @Autowired
    AccessManager accessManager;

    public DataFetcher<?> loadPermissions() {
        return environment -> {

            Set<String> defs = environment.getSelectionSet().getFieldsGroupedByResultKey().keySet();
            boolean loadEntities = defs.contains(NamingUtils.ENTITIES);
            boolean loadEntityAttrs = defs.contains(NamingUtils.ENTITY_ATTRS);
            boolean loadSpecifics = defs.contains(NamingUtils.SPECIFICS);
            return getPermissions(loadEntities, loadEntityAttrs, loadSpecifics);
        };
    }

    protected PermissionConfig getPermissions(boolean entities, boolean entityAttrs, boolean specifics) {
        PermissionConfig PermissionConfig = new PermissionConfig();

        List<ShortPermissionInfo> entityPermissions = new ArrayList<>();
        List<ShortPermissionInfo> entityAttributePermissions = new ArrayList<>();
        List<ShortPermissionInfo> specificPermissions = new ArrayList<>();
        PermissionConfig.setEntities(entityPermissions);
        PermissionConfig.setEntityAttributes(entityAttributePermissions);
        PermissionConfig.setSpecifics(specificPermissions);

        for (MetaClass metaClass : metadata.getSession().getClasses()) {
            CrudEntityContext entityContext = new CrudEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            if (entities) {
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
            }

            if (entityAttrs) {
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
        }

        if (specifics) {
            GraphQLAccessContext downloadContext = new GraphQLAccessContext(GRAPHQL_FILE_DOWNLOAD_ENABLED);
            accessManager.applyRegisteredConstraints(downloadContext);

            if (downloadContext.isPermitted()) {
                specificPermissions.add(new ShortPermissionInfo(downloadContext.getName(), 1));
            } else {
                specificPermissions.add(new ShortPermissionInfo(downloadContext.getName(), 0));
            }

            GraphQLAccessContext uploadContext = new GraphQLAccessContext(GRAPHQL_FILE_UPLOAD_ENABLED);
            accessManager.applyRegisteredConstraints(uploadContext);

            if (uploadContext.isPermitted()) {
                specificPermissions.add(new ShortPermissionInfo(uploadContext.getName(), 1));
            } else {
                specificPermissions.add(new ShortPermissionInfo(uploadContext.getName(), 0));
            }
        }

        return PermissionConfig;
    }

    protected String getEntityTarget(MetaClass metaClass, String operation) {
        return metaClass.getName() + ":" + operation;
    }

    protected String getEntityAttributeTarget(MetaClass metaClass, MetaProperty metaProperty) {
        return metaClass.getName() + ":" + metaProperty.getName();
    }

}
