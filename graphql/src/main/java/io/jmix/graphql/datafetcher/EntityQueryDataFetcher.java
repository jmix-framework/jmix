package io.jmix.graphql.datafetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import graphql.schema.DataFetcher;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.graphql.schema.NamingUtils;
import io.jmix.rest.api.service.filter.RestFilterParseException;
import io.jmix.rest.api.service.filter.RestFilterParseResult;
import io.jmix.rest.api.service.filter.RestFilterParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

@Component
public class EntityQueryDataFetcher {

    private final Logger log = LoggerFactory.getLogger(EntityQueryDataFetcher.class);

    @Autowired
    protected DataManager dataManager;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    protected RestFilterParser restFilterParser;
    @Autowired
    protected DataFetcherPlanBuilder dataFetcherPlanBuilder;

    public DataFetcher<?> loadEntity(MetaClass metaClass) {
        return environment -> {

            String id = environment.getArgument("id");
            log.debug("id {}", id);

            LoadContext<?> lc = new LoadContext<>(metaClass);
            // todo support not only UUID types of id
            lc.setId(UUID.fromString(id));
            lc.setFetchPlan(dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment));

            log.debug("loadEntity {}", lc);
            return dataManager.load(lc);
        };
    }

    public DataFetcher<List<Object>> loadEntities(MetaClass metaClass) {
        return environment -> {

            String sort = environment.getArgument(NamingUtils.SORT);

            Integer limit = environment.getArgument(NamingUtils.LIMIT);
            Integer offset = environment.getArgument(NamingUtils.OFFSET);

            String queryString = "select e from " + metaClass.getName() + " e";

            Map<String, Object> queryParameters = new HashMap<>();
            Object filterArg = environment.getArgument(NamingUtils.FILTER);
            if (filterArg != null) {
                RestFilterParseResult filterParseResult;
                try {
                    // todo converting to JSON not need here - rewrite parser to work with Map
                    filterParseResult = restFilterParser.parse(new ObjectMapper().writeValueAsString(filterArg), metaClass);
                } catch (RestFilterParseException e) {
                    throw new UnsupportedOperationException("Cannot parse entities filter" + e.getMessage(), e);
                }

                String jpqlWhere = filterParseResult.getJpqlWhere();
                queryParameters = filterParseResult.getQueryParameters();

                if (jpqlWhere != null) {
                    queryString += " where " + jpqlWhere.replace("{E}", "e");
                }
            }

            FetchPlan fetchPan = dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment);
            List<Object> objects = _loadEntitiesList(queryString, fetchPan, limit, offset, sort, metaClass, queryParameters);
            log.debug("loadEntities return {} objects for {}", objects.size(), metaClass.getName());
            return objects;
        };
    }

    public DataFetcher<?> countEntities(MetaClass metaClass) {
        return environment -> {
            LoadContext<? extends Entity> lc = new LoadContext<>(metaClass);
            long count = dataManager.getCount(lc);
            log.debug("countEntities return {} for {}", count, metaClass.getName());
            return count;
        };
    }

    // todo code below (#_loadEntitiesList #addOrderBy #getEntityPropertySortExpression) is copypasted from EntitiesControllerManager

    protected List<Object> _loadEntitiesList(String queryString,
                                             @Nullable FetchPlan fetchPlan,
                                             @Nullable Integer limit,
                                             @Nullable Integer offset,
                                             @Nullable String sort,
                                             MetaClass metaClass,
                                             Map<String, Object> queryParameters) {
        LoadContext<Object> ctx = new LoadContext<>(metaClass);

        String orderedQueryString = addOrderBy(queryString, sort, metaClass);
        LoadContext.Query query = new LoadContext.Query(orderedQueryString);

        if (limit != null) {
            query.setMaxResults(limit);
        } else {
            query.setMaxResults(100);
        }
        if (offset != null) {
            query.setFirstResult(offset);
        }
        if (queryParameters != null) {
            query.setParameters(queryParameters);
        }
        ctx.setQuery(query);

        if (fetchPlan != null) {
            ctx.setFetchPlan(fetchPlan);
        }

        return dataManager.loadList(ctx);
    }

    protected String addOrderBy(String queryString, @Nullable String sort, MetaClass metaClass) {
        if (Strings.isNullOrEmpty(sort)) {
            return queryString;
        }
        StringBuilder orderBy = new StringBuilder(queryString).append(" order by ");
        Iterable<String> iterableColumns = Splitter.on(",").trimResults().omitEmptyStrings().split(sort);
        for (String column : iterableColumns) {
            String order = "";
            if (column.startsWith("-") || column.startsWith("+")) {
                order = column.substring(0, 1);
                column = column.substring(1);
            }
            MetaPropertyPath propertyPath = metaClass.getPropertyPath(column);
            if (propertyPath != null) {
                switch (order) {
                    case "-":
                        order = " desc, ";
                        break;
                    case "+":
                    default:
                        order = " asc, ";
                        break;
                }
                MetaProperty metaProperty = propertyPath.getMetaProperty();
                if (metaProperty.getRange().isClass()) {
                    if (!metaProperty.getRange().getCardinality().isMany()) {
                        for (String exp : getEntityPropertySortExpression(propertyPath)) {
                            orderBy.append(exp).append(order);
                        }
                    }
                } else {
                    orderBy.append("e.").append(column).append(order);
                }
            }
        }
        return orderBy.substring(0, orderBy.length() - 2);
    }

    protected List<String> getEntityPropertySortExpression(MetaPropertyPath metaPropertyPath) {
        Collection<MetaProperty> properties = metadataTools.getInstanceNameRelatedProperties(
                metaPropertyPath.getMetaProperty().getRange().asClass());
        if (!properties.isEmpty()) {
            List<String> sortExpressions = new ArrayList<>(properties.size());
            for (MetaProperty metaProperty : properties) {
                if (metadataTools.isPersistent(metaProperty)) {
                    MetaPropertyPath childPropertyPath = new MetaPropertyPath(metaPropertyPath, metaProperty);
                    if (metaProperty.getRange().isClass()) {
                        if (!metaProperty.getRange().getCardinality().isMany()) {
                            sortExpressions.addAll(getEntityPropertySortExpression(childPropertyPath));
                        }
                    } else {
                        sortExpressions.add(String.format("e.%s", childPropertyPath.toString()));
                    }
                }
            }
            return sortExpressions;
        } else {
            return Collections.singletonList(String.format("e.%s", metaPropertyPath.toString()));
        }
    }

}
