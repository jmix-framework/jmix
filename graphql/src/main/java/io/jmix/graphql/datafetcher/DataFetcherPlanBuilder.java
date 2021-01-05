package io.jmix.graphql.datafetcher;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.graphql.schema.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class DataFetcherPlanBuilder {

    @Autowired
    private ApplicationContext context;

    private final static Logger log = LoggerFactory.getLogger(DataFetcherPlanBuilder.class);

    public <E extends Entity> FetchPlan buildFetchPlan(Class<E> entityClass, DataFetchingEnvironment environment) {
        Map<String, GraphQLFieldDefinition> definitions = environment.getSelectionSet().getDefinitions();
        log.debug("definitions {}", definitions.keySet());

        List<String> properties = definitions.keySet().stream()
                .map(def -> def.replaceAll("/", "."))
                // remove '__typename' from fetch plan
                .filter(prop -> !prop.equals(NamingUtils.SYS_ATTR_TYPENAME))
                // todo fetch failed, if we need to return instanceName in nested entity,
                //  but fetch plan does not contains attrs of nested entities required to compose instanceName
                //  i.e. for garage.car.instanceName we need to request garage.car.manufacturer and garage.car.model attrs,
                //  which are required for composing Car instanceName
                // remove 'instanceName' and '*.instanceName' attrs from fetch plan - no such attr in entity
                .filter(propertyNotMatch(NamingUtils.SYS_ATTR_INSTANCE_NAME))
                .collect(Collectors.toList());

        log.debug("properties {}", properties);

        // todo inject correctly
        io.jmix.core.FetchPlanBuilder fetchPlanBuilder = context.getBean(io.jmix.core.FetchPlanBuilder.class, entityClass);
        return fetchPlanBuilder
                .addAll(properties.toArray(new String[] {}))
                .build();
    }

    /**
     * @param property property to check
     * @return true if property NOT match 'someProperty' and '*.someProperty'
     */
    private static Predicate<String> propertyNotMatch(String property) {
        return prop -> !prop.equals(property) && !prop.matches(".*\\." + property);
    }


}
