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

package io.jmix.dataimport.extractor.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("datimp_ImportedDataExtractors")
public class ImportedDataExtractors {
    protected Map<String, ImportedDataExtractor> extractorsByInputFormats = new HashMap<>();

    @Autowired
    public void setDataExtractors(List<ImportedDataExtractor> dataExtractors) {
        this.extractorsByInputFormats = dataExtractors.stream()
                .collect(Collectors.toMap(ImportedDataExtractor::getSupportedDataFormat, Function.identity()));
    }

    public ImportedDataExtractor getExtractor(String inputDataFormat) {
        ImportedDataExtractor dataExtractor = extractorsByInputFormats.get(inputDataFormat);
        if (dataExtractor == null) {
            throw new IllegalArgumentException(String.format("Input data format [%s] is not supported for import", inputDataFormat));
        }
        return dataExtractor;
    }
}
