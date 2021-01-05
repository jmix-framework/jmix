package io.jmix.graphql.datafetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import io.jmix.core.*;
import io.jmix.core.impl.importexport.EntityImportPlanJsonBuilder;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.graphql.schema.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EntityMutationDataFetcher {

    private final Logger log = LoggerFactory.getLogger(EntityMutationDataFetcher.class);

    @Autowired
    private Metadata metadata;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected EntitySerialization entitySerialization;
    @Autowired
    EntityImportPlanJsonBuilder entityImportPlanJsonBuilder;
    @Autowired
    protected EntityImportExport entityImportExport;


    // todo batch commit with association not supported now (not transferred from cuba-graphql)
    public DataFetcher<?> createEntity(MetaClass metaClass) {
        return environment -> {

            Class<Object> javaClass = metaClass.getJavaClass();
            Map<String, String> input = environment.getArgument(NamingUtils.uncapitalizedSimpleName(javaClass));
            log.debug("createEntity: input {}", input);

            String entityJson =  new ObjectMapper().writeValueAsString(input);
            log.debug("createEntity: json {}", entityJson);

            Object entity = entitySerialization.entityFromJson(entityJson, metaClass);

            EntityImportPlan entityImportPlan = entityImportPlanJsonBuilder.buildFromJson(entityJson, metaClass);
            log.debug("createEntity: entityImportPlan {}", entityImportPlan);
            Collection<Object> objects = entityImportExport.importEntities(Collections.singletonList(entity), entityImportPlan);
            return getMainEntity(objects, metaClass);
        };
    }

    public DataFetcher<?> deleteEntity(MetaClass metaClass) {
        return environment -> {
            // todo support not only UUID types of id
            UUID id = UUID.fromString(environment.getArgument("id"));
            log.debug("deleteEntity: id {}", id);
            Id<?> entityId = Id.of(id, metaClass.getJavaClass());
            dataManager.remove(entityId);
            return null;
        };
    }

    /**
     * Finds entity with given metaClass.
     */
    protected Object getMainEntity(Collection<Object> importedEntities, MetaClass metaClass) {
        Object mainEntity = null;
        if (importedEntities.size() > 1) {
            Optional<Object> first = importedEntities.stream().filter(e -> metadata.getClass(e).equals(metaClass)).findFirst();
            if (first.isPresent()) mainEntity = first.get();
        } else {
            mainEntity = importedEntities.iterator().next();
        }
        return mainEntity;
    }


}
