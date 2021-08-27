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

import io.jmix.dataimport.DataImporter;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.extractor.data.ImportedData;
import io.jmix.dataimport.extractor.data.ImportedDataExtractor;
import io.jmix.dataimport.extractor.data.ImportedDataExtractors;
import io.jmix.dataimport.result.ImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component("datimp_DataImporter")
public class DataImporterImpl implements DataImporter {
    protected static final Logger log = LoggerFactory.getLogger(DataImporter.class);
    @Autowired
    protected ImportedDataExtractors importedDataExtractors;
    @Autowired
    protected ObjectProvider<DataImportExecutor> dataImportExecutors;
    @Autowired
    protected ImportConfigurationValidator importConfigurationValidator;

    @Override
    public ImportResult importData(ImportConfiguration configuration, byte[] content) {
        try {
            importConfigurationValidator.validate(configuration);
            ImportedDataExtractor dataExtractor = importedDataExtractors.getExtractor(configuration.getInputDataFormat());
            ImportedData importedData = dataExtractor.extract(configuration, content);
            return importData(configuration, importedData);
        } catch (Exception e) {
            log.error("Import failed: ", e);
            return new ImportResult()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage());
        }
    }


    @Override
    public ImportResult importData(ImportConfiguration configuration, InputStream inputStream) {
        try {
            importConfigurationValidator.validate(configuration);
            ImportedDataExtractor dataExtractor = importedDataExtractors.getExtractor(configuration.getInputDataFormat());
            ImportedData importedData = dataExtractor.extract(configuration, inputStream);
            return importData(configuration, importedData);
        } catch (Exception e) {
            log.error("Import failed: ", e);
            return new ImportResult()
                    .setSuccess(false)
                    .setErrorMessage(e.getMessage());
        }
    }

    @Override
    public ImportResult importData(ImportConfiguration configuration, ImportedData importedData) {
        DataImportExecutor dataImportExecutor = dataImportExecutors.getObject(configuration, importedData);
        return dataImportExecutor.importData();
    }

}
