package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetchingEnvironment;
import io.jmix.core.*;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetadataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component("gql_DataFetcherPlanBuilder")
public class DataFetcherPlanBuilder {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Metadata metadata;
    @Autowired
    private EnvironmentUtils environmentUtils;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private FetchPlans fetchPlans;

    private final static Logger log = LoggerFactory.getLogger(DataFetcherPlanBuilder.class);

    public <E extends Entity> FetchPlan buildFetchPlan(Class<E> entityClass, DataFetchingEnvironment environment) {
        MetaClass metaClass = metadata.getClass(entityClass);
        List<String> properties = excludeForbiddenProperties(metaClass, environmentUtils.getEntityProperties(environment));

        log.debug("properties {}", properties);

        // todo inject correctly
        io.jmix.core.FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(entityClass);

        // todo support _instName for nested entities too
        if (environmentUtils.hasInstanceNameProperty(environment)) {
            Collection<String> instanceNameRelatedProperties = metadataTools
                    .getInstanceNameRelatedProperties(metaClass).stream()
                    .map(MetadataObject::getName)
                    .collect(Collectors.toList());

            fetchPlanBuilder.addAll(instanceNameRelatedProperties.toArray(new String[]{}));
        }

        return fetchPlanBuilder
                .addAll(properties.toArray(new String[]{}))
                .build();
    }

    private List<String> excludeForbiddenProperties(MetaClass metaClass, Collection<String> properties) {
        List<String> result = new ArrayList<>();
        properties.forEach(property -> {
            EntityAttributeContext attributeContext = new EntityAttributeContext(metaClass, property);
            accessManager.applyRegisteredConstraints(attributeContext);
            if (attributeContext.canView()) {
                result.add(property);
            }
        });
        return result;
    }
}
