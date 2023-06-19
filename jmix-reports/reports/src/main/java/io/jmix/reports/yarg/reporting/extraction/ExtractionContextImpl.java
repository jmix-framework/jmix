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

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default data extraction context implementation
 */
public class ExtractionContextImpl implements ExtractionContext {
    protected DataExtractor extractor;
    protected ReportBand band;
    protected BandData parentBand;
    protected Map<String, Object> params;

    public ExtractionContextImpl(DataExtractor extractor, ReportBand band, BandData parentBand, Map<String, Object> params) {
        checkNotNull(extractor);
        checkNotNull(band);
        checkNotNull(params);

        this.extractor = extractor;
        this.band = band;
        this.parentBand = parentBand;
        this.params = params;
    }

    public boolean putEmptyRowIfNoDataSelected() {
        return extractor.getPutEmptyRowIfNoDataSelected();
    }

    public ReportBand getBand() {
        return band;
    }

    public BandData getParentBandData() {
        return parentBand;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public ExtractionContextImpl extendParams(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    public ExtractionContextImpl withParams(Map<String, Object> params) {
        return new ExtractionContextImpl(extractor, band, parentBand, params);
    }

    @Override
    public ExtractionContext withBand(ReportBand band, BandData parentBand) {
        return new ExtractionContextImpl(extractor, band, parentBand, params);
    }

    public ExtractionContextImpl withParentData(BandData parentBand) {
        return new ExtractionContextImpl(extractor, band, parentBand, params);
    }
}
