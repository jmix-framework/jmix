/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.delegate;

import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;

import java.util.Map;

/**
 * Custom JSON input provider
 * for a {@link io.jmix.reports.entity.DataSetType#JSON} data set.
 */
@FunctionalInterface
public interface JsonInputProvider {

    /**
     * Load JSON input for a data set by given parameters.
     *
     * @param reportQuery report query (data set)
     * @param parentBand parent band
     * @param reportParameters parameters map
     * @return JSON as string
     */
    String load(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParameters);
}
