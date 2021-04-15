package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetcher;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.graphql.schema.NamingUtils;
import io.jmix.graphql.schema.Types;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EntityQueryDataFetcher {

    public static final int DEFAULT_MAX_RESULTS = 100;

    private final Logger log = LoggerFactory.getLogger(EntityQueryDataFetcher.class);

    @Autowired
    ResponseBuilder responseBuilder;
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

    public DataFetcher<?> loadEntity(MetaClass metaClass) {
        return environment -> {

            checkCanReadEntity(metaClass);

            String id = environment.getArgument("id");
            LoadContext<?> lc = new LoadContext<>(metaClass);
            // todo support not only UUID types of id
            lc.setId(UUID.fromString(id));
            FetchPlan fetchPlan = dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment);
            lc.setFetchPlan(fetchPlan);

            log.debug("loadEntity: with context {}", lc);
            Object entity = dataManager.load(lc);
            if (entity == null) return null;

            return responseBuilder.buildResponse((Entity) entity, fetchPlan, metaClass, environmentUtils.getDotDelimitedProps(environment));
        };
    }

    public DataFetcher<List<Map<String, Object>>> loadEntities(MetaClass metaClass) {
        return environment -> {

            checkCanReadEntity(metaClass);

            Object filter = environment.getArgument(NamingUtils.FILTER);
            Object orderBy = environment.getArgument(NamingUtils.ORDER_BY);
            Integer limit = environment.getArgument(NamingUtils.LIMIT);
            Integer offset = environment.getArgument(NamingUtils.OFFSET);
            log.debug("loadEntities: metClass:{}, filter:{}, limit:{}, offset:{}, orderBy: {}",
                    metaClass, filter, limit, offset, orderBy);

            // build filter condition

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

            // fetch plan
            FetchPlan fetchPan = dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment);

            LoadContext.Query query = new LoadContext.Query("select e from " + metaClass.getName() + " e");
            if (condition != null) {
                query.setCondition(condition);
            }
            query.setMaxResults(limit != null ? limit : DEFAULT_MAX_RESULTS);
            query.setFirstResult(offset == null ? 0 : offset);

            // orderBy and sort
            Pair<String, Types.SortOrder> orderByPathAndOrder = buildOrderBy("", orderBy);
            if (orderByPathAndOrder != null) {
                String path = orderByPathAndOrder.getKey();
                Types.SortOrder sortOrder = orderByPathAndOrder.getValue();
                query.setSort(Sort.by(
                        sortOrder == Types.SortOrder.ASC ? Sort.Order.asc(path) : Sort.Order.desc(path)));
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
            List<Object> objects = dataManager.loadList(ctx);

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
     * @param path    - parent property path
     * @param orderBy - graphql orderBy object
     * @return pair that contains propertyPath as key ans SortOrder as value
     */
    @Nullable
    protected Pair<String, Types.SortOrder> buildOrderBy(String path, @Nullable Object orderBy) {
        if (orderBy == null || !Map.class.isAssignableFrom(orderBy.getClass())) {
            return null;
        }

        Map.Entry<String, Object> entry = ((Map<String, Object>) orderBy).entrySet().iterator().next();
        if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
            return buildOrderBy(entry.getKey(), entry.getValue());
        }

        String propertyPath = StringUtils.isBlank(path) ? entry.getKey() : path + "." + entry.getKey();
        return new ImmutablePair<>(propertyPath, Types.SortOrder.valueOf((String) entry.getValue()));
    }

    public DataFetcher<?> countEntities(MetaClass metaClass) {
        return environment -> {

            checkCanReadEntity(metaClass);

            LoadContext<? extends Entity> lc = new LoadContext<>(metaClass);
            long count = dataManager.getCount(lc);
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
            // todo implement correct exception class
            throw new UnsupportedOperationException(exceptionMessage);
//            throw new RestAPIException("Reading forbidden",
//                    String.format("Reading of the %s is forbidden", metaClass.getName()),
//                    HttpStatus.FORBIDDEN);
        }
    }

    protected CrudEntityContext applyEntityConstraints(MetaClass metaClass) {
        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext;
    }

}
