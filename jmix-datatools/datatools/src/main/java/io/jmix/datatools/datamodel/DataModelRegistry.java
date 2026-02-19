package io.jmix.datatools.datamodel;

import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.persistence.DbmsType;
import io.jmix.datatools.datamodel.engine.DiagramService;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides information about entity data models organized by data stores.
 */
@Component("datatl_DataModelProvider")
public class DataModelRegistry {

    private static final Logger log = LoggerFactory.getLogger(DataModelRegistry.class);

    @Autowired
    protected DbmsType dbmsType;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataSource dataSource;
    @Autowired
    protected DiagramService diagramService;

    protected final Map<String, Map<String, DataModel>> dataModels = new HashMap<>();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected volatile boolean initialized;

    /**
     * Make the registry to reload data models on the next request.
     */
    public void reset() {
        initialized = false;
    }

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        long startTime = System.currentTimeMillis();

        dataModels.clear();
        constructDataModel();

        log.info("{} initialized in {} ms", getClass().getSimpleName(), System.currentTimeMillis() - startTime);
    }

    protected void constructDataModel() {
        Collection<MetaClass> metaClasses = metadata.getClasses();

        for (MetaClass metaClass : metaClasses) {
            // TODO: gg, refactor
            if (metaClass.getJavaClass().isAnnotationPresent(Entity.class)
                    && metaClass.getJavaClass().isAnnotationPresent(Table.class)) {
                createEntityDescription(metaClass, false);
            }
        }
    }

    protected DataModel createEntityDescription(MetaClass entity, boolean isEmbeddable) {
        List<AttributeModel> attributeModelsList = new ArrayList<>();
        List<MetaProperty> fields = entity.getProperties().stream().toList();
        Map<RelationType, List<Relation>> relationsMap = new HashMap<>();
        String dataStoreName = entity.getStore().getName();

        for (MetaProperty field : fields) {
            String fieldName = field.getName();

            if (field.getType().equals(MetaProperty.Type.DATATYPE)
                    && isAnnotationPresent(field, Column.class)) {
                addDatatypeAttribute(entity, isEmbeddable, field, fieldName, attributeModelsList);

            } else if (field.getType().equals(MetaProperty.Type.EMBEDDED)) {
                addEmbeddedAttribute(entity, field, fieldName, attributeModelsList);

            } else if (isAnnotationPresent(field, ManyToOne.class)) {
                addManyToOneAttribute(entity, isEmbeddable, field, fieldName,
                        attributeModelsList, dataStoreName, relationsMap);

            } else if (isAnnotationPresent(field, OneToMany.class)) {
                addOneToManyAttribute(entity, field, fieldName, dataStoreName, relationsMap, attributeModelsList);

            } else if (isAnnotationPresent(field, OneToOne.class)) {
                addOneToOneAttribute(entity, isEmbeddable, field, fieldName,
                        dataStoreName, relationsMap, attributeModelsList);

            } else if (isAnnotationPresent(field, ManyToMany.class)) {
                addManyToManyAttribute(entity, field, fieldName, dataStoreName, relationsMap, attributeModelsList);

            } else {
                log.warn("Cannot generate data model description for '{}'", field);
            }
        }

        boolean isSystem = entity.getJavaClass().isAnnotationPresent(SystemLevel.class);

        String currentEntityType = entity.getName();
        String entityDescription = diagramService
                .constructEntityDescription(currentEntityType, dataStoreName, attributeModelsList);

        EntityModel entityModel = constructEntityModel(entity, isSystem);
        DataModel dataModel = new DataModel(
                currentEntityType,
                entity.getStore().getName(),
                entityModel,
                relationsMap,
                entityDescription,
                attributeModelsList
        );

        putDataModel(dataModel);

        return dataModel;
    }

    protected void addDatatypeAttribute(MetaClass entity, boolean isEmbeddable, MetaProperty field,
                                        String fieldName, List<AttributeModel> attributeModelsList) {
        String fieldType = field.getJavaType().getSimpleName();

        AttributeModel attributeModel = isEmbeddable
                ? constructAttribute(fieldName, fieldType, field.isMandatory())
                : constructAttribute(getAnnotation(field, Column.class).name(),
                fieldName, fieldType, entity, field.isMandatory());
        attributeModelsList.add(attributeModel);
    }

    protected void addEmbeddedAttribute(MetaClass entity, MetaProperty field, String fieldName,
                                        List<AttributeModel> attributeModelsList) {
        MetaClass embeddableClass = metadata.findClass(field.getJavaType());

        if (embeddableClass == null) {
            throw new IllegalStateException("Embeddable class not found");
        }

        DataModel embeddableDataModel = createEntityDescription(embeddableClass, true);
        String fieldType = field.getJavaType().getSimpleName();

        if (isAnnotationPresent(field, AttributeOverrides.class)) {
            AttributeOverride[] attributeOverrides = getAnnotation(field, AttributeOverrides.class).value();

            for (int i = 0; i < attributeOverrides.length; i++) {
                AttributeModel temp = constructAttribute(field.getAnnotatedElement()
                                .getAnnotation(AttributeOverrides.class).value()[i].column().name(),
                        fieldName, fieldType, entity, field.isMandatory());
                AttributeModel dataModelEntityAttributes = embeddableDataModel.attributeModels().get(i);
                String embeddableFieldName = fieldName + "." + dataModelEntityAttributes.getAttributeName();
                String embeddableJavaType = dataModelEntityAttributes.getJavaType();

                AttributeModel embeddableAttribute = constructAttribute(temp.getColumnName(),
                        embeddableFieldName, embeddableJavaType, temp.getDbType(),
                        isAnnotationPresent(field, NotNull.class));

                attributeModelsList.add(embeddableAttribute);
            }
        }
    }

    protected void addManyToOneAttribute(MetaClass entity, boolean isEmbeddable, MetaProperty field, String fieldName,
                                         List<AttributeModel> attributeModelsList, String dataStoreName,
                                         Map<RelationType, List<Relation>> relationsMap) {
        String fieldType = field.getJavaType().getSimpleName();

        AttributeModel attributeModel = isEmbeddable
                ? constructAttribute(fieldName, fieldType, isAnnotationPresent(field, NotNull.class))
                : constructAttribute(getAnnotation(field, JoinColumn.class).name(),
                fieldName, fieldType, entity, field.isMandatory());
        attributeModelsList.add(attributeModel);

        String relationDescription = diagramService.constructRelationDescription(entity.getName(),
                fieldType, RelationType.MANY_TO_ONE, dataStoreName);
        Relation relation = new Relation(dataStoreName, fieldType, relationDescription);
        putRelation(relationsMap, RelationType.MANY_TO_ONE, relation);
    }

    protected void addOneToManyAttribute(MetaClass entity, MetaProperty field, String fieldName,
                                         String dataStoreName, Map<RelationType, List<Relation>> relationsMap,
                                         List<AttributeModel> attributeModelsList) {
        String fieldType = field.getRange().asClass().getName();

        AttributeModel attributeModel =
                constructAttribute(fieldName, fieldType, isAnnotationPresent(field, NotNull.class));

        String relationDescription = diagramService.constructRelationDescription(entity.getName(),
                fieldType, RelationType.ONE_TO_MANY, dataStoreName);
        Relation relation = new Relation(dataStoreName, fieldType, relationDescription);

        putRelation(relationsMap, RelationType.ONE_TO_MANY, relation);
        attributeModelsList.add(attributeModel);
    }

    protected void addOneToOneAttribute(MetaClass entity, boolean isEmbeddable, MetaProperty field, String fieldName,
                                        String dataStoreName, Map<RelationType, List<Relation>> relationsMap,
                                        List<AttributeModel> attributeModelsList) {
        AttributeModel attributeModel;
        String fieldType = field.getJavaType().getSimpleName();

        if (isAnnotationPresent(field, JoinColumn.class)) {
            if (isEmbeddable) {
                attributeModel = constructAttribute(fieldName, fieldType, isAnnotationPresent(field, NotNull.class));
            } else {
                attributeModel = constructAttribute(getAnnotation(field, JoinColumn.class).name(),
                        fieldName, fieldType, entity, field.isMandatory());
            }
        } else {
            boolean isMandatory = getAnnotation(field, OneToOne.class).optional();
            attributeModel = constructAttribute(fieldName, fieldType, isMandatory);
        }

        String relationDescription = diagramService.constructRelationDescription(entity.getName(),
                fieldType, RelationType.ONE_TO_ONE, dataStoreName);
        Relation relation = new Relation(dataStoreName, fieldType, relationDescription);

        putRelation(relationsMap, RelationType.ONE_TO_ONE, relation);
        attributeModelsList.add(attributeModel);
    }

    protected void addManyToManyAttribute(MetaClass entity, MetaProperty field, String fieldName,
                                          String dataStoreName, Map<RelationType, List<Relation>> relationsMap,
                                          List<AttributeModel> attributeModelsList) {
        String fieldType = field.getRange().asClass().getName();

        AttributeModel attributeModel = constructAttribute(fieldName, fieldType, isAnnotationPresent(field, NotNull.class));

        JoinTable annotation = getAnnotation(field, JoinTable.class);
        String columnName = annotation.name()
                + "."
                + annotation.joinColumns()[0].name();
        attributeModel.setColumnName(columnName);

        String relationDescription = diagramService.constructRelationDescription(entity.getName(),
                fieldType, RelationType.MANY_TO_MANY, dataStoreName);
        Relation relation = new Relation(dataStoreName, fieldType, relationDescription);

        putRelation(relationsMap, RelationType.MANY_TO_MANY, relation);
        attributeModelsList.add(attributeModel);
    }

    protected EntityModel constructEntityModel(MetaClass entity, boolean isSystem) {
        EntityModel entityModel = metadata.create(EntityModel.class);

        entityModel.setName(entity.getName());
        entityModel.setDataStore(entity.getStore().getName());
        entityModel.setIsSystem(isSystem);
        entityModel.setTableName(entity.getJavaClass().isAnnotationPresent(Table.class)
                ? entity.getJavaClass().getAnnotation(Table.class).name()
                : "");

        return entityModel;
    }

    protected void putRelation(Map<RelationType, List<Relation>> relations,
                               RelationType relationType, Relation relation) {
        if (relations.containsKey(relationType)) {
            relations.get(relationType).add(relation);
        } else {
            relations.put(relationType, new ArrayList<>(List.of(relation)));
        }
    }

    protected AttributeModel constructAttribute(String fieldName, String fieldType) {
        AttributeModel attributeModel = metadata.create(AttributeModel.class);

        attributeModel.setAttributeName(fieldName);
        attributeModel.setJavaType(fieldType);

        return attributeModel;
    }

    protected AttributeModel constructAttribute(String fieldName, String fieldType,
                                                boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    protected AttributeModel constructAttribute(String columnName, String fieldName,
                                                String fieldType, MetaClass entity,
                                                boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setColumnName(columnName);
        attributeModel.setDbType(getDatabaseColumnType(entity, attributeModel, columnName));
        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    protected AttributeModel constructAttribute(String columnName, String fieldName,
                                                String fieldType, String dbType,
                                                boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setColumnName(columnName);
        attributeModel.setDbType(dbType);
        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    protected String getDatabaseColumnType(@javax.annotation.Nullable String schemaName,
                                           @javax.annotation.Nullable String catalogName,
                                           String tableName,
                                           String columnName,
                                           @javax.annotation.Nullable AttributeModel attributeModel) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData dbMetaData = conn.getMetaData();

            try (ResultSet columns = dbMetaData.getColumns(
                    catalogName,
                    schemaName,
                    tableName,
                    columnName)) {

                if (columns.next()) {
                    String typeName = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                    if (attributeModel != null) {
                        attributeModel.setIsNullable(columns.getString("IS_NULLABLE").equals("YES"));
                    }

                    StringBuilder type = new StringBuilder(typeName);

                    if (columnSize > 0) {
                        type.append("(").append(columnSize);
                        if (decimalDigits > 0) {
                            type.append(",").append(decimalDigits);
                        }
                        type.append(")");
                    }

                    return type.toString();
                } else {
                    throw new IllegalStateException("Column: " + columnName + " is not found in table: " + tableName);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error while receiving from database", e);
        }
    }

    protected String getDatabaseColumnType(MetaClass entity, AttributeModel attributeModel, String columnName) {
        Table annotation = entity.getJavaClass().getAnnotation(Table.class);
        String tableName;
        String catalogName = annotation.catalog().isEmpty()
                ? null
                : annotation.catalog().toUpperCase();
        String schemaName = annotation.schema().isEmpty()
                ? null
                : annotation.schema().toUpperCase();

        if (dbmsType.getType(entity.getStore().getName()).equals("POSTGRESQL")) {
            tableName = annotation.name().toLowerCase();
            columnName = columnName.toLowerCase();
        } else {
            tableName = annotation.name().toUpperCase();
            columnName = columnName.toUpperCase();
        }

        return getDatabaseColumnType(schemaName, catalogName, tableName, columnName, attributeModel);
    }

    protected void putDataModel(DataModel dataModel) {
        String dataStore = dataModel.dataStore();
        String entityName = dataModel.entityName();

        dataModels.computeIfAbsent(dataStore, __ -> new HashMap<>())
                .put(entityName, dataModel);
    }

    public Set<String> getDataStoreNames() {
        lock.readLock().lock();
        try {
            checkInitialized();

            return Collections.unmodifiableSet(dataModels.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves the {@link DataModel} associated with the given data store and entity name.
     *
     * @param dataStore  the name of the data store from which the data model is to be fetched
     * @param entityName the name of the entity whose data model is to be fetched
     * @return the {@link DataModel} corresponding to the specified data store and entity name,
     * or {@code null} if no such data model exists
     */
    @Nullable
    public DataModel getDataModel(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        return getDataModels(dataStore).get(entityName);
    }

    /**
     * Determines whether the given entity in the specified data store has any relationships defined.
     *
     * @param dataStore  the name of the data store to which the entity belongs; must not be null
     * @param entityName the name of the entity to check for relationships; must not be null
     * @return {@code true} if the entity has at least one relationship defined in the data model,
     * {@code false} otherwise
     */
    public boolean hasRelations(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        Map<String, DataModel> dataModels = getDataModels(dataStore);
        if (dataModels.isEmpty()) {
            return false;
        }

        if (dataModels.containsKey(entityName)) {
            DataModel dataModel = dataModels.get(entityName);
            return dataModel != null && !dataModel.relations().isEmpty();
        }

        return false;
    }

    /**
     * Retrieves the relationships defined for a specific entity in the data model of a given data store.
     *
     * @param dataStore  the name of the data store containing the desired entity; must not be null
     * @param entityName the name of the entity for which the relationships are to be retrieved; must not be null
     * @return a map where the keys specify the type of relationships (e.g., MANY_TO_ONE, ONE_TO_MANY)
     * and the values are lists of {@link Relation} objects detailing the relationships;
     * if the entity or data store has no relationships, an empty map is returned
     */
    public Map<RelationType, List<Relation>> getEntityRelations(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        DataModel dataModel = getDataModels(dataStore).get(entityName);
        return dataModel != null
                ? Collections.unmodifiableMap(dataModel.relations())
                : Collections.emptyMap();
    }

    /**
     * Retrieves a list of attributes for a specific entity within a given data store.
     *
     * @param dataStore  the name of the data store containing the entity; must not be null
     * @param entityName the name of the entity whose attributes are to be retrieved; must not be null
     * @return a list of {@link AttributeModel} representing the attributes defined for the specified entity;
     * an empty list is returned if the entity has no attributes or if the data store or entity does not exist
     */
    public List<AttributeModel> getEntityAttributes(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        DataModel dataModel = getDataModels(dataStore).get(entityName);
        return dataModel != null
                ? Collections.unmodifiableList(dataModel.attributeModels())
                : Collections.emptyList();
    }

    /**
     * Retrieves the {@link EntityModel} associated with the specified data store and entity name.
     *
     * @param dataStore  the name of the data store where the entity resides; must not be null
     * @param entityName the name of the entity whose model is to be retrieved; must not be null
     * @return the {@link EntityModel} corresponding to the specified data store and entity name,
     * or {@code null} if no such entity model exists
     */
    @Nullable
    public EntityModel getEntityModel(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        DataModel dataModel = getDataModels(dataStore).get(entityName);
        return dataModel != null
                ? dataModel.entityModel()
                : null;
    }

    /**
     * Retrieves an unmodifiable view of the internal data model storage.
     * The returned map represents an organizational structure where the first-level keys are
     * data store identifiers and the values are nested maps. The nested maps use
     * entity names as keys and their corresponding {@link DataModel} objects as values.
     *
     * @return a map containing data store identifiers as keys, where each value is another map that
     * maps entity names to their respective {@link DataModel} instances. The returned map
     * is unmodifiable.
     */
    public Map<String, Map<String, DataModel>> getDataModels() {
        lock.readLock().lock();
        try {
            checkInitialized();

            return Collections.unmodifiableMap(dataModels);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves the data models associated with a specific data store.
     *
     * @param dataStore the name of the data store whose data models are to be retrieved; must not be null
     * @return a map where the keys are entity names and the values are the corresponding {@link DataModel} instances
     */
    public Map<String, DataModel> getDataModels(String dataStore) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");

        lock.readLock().lock();
        try {
            checkInitialized();

            Map<String, DataModel> dataModelMap = dataModels.get(dataStore);
            return dataModelMap != null ? Collections.unmodifiableMap(dataModelMap) : Collections.emptyMap();
        } finally {
            lock.readLock().unlock();
        }
    }

    protected boolean isAnnotationPresent(MetaProperty field, Class<? extends Annotation> annotationClass) {
        return field.getAnnotatedElement().isAnnotationPresent(annotationClass);
    }

    protected <T extends Annotation> T getAnnotation(MetaProperty field, Class<T> annotationClass) {
        return field.getAnnotatedElement().getAnnotation(annotationClass);
    }
}