/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.reporting.extraction;

import io.jmix.reports.yarg.reporting.DataExtractor;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportBand;

import java.util.Map;

/**
 * Default extraction context implementation
 */
public class DefaultExtractionContextFactory implements ExtractionContextFactory {

    protected DataExtractor dataExtractor;

    public DefaultExtractionContextFactory(DataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
    }

    @Override
    public ExtractionContext context(ReportBand band, BandData parentBand, Map<String, Object> params) {
        return new ExtractionContextImpl(dataExtractor, band, parentBand, params);
    }
}
