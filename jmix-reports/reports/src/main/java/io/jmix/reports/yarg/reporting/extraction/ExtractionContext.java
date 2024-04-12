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

import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportBand;

import java.util.Map;

/**
 * This interface implementation should presents extraction context dependent state
 *
 * <p>The default implementation is <b>io.jmix.reports.yarg.reporting.extraction.ExtractionContextImpl</b></p>
 */
public interface ExtractionContext {
    /**
     * @return boolean flag that controller should create empty data row if no report query data presented
     */
    boolean putEmptyRowIfNoDataSelected();

    /**
     * @return current processing report band
     */
    ReportBand getBand();

    /**
     * @return parent report band loaded data
     */
    BandData getParentBandData();

    /**
     * @return params for data loader
     */
    Map<String, Object> getParams();

    /**
     * Method must extend existed params with presented params map
     */
    ExtractionContext extendParams(Map<String, Object> params);

    /**
     * Method must create new version of context with new params (not extended)
     */
    ExtractionContext withParams(Map<String, Object> params);

    /**
     * Method must create new version of context with new report band and parent band data
     */
    ExtractionContext withBand(ReportBand band, BandData parentBand);

    /**
     * Method must create new version of context with parent band data
     */
    ExtractionContext withParentData(BandData parentBand);
}
