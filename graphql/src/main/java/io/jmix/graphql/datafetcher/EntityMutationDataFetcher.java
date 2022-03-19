package io.jmix.graphql.datafetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.Entity;
import io.jmix.core.EntityImportExport;
import io.jmix.core.EntityImportPlan;
import io.jmix.core.EntityImportPlanProperty;
import io.jmix.core.EntitySerialization;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.importexport.EntityImportPlanJsonBuilder;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.validation.EntityValidationException;
import io.jmix.graphql.service.IdentifierService;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.modifier.GraphQLRemoveEntityDataFetcher;
import io.jmix.graphql.modifier.GraphQLRemoveEntityDataFetcherContext;
import io.jmix.graphql.modifier.GraphQLUpsertEntityDataFetcher;
import io.jmix.graphql.modifier.GraphQLUpsertEntityDataFetcherContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PersistenceException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component("gql_EntityMutationDataFetcher")
public class EntityMutationDataFetcher {

    private final Logger log = LoggerFactory.getLogger(EntityMutationDataFetcher.class);

    @Autowired
    ResponseBuilder responseBuilder;
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
    @Autowired
    protected DataFetcherPlanBuilder dataFetcherPlanBuilder;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected IdentifierService identifierService;
    @Autowired
    private EnvironmentUtils environmentUtils;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private ObjectProvider<ObjectMapper> objectMapperProvider;
    @Autowired
    private MutationDataFetcherLoader mutationDataFetcherLoader;

    private static final String GRAPHQL_ENTITY_REMOVER_METHOD_NAME = GraphQLRemoveEntityDataFetcher.class.getDeclaredMethods()[0].getName();

    private static final String GRAPHQL_ENTITIES_UPDATER_METHOD_NAME = GraphQLUpsertEntityDataFetcher.class.getDeclaredMethods()[0].getName();

    // todo batch commit with association not supported now (not transferred from cuba-graphql)
    public DataFetcher<?> upsertEntity(MetaClass metaClass) {

        return environment -> {
            Class<Object> javaClass = metaClass.getJavaClass();
            Map<String, String> input = environment.getArgument(NamingUtils.uncapitalizedSimpleName(javaClass));
            log.debug("upsertEntity: input {}", input);

            String entityJson = objectMapperProvider.getObject().writeValueAsString(input);
            log.debug("upsertEntity: json {}", entityJson);

            Object entity = entitySerialization.entityFromJson(entityJson, metaClass);

            EntityImportPlan entityImportPlan = entityImportPlanJsonBuilder.buildFromJson(entityJson, metaClass);
            checkReadOnlyAttributeWrite(metaClass, entityImportPlan, entity);
            populateAndCheckComposition(entity, new HashSet<>());

            Object mainEntity;
            FetchPlan fetchPlan = dataFetcherPlanBuilder.buildFetchPlan(metaClass.getJavaClass(), environment);
            if (mutationDataFetcherLoader.getCustomEntityUpsert(metaClass.getJavaClass()) == null) {
                Collection<Object> objects;
                try {
                    objects = entityImportExport.importEntities(Collections.singletonList(entity), entityImportPlan, true);
                } catch (EntityValidationException ex) {
                    throw new GqlEntityValidationException(ex, entity, metaClass);
                } catch (PersistenceException ex) {
                    throw new GqlEntityValidationException(ex, "Can't save entity to database");
                } catch (AccessDeniedException ex) {
                    throw new GqlEntityValidationException(ex, "Can't save entity to database. Access denied");
                }

                mainEntity = objects.isEmpty() ? entity : getMainEntity(objects, metaClass);

                // reload for response fetch plan, if required
                if (!entityStates.isLoadedWithFetchPlan(mainEntity, fetchPlan)) {
                    LoadContext loadContext = new LoadContext(metaClass).setFetchPlan(fetchPlan);
                    loadContext.setId(EntityValues.getId(mainEntity));
                    mainEntity = dataManager.load(loadContext);
                }

            } else {
                Object bean = mutationDataFetcherLoader.getCustomEntityUpsert(metaClass.getJavaClass());
                Method method = bean.getClass().getDeclaredMethod(GRAPHQL_ENTITIES_UPDATER_METHOD_NAME,
                        GraphQLUpsertEntityDataFetcherContext.class);
                mainEntity = method.invoke(bean, new GraphQLUpsertEntityDataFetcherContext(metaClass,
                        Collections.singletonList(entity), entityImportPlan));
            }

            return responseBuilder.buildResponse((Entity) mainEntity, fetchPlan, metaClass,
                    environmentUtils.getDotDelimitedProps(environment));
        };
    }

