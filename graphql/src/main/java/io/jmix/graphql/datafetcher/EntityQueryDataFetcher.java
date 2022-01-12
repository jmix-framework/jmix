package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetcher;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.graphql.service.IdentifierService;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.loader.*;
import io.jmix.graphql.schema.Types;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.graphql.NamingUtils.SYS_ATTR_INSTANCE_NAME;

@Component("gql_EntityQueryDataFetcher")
public class EntityQueryDataFetcher {

    public static final int DEFAULT_MAX_RESULTS = 100;

    private final Logger log = LoggerFactory.getLogger(EntityQueryDataFetcher.class);

    @Autowired
    protected ResponseBuilder responseBuilder;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected DataFetcherPlanBuilder dataFetcherPlanBuilder;
    @Autowired
    protected FilterConditionBuilder filterConditionBuilder;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected EnvironmentUtils environmentUtils;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected QueryDataFetcherLoader queryDataFetcherLoader;
    @Autowired
    protected IdentifierService identifierService;

    private static final String GRAPHQL_ENTITY_LOADER_METHOD_NAME = GraphQLEntityDataFetcher.class.getDeclaredMethods()[0].getName();

    private static final String GRAPHQL_ENTITIES_LOADER_METHOD_NAME = GraphQLEntityListDataFetcher.class.getDeclaredMethods()[0].getName();

    private static final String GRAPHQL_COUNT_LOADER_METHOD_NAME = GraphQLEntityCountDataFetcher.class.getDeclaredMethods()[0].getName();

