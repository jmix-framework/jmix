package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetcher;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class EntityQueryDataFetcher {

    public static final int DEFAULT_MAX_RESULTS = 100;

    private final Logger log = LoggerFactory.getLogger(EntityQueryDataFetcher.class);

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
            lc.setFetchPlan(dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment));

            log.debug("loadEntity: {}", lc);
            return dataManager.load(lc);
        };
    }

    public DataFetcher<List<Object>> loadEntities(MetaClass metaClass) {
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
                condition = filterConditionBuilder.buildCollectionOfConditions("", (Collection<?>) filter);
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

            log.debug("loadEntities return {} objects for {}", objects.size(), metaClass.getName());
            return objects;
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
