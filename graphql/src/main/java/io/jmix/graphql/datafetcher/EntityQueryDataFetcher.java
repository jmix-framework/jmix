package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetcher;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
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
    MetadataTools metadataTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected DataFetcherPlanBuilder dataFetcherPlanBuilder;
    @Autowired
    protected FilterConditionBuilder filterConditionBuilder;

    public DataFetcher<?> loadEntity(MetaClass metaClass) {
        return environment -> {
            String id = environment.getArgument("id");
            LoadContext<?> lc = new LoadContext<>(metaClass);
            // todo support not only UUID types of id
            lc.setId(UUID.fromString(id));
            FetchPlan fetchPlan = dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment);
            lc.setFetchPlan(fetchPlan);

            log.debug("loadEntity: with context {}", lc);
            Object entity = dataManager.load(lc);
            if (entity == null) return null;

            return buildResponse((Entity) entity, fetchPlan, metaClass, EnvironmentUtils.getDotDelimitedProps(environment));
        };
    }

    /**
     * Convert loaded entity to data fetcher return format (Map<String, Object>)
     *
     * @param entity loaded entity
     * @param fetchPlan loaded entity properties
     * @param metaClass entity meta class
     * @param props we need pass full set of properties to have information about system props such '_instanceName'
     * @return entity converted to response as Map<String, Object>
     */
    protected Map<String, Object> buildResponse(Entity entity, FetchPlan fetchPlan, MetaClass metaClass, Set<String> props) {
        Map<String, Object> entityAsMap = new HashMap<>();

        // check and evaluate _instanceName, if required
        if (EnvironmentUtils.hasInstanceNameProperty(props)) {
            entityAsMap.put(NamingUtils.SYS_ATTR_INSTANCE_NAME, metadataTools.getInstanceName(entity));
        }

        // compose result object by iterating over fetch plan props
        fetchPlan.getProperties().forEach(prop -> {

            String propName = prop.getName();
            MetaProperty metaProperty = metaClass.getProperty(propName);
            Object fieldValue = EntityValues.getValue(entity, propName);
            Range propertyRange = metaProperty.getRange();

            if (fieldValue == null) {
                entityAsMap.put(propName, null);
                return;
            }

            if (propertyRange.isDatatype() || propertyRange.isEnum()) {
                entityAsMap.put(propName, fieldValue);
                return;
            }

            if (propertyRange.isClass()) {
                Set<String> nestedProps = EnvironmentUtils.getNestedProps(props, propName);

                if (fieldValue instanceof Entity) {
                    entityAsMap.put(propName, buildResponse((Entity) fieldValue, prop.getFetchPlan(), propertyRange.asClass(), nestedProps));
                    return;
                }

                if (fieldValue instanceof Collection) {
                    Collection<Object> values = ((Collection<Entity>)fieldValue).stream()
                            .map(e -> buildResponse(e, prop.getFetchPlan(), propertyRange.asClass(), nestedProps))
                            .collect(Collectors.toList());
                    entityAsMap.put(propName, values);
                    return;
                }
            }

            log.warn("buildResponse: failed for {}.{} unsupported range type ", metaClass.getName(), prop.getName());
            throw new IllegalStateException("Unsupported range type " + propertyRange);
        });
        return entityAsMap;
    }

    public DataFetcher<List<Map<String, Object>>> loadEntities(MetaClass metaClass) {
        return environment -> {

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
            }

            LoadContext<Object> ctx = new LoadContext<>(metaClass);
            ctx.setQuery(query);
            ctx.setFetchPlan(fetchPan);
            List<Object> objects = dataManager.loadList(ctx);

            Set<String> props = EnvironmentUtils.getDotDelimitedProps(environment);
            List<Map<String, Object>> entitiesAsMap = objects.stream()
                    .map(e -> buildResponse((Entity) e, fetchPan, metaClass, props))
                    .collect(Collectors.toList());

            log.debug("loadEntities return {} objects for {}", entitiesAsMap.size(), metaClass.getName());
            return entitiesAsMap;
        };
    }

    /**
     * Convert graphql orderBy object to jmix format.
     *
     * @param orderBy - graphql orderBy object
     * @return pair that contains propertyPath as key ans SortOrder as value
     */
    @Nullable protected Pair<String, Types.SortOrder> buildOrderBy(String path, @Nullable Object orderBy) {
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
            LoadContext<? extends Entity> lc = new LoadContext<>(metaClass);
            long count = dataManager.getCount(lc);
            log.debug("countEntities return {} for {}", count, metaClass.getName());
            return count;
        };
    }

}
