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

package io.jmix.reports.yarg.loaders.impl;

import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;

import java.util.List;
import java.util.Map;

/**
 * Delegates data loading operation to an object provided by {@link ReportQuery}.
 */
public class DelegatingDataLoader implements ReportDataLoader {

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        ReportDataLoader delegate = reportQuery.getLoaderDelegate();
        if (delegate == null) {
            throw new DataLoadingException(
                    String.format("LoaderDelegate must be set in the data set [%s]", reportQuery.getName())
            );
        }
        return delegate.loadData(reportQuery, parentBand, params);
    }
}
