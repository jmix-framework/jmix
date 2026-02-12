/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.dataimport.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.validation.EntityValidationException;
import io.jmix.dataimport.DuplicateEntityManager;
import io.jmix.dataimport.configuration.DuplicateEntityPolicy;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.configuration.ImportTransactionStrategy;
import io.jmix.dataimport.configuration.UniqueEntityConfiguration;
import io.jmix.dataimport.configuration.mapping.PropertyMapping;
import io.jmix.dataimport.configuration.mapping.ReferenceMultiFieldPropertyMapping;
import io.jmix.dataimport.configuration.mapping.ReferencePropertyMapping;
import io.jmix.dataimport.configuration.mapping.SimplePropertyMapping;
import io.jmix.dataimport.exception.ImportUniqueAbortException;
import io.jmix.dataimport.extractor.data.ImportedData;
import io.jmix.dataimport.extractor.data.ImportedDataItem;
import io.jmix.dataimport.extractor.entity.EntityExtractionResult;
import io.jmix.dataimport.extractor.entity.EntityExtractor;
import io.jmix.dataimport.property.populator.EntityInfo;
import io.jmix.dataimport.property.populator.EntityPropertiesPopulator;
import io.jmix.dataimport.result.EntityImportError;
import io.jmix.dataimport.result.EntityImportErrorType;
import io.jmix.dataimport.result.ImportResult;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executes import for a given import configuration and {@link ImportedData}.
 */
