package io.jmix.graphql.schema;

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

    public static final String SYS_ATTR_TYPENAME = "__typename";
    public static final String SYS_ATTR_INSTANCE_NAME = "_instanceName";

    public static final String FILTER = "filter";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String ORDER_BY = "orderBy";

    public static final String ID_ATTR_NAME = "id";


    /**
     * Replace all symbols that we can't use in graphql types, such '$'
     * @param name name to be normalized
     * @return normalized name
     */
    public static String normalizeOutTypeName(String name) {
        return name.replaceAll("\\$", "_");
    }

    /**
     * Replace all symbols that we can't use in graphql types, such '$', add input type prefix
     * @param name name to be normalized
     * @return normalized name
     */
    public static String normalizeInpTypeName(String name) {
        return "inp_" + name.replaceAll("\\$", "_");
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
