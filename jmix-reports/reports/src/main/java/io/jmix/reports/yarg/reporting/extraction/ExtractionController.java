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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This interface implementation should contains data extraction logic
 *
 * <p>The default implementation is <b>io.jmix.reports.yarg.reporting.extraction.controller.DefaultExtractionController</b></p>
 */
@FunctionalInterface
public interface ExtractionController {

    /**
     * Method should presents controller logic for data extraction and band tree traversal logic
     * @param context contains band, parent band data and params
     * @return list of loaded and wrapped for formatting data
     */
    List<BandData> extract(ExtractionContext context);

    /**
     * <p>Method may presents specific logic for for data extraction without traverse</p>
     * ex: data extraction for root band
     * io.jmix.reports.yarg.reporting.DataExtractorImpl#extractData
     *
     * @param context - should contains band, parent band data and params
     * @return list of loaded data
     */
    default List<Map<String, Object>> extractData(ExtractionContext context) {
        return Collections.emptyList();
    }

}
