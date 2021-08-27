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

package io.jmix.dataimport.configuration;

import io.jmix.dataimport.configuration.mapping.PropertyMapping;
import io.jmix.dataimport.extractor.entity.EntityExtractionResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An object that allows configuring import process of entities from JSON, XML, CSV, XLSX.
 * For that, there are the following options in the import configuration:
 * <ol>
 *     <li>Entity class (required): class of the entity that should be imported.</li>
 *     <li>Input data format (required): xlsx, csv, json or xml.</li>
 *     <li>Property mappings: list of {@link PropertyMapping}.</li>
 *     <li>Transaction strategy: {@link ImportTransactionStrategy}. By default, each entity is imported in the separate transaction.</li>
 *     <li>Import batch size: number of entities that will imported in one batch if {@link ImportTransactionStrategy#TRANSACTION_PER_BATCH} is used. By default, 100. </li>
 *     <li>Date format: date format used in the input data.</li>
 *     <li>Custom formats of boolean true and false values.</li>
 *     <li>Pre-import predicate: a predicate that is executed for each extracted entity before import. If the predicate returns false, the entity won't be imported.</li>
 *     <li>Entity initializer: a consumer that is executed after pre-import check and allows to make additional changes with extracted entity before import.</li>
 *     <li>Input data charset: this parameter is required if CSV is input data format. Default value: UTF-8.</li>
 *     <li>Unique entity configurations: list of {@link UniqueEntityConfiguration}.</li>
 * </ol>
 * <br>
 * Import configuration can be created by constructor or by {@link ImportConfigurationBuilder}.
 * <br>
 * Creation examples:
 * <pre>
 * ImportConfiguration importConfiguration = ImportConfiguration.builder(Order.class, InputDataFormat.CSV)
 *                 .addSimplePropertyMapping("orderNumber", "Order Num")
 *                 .addSimplePropertyMapping("date", "Order Date")
 *                 .addSimplePropertyMapping("amount", "Order Amount")
 *                 .addPropertyMapping(ReferenceMultiFieldPropertyMapping.builder("customer", ReferenceImportPolicy.CREATE_IF_MISSING)
 *                         .addSimplePropertyMapping("name", "Customer Name")
 *                         .addSimplePropertyMapping("email", "Customer Email")
 *                         .lookupByAllSimpleProperties()
 *                         .build())
 *                 .withDateFormat("dd/MM/yyyy HH:mm")
 *                 .withTransactionStrategy(ImportTransactionStrategy.SINGLE_TRANSACTION)
 *                 .build();
 *
 *  ImportConfiguration importConfiguration = ImportConfiguration.builder(Customer.class, InputDataFormat.JSON)
 *                 .addSimplePropertyMapping("name", "name")
 *                 .addSimplePropertyMapping("email", "email")
 *                 .addReferencePropertyMapping("bonusCard", "bonusCardNumber", "cardNumber", ReferenceImportPolicy.IGNORE_IF_MISSING)
 *                 .addUniqueEntityConfiguration(DuplicateEntityPolicy.ABORT, "name", "email")
 *                 .build();
 *
 *  ImportConfiguration importConfiguration = ImportConfiguration.builder(Customer.class, InputDataFormat.XML)
 *                 .addSimplePropertyMapping("name", "name")
 *                 .addSimplePropertyMapping("email", "email")
 *                 .withTransactionStrategy(ImportTransactionStrategy.TRANSACTION_PER_BATCH)
 *                 .withImportBatchSize(50)
 *                 .build();
 * </pre>
 *
 * @see ImportConfigurationBuilder
 */
public class ImportConfiguration {
    protected Class entityClass;

    protected List<PropertyMapping> propertyMappings = new ArrayList<>();

    protected ImportTransactionStrategy transactionStrategy;
    protected int importBatchSize = 100;

    protected String inputDataFormat;

    protected String dateFormat;
    protected String booleanTrueValue;
    protected String booleanFalseValue;

    protected String inputDataCharset = StandardCharsets.UTF_8.name();

    protected List<UniqueEntityConfiguration> uniqueEntityConfigurations = new ArrayList<>();

    protected Predicate<EntityExtractionResult> preImportPredicate;
    protected Consumer<Object> entityInitializer;

    public ImportConfiguration(Class entityClass, String inputDataFormat) {
        this.entityClass = entityClass;
        this.inputDataFormat = inputDataFormat;
    }

    /**
     * Gets mappings for entity properties.
     *
     * @return property mappings
     */
    public List<PropertyMapping> getPropertyMappings() {
        return propertyMappings;
    }

    /**
     * Sets mappings for entity properties.
     *
     * @param propertyMappings property mappings
     * @return current instance of import configuration
     */
    public ImportConfiguration setPropertyMappings(List<PropertyMapping> propertyMappings) {
        this.propertyMappings = propertyMappings;
        return this;
    }

    /**
     * Adds a property mapping.
     *
     * @param propertyMapping property mapping
     * @return current instance of import configuration
     */
    public ImportConfiguration addPropertyMapping(PropertyMapping propertyMapping) {
        this.propertyMappings.add(propertyMapping);
        return this;
    }

    /**
     * Gets an import transaction strategy.
     *
     * @return transaction strategy
     */
    public ImportTransactionStrategy getTransactionStrategy() {
        return transactionStrategy;
    }

    /**
     * Sets an import transaction strategy.
     *
     * @param transactionStrategy transaction strategy
     * @return current instance of import configuration
     */
    public ImportConfiguration setTransactionStrategy(ImportTransactionStrategy transactionStrategy) {
        this.transactionStrategy = transactionStrategy;
        return this;
    }

    /**
     * Gets a number of entities that will be imported in one batch.
     *
     * @return number of entities that will be imported in one batch
     */
    public int getImportBatchSize() {
        return importBatchSize;
    }

    /**
     * Sets a number of entities that will be imported in one batch.
     * <br>
     * Note: it is actual if {@link ImportTransactionStrategy#TRANSACTION_PER_BATCH} is used.
     *
     * @param importBatchSize number of entities that will be imported in one batch.
     * @return current instance of import configuration
     */
    public ImportConfiguration setImportBatchSize(int importBatchSize) {
        this.importBatchSize = importBatchSize;
        return this;
    }

    /**
     * Gets a class of entity to import.
     *
     * @return class of entity to import
     */
    public Class getEntityClass() {
        return entityClass;
    }

    /**
     * Gets an input data format.
     *
     * @return input data format
     */
    public String getInputDataFormat() {
        return inputDataFormat;
    }

    /**
     * Gets a charset of input data.
     *
     * @return charset of input data
     */
    public String getInputDataCharset() {
        return inputDataCharset;
    }

    /**
     * Sets a charset of input data.
     *
     * @param inputDataCharset charset of input data
     * @return current instance of import configuration
     */
    public ImportConfiguration setInputDataCharset(String inputDataCharset) {
        this.inputDataCharset = inputDataCharset;
        return this;
    }

    /**
     * Gets a list of unique entity configurations.
     *
     * @return list of {@link UniqueEntityConfiguration}
     */
    public List<UniqueEntityConfiguration> getUniqueEntityConfigurations() {
        return uniqueEntityConfigurations;
    }

    /**
     * Sets a list of unique entity configurations.
     *
     * @param uniqueEntityConfigurations list of configurations for unique entity.
     * @return current instance of import configuration
     */
    public ImportConfiguration setUniqueEntityConfigurations(List<UniqueEntityConfiguration> uniqueEntityConfigurations) {
        this.uniqueEntityConfigurations = uniqueEntityConfigurations;
        return this;
    }

    /**
     * Adds a unique entity configuration.
     *
     * @param uniqueEntityConfiguration unique entity configuration
     * @return current instance of import configuration
     */
    public ImportConfiguration addUniqueEntityConfiguration(UniqueEntityConfiguration uniqueEntityConfiguration) {
        this.uniqueEntityConfigurations.add(uniqueEntityConfiguration);
        return this;
    }

    /**
     * Gets a date format.
     *
     * @return date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets a date format to parse date strings.
     *
     * @param dateFormat date format used in input data
     * @return current instance of import configuration
     */
    public ImportConfiguration setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * Gets a format of boolean true value.
     *
     * @return format of boolean true value
     */
    public String getBooleanTrueValue() {
        return booleanTrueValue;
    }

    /**
     * Sets a format of boolean true value.
     *
     * @param booleanTrueValue format of boolean true value
     * @return current instance of import configuration
     */
    public ImportConfiguration setBooleanTrueValue(String booleanTrueValue) {
        this.booleanTrueValue = booleanTrueValue;
        return this;
    }

    /**
     * Gets a format of boolean false value.
     *
     * @return format of boolean false value
     */
    public String getBooleanFalseValue() {
        return booleanFalseValue;
    }

    /**
     * Sets a format of boolean false value.
     *
     * @param booleanFalseValue format of boolean false value
     * @return current instance of import configuration
     */
    public ImportConfiguration setBooleanFalseValue(String booleanFalseValue) {
        this.booleanFalseValue = booleanFalseValue;
        return this;
    }

    /**
     * Gets a predicate executed before entity import.
     *
     * @return predicate executed before entity import
     */
    public Predicate<EntityExtractionResult> getPreImportPredicate() {
        return preImportPredicate;
    }

    /**
     * Sets a predicate executed before entity import.
     *
     * @param preImportPredicate predicate executed before entity import
     * @return current instance of import configuration
     */
    public ImportConfiguration setPreImportPredicate(Predicate<EntityExtractionResult> preImportPredicate) {
        this.preImportPredicate = preImportPredicate;
        return this;
    }

    /**
     * Gets a consumer that makes additional changes with entity before import.
     *
     * @return consumer that makes additional changes with entity before import
     */
    public Consumer<Object> getEntityInitializer() {
        return entityInitializer;
    }

    /**
     * Sets a consumer that makes additional changes with entity before import.
     *
     * @param entityInitializer consumer that makes additional changes with entity before import
     * @return current instance of import configuration
     */
    public ImportConfiguration setEntityInitializer(Consumer<Object> entityInitializer) {
        this.entityInitializer = entityInitializer;
        return this;
    }

    /**
     * Creates an instance of {@link ImportConfigurationBuilder} for the specified entity class and import configuration code.
     *
     * @param entityClass     entity class
     * @param inputDataFormat input data format
     * @return new instance of {@link ImportConfigurationBuilder}
     */
    public static ImportConfigurationBuilder builder(Class entityClass, String inputDataFormat) {
        return new ImportConfigurationBuilder(entityClass, inputDataFormat);
    }

}