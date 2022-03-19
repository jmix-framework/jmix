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

package io.jmix.graphql;

import io.jmix.core.metamodel.model.MetaClass;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

public class NamingUtils {

    public static final String QUERY_ENTITY_MESSAGES = "entityMessages";
    public static final String QUERY_ENUM_MESSAGES = "enumMessages";
    public static final String QUERY_PERMISSIONS = "permissions";
    public static final String TYPE_GQL_MESSAGE_DETAIL = "gql_MessageDetail";
    public static final String TYPE_SEC_PERMISSION = "sec_Permission";
    public static final String TYPE_SEC_PERMISSION_CONFIG = "sec_PermissionConfig";
    public static final String ENTITIES = "entities";
    public static final String ENTITY_ATTRS = "entityAttributes";
    public static final String SPECIFICS = "specifics";

    public static final String SYS_ATTR_TYPENAME = "__typename";
    public static final String SYS_ATTR_INSTANCE_NAME = "_instanceName";

    public static final String FILTER = "filter";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String ORDER_BY = "orderBy";
    public static final String SOFT_DELETION = "softDeletion";

    public static final String ID_ATTR_NAME = "id";
    public static final String INPUT_TYPE_PREFIX = "inp_";

    /**
     * Replace all symbols that we can't use in graphql types, such '$', add input type prefix
     *
     * @param name name to be normalized
     * @return normalized name
     */
    public static String normalizeInpTypeName(String name) {
        return INPUT_TYPE_PREFIX + name.replaceAll("\\$", "_");
    }

    public static String normalizeName(String name) {
        return name.replaceAll("\\$", "_");
    }

    @NotNull
    public static String composeListQueryName(MetaClass metaClass) {
        return normalizeName(metaClass.getName()) + "List";
    }

    @NotNull
    public static String composeCountQueryName(MetaClass metaClass) {
        return normalizeName(metaClass.getName()) + "Count";
    }

    @NotNull
    public static String composeByIdQueryName(MetaClass metaClass) {
        return normalizeName(metaClass.getName()) + "ById";
    }

    @NotNull
    public static String composeUpsertMutationName(MetaClass metaClass) {
        return "upsert_" + normalizeName(metaClass.getName());
    }

    @NotNull
    public static String composeDeleteMutationName(MetaClass metaClass) {
        return "delete_" + normalizeName(metaClass.getName());
    }

    @NotNull
    public static String uncapitalizedSimpleName(Class<?> aClass) {
        return StringUtils.uncapitalize(aClass.getSimpleName());
    }
}
