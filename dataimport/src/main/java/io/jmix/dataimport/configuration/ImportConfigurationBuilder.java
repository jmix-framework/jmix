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

import io.jmix.dataimport.configuration.mapping.*;
import io.jmix.dataimport.extractor.entity.EntityExtractionResult;
import io.jmix.dataimport.property.populator.CustomMappingContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builds an instance of {@link ImportConfiguration} using the following parameters:
 * <ol>
 *     <li>Entity class (required): class of entity that will be imported using created import configuration</li>
 *     <li>Input data format (required): xlsx, csv, json or xml.</li>
 *     <li>Property mappings: list of {@link PropertyMapping}</li>
 *     <li>Transaction strategy: {@link ImportTransactionStrategy}. By default, each entity is imported in the separate transaction.</li>
 *     <li>Import batch size: number of entities that will imported in one batch if {@link ImportTransactionStrategy#TRANSACTION_PER_BATCH} is used. By default, 100. </li>
 *     <li>Date format</li>
 *     <li>Custom formats of boolean true and false values</li>
 *     <li>Pre-import predicate: a predicate that is executed for each extracted entity before import. If the predicate returns false, the entity won't be imported.</li>
 *     <li>Entity initializer: a consumer that is executed after pre-import check and allows to make additional changes with extracted entity before import.</li>
 *     <li>Input data charset: this parameter is required if CSV is input data format. Default value: UTF-8</li>
 *     <li>Unique entity configurations: list of {@link UniqueEntityConfiguration}.</li>
 * </ol>
 */
public class ImportConfigurationBuilder {
    private Class entityClass;
    private List<PropertyMapping> propertyMappings = new ArrayList<>();

    private ImportTransactionStrategy transactionStrategy;
    private int importBatchSize = 100;

    private String inputDataFormat;

    private String dateFormat;
    private String booleanTrueValue;
    private String booleanFalseValue;

    private String inputDataCharset = StandardCharsets.UTF_8.name();

    private Predicate<EntityExtractionResult> preImportPredicate;
    private Consumer<Object> entityInitializer;

    private List<UniqueEntityConfiguration> uniqueEntityConfigurations = new ArrayList<>();

    protected ImportConfigurationBuilder(Class entityClass, String inputDataFormat) {
        this.entityClass = entityClass;
        this.inputDataFormat = inputDataFormat;
    }

    /**
     * Sets a charset of input data.
     *
     * @param inputDataCharset charset of input data.
     * @return current instance of builder
     */
    public ImportConfigurationBuilder withInputDataCharset(String inputDataCharset) {
        this.inputDataCharset = inputDataCharset;
        return this;
    }

    /**
     * Adds a mapping for specified simple property.
     *
     * @param entityPropertyName entity property name
     * @param dataFieldName      name of the field from input data that contains a raw value of property
     * @return current instance of builder
     */
    public ImportConfigurationBuilder addSimplePropertyMapping(String entityPropertyName, String dataFieldName) {
        this.propertyMappings.add(new SimplePropertyMapping(entityPropertyName, dataFieldName));
        return this;
    }

    /**
     * Adds a custom mapping for property.
     *
     * @param entityPropertyName  entity property name
     * @param customValueFunction function to get value for the
     * @return current instance of builder
     */
    public ImportConfigurationBuilder addCustomPropertyMapping(String entityPropertyName,
                                                               Function<CustomMappingContext, Object> customValueFunction) {
        this.propertyMappings.add(new CustomPropertyMapping(entityPropertyName, customValueFunction));
        return this;
    }

    /**
     * Creates and adds a {@link UniqueEntityConfiguration}.
     *
     * @param policy              policy which be applied for found duplicates
     * @param entityPropertyNames names of the entity properties by which values the duplicates will be searched
     * @return current instance of builder
     */
    public ImportConfigurationBuilder addUniqueEntityConfiguration(DuplicateEntityPolicy policy, String... entityPropertyNames) {
        this.uniqueEntityConfigurations.add(new UniqueEntityConfiguration(Arrays.asList(entityPropertyNames), policy));
        return this;
    }

    /**
     * Creates and adds a {@link UniqueEntityConfiguration}.
     *
     * @param policy              policy which be applied for found duplicates
     * @param entityPropertyNames names of the entity properties by which values the duplicates will be searched
     * @return current instance of builder
     */
    public ImportConfigurationBuilder addUniqueEntityConfiguration(DuplicateEntityPolicy policy, List<String> entityPropertyNames) {
        this.uniqueEntityConfigurations.add(new UniqueEntityConfiguration(entityPropertyNames, policy));
        return this;
    }

    /**
     * Sets a transaction strategy.
     *
     * @param transactionStrategy transaction strategy
     * @return current instance of builder
     */
    public ImportConfigurationBuilder withTransactionStrategy(ImportTransactionStrategy transactionStrategy) {
        this.transactionStrategy = transactionStrategy;
        return this;
    }

    /**
     * Sets a number of entities that will be imported in one batch.
     * <br>
     * Note: it is actual if {@link ImportTransactionStrategy#TRANSACTION_PER_BATCH} is used.
     *
     * @param importBatchSize number of entities that will be imported in one batch
     * @return current instance of builder
     */

    public ImportConfigurationBuilder withImportBatchSize(int importBatchSize) {
        this.importBatchSize = importBatchSize;
        return this;
    }

    /**
     * Creates and adds a property mapping for the reference property mapped by one data field.
     *
     * @param entityPropertyName reference property name
     * @param dataFieldName      name of the field from input data that contains a raw value of lookup property
     * @param lookupPropertyName property name from the reference entity
     * @param policy             reference import policy
     * @return current instance of builder
     * @see ReferencePropertyMapping
     */
    public ImportConfigurationBuilder addReferencePropertyMapping(String entityPropertyName,
                                                                  String dataFieldName,
                                                                  String lookupPropertyName,
                                                                  ReferenceImportPolicy policy) {
        this.propertyMappings.add(new ReferencePropertyMapping(entityPropertyName, dataFieldName, lookupPropertyName, policy));
        return this;
    }

    /**
     * Adds specified property mapping. Property mapping can be:
     * <ul>
     *     <li>{@link SimplePropertyMapping}</li>
     *     <li>{@link CustomPropertyMapping}</li>
     *     <li>{@link ReferencePropertyMapping}</li>
     *     <li>{@link ReferenceMultiFieldPropertyMapping}</li>
     * </ul>
     *
     * @param propertyMapping property mapping
     * @return current instance of builder
     * @see ReferenceMultiFieldPropertyMapping.Builder
     */
    public ImportConfigurationBuilder addPropertyMapping(PropertyMapping propertyMapping) {
        this.propertyMappings.add(propertyMapping);
        return this;
    }

    /**
     * Sets a date format.
     *
     * @param dateFormat date format used in the input data
     * @return current instance of builder
     */
    public ImportConfigurationBuilder withDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * Sets the strings for boolean true and false values.
     *
     * @param booleanTrueValue  string that represents boolean true value
     * @param booleanFalseValue string that represents boolean false value
     * @return current instance of builder
     */
    public ImportConfigurationBuilder withBooleanFormats(String booleanTrueValue, String booleanFalseValue) {
        this.booleanTrueValue = booleanTrueValue;
        this.booleanFalseValue = booleanFalseValue;
        return this;
    }

    /**
     * Sets a predicate that is executed for each created entity before import.
     * If the predicate returns false, the entity is not imported.
     *
     * @param preImportPredicate pre-import predicate
     * @return current instance of builder
     */
    public ImportConfigurationBuilder withPreImportPredicate(Predicate<EntityExtractionResult> preImportPredicate) {
        this.preImportPredicate = preImportPredicate;
        return this;
    }

    /**
     * Sets a consumer that makes additional changes with entity before import.
     *
     * @param entityInitializer consumer that makes additional changes with entity before import
     * @return current instance of builder
     */
    public ImportConfigurationBuilder withEntityInitializer(Consumer<Object> entityInitializer) {
        this.entityInitializer = entityInitializer;
        return this;
    }

    /**
     * Creates an instance of {@link ImportConfiguration} based on the specified parameters.
     *
     * @return created instance of {@link ImportConfiguration}
     */
    public ImportConfiguration build() {
        return new ImportConfiguration(entityClass, this.inputDataFormat)
                .setDateFormat(dateFormat)
                .setBooleanTrueValue(booleanTrueValue)
                .setBooleanFalseValue(booleanFalseValue)
                .setTransactionStrategy(this.transactionStrategy)
                .setImportBatchSize(importBatchSize)
                .setPropertyMappings(propertyMappings)
                .setInputDataCharset(this.inputDataCharset)
                .setPreImportPredicate(this.preImportPredicate)
                .setEntityInitializer(entityInitializer)
                .setUniqueEntityConfigurations(this.uniqueEntityConfigurations);
    }
}