    public DataFetcher<?> deleteEntity(MetaClass metaClass) {

        return environment -> {
            try {
                checkCanDeleteEntity(metaClass);
            } catch (PersistenceException ex) {
                throw new GqlEntityValidationException(ex, ex.getMessage());
            }
            Object id = identifierService.parse(environment.getArgument("id"), metaClass);
            log.debug("deleteEntity: id {}", id);
            Id<?> entityId = Id.of(id, metaClass.getJavaClass());
            if (mutationDataFetcherLoader.getCustomEntityRemover(metaClass.getJavaClass()) == null) {
                dataManager.remove(entityId);
            } else {
                Object bean = mutationDataFetcherLoader.getCustomEntityRemover(metaClass.getJavaClass());
                Method method = bean.getClass().getDeclaredMethod(GRAPHQL_ENTITY_REMOVER_METHOD_NAME,
                        GraphQLRemoveEntityDataFetcherContext.class);
                method.invoke(bean, new GraphQLRemoveEntityDataFetcherContext(metaClass, entityId));
            }
            return null;
        };
    }

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

    protected void checkCanDeleteEntity(MetaClass metaClass) {
        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        if (!entityContext.isDeletePermitted()) {
            throw new PersistenceException(
                    String.format("Deletion of the %s is forbidden", metaClass.getName()));
        }
    }

    protected void checkReadOnlyAttributeWrite(MetaClass metaClass, EntityImportPlan importPlan, Object entity) {
        List<String> readOnlyAttributes = importPlan.getProperties().stream()
                .map(property -> metaClass.getProperty(property.getName()))
                .filter(MetaProperty::isReadOnly)
                .map(MetaProperty::getName)
                .collect(Collectors.toList());

        if (!readOnlyAttributes.isEmpty()) {
            throw new GqlEntityValidationException("Modifying read-only attributes is forbidden " + readOnlyAttributes);
        }

        List<String> availableProperties = importPlan.getProperties().stream()
                .map(EntityImportPlanProperty::getName)
                .collect(Collectors.toList());

        Object id = EntityValues.getId(entity);
        List<String> excludedProperties = metaClass.getProperties().stream()
                .filter(metaProperty -> !metaProperty.isReadOnly())
                .filter(metaProperty -> {
                    Object value = EntityValues.getValue(entity, metaProperty.getName());
                    return value != null && !value.equals(id);
                })
                .map(MetaProperty::getName)
                .filter(name -> !availableProperties.contains(name))
                .collect(Collectors.toList());

        if (!excludedProperties.isEmpty()) {
            String message = "Modifying attributes is forbidden " + excludedProperties;
            log.error(message);
            throw new GqlEntityValidationException(message);
        }
    }

    /**
     * Walk through entity graph and check that all compositions have correct relations. If composition inverse value is
     * null - update it to correct value. If composition inverse value doesn't match parent - throw exception.
     *
     * @param entity  entity to be checked
     * @param visited a set of entities that already be checked in graph
     */
    protected void populateAndCheckComposition(Object entity, Set<Object> visited) {
        if (visited.contains(entity)) {
            return;
        }
        MetaClass metaClass = metadata.getClass(entity);
        visited.add(entity);
        metaClass.getProperties().stream()
                .filter(metaProperty -> MetaProperty.Type.COMPOSITION.equals(metaProperty.getType()))
                .filter(metaProperty -> !visited.contains(EntityValues.getValue(entity, metaProperty.getName())))
                .filter(metaProperty -> EntityValues.getValue(entity, metaProperty.getName()) != null)
                .forEach(metaProperty -> {
                    //value from COMPOSITION metaProperty
                    Object childEntity = EntityValues.getValue(entity, metaProperty.getName());
                    Stream<?> childEntityStream = (childEntity instanceof Iterable<?>)
                            ? StreamSupport.stream(((Iterable<?>) childEntity).spliterator(), false)
                            : Stream.of(childEntity);

                    childEntityStream.forEach(childElement -> {
                        //have to check a linking for retrieved composition with a parent it can be different
                        assureCompositionInverseLink(entity, metaClass, metaProperty, childElement);
                        //digging deeper for child element to find any compositions inside
                        populateAndCheckComposition(childElement, visited);
                    });
                });
    }

    /**
     * Check that inverse link is set correctly in composition relation. If link is null - fix it (assign to parent).
     * If link point to different entity (not equals to parent) - throw exception.
     *
     * @param parent          - parent entity
     * @param parentMetaClass - meta class of parent entity
     * @param metaProperty    - metadata of property which points to child in parent entity
     * @param child           - child entity
     */
    protected void assureCompositionInverseLink(Object parent, MetaClass parentMetaClass, MetaProperty metaProperty, Object child) {
        MetaProperty inverseMetaProperty = metaProperty.getInverse();
        if (inverseMetaProperty == null) {
            return;
        }

        Object inverseValue = EntityValues.getValue(child, inverseMetaProperty.getName());
        if (inverseValue == null) {
            //inverse value is null - update it to correct value
            EntityValues.setValue(child, inverseMetaProperty.getName(), parent);
        } else {
            if (!parent.equals(inverseValue)) {
                // parent id doesn't match parent in graph - throw exception
                String message = String.format(
                        "Composition attribute '%s' in class '%s' doesn't contain the correct link to parent entity. " +
                                "Please set correct parent ID '%s' in composition relation.",
                        metaProperty.getName(),
                        parentMetaClass.getName(),
                        EntityValues.getId(parent));
                log.error(message);
                throw new GqlEntityValidationException(message);
            }
        }
    }


}