@Component("datimp_DataImportExecutor")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DataImportExecutor {
    protected static final Logger log = LoggerFactory.getLogger(DataImportExecutor.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected DuplicateEntityManager duplicateEntityManager;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected EntityPropertiesPopulator entityPropertiesPopulator;
    @Autowired
    protected EntityExtractor entityExtractor;

    protected ImportResult importResult = new ImportResult();

    protected ImportConfiguration importConfiguration;
    protected ImportedData importedData;

    public DataImportExecutor(ImportConfiguration importConfiguration, ImportedData importedData) {
        this.importConfiguration = importConfiguration;
        this.importedData = importedData;
    }

    public ImportResult importData() {
        if (importConfiguration == null) {
            throw new IllegalStateException("Import configuration is not set to execute data import");
        }

        if (importConfiguration.getTransactionStrategy() == ImportTransactionStrategy.SINGLE_TRANSACTION) {
            importInOneTransaction();
        } else if (importConfiguration.getTransactionStrategy() == ImportTransactionStrategy.TRANSACTION_PER_BATCH) {
            importByBatches();
        } else {
            importInMultipleTransactions();
        }

        return importResult;
    }

    protected void importInOneTransaction() {
        try {
            List<EntityExtractionResult> extractionResults = null;
            try {
                extractionResults = entityExtractor.extractEntities(importConfiguration, importedData);
            } catch (Exception e) {
                createErrorResult(e, "Entities extraction failed: " + e.getMessage());
            }

            if (extractionResults != null) {
                List<Object> entitiesToImport = checkExtractionResults(extractionResults);
                List<Object> importedEntities = importEntities(entitiesToImport);
                importResult.setImportedEntityIds(importedEntities);
            }
        } catch (ImportUniqueAbortException e) {
            createErrorResult(e, String.format("Unique violation occurred with Unique Policy ABORT for entity: '%s' with data item: '%s'. Found entity: '%s'",
                    e.getCreatedEntity(), e.getImportedDataItem(),
                    e.getExistingEntity()));
        } catch (Exception e) {
            createErrorResult(e, String.format("Error while importing the data: %s. \nTransaction abort - no entity is stored in the database.", e.getMessage()));
        }
    }

    protected void importByBatches() {
        int offset = 0;
        int batchSize = importConfiguration.getImportBatchSize();
        List<ImportedDataItem> allItems = importedData.getItems();
        while (true) {
            int end = offset + batchSize;
            List<ImportedDataItem> importedDataItemsBatch = allItems.subList(offset, Math.min(end, allItems.size()));
            try {
                processBatch(importedDataItemsBatch);
            } catch (ImportUniqueAbortException e) {
                createErrorResult(e, String.format("Unique violation occurred with Unique Policy ABORT for entity: '%s' with data item: '%s'. Found entity: '%s'",
                        e.getCreatedEntity(), e.getImportedDataItem(),
                        e.getExistingEntity()));
                break;
            }
            if (end >= allItems.size()) {
                break;
            } else {
                offset += batchSize;
            }
        }
    }

    protected void processBatch(List<ImportedDataItem> importedDataItemsBatch) {
        List<EntityExtractionResult> extractionResults = null;
        try {
            try {
                extractionResults = entityExtractor.extractEntities(importConfiguration, importedDataItemsBatch);
            } catch (Exception e) {
                importResult.setSuccess(false);
                importedDataItemsBatch.forEach(dataItem -> importResult.addFailedEntity(new EntityImportError()
                        .setImportedDataItem(dataItem)
                        .setErrorType(EntityImportErrorType.DATA_BINDING)
                        .setErrorMessage(e.getMessage())));
            }

            if (extractionResults != null) {
                List<Object> entitiesToImport = checkExtractionResults(extractionResults);
                Collection<Object> importedEntities = importEntities(entitiesToImport);
                importedEntities.stream()
                        .filter(importedEntityId -> !importResult.getImportedEntityIds().contains(importedEntityId))
                        .forEach(importedEntityId -> importResult.addImportedEntityId(importedEntityId));
            }
        } catch (ImportUniqueAbortException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while importing entities batch: ", e);
            importResult.setSuccess(false);
            if (extractionResults != null) {
                extractionResults.forEach(extractionResult -> importResult.addFailedEntity(new EntityImportError(extractionResult.getEntity())
                        .setImportedDataItem(extractionResult.getImportedDataItem())
                        .setErrorType(EntityImportErrorType.NOT_IMPORTED_BATCH)
                        .setErrorMessage(e.getMessage())));
            }

        }
    }

    protected void importInMultipleTransactions() {
        try {
            importResult.setSuccess(true);
            importedData.getItems().forEach(dataItem -> {
                EntityExtractionResult extractionResult = null;
                try {
                    extractionResult = entityExtractor.extractEntity(importConfiguration, dataItem);
                } catch (Exception e) {
                    log.error(String.format("Entity extraction failed for data item: %s", dataItem.toString()), e);
                    importResult.setSuccess(false);
                    importResult.addFailedEntity(new EntityImportError()
                            .setImportedDataItem(dataItem)
                            .setErrorType(EntityImportErrorType.DATA_BINDING)
                            .setErrorMessage(e.getMessage()));
                }
                if (extractionResult != null) {
                    boolean needToImport = checkExtractedEntity(extractionResult);
                    if (needToImport) {
                        importEntity(extractionResult);
                    }
                }
            });
        } catch (ImportUniqueAbortException e) {
            String errorMessage = String.format("Unique violation occurred with Unique Policy ABORT for data row: %s. Found entity: %s",
                    e.getImportedDataItem(),
                    e.getExistingEntity());
            createErrorResult(e, errorMessage);
        }
    }

    protected List<Object> checkExtractionResults(List<EntityExtractionResult> extractionResults) {
        List<EntityExtractionResult> processedResults = new ArrayList<>(); //to search duplicates
        return extractionResults.stream()
                .filter(extractionResult -> checkEntityDuplicate(extractionResult, processedResults))
                .filter(this::checkPreImportPredicate)
                .map(EntityExtractionResult::getEntity)
                .distinct()
                .collect(Collectors.toList());
    }

    protected boolean checkExtractedEntity(EntityExtractionResult entityExtractionResult) {
        boolean needToImport = checkEntityDuplicate(entityExtractionResult);
        if (needToImport) {
            try {
                needToImport = checkPreImportPredicate(entityExtractionResult);
            } catch (Exception e) {
                log.error("Pre-import predicate execution failed with: ", e);
                importResult.addFailedEntity(createEntityImportErrorResult(entityExtractionResult, String.format("Pre-import predicate execution failed with: %s", e.getMessage()), EntityImportErrorType.PRE_IMPORT_PREDICATE));
                return false;
            }
            return needToImport;
        }
        return false;
    }

    protected boolean checkEntityDuplicate(EntityExtractionResult extractionResult) {
        if (CollectionUtils.isNotEmpty(importConfiguration.getUniqueEntityConfigurations())) {
            for (UniqueEntityConfiguration configuration : importConfiguration.getUniqueEntityConfigurations()) {
                Object existingEntity = getDuplicateEntity(extractionResult.getEntity(), configuration, null);
                if (existingEntity != null) {
                    return processExistingEntity(extractionResult, configuration, existingEntity);
                }
            }
        }
        return true;
    }

    protected boolean processExistingEntity(EntityExtractionResult extractionResult, UniqueEntityConfiguration configuration, Object existingEntity) {
        if (configuration.getDuplicateEntityPolicy() == DuplicateEntityPolicy.UPDATE) {
            EntityInfo entityInfo = entityPropertiesPopulator.populateProperties(existingEntity, importConfiguration, extractionResult.getImportedDataItem());
            existingEntity = entityInfo.getEntity();
            extractionResult.setEntity(existingEntity);
            return true;
        } else if (configuration.getDuplicateEntityPolicy() == DuplicateEntityPolicy.ABORT) {
            throw new ImportUniqueAbortException(existingEntity, extractionResult);
        } else {
            importResult.addFailedEntity(createEntityImportErrorResult(extractionResult,
                    "Entity not imported since it is already existing and Unique policy is set to SKIP",
                    EntityImportErrorType.UNIQUE_VIOLATION));
            return false;
        }
    }

    protected boolean checkEntityDuplicate(EntityExtractionResult entityExtractionResult, List<EntityExtractionResult> processedResults) {
        boolean needToImport = true;
        if (CollectionUtils.isNotEmpty(importConfiguration.getUniqueEntityConfigurations())) {
            Object extractedEntity = entityExtractionResult.getEntity();
            for (UniqueEntityConfiguration configuration : importConfiguration.getUniqueEntityConfigurations()) {
                Object existingEntity = getDuplicateEntity(extractedEntity, configuration, processedResults);
                if (existingEntity != null) {
                    needToImport = processExistingEntity(entityExtractionResult, configuration, existingEntity);
                }
            }
        }
        processedResults.add(entityExtractionResult);
        return needToImport;
    }

    @Nullable
    protected Object getDuplicateEntity(Object extractedEntity, UniqueEntityConfiguration configuration, @Nullable List<EntityExtractionResult> processedResults) {
        FetchPlan fetchPlan = getFetchPlanBuilder(createEntityImportPlan(extractedEntity)).build();
        Object existingEntity = duplicateEntityManager.load(extractedEntity, configuration, fetchPlan);
        if (existingEntity == null) {
            if (processedResults != null) {
                EntityExtractionResult duplicateResult = processedResults.stream()
                        .filter(processedResult -> duplicateEntityManager.isDuplicated(extractedEntity, processedResult.getEntity(), configuration))
                        .findFirst()
                        .orElse(null);
                if (duplicateResult != null) {
                    existingEntity = duplicateResult.getEntity();
                }
            }
        }
        return existingEntity;
    }

    protected boolean checkPreImportPredicate(EntityExtractionResult entityExtractionResult) {
        if (importConfiguration.getPreImportPredicate() != null) {
            boolean needToImport = importConfiguration.getPreImportPredicate().test(entityExtractionResult);
            if (!needToImport) {
                importResult.addFailedEntity(createEntityImportErrorResult(entityExtractionResult,
                        "Entity not imported due to pre-commit predicate", EntityImportErrorType.VALIDATION));
            }
            return needToImport;
        }
        return true;
    }

    protected void applyEntityInitializer(List<Object> entitiesToImport) {
        if (importConfiguration.getEntityInitializer() != null) {
            entitiesToImport.forEach(entityToImport -> importConfiguration.getEntityInitializer().accept(entityToImport));
        }
    }

    protected void importEntity(EntityExtractionResult entityExtractionResult) {
        try {
            Collection<Object> importedEntities = importEntities(Collections.singletonList(entityExtractionResult.getEntity()));
            Object importedEntityId = importedEntities.iterator().next();
            if (!importResult.getImportedEntityIds().contains(importedEntityId)) {
                importResult.addImportedEntityId(importedEntityId);
            }
        } catch (EntityValidationException e) {
            log.error(String.format("Import failed for entity: %s, data item: %s",
                    entityImportExport.exportEntitiesToJSON(Collections.singletonList(entityExtractionResult.getEntity())),
                    entityExtractionResult.getImportedDataItem()), e);
            importResult.setSuccess(false);
            importResult.addFailedEntity(createEntityImportErrorResult(entityExtractionResult, e.toString(), EntityImportErrorType.VALIDATION));
        } catch (PersistenceException e) {
            log.error(String.format("Import failed for entity: %s, data item: %s",
                    entityImportExport.exportEntitiesToJSON(Collections.singletonList(entityExtractionResult.getEntity())),
                    entityExtractionResult.getImportedDataItem()), e);
            importResult.setSuccess(false);
            importResult.addFailedEntity(createEntityImportErrorResult(entityExtractionResult,
                    String.format("Error while importing entity: %s", e.getMessage()),
                    EntityImportErrorType.PERSISTENCE));
        }
    }

    protected List<Object> importEntities(List<Object> entitiesToImport) {
        applyEntityInitializer(entitiesToImport);

        List<Object> resultList = new ArrayList<>();
        entitiesToImport.forEach(entityToImport -> {
            EntityImportPlan entityImportPlan = createEntityImportPlan(entityToImport);
            Collection<Object> importedEntities = entityImportExport.importEntities(Collections.singletonList(entityToImport), entityImportPlan, true);
            Collection<Object> filteredImportedEntities = importedEntities.stream()
                    .filter(importedEntity -> (importedEntity.getClass().isAssignableFrom(importConfiguration.getEntityClass())))
                    .collect(Collectors.toList());
            resultList.add(filteredImportedEntities.iterator().next());
        });
        return resultList.stream()
                .map(EntityValues::getId)
                .collect(Collectors.toList());
    }

    protected EntityImportError createEntityImportErrorResult(EntityExtractionResult result, String errorMessage, EntityImportErrorType entityImportErrorType) {
        return new EntityImportError(result.getEntity())
                .setImportedDataItem(result.getImportedDataItem())
                .setErrorMessage(errorMessage)
                .setErrorType(entityImportErrorType);
    }

    protected void createErrorResult(Exception e, String errorMessage) {
        log.error(errorMessage, e);
        importResult.setSuccess(false)
                .setErrorMessage(errorMessage);
    }

    protected FetchPlanBuilder getFetchPlanBuilder(EntityImportPlan plan) {
        FetchPlanBuilder builder = fetchPlans.builder(plan.getEntityClass());
        plan.getProperties().forEach(entityImportPlanProperty -> {
            EntityImportPlan propertyPlan = entityImportPlanProperty.getPlan();
            if (propertyPlan != null) {
                builder.add(entityImportPlanProperty.getName(), getFetchPlanBuilder(propertyPlan));
            } else {
                builder.add(entityImportPlanProperty.getName());
            }
        });
        return builder;
    }

    protected EntityImportPlan createEntityImportPlan(Object entityToImport) {
        MetaClass entityMetaClass = metadata.getClass(importConfiguration.getEntityClass());
        EntityImportPlanBuilder entityImportPlanBuilder = createEntityImportPlanBuilder(entityMetaClass, importConfiguration.getPropertyMappings(), entityToImport);
        return entityImportPlanBuilder.build();
    }

    protected EntityImportPlanBuilder createEntityImportPlanBuilder(MetaClass ownerEntityMetaClass,
                                                                    List<PropertyMapping> propertyMappings,
                                                                    Object ownerEntity) {
        EntityImportPlanBuilder builder = entityImportPlans.builder(ownerEntityMetaClass.getJavaClass())
                .addLocalProperties();
        propertyMappings.stream()
                .filter(propertyMapping -> !(propertyMapping instanceof SimplePropertyMapping))
                .forEach(propertyMapping -> {
                    String propertyName = propertyMapping.getEntityPropertyName();
                    MetaProperty property = ownerEntityMetaClass.getProperty(propertyName);
                    Object propertyValue = EntityValues.getValue(ownerEntity, propertyName);
                    if (!property.getRange().isClass() || propertyValue == null) {
                        builder.addProperties(propertyName);
                    } else {
                        EntityImportPlanBuilder propertyImportPlanBuilder = null;
                        if (propertyValue instanceof Collection) {
                            propertyImportPlanBuilder = createEntityImportPlanForCollection(property, (ReferenceMultiFieldPropertyMapping) propertyMapping);
                        } else if (entityStates.isNew(propertyValue)) {
                            if (propertyMapping instanceof ReferenceMultiFieldPropertyMapping) {
                                propertyImportPlanBuilder = createEntityImportPlanBuilder(property.getRange().asClass(),
                                        ((ReferenceMultiFieldPropertyMapping) propertyMapping).getReferencePropertyMappings(), propertyValue);
                            } else if (propertyMapping instanceof ReferencePropertyMapping) {
                                propertyImportPlanBuilder = entityImportPlans.builder(property.getRange().asClass().getJavaClass())
                                        .addProperties(((ReferencePropertyMapping) propertyMapping).getLookupPropertyName());
                            }
                        } else if (metadataTools.isEmbedded(property)) {
                            propertyImportPlanBuilder = entityImportPlans.builder(property.getRange().asClass().getJavaClass())
                                    .addLocalProperties();
                        } else {
                            builder.addProperties(propertyName);
                        }
                        if (propertyImportPlanBuilder != null) {
                            addReferencePropertyToImportPlan(builder, propertyName, property, propertyImportPlanBuilder.build());
                        }
                    }
                });
        return builder;
    }

    protected EntityImportPlanBuilder createEntityImportPlanForCollection(MetaProperty property, ReferenceMultiFieldPropertyMapping referenceMapping) {
        EntityImportPlanBuilder collectionImportPlan = entityImportPlans.builder(property.getRange().asClass().getJavaClass())
                .addLocalProperties();
        referenceMapping.getReferencePropertyMappings()
                .stream().filter(propertyMapping -> !(propertyMapping instanceof SimplePropertyMapping))
                .forEach(propertyMapping -> collectionImportPlan.addProperties(propertyMapping.getEntityPropertyName()));
        return collectionImportPlan;
    }

    protected void addReferencePropertyToImportPlan(EntityImportPlanBuilder ownerBuilder, String propertyName, MetaProperty property, EntityImportPlan propertyImportPlan) {
        if (metadataTools.isEmbedded(property)) {
            ownerBuilder.addEmbeddedProperty(propertyName, propertyImportPlan);
            return;
        }

        Range.Cardinality cardinality = property.getRange().getCardinality();
        switch (cardinality) {
            case ONE_TO_ONE:
                ownerBuilder.addOneToOneProperty(propertyName, propertyImportPlan);
                break;
            case MANY_TO_ONE:
                ownerBuilder.addManyToOneProperty(propertyName, propertyImportPlan);
                break;
            case ONE_TO_MANY:
                ownerBuilder.addOneToManyProperty(propertyName, propertyImportPlan, CollectionImportPolicy.KEEP_ABSENT_ITEMS);
                break;
            default:
                break;
        }
    }
}