    public DataFetcher<?> loadEntity(MetaClass metaClass) {

        return environment -> {
            checkCanReadEntity(metaClass);

            String id = environment.getArgument("id");
            LoadContext<?> lc = new LoadContext<>(metaClass);
            lc.setId(identifierService.parse(id, metaClass));
            FetchPlan fetchPlan = dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment);
            lc.setFetchPlan(fetchPlan);
            Boolean softDeletion = environment.getArgument(NamingUtils.SOFT_DELETION);
            if (softDeletion != null) {
                lc.setHint("jmix.softDeletion", softDeletion);
            }

            log.debug("loadEntity: with context {}", lc);
            if (queryDataFetcherLoader.getCustomEntityFetcher(metaClass.getJavaClass()) == null) {
                Object entity = dataManager.load(lc);
                if (entity == null) return null;
                return responseBuilder.buildResponse((Entity) entity, fetchPlan, metaClass, environmentUtils.getDotDelimitedProps(environment));
            } else {
                Object bean = queryDataFetcherLoader.getCustomEntityFetcher(metaClass.getJavaClass());
                Method method = bean.getClass().getDeclaredMethod(GRAPHQL_ENTITY_LOADER_METHOD_NAME,
                        GraphQLEntityDataFetcherContext.class);
                return responseBuilder.buildResponse((Entity) method.invoke(bean,
                                new GraphQLEntityDataFetcherContext(metaClass, id, lc, fetchPlan)), fetchPlan, metaClass,
                        environmentUtils.getDotDelimitedProps(environment));
            }
        };
    }

    public DataFetcher<List<Map<String, Object>>> loadEntities(MetaClass metaClass) {

        return environment -> {
            checkCanReadEntity(metaClass);

            Object filter = environment.getArgument(NamingUtils.FILTER);
            Object orderBy = environment.getArgument(NamingUtils.ORDER_BY);
            Integer limit = environment.getArgument(NamingUtils.LIMIT);
            Integer offset = environment.getArgument(NamingUtils.OFFSET);
            Boolean softDeletion = environment.getArgument(NamingUtils.SOFT_DELETION);
            log.debug("loadEntities: metClass:{}, filter:{}, limit:{}, offset:{}, orderBy: {}, softDelete: {}",
                    metaClass, filter, limit, offset, orderBy, softDeletion);

            // fetch plan
            FetchPlan fetchPan = dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment);

            // build filter condition
            LogicalCondition condition = createCondition(filter);
            LoadContext.Query query = generateQuery(metaClass, condition);
            query.setMaxResults(limit != null ? limit : DEFAULT_MAX_RESULTS);
            query.setFirstResult(offset == null ? 0 : offset);

            // orderBy
            OrderedMap<String, Types.SortOrder> orderByConditions = buildOrderByConditionSet(metaClass, orderBy);
            if (orderByConditions.size() > 0) {
                List<Sort.Order> orders = orderByConditions.entrySet().stream().map(entry -> {
                    String path = entry.getKey();
                    Types.SortOrder sortOrder = entry.getValue();
                    return (sortOrder == Types.SortOrder.ASC) ? Sort.Order.asc(path) : Sort.Order.desc(path);
                }).collect(Collectors.toList());
                query.setSort(Sort.by(orders));
            } else {
                String lastModifiedDateProperty = metadataTools.findLastModifiedDateProperty(metaClass.getJavaClass());

                if (lastModifiedDateProperty != null) {
                    query.setSort(
                            Sort.by(Sort.Order.desc(lastModifiedDateProperty)));
                }
            }

            LoadContext<Object> ctx = new LoadContext<>(metaClass);
            ctx.setQuery(query);
            ctx.setFetchPlan(fetchPan);
            if (softDeletion != null) {
                ctx.setHint("jmix.softDeletion", softDeletion);
            }
            List<Object> objects;
            if (queryDataFetcherLoader.getCustomEntitiesFetcher(metaClass.getJavaClass()) == null) {
                objects = dataManager.loadList(ctx);
            } else {
                Object bean = queryDataFetcherLoader.getCustomEntitiesFetcher(metaClass.getJavaClass());
                Method method = bean.getClass().getDeclaredMethod(GRAPHQL_ENTITIES_LOADER_METHOD_NAME,
                        GraphQLEntityListDataFetcherContext.class);
                objects = (List<Object>) method.invoke(bean, new GraphQLEntityListDataFetcherContext(metaClass, ctx, condition,
                        orderByConditions, limit, offset, fetchPan));
            }

            Set<String> props = environmentUtils.getDotDelimitedProps(environment);
            List<Map<String, Object>> entitiesAsMap = objects.stream()
                    .map(e -> responseBuilder.buildResponse((Entity) e, fetchPan, metaClass, props))
                    .collect(Collectors.toList());

            log.debug("loadEntities return {} objects for {}", entitiesAsMap.size(), metaClass.getName());
            return entitiesAsMap;
        };
    }

    /**
     * Convert graphql orderBy object to jmix format.
     *
     * @param metaClass
     * @param orderBy   - graphql orderBy object
     * @return an ordered set of pairs that contains propertyPath as key ans SortOrder as value
     */
    protected OrderedMap<String, Types.SortOrder> buildOrderByConditionSet(MetaClass metaClass, @Nullable Object orderBy) {
        OrderedMap<String, Types.SortOrder> result = new ListOrderedMap<>();
        if (orderBy == null) {
            return result;
        }

        String rootPath = "";
        if (Collection.class.isAssignableFrom(orderBy.getClass())) {
            ((Collection<?>) orderBy).forEach(ob -> {
                List<Pair<String, Types.SortOrder>> singleOrderByConditions = buildOrderBy(metaClass, rootPath, ob);
                singleOrderByConditions.forEach(singleOrderByCondition -> {
                    result.put(singleOrderByCondition.getKey(), singleOrderByCondition.getValue());
                });
            });
        } else {
            List<Pair<String, Types.SortOrder>> singleOrderByConditions = buildOrderBy(metaClass, rootPath, orderBy);
            singleOrderByConditions.forEach(singleOrderByCondition -> {
                result.put(singleOrderByCondition.getKey(), singleOrderByCondition.getValue());
            });
        }
        return result;
    }

    /**
     * Convert graphql orderBy object to jmix format.
     *
     * @param metaClass
     * @param path      - parent property path
     * @param orderBy   - graphql orderBy object
     * @return pair that contains propertyPath as key ans SortOrder as value
     */
    protected List<Pair<String, Types.SortOrder>> buildOrderBy(MetaClass metaClass, String path, Object orderBy) {
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) orderBy).entrySet()) {
            String key = entry.getKey();
            Object valueObj = entry.getValue();
            Class<?> valueClass = valueObj.getClass();

            if (valueObj instanceof List) {
                List<Pair<String, Types.SortOrder>> sortOrder = new ArrayList<>();
                MetaClass propertyMetaClass = metaClass.findProperty(key).getRange().asClass();
                for (Object order : (List<?>) valueObj) {
                    if (order instanceof Map) {
                        order = processInnerOrderBy(propertyMetaClass, key, (Map<String, Object>) order);
                    }
                    sortOrder.addAll(buildOrderBy(propertyMetaClass, key, order));
                }
                return sortOrder;
            }

            if (Map.class.isAssignableFrom(valueClass)) {
                MetaClass propertyMetaClass = metaClass.findProperty(key).getRange().asClass();
                Map<String, Object> innerOrderBy = processInnerOrderBy(propertyMetaClass, key, (Map<String, Object>) valueObj);
                return buildOrderBy(metaClass, key, innerOrderBy);
            }

            String propertyPath = StringUtils.isBlank(path) ? key : path + "." + key;

            if (Types.SortOrder.class.isAssignableFrom(valueObj.getClass())) {
                return Collections.singletonList(new ImmutablePair<>(propertyPath, ((Types.SortOrder) valueObj)));
            }

            if (String.class.isAssignableFrom(valueClass)) {
                String value = (String) valueObj;
                return Collections.singletonList(new ImmutablePair<>(propertyPath, Types.SortOrder.valueOf(value)));
            }

            if (Collection.class.isAssignableFrom(valueClass)) {
                valueObj = ((Collection<?>) valueObj).iterator().next();
                if (Types.SortOrder.class.isAssignableFrom(valueObj.getClass())) {
                    return Collections.singletonList(new ImmutablePair<>(propertyPath, ((Types.SortOrder) valueObj)));
                }
                throw new UnsupportedOperationException("Can't parse orderBy value from value class " + valueObj.getClass());
            }

            throw new UnsupportedOperationException("Can't parse orderBy value from value class " + valueObj.getClass());
        }
        return Collections.emptyList();
    }

    private Map<String, Object> processInnerOrderBy(MetaClass metaClass, String property, Map<String, Object> valueObj) {
        Map<String, Object> innerOrderBy = new LinkedHashMap<>();
        for (String key : valueObj.keySet()) {
            if (key.equals(SYS_ATTR_INSTANCE_NAME)) {
                for (MetaProperty metaProperty : metadataTools.getInstanceNameRelatedProperties(metaClass)) {
                    innerOrderBy.put(metaProperty.getName(), valueObj.get(key));
                }
            } else {
                innerOrderBy.put(key, valueObj.get(key));
            }
        }
        return innerOrderBy;
    }

    public DataFetcher<?> countEntities(MetaClass metaClass) {

        return environment -> {
            checkCanReadEntity(metaClass);

            Object filter = environment.getArgument(NamingUtils.FILTER);
            Boolean softDeletion = environment.getArgument(NamingUtils.SOFT_DELETION);
            log.debug("countEntities: metClass:{}, filter:{}, softDeletion: {}", metaClass, filter, softDeletion);

            LogicalCondition condition = createCondition(filter);
            LoadContext.Query query = generateQuery(metaClass, condition);

            LoadContext<? extends Entity> lc = new LoadContext<>(metaClass);
            lc.setQuery(query);
            if (softDeletion != null) {
                lc.setHint("jmix.softDeletion", softDeletion);
            }
            long count;
            if (queryDataFetcherLoader.getCustomCountFetcher(metaClass.getJavaClass()) == null) {
                count = dataManager.getCount(lc);
            } else {
                Object bean = queryDataFetcherLoader.getCustomCountFetcher(metaClass.getJavaClass());
                Method method = bean.getClass().getDeclaredMethod(GRAPHQL_COUNT_LOADER_METHOD_NAME, GraphQLEntityCountDataFetcherContext.class);
                count = (Long) method.invoke(bean, new GraphQLEntityCountDataFetcherContext(metaClass, lc,
                        condition));
            }
            log.debug("countEntities return {} for {}", count, metaClass.getName());
            return count;
        };
    }

    // todo methods above copypasted from 'jmix-rest'
    protected void checkCanReadEntity(MetaClass metaClass) {
        CrudEntityContext entityContext = applyEntityConstraints(metaClass);
        if (!entityContext.isReadPermitted()) {
            String exceptionMessage = String.format("Reading of the %s is forbidden", metaClass.getName());
            log.warn("checkCanReadEntity: throw exception {}", exceptionMessage);
            throw new GqlEntityValidationException(String.format("Reading of the %s is forbidden", metaClass.getName()));
        }
    }

    protected CrudEntityContext applyEntityConstraints(MetaClass metaClass) {
        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext;
    }

    @Nullable
    protected LogicalCondition createCondition(Object filter) {
        LogicalCondition condition = null;
        if (filter != null && Collection.class.isAssignableFrom(filter.getClass())) {
                /*
                root conditions always aggregated by 'and', 'or' - could be achieved to add the only one 'or' condition in next level
                carList (filter: {OR: [
                      {manufacturer: {EQ: "TESLA"}}
                      {manufacturer: {EQ: "TATA"}}
                    ]})
                 */
            condition = LogicalCondition.and(
                    filterConditionBuilder.buildCollectionOfConditions("", (Collection<Map<String, Object>>) filter)
                            .toArray(new Condition[0]));
        }

        return condition;
    }

    @NotNull
    protected LoadContext.Query generateQuery(MetaClass metaClass, LogicalCondition condition) {
        LoadContext.Query query = new LoadContext.Query("select e from " + metaClass.getName() + " e");
        if (condition != null) {
            query.setCondition(condition);
        }

        return query;
    }


}
