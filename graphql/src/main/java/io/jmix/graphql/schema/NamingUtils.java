package io.jmix.graphql.schema;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

public class NamingUtils {

    public static final String SYS_ATTR_TYPENAME = "__typename";
    public static final String SYS_ATTR_INSTANCE_NAME = "_instanceName";

    public static final String FILTER = "filter";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String ORDER_BY = "orderBy";


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
        return "inp_" + StringUtils.uncapitalize(name.replaceAll("\\$", "_"));
    }

    @NotNull
    public static String composeListQueryName(Class<?> aClass) {
        return uncapitalizedSimpleName(aClass) + "List";
    }

    @NotNull
    public static String composeCountQueryName(Class<?> aClass) {
        return uncapitalizedSimpleName(aClass) + "Count";
    }

    @NotNull
    public static String composeByIdQueryName(Class<?> aClass) {
        return uncapitalizedSimpleName(aClass) + "ById";
    }

    @NotNull
    public static String composeUpsertMutationName(Class<?> aClass) {
        return "upsert" + aClass.getSimpleName();
    }

    @NotNull
    public static String composeDeleteMutationName(Class<?> aClass) {
        return "delete" + aClass.getSimpleName();
    }

    @NotNull
    public static String uncapitalizedSimpleName(Class<?> aClass) {
        return StringUtils.uncapitalize(aClass.getSimpleName());
    }
}
