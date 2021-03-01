package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetchingEnvironment;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetadataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataFetcherPlanBuilder {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Metadata metadata;

    private final static Logger log = LoggerFactory.getLogger(DataFetcherPlanBuilder.class);

    public <E extends Entity> FetchPlan buildFetchPlan(Class<E> entityClass, DataFetchingEnvironment environment) {
        List<String> properties = EnvironmentUtils.getEntityProperties(environment);
        log.debug("properties {}", properties);

        // todo inject correctly
        io.jmix.core.FetchPlanBuilder fetchPlanBuilder = context.getBean(io.jmix.core.FetchPlanBuilder.class, entityClass);

        // todo support _instName for nested entities too
        if (EnvironmentUtils.hasInstanceNameProperty(environment)) {
            Collection<String> instanceNameRelatedProperties = metadataTools
                    .getInstanceNameRelatedProperties(metadata.getClass(entityClass)).stream()
                    .map(MetadataObject::getName)
                    .collect(Collectors.toList());

            fetchPlanBuilder.addAll(instanceNameRelatedProperties.toArray(new String[]{}));
        }

        return fetchPlanBuilder
                .addAll(properties.toArray(new String[] {}))
                .build();
    }


}
