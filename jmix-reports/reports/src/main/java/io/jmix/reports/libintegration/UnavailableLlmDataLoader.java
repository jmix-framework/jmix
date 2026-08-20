/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reports.libintegration;

import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Stands in for {@link LlmDataLoader} in an application without the AI Tools add-on, so that running a report
 * authored elsewhere says what is missing.
 * <p>
 * A report keeps the type of its data sets wherever it travels, and the designer can only stop offering the
 * type — it cannot rewrite a report that already uses it. Without this loader the run would fail with the
 * report engine's own message about an unknown loader type, which names the type but not the add-on that
 * provides it.
 */
public class UnavailableLlmDataLoader implements ReportDataLoader {

    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, @Nullable BandData parentBand,
                                              Map<String, Object> params) {
        throw new DataLoadingException(String.format(
                "Data set [%s] is of type [%s], which the AI Tools add-on provides. Add the add-on to the "
                        + "application and configure a chat model to run this report",
                reportQuery.getName(), DataSetType.LLM.getCode()));
    }
}
